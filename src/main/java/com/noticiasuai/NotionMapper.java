package com.noticiasuai;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NotionMapper {

    public NoticiaResumenDto toResumen(JsonNode page) {
        JsonNode props = page.get("properties");

        return new NoticiaResumenDto(
                page.get("id").asText(),
                extraerTitle(props, "Título"),
                extraerTexto(props, "Slug"),
                extraerTexto(props, "Resumen"),
                extraerSelect(props, "Categoría"),
                extraerTexto(props, "Autor"),
                extraerFecha(props, "Fecha de publicación"),
                extraerArchivo(props, "Portada"),
                extraerCheckbox(props, "Destacada")
        );
    }

    public NoticiaDto toCompleta(JsonNode page, JsonNode bloques) {
        JsonNode props = page.get("properties");

        return new NoticiaDto(
                page.get("id").asText(),
                extraerTitle(props, "Título"),
                extraerTexto(props, "Slug"),
                extraerTexto(props, "Resumen"),
                bloquesAHtml(bloques),
                extraerSelect(props, "Categoría"),
                extraerTexto(props, "Autor"),
                extraerFecha(props, "Fecha de publicación"),
                extraerArchivo(props, "Portada"),
                extraerUrl(props, "Video"),
                extraerCheckbox(props, "Destacada"),
                extraerMultiSelect(props, "Etiquetas")
        );
    }

    // ── Extractores de propiedades de Notion ──
    // Cada tipo de propiedad en Notion tiene una estructura JSON diferente

    private String extraerTitle(JsonNode props, String nombre) {
        try {
            JsonNode arr = props.get(nombre).get("title");
            if (arr == null || arr.isEmpty()) return "";
            return arr.get(0).get("plain_text").asText();
        } catch (Exception e) {
            return "";
        }
    }

    private String extraerTexto(JsonNode props, String nombre) {
        try {
            JsonNode arr = props.get(nombre).get("rich_text");
            if (arr == null || arr.isEmpty()) return "";
            StringBuilder sb = new StringBuilder();
            for (JsonNode fragmento : arr) {
                sb.append(fragmento.get("plain_text").asText());
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String extraerSelect(JsonNode props, String nombre) {
        try {
            JsonNode select = props.get(nombre).get("select");
            if (select == null || select.isNull()) return null;
            return select.get("name").asText();
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> extraerMultiSelect(JsonNode props, String nombre) {
        List<String> result = new ArrayList<>();
        try {
            JsonNode arr = props.get(nombre).get("multi_select");
            if (arr != null) {
                for (JsonNode item : arr) {
                    result.add(item.get("name").asText());
                }
            }
        } catch (Exception ignored) {}
        return result;
    }

    private String extraerFecha(JsonNode props, String nombre) {
        try {
            JsonNode date = props.get(nombre).get("date");
            if (date == null || date.isNull()) return null;
            return date.get("start").asText();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean extraerCheckbox(JsonNode props, String nombre) {
        try {
            return props.get(nombre).get("checkbox").asBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    private String extraerUrl(JsonNode props, String nombre) {
        try {
            JsonNode url = props.get(nombre).get("url");
            if (url == null || url.isNull()) return null;
            return url.asText();
        } catch (Exception e) {
            return null;
        }
    }

    // Notion puede tener archivos subidos o enlaces externos
    private String extraerArchivo(JsonNode props, String nombre) {
        try {
            JsonNode files = props.get(nombre).get("files");
            if (files == null || files.isEmpty()) return null;

            JsonNode primerArchivo = files.get(0);
            String tipo = primerArchivo.get("type").asText();

            if ("file".equals(tipo)) {
                return primerArchivo.get("file").get("url").asText();
            }
            if ("external".equals(tipo)) {
                return primerArchivo.get("external").get("url").asText();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // ── Conversión de bloques de Notion a HTML ──

    private String bloquesAHtml(JsonNode bloques) {
        // TODO: faltan tipos: table, toggle, callout, code block
        if (bloques == null) return "";

        StringBuilder html = new StringBuilder();

        for (JsonNode bloque : bloques) {
            String tipo = bloque.get("type").asText();

            switch (tipo) {
                case "paragraph":
                    String texto = extraerTextoDeBloque(bloque.get("paragraph"));
                    if (!texto.isBlank()) {
                        html.append("<p>").append(texto).append("</p>\n");
                    }
                    break;

                case "heading_1":
                    html.append("<h1>")
                        .append(extraerTextoDeBloque(bloque.get("heading_1")))
                        .append("</h1>\n");
                    break;

                case "heading_2":
                    html.append("<h2>")
                        .append(extraerTextoDeBloque(bloque.get("heading_2")))
                        .append("</h2>\n");
                    break;

                case "heading_3":
                    html.append("<h3>")
                        .append(extraerTextoDeBloque(bloque.get("heading_3")))
                        .append("</h3>\n");
                    break;

                case "bulleted_list_item":
                    // TODO: debería envolver los <li> consecutivos en <ul>...</ul>
                    html.append("<li>")
                        .append(extraerTextoDeBloque(bloque.get("bulleted_list_item")))
                        .append("</li>\n");
                    break;

                case "numbered_list_item":
                    html.append("<li>")
                        .append(extraerTextoDeBloque(bloque.get("numbered_list_item")))
                        .append("</li>\n");
                    break;

                case "image":
                    String imgUrl = extraerUrlDeImagen(bloque.get("image"));
                    if (imgUrl != null) {
                        html.append("<img src=\"").append(imgUrl)
                            .append("\" alt=\"\" style=\"max-width:100%\" />\n");
                    }
                    break;

                case "quote":
                    html.append("<blockquote>")
                        .append(extraerTextoDeBloque(bloque.get("quote")))
                        .append("</blockquote>\n");
                    break;

                case "divider":
                    html.append("<hr />\n");
                    break;

                default:
                    break;
            }
        }

        return html.toString();
    }

    // Extrae texto de un bloque respetando negritas, cursivas y links
    private String extraerTextoDeBloque(JsonNode bloque) {
        if (bloque == null) return "";

        JsonNode richText = bloque.get("rich_text");
        if (richText == null || richText.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (JsonNode fragmento : richText) {
            String texto = fragmento.get("plain_text").asText();

            JsonNode annotations = fragmento.get("annotations");
            if (annotations != null) {
                if (annotations.get("bold").asBoolean()) {
                    texto = "<strong>" + texto + "</strong>";
                }
                if (annotations.get("italic").asBoolean()) {
                    texto = "<em>" + texto + "</em>";
                }
                if (annotations.get("code").asBoolean()) {
                    texto = "<code>" + texto + "</code>";
                }
            }

            JsonNode href = fragmento.get("href");
            if (href != null && !href.isNull()) {
                texto = "<a href=\"" + href.asText() + "\" target=\"_blank\">" + texto + "</a>";
            }

            sb.append(texto);
        }
        return sb.toString();
    }

    private String extraerUrlDeImagen(JsonNode imagen) {
        try {
            String tipo = imagen.get("type").asText();
            if ("file".equals(tipo)) {
                return imagen.get("file").get("url").asText();
            }
            if ("external".equals(tipo)) {
                return imagen.get("external").get("url").asText();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
