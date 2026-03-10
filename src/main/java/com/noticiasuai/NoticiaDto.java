package com.noticiasuai;

import java.util.List;

public class NoticiaDto {

    private String id;
    private String titulo;
    private String slug;
    private String resumen;
    private String contenido;       // HTML generado de los bloques de Notion
    private String categoria;
    private String autor;
    private String fechaPublicacion;
    private String portadaUrl;
    private String videoUrl;
    private boolean destacada;
    private List<String> tags;

    public NoticiaDto() {}

    public NoticiaDto(String id, String titulo, String slug, String resumen,
                      String contenido, String categoria, String autor,
                      String fechaPublicacion, String portadaUrl, String videoUrl,
                      boolean destacada, List<String> tags) {
        this.id = id;
        this.titulo = titulo;
        this.slug = slug;
        this.resumen = resumen;
        this.contenido = contenido;
        this.categoria = categoria;
        this.autor = autor;
        this.fechaPublicacion = fechaPublicacion;
        this.portadaUrl = portadaUrl;
        this.videoUrl = videoUrl;
        this.destacada = destacada;
        this.tags = tags;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getResumen() { return resumen; }
    public void setResumen(String resumen) { this.resumen = resumen; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(String fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }

    public String getPortadaUrl() { return portadaUrl; }
    public void setPortadaUrl(String portadaUrl) { this.portadaUrl = portadaUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public boolean isDestacada() { return destacada; }
    public void setDestacada(boolean destacada) { this.destacada = destacada; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
