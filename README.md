# dumpcap-ws Project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

- The idea for the project is to use the Wireshark dumpcap.exe program to capture packets into a temporary .pcapng file,
  then retrieve the capture at an arbitrary later time.
- The project can be secured using an OpenID-connect compatible server like Keycloak. Some instructions are given
on how to enable authorization in a test environment.
- There are four commands: start, stop, read and delete.
- The start command returns a token, which is then used as a header parameter for the other commands.

## Status and Future Directions

- At this point the web service can tie in with a an OpenID-connect authentication server like Keycloak. There is no client front-end at this point. So the next steps would probably be to develop a single page application that uses JavaScript to fetch the authorization token and use the various REST functions to start/stop/download/delete captures. 
- I suggest cloning the PacketCaptureResource class to something like SvPacketCaptureResource, GoosePacketCaptureResource, GsePacketCaptureResource, and changing the @Path annotations on them to keep them all unique. You could also customize the startCaptureScript.ps1 to also pass in the capture filter strings as arguments, and have each class use a different capture filter.

## Authentication and Authorization

To enable OpenID-connect authorization, you'll need to uncomment the @Authenticated annotation in the PacketCaptureResource
class. And you'll need to update some of the settings in the application.properties file to point to your OpenID-connect
authorization server. These settings my be commented, or uncommented. It won't make any difference unless the @Authenticated
annotation is enabled.

When Quarkus is running in dev mode, it automatically runs a Keycloak server - if you have a system with Docker running
on it. The TCP port number the Keycloak server runs on is random, so you'll need to look at the server output to find
the correct port to connect to. Once you connect to the Keycloak server, you can login with admin/admin. Then you
will need to delete the Quarkus realm, and re-create it. During the re-creation, it will ask you if you want to import
a file. Click on the button to do an import, and import the file from this GitHub site:

https://github.com/quarkusio/quarkus-quickstarts/blob/main/security-openid-connect-quickstart/config/quarkus-realm.json

The file creates a realm with some default passwords that the application has configured in it's application.properties file.
These passwords are very bad, so you might want to update them right away. There's also a client secret that should be
regenerated. Quarkus will use a different TCP port for Keycloak every time it starts, or restarts. So you might want
to use docker to create a slightly more permanenent version of Keycloak for testing.

One more point: Once the authorization is enabled, the tests will break. They haven't been designed to handle authorization yet.

### Testing with Authorization Enabled

(Install the jq JSON query client using one of the preferred methods. I used the chocolatey package manager to install mine.)

Using the HttPie client to get an access token from the test keycloak server:

- Getting the access token from the Keycloak server returns it's response in JSON format, so you need to use jq to extract the access_token, or you need to cut and paste yourself. However, the access tokens only last 5 minutes, so be quick!

```shell script
# Fetch the access token from the Keycloak server
$access_token=(http -v -a backend-service:secret --form POST :49254/auth/realms/quarkus/protocol/openid-connect/token username=alice password=alice grant_type=password | jq --raw-output '.access_token')

# Start a packet capture, storing the token response. It's needed for the other operations.

$token=(http POST :8080/capture "Authorization:Bearer $access_token")

# The capture is currently running. Do the PUT method to stop it.

http PUT :8080/capture "Authorization:Bearer $access_token" token:$token

# And do the GET method to download the capture data.

http GET :8080/capture "Authorization:Bearer $access_token" token:$token

# And finally use DELETE to delete the capture data

http DELETE :8080/capture "Authorization:Bearer $access_token" token:$token
```

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/dumpcap-ws-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.

## Related Guides

- RESTEasy JAX-RS ([guide](https://quarkus.io/guides/rest-json)): REST endpoint framework implementing JAX-RS and more

## Provided Code

### RESTEasy JAX-RS

Easily start your RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started#the-jax-rs-resources)
