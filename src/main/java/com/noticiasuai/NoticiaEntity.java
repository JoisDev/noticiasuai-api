package com.noticiasuai;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "noticias")
public class NoticiaEntity {

    @Id
    private String id;  // Notion page ID

    private String titulo;

    @Column(unique = true)
    private String slug;

    @Column(length = 500)
    private String resumen;

    @Column(columnDefinition = "TEXT")
    private String contenido;  // HTML

    private String categoria;
    private String autor;
    private LocalDate fechaPublicacion;

    @Column(columnDefinition = "TEXT")
    private String portadaBase64;  // imagen convertida a base64

    private String videoUrl;
    private boolean destacada;
    private String estado;
    private String etiquetas;  // comma separated

    private LocalDateTime ultimoSync;

    public NoticiaEntity() {}

    // --- Getters y Setters ---

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

    public LocalDate getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDate fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }

    public String getPortadaBase64() { return portadaBase64; }
    public void setPortadaBase64(String portadaBase64) { this.portadaBase64 = portadaBase64; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public boolean isDestacada() { return destacada; }
    public void setDestacada(boolean destacada) { this.destacada = destacada; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getEtiquetas() { return etiquetas; }
    public void setEtiquetas(String etiquetas) { this.etiquetas = etiquetas; }

    public LocalDateTime getUltimoSync() { return ultimoSync; }
    public void setUltimoSync(LocalDateTime ultimoSync) { this.ultimoSync = ultimoSync; }
}
