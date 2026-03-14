# noticiasuai-api

API REST con Spring Boot que consume Notion como CMS para noticias corporativas.

## Estructura

```
noticiasuai-api/
├── src/main/java/com/noticiasuai/
│   ├── NoticiasApp.java
│   ├── NoticiaController.java
│   ├── NoticiaService.java
│   ├── NotionClient.java
│   ├── NotionMapper.java
│   ├── NoticiaDto.java
│   ├── NoticiaResumenDto.java
│   ├── NoticiaNotFoundException.java
│   └── GlobalExceptionHandler.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-main.yml
│   └── application-qa.yml
├── Dockerfile
├── docker-compose.yml
├── .env.main.example
├── .env.qa.example
└── .dockerignore
```

## Requisitos

- Java 17+
- Maven 3.8+
- Docker y Docker Compose
- Token de integración de Notion
- ID de base de datos en Notion

---

## Docker

### Ambientes

| Ambiente | Puerto | Base de datos Notion | Logs | Uso |
|----------|--------|----------------------|------|-----|
| **main** | 8080 | Producción | INFO | Datos reales |
| **qa** | 8081 | Pruebas | DEBUG | Probar cambios antes de subir |

### Configurar variables de entorno

```bash
cp .env.main.example .env.main
cp .env.qa.example .env.qa
```

Editar cada archivo con los tokens correspondientes. Cada ambiente usa una base de datos de Notion diferente.

### Levantar

```bash
# Los dos ambientes
docker compose up -d

# Solo producción
docker compose up -d app-main

# Solo QA
docker compose up -d app-qa
```

### Verificar

```bash
docker compose ps
docker compose logs -f app-main
curl http://localhost:8080/api/categorias    # main
curl http://localhost:8081/api/categorias    # qa
```

### Reconstruir después de cambios

```bash
docker compose build
docker compose up -d
```

### Detener

```bash
docker compose down
```

---

## Seguridad

### Contenedor

- **Usuario sin root:** corre con `appuser` sin privilegios de administrador
- **Multi-stage build:** imagen final solo tiene JRE, no compilador ni Maven
- **Alpine:** imagen mínima (~80MB)

### Variables de entorno

- Tokens en archivos `.env.main` y `.env.qa` que Git ignora
- Los `.env.*.example` son plantillas sin datos reales

### API

- **CORS:** solo el dominio del frontend puede llamar a la API
- **Sin stack traces:** errores genéricos al usuario, detalle en logs
- **Solo lectura:** solo endpoints GET
- **Sanitización:** parámetros limpiados antes de enviar a Notion

---

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | /api/noticias | Lista noticias (filtro: `?categoria=X`) |
| GET | /api/noticias/{slug} | Detalle completo |
| GET | /api/categorias | Categorías únicas |

### GET /api/noticias

```json
[
  {
    "id": "a1b2c3d4-e5f6-...",
    "titulo": "Inauguración de nueva sede regional",
    "slug": "inauguracion-nueva-sede-regional",
    "resumen": "La empresa inauguró su nueva sede...",
    "categoria": "Institucional",
    "autor": "Comunicación Corporativa",
    "fechaPublicacion": "2026-03-05",
    "portadaUrl": "https://...",
    "destacada": true
  }
]
```

### GET /api/noticias/{slug}

```json
{
  "id": "a1b2c3d4-e5f6-...",
  "titulo": "Inauguración de nueva sede regional",
  "contenido": "<h2>Un paso importante</h2><p>...</p>",
  "categoria": "Institucional",
  "videoUrl": "https://youtube.com/watch?v=...",
  "tags": ["sedes", "crecimiento"]
}
```

---

## Base de datos en Notion

1. Crear integración en https://www.notion.so/my-integrations
2. Copiar el token (`secret_...`)
3. Crear base de datos con estas propiedades:

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

El contenido se escribe en el cuerpo de la página de Notion. El backend lo convierte a HTML.

4. Compartir la base de datos con la integración
5. Copiar el database ID de la URL

---

## Desarrollo local (sin Docker)

```bash
export NOTION_TOKEN=secret_xxx
export NOTION_DATABASE_ID=abc123...
mvn spring-boot:run
```

---

## Arquitectura

```
Notion (CMS)  →  Spring Boot (API)  →  React (Frontend)
                      ↓
                 Caché en memoria (5 min)
```

9 clases · 689 líneas · 3 endpoints · 2 ambientes Docker
