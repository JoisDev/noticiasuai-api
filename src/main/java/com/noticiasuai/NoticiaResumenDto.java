package com.noticiasuai;

// TODO: investigar si se puede usar Java Record en vez de clase con getters/setters
public class NoticiaResumenDto {

    private String id;
    private String titulo;
    private String slug;
    private String resumen;
    private String categoria;
    private String autor;
    private String fechaPublicacion;
    private String portadaUrl;
    private boolean destacada;

    public NoticiaResumenDto() {}

    public NoticiaResumenDto(String id, String titulo, String slug, String resumen,
                             String categoria, String autor, String fechaPublicacion,
                             String portadaUrl, boolean destacada) {
        this.id = id;
        this.titulo = titulo;
        this.slug = slug;
        this.resumen = resumen;
        this.categoria = categoria;
        this.autor = autor;
        this.fechaPublicacion = fechaPublicacion;
        this.portadaUrl = portadaUrl;
        this.destacada = destacada;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getResumen() { return resumen; }
    public void setResumen(String resumen) { this.resumen = resumen; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(String fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }

    public String getPortadaUrl() { return portadaUrl; }
    public void setPortadaUrl(String portadaUrl) { this.portadaUrl = portadaUrl; }

    public boolean isDestacada() { return destacada; }
    public void setDestacada(boolean destacada) { this.destacada = destacada; }
}
