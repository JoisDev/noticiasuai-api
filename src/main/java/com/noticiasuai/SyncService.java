package com.noticiasuai;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SyncService {

    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

    private final NotionClient notionClient;
    private final NotionMapper mapper;
    private final NoticiaRepository repo;

    public SyncService(NotionClient notionClient, NotionMapper mapper, NoticiaRepository repo) {
        this.notionClient = notionClient;
        this.mapper = mapper;
        this.repo = repo;
    }

    /**
     * Cada 5 minutos: lee Notion, descarga imágenes, convierte a base64,
     * guarda/actualiza en PostgreSQL.
     */
    @Scheduled(fixedRateString = "${app.sync-interval:300000}", initialDelay = 5000)
    public void sincronizar() {
        log.info("── Sync iniciado ──");

        try {
            JsonNode results = notionClient.queryDatabase();
            if (results == null || !results.isArray()) {
                log.warn("Notion no devolvió resultados");
                return;
            }

            int nuevas = 0, actualizadas = 0, errores = 0;

            for (JsonNode page : results) {
                try {
                    String pageId = page.get("id").asText();
                    String lastEdited = page.get("last_edited_time").asText();

                    // Verificar si ya existe y si cambió
                    var existente = repo.findById(pageId);
                    if (existente.isPresent()) {
                        NoticiaEntity actual = existente.get();
                        // Si el último sync es reciente y no cambió, saltar
                        if (actual.getUltimoSync() != null &&
                                actual.getUltimoSync().toString().compareTo(lastEdited) > 0) {
                            continue;
                        }
                        actualizadas++;
                    } else {
                        nuevas++;
                    }

                    // Convertir página de Notion a entidad (incluye descarga de imágenes)
                    NoticiaEntity entity = mapper.toEntity(page);
                    repo.save(entity);

                } catch (Exception e) {
                    errores++;
                    log.warn("Error sincronizando página: {}", e.getMessage());
                }
            }

            log.info("── Sync completado: {} nuevas, {} actualizadas, {} errores ──",
                    nuevas, actualizadas, errores);

        } catch (Exception e) {
            log.error("Error en sync general: {}", e.getMessage());
        }
    }
}
