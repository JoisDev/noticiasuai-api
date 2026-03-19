package com.noticiasuai;

import java.util.List;

public class NoticiaDto {

    private String id;
    private String titulo;
    private String slug;
    private String resumen;
    private String contenido;
    private String categoria;
    private String autor;
    private String fechaPublicacion;
    private String portadaBase64;
    private String videoUrl;
    private boolean destacada;
    private List<String> tags;

    public NoticiaDto() {}

    public static NoticiaDto fromEntity(NoticiaEntity e) {
        NoticiaDto d = new NoticiaDto();
        d.id = e.getId();
        d.titulo = e.getTitulo();
        d.slug = e.getSlug();
        d.resumen = e.getResumen();
        d.contenido = e.getContenido();
        d.categoria = e.getCategoria();
        d.autor = e.getAutor();
        d.fechaPublicacion = e.getFechaPublicacion() != null ? e.getFechaPublicacion().toString() : null;
        d.portadaBase64 = e.getPortadaBase64();
        d.videoUrl = e.getVideoUrl();
        d.destacada = e.isDestacada();
        d.tags = e.getEtiquetas() != null && !e.getEtiquetas().isBlank()
                ? List.of(e.getEtiquetas().split(","))
                : List.of();
        return d;
    }

    public static NoticiaDto resumenFromEntity(NoticiaEntity e) {
        NoticiaDto d = new NoticiaDto();
        d.id = e.getId();
        d.titulo = e.getTitulo();
        d.slug = e.getSlug();
        d.resumen = e.getResumen();
        d.categoria = e.getCategoria();
        d.autor = e.getAutor();
        d.fechaPublicacion = e.getFechaPublicacion() != null ? e.getFechaPublicacion().toString() : null;
        d.portadaBase64 = e.getPortadaBase64();
        d.destacada = e.isDestacada();
        d.videoUrl = e.getVideoUrl();
        return d;
    }

    // Getters
    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getSlug() { return slug; }
    public String getResumen() { return resumen; }
    public String getContenido() { return contenido; }
    public String getCategoria() { return categoria; }
    public String getAutor() { return autor; }
    public String getFechaPublicacion() { return fechaPublicacion; }
    public String getPortadaBase64() { return portadaBase64; }
    public String getVideoUrl() { return videoUrl; }
    public boolean isDestacada() { return destacada; }
    public List<String> getTags() { return tags; }
}
