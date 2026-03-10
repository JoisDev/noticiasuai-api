package com.noticiasuai;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NotionClient {

    private static final Logger log = LoggerFactory.getLogger(NotionClient.class);
    private static final String BASE_URL = "https://api.notion.com/v1";

    private final RestTemplate rest;
    private final String token;
    private final String databaseId;
    private final String apiVersion;

    public NotionClient(
            @Value("${notion.token}") String token,
            @Value("${notion.database-id}") String databaseId,
            @Value("${notion.api-version}") String apiVersion) {
        this.token = token;
        this.databaseId = databaseId;
        this.apiVersion = apiVersion;
        this.rest = new RestTemplate();
    }

    /**
     * Consulta la base de datos de Notion.
     * Filtra solo noticias con Estado = "publicado".
     */
    public JsonNode queryDatabase(String categoria) {
        String url = BASE_URL + "/databases/" + databaseId + "/query";
        String body = buildQueryBody(categoria);
        HttpEntity<String> request = new HttpEntity<>(body, buildHeaders());

        try {
            ResponseEntity<JsonNode> response = rest.postForEntity(url, request, JsonNode.class);
            return response.getBody().get("results");
        } catch (Exception e) {
            log.error("Error consultando Notion database: {}", e.getMessage());
            throw new RuntimeException("No se pudo conectar con Notion. Intente de nuevo.");
        }
    }

    /**
     * Lee los bloques (contenido) de una página: párrafos, imágenes, headings, etc.
     */
    public JsonNode getBlocks(String pageId) {
        // TODO: Notion pagina de 100 en 100. Si una noticia es muy larga
        //       habría que usar "next_cursor" para traer más bloques.
        String url = BASE_URL + "/blocks/" + pageId + "/children?page_size=100";
        HttpEntity<String> request = new HttpEntity<>(buildHeaders());

        try {
            ResponseEntity<JsonNode> response = rest.exchange(
                    url, HttpMethod.GET, request, JsonNode.class);
            return response.getBody().get("results");
        } catch (Exception e) {
            log.error("Error leyendo bloques de página {}: {}", pageId, e.getMessage());
            throw new RuntimeException("No se pudo leer el contenido de la noticia.");
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("Notion-Version", apiVersion);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // TODO: esto se podría hacer más limpio con un Map<String, Object> y ObjectMapper
    private String buildQueryBody(String categoria) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"filter\":{\"and\":[");
        sb.append("{\"property\":\"Estado\",\"select\":{\"equals\":\"publicado\"}}");

        if (categoria != null && !categoria.isBlank()) {
            sb.append(",{\"property\":\"Categoría\",\"select\":{\"equals\":\"")
              .append(categoria.replace("\"", ""))
              .append("\"}}");
        }

        sb.append("]},");
        sb.append("\"sorts\":[{\"property\":\"Fecha de publicación\",\"direction\":\"descending\"}]");
        sb.append("}");
        return sb.toString();
    }
}
