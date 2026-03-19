package com.noticiasuai;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class NotionMapper {

    private final NotionClient notionClient;

    public NotionMapper(NotionClient notionClient) {
        this.notionClient = notionClient;
    }

    public NoticiaEntity toEntity(JsonNode page) {
        JsonNode props = page.get("properties");
        NoticiaEntity e = new NoticiaEntity();

        e.setId(page.get("id").asText());
        e.setTitulo(extraerTitle(props, "Título"));
        e.setSlug(extraerTexto(props, "Slug"));
        e.setResumen(extraerTexto(props, "Resumen"));
        e.setCategoria(extraerSelect(props, "Categoría"));
        e.setAutor(extraerTexto(props, "Autor"));
        e.setEstado(extraerSelect(props, "Estado"));
        e.setDestacada(extraerCheckbox(props, "Destacada"));
        e.setVideoUrl(extraerUrl(props, "Video"));
        e.setEtiquetas(extraerMultiSelectAsString(props, "Etiquetas"));

        String fecha = extraerFecha(props, "Fecha de publicación");
        if (fecha != null) {
            try { e.setFechaPublicacion(LocalDate.parse(fecha)); }
            catch (Exception ex) { /* fecha inválida, se deja null */ }
        }

        // Descargar portada y convertir a base64
        String portadaUrl = extraerArchivo(props, "Portada");
        if (portadaUrl != null) {
            e.setPortadaBase64(notionClient.downloadImageAsBase64(portadaUrl));
        }

        // Leer bloques (contenido) y convertir a HTML
        JsonNode bloques = notionClient.getBlocks(e.getId());
        e.setContenido(bloquesAHtml(bloques));

        e.setUltimoSync(LocalDateTime.now());
        return e;
    }

    // ── Extractores ──

    private String extraerTitle(JsonNode props, String name) {
        try {
            JsonNode arr = props.get(name).get("title");
            if (arr == null || arr.isEmpty()) return "";
            return arr.get(0).get("plain_text").asText();
        } catch (Exception e) { return ""; }
    }

    private String extraerTexto(JsonNode props, String name) {
        try {
            JsonNode arr = props.get(name).get("rich_text");
            if (arr == null || arr.isEmpty()) return "";
            StringBuilder sb = new StringBuilder();
            for (JsonNode f : arr) sb.append(f.get("plain_text").asText());
            return sb.toString();
        } catch (Exception e) { return ""; }
    }

    private String extraerSelect(JsonNode props, String name) {
        try {
            JsonNode sel = props.get(name).get("select");
            if (sel == null || sel.isNull()) return null;
            return sel.get("name").asText();
        } catch (Exception e) { return null; }
    }

    private String extraerMultiSelectAsString(JsonNode props, String name) {
        try {
            JsonNode arr = props.get(name).get("multi_select");
            if (arr == null) return "";
            List<String> tags = new ArrayList<>();
            for (JsonNode item : arr) tags.add(item.get("name").asText());
            return String.join(",", tags);
        } catch (Exception e) { return ""; }
    }

    private String extraerFecha(JsonNode props, String name) {
        try {
            JsonNode date = props.get(name).get("date");
            if (date == null || date.isNull()) return null;
            return date.get("start").asText();
        } catch (Exception e) { return null; }
    }

    private boolean extraerCheckbox(JsonNode props, String name) {
        try { return props.get(name).get("checkbox").asBoolean(); }
        catch (Exception e) { return false; }
    }

    private String extraerUrl(JsonNode props, String name) {
        try {
            JsonNode url = props.get(name).get("url");
            if (url == null || url.isNull()) return null;
            return url.asText();
        } catch (Exception e) { return null; }
    }

    private String extraerArchivo(JsonNode props, String name) {
        try {
            JsonNode files = props.get(name).get("files");
            if (files == null || files.isEmpty()) return null;
            JsonNode f = files.get(0);
            String tipo = f.get("type").asText();
            if ("file".equals(tipo)) return f.get("file").get("url").asText();
            if ("external".equals(tipo)) return f.get("external").get("url").asText();
            return null;
        } catch (Exception e) { return null; }
    }

    // ── Bloques a HTML ──

    private String bloquesAHtml(JsonNode bloques) {
        // TODO: faltan tipos: table, toggle, callout, code block
        if (bloques == null) return "";
        StringBuilder html = new StringBuilder();

        for (JsonNode bloque : bloques) {
            String tipo = bloque.get("type").asText();
            switch (tipo) {
                case "paragraph":
                    String texto = richTextToHtml(bloque.get("paragraph"));
                    if (!texto.isBlank()) html.append("<p>").append(texto).append("</p>\n");
                    break;
                case "heading_1":
                    html.append("<h1>").append(richTextToHtml(bloque.get("heading_1"))).append("</h1>\n");
                    break;
                case "heading_2":
                    html.append("<h2>").append(richTextToHtml(bloque.get("heading_2"))).append("</h2>\n");
                    break;
                case "heading_3":
                    html.append("<h3>").append(richTextToHtml(bloque.get("heading_3"))).append("</h3>\n");
                    break;
                case "bulleted_list_item":
                    // TODO: envolver consecutivos en <ul>
                    html.append("<li>").append(richTextToHtml(bloque.get("bulleted_list_item"))).append("</li>\n");
                    break;
                case "numbered_list_item":
                    html.append("<li>").append(richTextToHtml(bloque.get("numbered_list_item"))).append("</li>\n");
                    break;
                case "image":
                    String imgUrl = extraerImagenDeBloque(bloque.get("image"));
                    if (imgUrl != null) {
                        // Convertir imagen del contenido a base64 también
                        String base64 = notionClient.downloadImageAsBase64(imgUrl);
                        if (base64 != null) {
                            html.append("<img src=\"").append(base64).append("\" style=\"max-width:100%\" />\n");
                        }
                    }
                    break;
                case "quote":
                    html.append("<blockquote>").append(richTextToHtml(bloque.get("quote"))).append("</blockquote>\n");
                    break;
                case "divider":
                    html.append("<hr />\n");
                    break;
                default: break;
            }
        }
        return html.toString();
    }

    private String richTextToHtml(JsonNode bloque) {
        if (bloque == null) return "";
        JsonNode rt = bloque.get("rich_text");
        if (rt == null || rt.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (JsonNode f : rt) {
            String t = f.get("plain_text").asText();
            JsonNode ann = f.get("annotations");
            if (ann != null) {
                if (ann.get("bold").asBoolean()) t = "<strong>" + t + "</strong>";
                if (ann.get("italic").asBoolean()) t = "<em>" + t + "</em>";
                if (ann.get("code").asBoolean()) t = "<code>" + t + "</code>";
            }
            JsonNode href = f.get("href");
            if (href != null && !href.isNull()) t = "<a href=\"" + href.asText() + "\" target=\"_blank\">" + t + "</a>";
            sb.append(t);
        }
        return sb.toString();
    }

    private String extraerImagenDeBloque(JsonNode imagen) {
        try {
            String tipo = imagen.get("type").asText();
            if ("file".equals(tipo)) return imagen.get("file").get("url").asText();
            if ("external".equals(tipo)) return imagen.get("external").get("url").asText();
            return null;
        } catch (Exception e) { return null; }
    }
}
