package com.noticiasuai;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "${app.frontend-url}")
public class NoticiaController {

    private final NoticiaService service;

    public NoticiaController(NoticiaService service) {
        this.service = service;
    }

    @GetMapping("/noticias")
    public List<NoticiaResumenDto> listar(
            @RequestParam(required = false) String categoria) {
        // TODO: agregar paginación cuando haya muchas noticias
        return service.listar(categoria);
    }

    @GetMapping("/noticias/{slug}")
    public NoticiaDto detalle(@PathVariable String slug) {
        return service.detalle(slug);
    }

    @GetMapping("/categorias")
    public List<String> categorias() {
        return service.categorias();
    }
}
