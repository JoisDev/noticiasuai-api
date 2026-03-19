package com.noticiasuai;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "${app.frontend-url}")
public class ContentApiController {

    private final NoticiaRepository repo;

    public ContentApiController(NoticiaRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/noticias")
    public List<NoticiaDto> listar(@RequestParam(required = false) String categoria) {
        List<NoticiaEntity> noticias;
        if (categoria != null && !categoria.isBlank()) {
            noticias = repo.findByEstadoAndCategoriaOrderByFechaPublicacionDesc("publicado", categoria);
        } else {
            noticias = repo.findByEstadoOrderByFechaPublicacionDesc("publicado");
        }
        return noticias.stream().map(NoticiaDto::resumenFromEntity).toList();
    }

    @GetMapping("/noticias/{slug}")
    public NoticiaDto detalle(@PathVariable String slug) {
        return repo.findBySlug(slug)
                .map(NoticiaDto::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró la noticia: " + slug));
    }

    @GetMapping("/categorias")
    public List<String> categorias() {
        return repo.findCategoriasPublicadas();
    }

    @GetMapping("/noticias/destacada")
    public NoticiaDto destacada() {
        return repo.findByEstadoAndDestacadaTrue("publicado")
                .map(NoticiaDto::fromEntity)
                .orElse(null);
    }
}
