$dbid=$args[0]

# Read environment vqriables from the ".env" file - if it exists.

$location = (Get-Location).Path
while ($location -and !(Test-Path (Join-Path $location '.env'))) {
    $location = Split-Path $location -Parent
}
$envpath = Join-Path $location '.env'

if (Test-Path $envpath) {
    foreach($line in Get-Content $envpath) {
        if($line -and !$line.startsWith("#")) {
            $vars=$line.Split("=")
            $location=Get-Location
            Set-Location Env:
            Set-Content -Path $vars[0] -Value $vars[1]
            Set-Location $location
        }
    }
}

if($ENV:API_CA_CERT -and (Test-Path $ENV:API_CA_CERT)) {
    $API_VERIFY="--verify=$ENV:API_CA_CERT"
}

if($ENV:KEYCLOAK_CA_CERT -and (Test-Path $ENV:KEYCLOAK_CA_CERT)) {
    $KEYCLOAK_VERIFY="--verify=$ENV:KEYCLOAK_CA_CERT"
}

# Get the access token from the keycloak server using the environment settings (read from the file, or set prior to this script) if possible.

if($ENV:QUARKUS_OIDC_AUTH_SERVER_URL -and $ENV:QUARKUS_OIDC_CREDENTIALS_SECRET) {

    $access_token=(http $KEYCLOAK_VERIFY -a backend-service:$ENV:QUARKUS_OIDC_CREDENTIALS_SECRET --form POST $ENV:QUARKUS_OIDC_AUTH_SERVER_URL/protocol/openid-connect/token username="$ENV:ADMIN_USER_NAME" password="$ENV:ADMIN_USER_PASSWORD" grant_type=password | jq --raw-output '.access_token')

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

    $access_token=(http $KEYCLOAK_VERIFY -a backend-service:secret --form POST :$port/auth/realms/quarkus/protocol/openid-connect/token username="$ENV:ADMIN_USER_NAME" password="$ENV:ADMIN_USER_PASSWORD" grant_type=password | jq --raw-output '.access_token')

}

Write-Host "Deleting capture..."

http -q $API_VERIFY DELETE $ENV:API_SERVER/api/capture/$dbid "Authorization:Bearer $access_token"
