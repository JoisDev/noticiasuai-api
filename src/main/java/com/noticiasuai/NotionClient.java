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

    public JsonNode queryDatabase() {
        String url = BASE_URL + "/databases/" + databaseId + "/query";
        String body = "{\"filter\":{\"property\":\"Estado\",\"select\":{\"equals\":\"publicado\"}},"
                + "\"sorts\":[{\"property\":\"Fecha de publicación\",\"direction\":\"descending\"}]}";
        HttpEntity<String> request = new HttpEntity<>(body, headers());

        try {
            ResponseEntity<JsonNode> resp = rest.postForEntity(url, request, JsonNode.class);
            return resp.getBody().get("results");
        } catch (Exception e) {
            log.error("Error consultando Notion: {}", e.getMessage());
            return null;
        }
    }

    public JsonNode getBlocks(String pageId) {
        // TODO: paginar si hay más de 100 bloques
        String url = BASE_URL + "/blocks/" + pageId + "/children?page_size=100";
        HttpEntity<String> request = new HttpEntity<>(headers());

        try {
            ResponseEntity<JsonNode> resp = rest.exchange(url, HttpMethod.GET, request, JsonNode.class);
            return resp.getBody().get("results");
        } catch (Exception e) {
            log.error("Error leyendo bloques {}: {}", pageId, e.getMessage());
            return null;
        }
    }

    /**
     * Descarga una imagen desde URL y la convierte a base64
     */
    public String downloadImageAsBase64(String imageUrl) {
        try {
            ResponseEntity<byte[]> resp = rest.getForEntity(imageUrl, byte[].class);
            if (resp.getBody() != null) {
                String contentType = resp.getHeaders().getContentType() != null
                        ? resp.getHeaders().getContentType().toString()
                        : "image/png";
                return "data:" + contentType + ";base64," +
                        java.util.Base64.getEncoder().encodeToString(resp.getBody());
            }
        } catch (Exception e) {
            log.warn("No se pudo descargar imagen: {}", e.getMessage());
        }
        return null;
    }

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.set("Notion-Version", apiVersion);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }
}
