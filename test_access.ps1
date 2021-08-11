
# Read environment vqriables from the ".env" file - if it exists.

if (Test-Path .env) {
    foreach($line in Get-Content .\.env) {
        $vars=$line.Split("=")
        $location=Get-Location
        Set-Location Env:
        Set-Content -Path $vars[0] -Value $vars[1]
        Set-Location $location
    }
}

# Get the access token from the keycloak server using the environment settings (read from the file, or set prior to this script) if possible.

if($ENV:QUARKUS_OIDC_AUTH_SERVER_URL -and $ENV:QUARKUS_OIDC_CREDENTIALS_SECRET) {

    $access_token=(http -a backend-service:$ENV:QUARKUS_OIDC_CREDENTIALS_SECRET --form POST $ENV:QUARKUS_OIDC_AUTH_SERVER_URL/protocol/openid-connect/token username=alice password='alice' grant_type=password | jq --raw-output '.access_token')

} else {

    # If the environment doesn't have settings form QUARKUS_AUTH_SERVER_URL and QUARKUS_OIDC_CREDENTIALS_SECRET, then try to determine the local docker
    # port that the quarkus:dev keycloak instance is running on. This keycloak instance is only started if the QUARKUS_OIDC_AUTH_SERVER_URL is not set
    # and it's corresponding property quarkus.oidc.auth-server-url is also not set.

    # This *MIGHT* work for you. I'm trying to extract the port from the docker output. Unfortunately, I can't seem to find a reliable way of doing this.
    # Here I'm making the very rash assumption that the only docker process which is running on both internal ports 8443 and 8080 is the keycloak server.
    # It should work as long as there aren't any other docker containers running on the dev host when quarkus starts running. In other words, all containers
    # should have been started by .\mvnw clean compile quarkus:dev, and you should only be running one quarkus:dev server at a time.

    try {
        $port=((docker port (docker ps -f "expose=8443/tcp" -q) 8080/tcp | Select-Object -First 1).split(":") | Select-Object -Last 1)
    } catch {
        $port = Read-Host "Please enter the Keycloak TCP port"
    }

    $access_token=(http -a backend-service:secret --form POST :$port/auth/realms/quarkus/protocol/openid-connect/token username=alice password='alice' grant_type=password | jq --raw-output '.access_token')

}

Write-Host "Starting capture"

$dbid=(http POST :8080/api/capture "Authorization:Bearer $access_token")

Write-Host "Capture ID is"

$dbid

Start-Sleep -s 1.5

Write-Host "Stopping capture..."

http PUT :8080/api/capture/$dbid "Authorization:Bearer $access_token"

Start-Sleep -s 3.0

Write-Host "Listing captures..."

http GET :8080/api/capture "Authorization:Bearer $access_token"

Write-Host "Downloading capture..."

http GET :8080/api/capture/$dbid "Authorization:Bearer $access_token" --download -o capture.pcapng

Start-Sleep -s 1.5

Write-Host "Deleting capture..."

http DELETE :8080/api/capture/$dbid "Authorization:Bearer $access_token"

Write-Host "Listing captures remaining..."

http GET :8080/api/capture "Authorization:Bearer $access_token"
