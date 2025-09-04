PV Challenge - Entrega rápida
=============================

1) Requisitos
- Docker y Docker Compose
- (Opcional) Maven 3.9+ y JDK 21 si querés compilar local

2) Build y ejecución
- Compilar jar local (opcional):  mvn -DskipTests package
- Levantar todo:                 docker compose up --build -d
- Ver logs de la app:            docker compose logs -f app
- Parar:                         docker compose down

3) Probar (usuarios demo)
- user/user y admin/admin
- Ejemplo: curl -i -u user:user http://localhost:8080/api/pointsofsale

4) Postman
- Importar pv-challenge.postman_collection.json
- Variables: baseUrl=http://localhost:8080, usuarios por defecto cargados.

5) Archivos incluidos
- Dockerfile
- docker-compose.yml
- pv-challenge.postman_collection.json
- PV_Challenge_Version1_Manual.rtf (manual completo)

6) Notas
- SPRING_DATA_MONGODB_URI apunta a 'mongo' (la red de compose resuelve el name del servicio).
- El perfil 'compose' puede usarse para overrides (application-compose.yml) si fuera necesario.
- Swagger UI (si usás springdoc): http://localhost:8080/swagger-ui/index.html
- Actuator (si habilitado): http://localhost:8080/actuator/health
