package com.noticiasuai;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoticiaService {

    private static final Logger log = LoggerFactory.getLogger(NoticiaService.class);

    private final NotionClient notionClient;
    private final NotionMapper mapper;

    public NoticiaService(NotionClient notionClient, NotionMapper mapper) {
        this.notionClient = notionClient;
        this.mapper = mapper;
    }

    @Cacheable(value = "noticias", key = "#categoria != null ? #categoria : 'todas'")
    public List<NoticiaResumenDto> listar(String categoria) {
        log.info("Consultando Notion (sin caché): categoria={}", categoria);

        JsonNode results = notionClient.queryDatabase(categoria);
        List<NoticiaResumenDto> noticias = new ArrayList<>();

        if (results != null && results.isArray()) {
            for (JsonNode page : results) {
                try {
                    noticias.add(mapper.toResumen(page));
                } catch (Exception e) {
                    // Si una noticia tiene datos incompletos, la saltamos
                    log.warn("Error mapeando noticia {}: {}",
                            page.get("id").asText(), e.getMessage());
                }
            }
        }

        return noticias;
    }

    @Cacheable(value = "detalle", key = "#slug")
    public NoticiaDto detalle(String slug) {
        log.info("Consultando detalle en Notion (sin caché): slug={}", slug);

        // Notion no permite filtrar rich_text con "equals",
        // así que traemos todas y buscamos por slug en Java
        // TODO: si crecen mucho las noticias, guardar un mapa slug -> pageId
        JsonNode results = notionClient.queryDatabase(null);

        if (results != null && results.isArray()) {
            for (JsonNode page : results) {
                try {
                    NoticiaResumenDto resumen = mapper.toResumen(page);
                    if (slug.equals(resumen.getSlug())) {
                        String pageId = page.get("id").asText();
                        JsonNode bloques = notionClient.getBlocks(pageId);
                        return mapper.toCompleta(page, bloques);
                    }
                } catch (Exception e) {
                    log.warn("Error procesando página: {}", e.getMessage());
                }
            }
        }

        throw new NoticiaNotFoundException(slug);
    }

    @Cacheable("categorias")
    public List<String> categorias() {
        List<NoticiaResumenDto> todas = listar(null);
        return todas.stream()
                .map(NoticiaResumenDto::getCategoria)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 300_000)  // TODO: hacer configurable desde application.yml
    @CacheEvict(value = {"noticias", "detalle", "categorias"}, allEntries = true)
    public void limpiarCache() {
        log.debug("Caché limpiado");
    }
}
