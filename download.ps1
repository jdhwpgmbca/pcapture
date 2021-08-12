$uuid=$args[0]

# Read environment vqriables from the ".env" file - if it exists.

if (Test-Path .env) {
    foreach($line in Get-Content .\.env) {
        if(!$line.startsWith("#")) {
            $vars=$line.Split("=")
            $location=Get-Location
            Set-Location Env:
            Set-Content -Path $vars[0] -Value $vars[1]
            Set-Location $location
        }
    }
}

# Get the access token from the keycloak server using the environment settings (read from the file, or set prior to this script) if possible.

if($ENV:QUARKUS_OIDC_AUTH_SERVER_URL -and $ENV:QUARKUS_OIDC_CREDENTIALS_SECRET) {

    $access_token=(http -a backend-service:$ENV:QUARKUS_OIDC_CREDENTIALS_SECRET --form POST $ENV:QUARKUS_OIDC_AUTH_SERVER_URL/protocol/openid-connect/token username=alice password='alice' grant_type=password | jq --raw-output '.access_token')

} else {

    # If the environment doesn't have settings form QUARKUS_AUTH_SERVER_URL and QUARKUS_OIDC_CREDENTIALS_SECRET, then try to determine the local docker
    # port that the quarkus:dev keycloak instance is running on. This keycloak instance is only started if the QUARKUS_OIDC_AUTH_SERVER_URL is not set
    # and it's corresponding property quarkus.oidc.auth-server-url is also not set.

    try {

        # Find the last keycloak process in the process list (the first started), then use docker port to get the external bind address, then separate it
        # into host and port parts, and finally select the port.

        $port=((docker port (docker ps -f "ancestor=quay.io/keycloak/keycloak:14.0.0" -q | Select-Object -Last 1) 8080/tcp | Select-Object -First 1).Split(":") | Select-Object -Last 1)

    } catch {

        # If we couldn't get the port, then give the user the opportunity to enter it themselves.

        $port = Read-Host "Please enter the Keycloak TCP port"

    }

    # Get the access token using the default credentials.

    $access_token=(http -a backend-service:secret --form POST :$port/auth/realms/quarkus/protocol/openid-connect/token username='alice' password='alice' grant_type=password | jq --raw-output '.access_token')

}

Write-Host "Downloading capture..."

http -b GET :8080/api/capture/$uuid "Authorization:Bearer $access_token" --download -o capture.pcapng
