# Way Noticias

Portal de noticias internas para Agencias Way Guatemala.  
Equipo de comunicaciones publica en Notion → aparece automáticamente en TVs del lobby e intranet.

## Arquitectura

```
Notion (CMS) → SyncService (cada 5 min) → PostgreSQL → API REST → tv.html / intranet.html
                    ↓
            Descarga imágenes
            Convierte a base64
```

## Stack

- Java 17 + Spring Boot 3.2.5
- PostgreSQL 16
- Nginx
- Docker Compose
- Notion API
- HTML/JS puro (sin frameworks)

## Estructura

```
├── src/main/java/com/noticiasuai/
│   ├── NoticiasApp.java            ← Punto de entrada
│   ├── NoticiaController.java      ← API original (lee de Notion)
│   ├── ContentApiController.java   ← API nueva (lee de PostgreSQL)
│   ├── SyncService.java            ← Sincroniza Notion → BD cada 5 min
│   ├── NoticiaEntity.java          ← Entidad JPA
│   ├── NoticiaRepository.java      ← JPA Repository
│   ├── NoticiaService.java         ← Servicio original
│   ├── NotionClient.java           ← Cliente Notion API
│   ├── NotionMapper.java           ← JSON Notion → DTOs/HTML
│   ├── NoticiaDto.java             ← DTO detalle
│   ├── NoticiaResumenDto.java      ← DTO tarjeta
│   ├── NoticiaNotFoundException.java
│   └── GlobalExceptionHandler.java
├── src/main/resources/
│   ├── static/
│   │   ├── tv.html                 ← Pantalla TVs lobby
│   │   └── intranet.html           ← Portal empleados
│   ├── application.yml
│   ├── application-main.yml
│   └── application-qa.yml
├── nginx/default.conf
├── Dockerfile
├── docker-compose.yml
├── .env.main.example
└── .env.qa.example
```

## Levantar

```bash
# 1. Copiar y llenar variables de entorno
cp .env.main.example .env.main
cp .env.qa.example .env.qa

# 2. Levantar todo
docker compose up -d

# 3. Solo QA
docker compose up -d db app-qa
```

## URLs

| Qué | URL | Nota |
|-----|-----|------|
| Intranet | http://localhost | Portal empleados (Nginx) |
| TV | http://localhost/tv | Pantalla TVs lobby |
| API (main) | http://localhost:8080/api/content/noticias | Desde PostgreSQL |
| API (QA) | http://localhost:8081/api/content/noticias | Ambiente pruebas |
| Sync status | http://localhost:8080/api/content/sync-status | Estado de sincronización |

## Endpoints (ContentApiController — desde PostgreSQL)

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | /api/content/noticias | Lista noticias (?categoria=X) |
| GET | /api/content/noticias/{slug} | Detalle completo |
| GET | /api/content/categorias | Categorías únicas |
| GET | /api/content/destacada | Noticia destacada |
| GET | /api/content/sync-status | Estado de sincronización |

## TV — 7 tipos de slides

1. Noticia destacada (imagen + overlay magenta)
2. Cumpleaños de la semana
3. Frase motivacional con Wayito
4. Próximos eventos
5. Vacante interna
6. Clima Guatemala City
7. Noticia de la semana

Auto-rotación cada 7 segundos. Se recargan datos cada 5 minutos.

## Flujo de sincronización

1. `SyncService` se ejecuta cada 5 minutos
2. Consulta la base de datos de Notion (solo noticias con Estado = "publicado")
3. Para cada noticia, compara `last_edited_time` con lo guardado en PostgreSQL
4. Si cambió: descarga la imagen de portada, la convierte a base64, lee los bloques de contenido y los convierte a HTML
5. Guarda/actualiza en PostgreSQL
6. Los frontends leen de PostgreSQL (rápido, sin depender de Notion)

## Seguridad

- Contenedor corre con usuario `appuser` sin root
- Multi-stage build: imagen final solo tiene JRE
- Imagen Alpine (~80MB)
- Tokens en archivos .env que Git ignora
- CORS configurado por variable de entorno
- Solo endpoints GET (lectura)
- Errores genéricos al usuario, detalle en logs

## Comandos útiles

```bash
# Ver logs de sync
docker compose logs -f app-main | grep -i sync

# Ver estado de la BD
docker compose exec db psql -U waynoticias -c "SELECT count(*) FROM noticias;"

# Reconstruir después de cambios
docker compose build && docker compose up -d

# Detener todo
docker compose down

# Detener y borrar datos
docker compose down -v
```
