package com.noticiasuai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface NoticiaRepository extends JpaRepository<NoticiaEntity, String> {

    Optional<NoticiaEntity> findBySlug(String slug);

    List<NoticiaEntity> findByEstadoOrderByFechaPublicacionDesc(String estado);

    List<NoticiaEntity> findByEstadoAndCategoriaOrderByFechaPublicacionDesc(String estado, String categoria);

    Optional<NoticiaEntity> findByEstadoAndDestacadaTrue(String estado);

    @Query("SELECT DISTINCT n.categoria FROM NoticiaEntity n WHERE n.estado = 'publicado' AND n.categoria IS NOT NULL ORDER BY n.categoria")
    List<String> findCategoriasPublicadas();
}
