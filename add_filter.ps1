
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

    $access_token=(http -a backend-service:"$ENV:QUARKUS_OIDC_CREDENTIALS_SECRET" --form POST "$ENV:QUARKUS_OIDC_AUTH_SERVER_URL/protocol/openid-connect/token" username="$ENV:TEST_USER_NAME" password="$ENV:TEST_USER_PASSWORD" grant_type=password | jq --raw-output '.access_token')

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

    $access_token=(http -a backend-service:secret --form POST :$port/auth/realms/quarkus/protocol/openid-connect/token username='alice' password='alice' grant_type=password | jq --raw-output '.access_token')

}

# Write-Host "Listing capture filters"

# http -b GET :8080/api/filter "Authorization:Bearer $access_token"

Write-Host "Adding capture filters for Generic, Goose, GSE, and SV"

# Notice how the captureFilter:=null uses a colon in the assignment. This means it's raw JSON, it's used for non-text JSON values like numbers, true/false, and null.
# This *MUST* be run as a user with the admin role assigned in Keycloak. Check the user above that's being used to obtain the access token. The user and their role
# mapping in Keycloak will determine the access they have. The roles themselves are created in the Roles section under the Keycloak realm. So if you wanted to, you
# could create a role called 'filter_maintainer', and change the @RolesAllowed annotations in the PacketFilterResource to "filter_maintainer". That way all users
# with the "filter_maintainer role would be able to add/remove and list capture filters.
# 
# One other thing to keep in mind: All of these scripts use what's called a "Direct Access Grant". If you turn that off in Keycloak for the backend-service client,
# it will block these scripts from running. But the users will still be able to use the frontend-client web page, because that's considered the "Standard Flow".

http POST :8080/api/filter "Authorization:Bearer $access_token" label="Start Generic Capture" urlSuffix=all captureFilter:=null
http POST :8080/api/filter "Authorization:Bearer $access_token" label="Start Goose Capture" urlSuffix=goose captureFilter="ether proto 0x88B8"
http POST :8080/api/filter "Authorization:Bearer $access_token" label="Start GSE Capture" urlSuffix=gse captureFilter="ether proto 0x88B9"
http POST :8080/api/filter "Authorization:Bearer $access_token" label="Start SV Capture" urlSuffix=sv captureFilter="ether proto 0x88BA"

# Write-Host "Listing capture filters"

# http -b GET :8080/api/filter "Authorization:Bearer $access_token"

# Write-Host "Deleting capture filters"

# http DELETE :8080/api/filter/all "Authorization:Bearer $access_token"
# http DELETE :8080/api/filter/goose "Authorization:Bearer $access_token"
# http DELETE :8080/api/filter/gse "Authorization:Bearer $access_token"
# http DELETE :8080/api/filter/sv "Authorization:Bearer $access_token"

Write-Host "Listing capture filters again"

http -b GET :8080/api/filter "Authorization:Bearer $access_token"
