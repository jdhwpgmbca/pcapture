# Get the access token from the keycloak server

$access_token=(http -a backend-service:cb98dbc5-7de7-42e8-885a-533cef709f69 --form POST https://keycloak.rtds.com/auth/realms/quarkus/protocol/openid-connect/token username=alice password='alice' grant_type=password | jq --raw-output '.access_token')

$access_token

# Start a capture, returning the capture ID.

$dbid=(http POST :8080/api/v2/capture "Authorization:Bearer $access_token")

# Print the capture ID. The capture ID is user specific.

$dbid

# Stop the capture using the capture ID parameter.

http GET :8080/api/v2/capture "Authorization:Bearer $access_token"

http PUT :8080/api/v2/capture/$dbid "Authorization:Bearer $access_token"

Start-Sleep -s 1.5

http GET :8080/api/v2/capture/$dbid "Authorization:Bearer $access_token" -o capture.pcapng

Start-Sleep -s 1.5

http DELETE :8080/api/v2/capture/$dbid "Authorization:Bearer $access_token"

http GET :8080/api/v2/capture "Authorization:Bearer $access_token"
