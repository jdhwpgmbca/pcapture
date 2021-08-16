# dumpcap-ws Project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

- The idea for the project is to use the Wireshark dumpcap.exe program to capture packets into a temporary .pcapng file,
  then retrieve the capture at an arbitrary later time.
- There are five commands: start, stop, read and delete and list.
- The project can be secured using an OpenID-connect compatible server like Keycloak.
- The start command returns a unique UUID, which is then used as a path parameter for the other commands (except list).

## Status and Future Directions

- At this point the web service must tie in with a an OpenID-connect authentication server like Keycloak.
- There is now a rudimentary web-based user interface that allows you to start/stop/download/delete captures.
- I suggest that you create a settings.xml file in your maven $HOME/.m2 directory to store the ${auth.server.url} property which should point to your keycloak server.
- I also suggest you create a .env file in the top level project directory.
- I suggest cloning the PacketCaptureResource class to something like SvPacketCaptureResource, GoosePacketCaptureResource, GsePacketCaptureResource, and changing the @Path annotations on them to keep them all unique. You could also customize the startCaptureScript.ps1 to also pass in the capture filter strings as arguments, and have each class use a different capture filter.

## The .env File (stored in your project's top level folder)

(This file configures the back-end web service, it doesn't affect the web client.)

QUARKUS_OIDC_AUTH_SERVER_URL=https://your.keycloak.server/auth/realms/quarkus
QUARKUS_OIDC_CLIENT_ID=backend-service
QUARKUS_OIDC_CREDENTIALS_SECRET=your-keycloak-client-credentials
QUARKUS_OIDC_TLS_VERIFICATION=required

# The web client front-end configuration (src/main/resources/META-INF/resources)

The ${auth.server.url} property gets replaced in this file, and index.html by maven at build time if you have the property configured in $HOME/.m2/settings.xml.

```json
{
  "realm": "quarkus",
  "auth-server-url": "${auth.server.url}/auth/",
  "ssl-required": "external",
  "resource": "frontend-client",
  "public-client": true,
  "confidential-port": 0
}
```

```xml
<settings 
    xmlns="http://maven.apache.org/SETTINGS/1.2.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 http://maven.apache.org/xsd/settings-1.2.0.xsd"
>

<profiles>
    <profile>
        <id>development</id>
        <properties>
            <auth.server.url>https://your.development.keycloak.server</auth.server.url>
        </properties>
    </profile>
    <profile>
        <id>production</id>
        <properties>
            <auth.server.url>https://your.production.keycloak.server</auth.server.url>
        </properties>
    </profile>
</profiles>

</settings>
```

If you have a $HOME/.m2/settings.xml file like the one above, using .\mvnw clean compile quarkus:dev -Pdevelop (or -Pproduction) will perform the proper ${auth.server.url} substitutions so the web client will work.

## Authentication and Authorization

The application is now fully integrated with authorization servers supporting the Open-ID Connect standard (OIDC for short).
If you're running docker on your development workstation, all you need to do is run quarkus:dev. It will run a keycloak
server in the background, that's fully provisioned and setup for testing. Be warned, the web client doesn't work with this
unless you manually edit the web client configuration file (keycloak.json), and the index.html file to point to the keycloak
server. It may be easier to run keycloak from docker manually, and import the realm file below if you want to use the web client.

If you wish to setup a standalone keycloak server, you can import this realm as a quick-start:

https://github.com/quarkusio/quarkus-quickstarts/blob/main/security-openid-connect-quickstart/config/quarkus-realm.json

The file creates a realm with some default passwords that the application has configured in it's application.properties file.
These passwords are very bad, so you might want to update them right away. There's also a client secret that should be
regenerated.

### Testing

- The program can be tested with .\mvnw clean compile test, or you can run .\mvnw clean compile quarkus:dev and press <o> to show the test output, followed by <r> to run the tests.
- An alternative way of testing that involves the Keycloak (OIDC) server is to run the test_access.ps1 script. There are now also separate scripts to start/stop/download and delete.

(Install the jq JSON query client using one of the preferred methods. I used the chocolatey package manager to install mine.)

Using the HttPie client to get an access token from the test keycloak server:

- Getting the access token from the Keycloak server returns it's response in JSON format, so you need to use jq to extract the access_token, or you need to cut and paste yourself. However, the access tokens only last 5 minutes, so be quick!
- There's a test_access.ps1 PowerShell script in the top level directory which can be used for testing, or as an example for developing your own scripts.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw clean compile quarkus:dev
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

You may also want to add additional -D parameters to set properties like the auth server url, secret, etc.

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
