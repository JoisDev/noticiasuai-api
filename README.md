# noticias-api

API REST con Spring Boot que consume Notion como CMS para noticias corporativas.

## Estructura

```
src/main/java/com/noticiasuai/
├── NoticiasApp.java
├── NoticiaController.java
├── NoticiaService.java
├── NotionClient.java
├── NotionMapper.java
├── NoticiaDto.java
├── NoticiaResumenDto.java
├── NoticiaNotFoundException.java
└── GlobalExceptionHandler.java
```

## Requisitos

- Java 17+
- Maven 3.8+
- Token de integración de Notion
- ID de base de datos en Notion

## Variables de entorno

```bash
NOTION_TOKEN=secret_xxxxxxxxxxxxxxxxxxxxxxxxx
NOTION_DATABASE_ID=abc123def456abc123def456abc123de
FRONTEND_URL=http://localhost:5173
```

## Ejecutar

```bash
export NOTION_TOKEN=secret_xxx
export NOTION_DATABASE_ID=abc123...
mvn spring-boot:run
```

Servidor en `http://localhost:8080`

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | /api/noticias | Lista noticias (filtro opcional: ?categoria=X) |
| GET | /api/noticias/{slug} | Detalle completo de una noticia |
| GET | /api/categorias | Lista de categorías únicas |

## Base de datos en Notion

| Propiedad | Tipo | Ejemplo |
|-----------|------|---------|
| Título | Title | Inauguración nueva sede |
| Slug | Rich Text | inauguracion-nueva-sede |
| Resumen | Rich Text | La empresa inauguró... |
| Categoría | Select | Institucional |
| Autor | Rich Text | Comunicación |
| Fecha de publicación | Date | 2026-03-05 |
| Portada | Files & Media | (imagen) |
| Video | URL | https://youtu.be/... |
| Destacada | Checkbox | ✓ |
| Estado | Select | publicado |
| Etiquetas | Multi-select | sedes, 2026 |

**Opciones de Estado:** borrador, publicado, archivado

## Deploy


