$vol = (Join-Path (Get-Location).Path "realm-import.json") + ":/tmp/realm-import.json"

docker run -d --name keycloak -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin -e KEYCLOAK_IMPORT=/tmp/realm-import.json -v "$vol" -p 8180:8080 jboss/keycloak
