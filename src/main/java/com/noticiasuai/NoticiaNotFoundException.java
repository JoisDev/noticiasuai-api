package com.noticiasuai;

public class NoticiaNotFoundException extends RuntimeException {

    public NoticiaNotFoundException(String slug) {
        super("No se encontró la noticia: " + slug);
    }
}
