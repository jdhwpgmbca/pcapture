# This script can be used to add or remove capture filters from the program. It assumes you have both the HttPie program, and the jq program
# installed on your computer. The jq for Windows program can be installed using the Chocolatey Package Manager. HttPie is available from the
# Python pip package manager: python -m pip install --upgrade pip setuptools, then python -m pip install --upgrade httpie.

# Read environment vqriables from the ".env" file - if it exists.

if (Test-Path .env) {
    foreach($line in Get-Content .\.env) {
        if($line -and !$line.startsWith("#")) {
            $vars=$line.Split("=")
            $location=Get-Location
            Set-Location Env:
            Set-Content -Path $vars[0] -Value $vars[1]
            Set-Location $location
        }
    }
} elseif (Test-Path ..\.env) {
    foreach($line in Get-Content ..\.env) {
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

.\list-filters.ps1

$url_suffix = Read-Host -Prompt "Enter the URL suffix for the capture filter you wish to delete: "

Write-Host "URL suffix     = $url_suffix"

Read-Host -Prompt "If this is correct, press <Enter> to continue, or <CTRL-C> to abort."

# Get the access token from the keycloak server using the environment settings (read from the file, or set prior to this script) if possible.

if($ENV:QUARKUS_OIDC_AUTH_SERVER_URL -and $ENV:QUARKUS_OIDC_CREDENTIALS_SECRET) {

    $access_token=(http $KEYCLOAK_VERIFY -a backend-service:"$ENV:QUARKUS_OIDC_CREDENTIALS_SECRET" --form POST "$ENV:QUARKUS_OIDC_AUTH_SERVER_URL/protocol/openid-connect/token" username="$ENV:ADMIN_USER_NAME" password="$ENV:ADMIN_USER_PASSWORD" grant_type=password | jq --raw-output '.access_token')

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

    Write-Host "Keycloak docker port is $port"

    # Get the access token using the default credentials.

    $access_token=(http $KEYCLOAK_VERIFY -a backend-service:secret --form POST :$port/auth/realms/quarkus/protocol/openid-connect/token username="$ENV:ADMIN_USER_NAME" password="$ENV:ADMIN_USER_PASSWORD" grant_type=password | jq --raw-output '.access_token')

}

http $API_VERIFY DELETE $ENV:API_SERVER/api/filter/$url_suffix "Authorization:Bearer $access_token"

Write-Host "Listing capture filters again"

http $API_VERIFY -b GET $ENV:API_SERVER/api/filter "Authorization:Bearer $access_token"
