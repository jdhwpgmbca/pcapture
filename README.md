# dumpcap-ws Project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

- The idea for the project is to use the Wireshark `dumpcap.exe` (or `tcpdump`) program to capture packets into a temporary .pcapng file,
  then retrieve the capture at an arbitrary later time.
- There are eight commands: `startAll`, `startGoose`, `startGSE`, `startSV`, `stop`, `read`, `delete` and `list`.
- The project can be secured using an `OpenID-connect` compatible server like `Keycloak`.
- The start commands return a unique UUID (Universally Unique Identifier), which is then used as a path parameter for the other commands (except list).

## Status and Future Directions

- At this point the web service must tie in with a an `OpenID-connect` authentication server like `Keycloak`.
- There is now a rudimentary web-based user interface that allows you to start/stop/download/delete captures.
- There is also now an `admin` mode for people that have been assigned an `admin` role in Keycloak. Admin users can see the captures of other people, stop them, download them and delete them.
- Non-admin users can only start/stop/delete/download their own captures.
- I suggest that you create a settings.xml file in your maven `$HOME/.m2` directory to store the `${auth.server.url}` property which should point to your keycloak server. See below for an example.
- I also suggest you create a `.env` file in the top level project directory. This holds environment settings for Quarkus when running from the local folder (does *NOT* affect production jar).
- The program now works on both Windows and Linux.
- The program also works inside a Docker container with some caveats. It must be run with the `--privileged` and `--net=host` flags.
- The docker `--net=host` flag is a problem on Windows. At this point in time, it doesn't work at all because there's no bridging between the Docker Linux VM and the Windows physical interface.
  As a workaround, I've removed the `--net=host` and added `-p 8080:8080` flags. However, while you can connect to the interface and capture packets, you're only able to do it on the Docker private
  network - which isn't really useful.
- I'm now using an `Alpine Linux` based `Docker` image, and use `tcpdump` rather than Wireshark's `dumpcap` program.
- I can always switch back to Wireshark's `dumpcap` if we need it, but that would greatly increase the Docker image size.
- It's unlikely that you'll be able to run it inside a `Kubernetes` cluster, because `Kubernetes` uses separate internal Pod networks. Most traffic is redirected into Kubernetes clusters via LoadBalancer and Ingress resources.
- You may want to write a separate standalone client web application to furthur separate the client from the back-end service in terms of security.
- There may also be other reasons why you'd want a different client. I know that there are quite a few `Node.JS` based frameworks out there that are very popular.
- There's now an `add_filter.ps1` script that can be used after installation to add/remove capture filters. The add_filters.ps1 script, like all the other PowerShell scripts in the directory reads the `.env` file for environment
  settings.

## Understanding Project Settings

In this Quarkus project I'm using two kinds of configuration variable overrides. First, there's the Apache Maven `pom.xml` file. It contains properties in two different places; up near the top in the properties section, and down near the bottom in the `<profiles>` section. The properties up top are used all the time, unless overridden by something in the `<properties>` section, or by another Maven file called `settings.xml` that's stored in `$HOME/.m2/`. There's also a `<resources>` section in the `pom.xml` file which controls how files in the `src/main/resources` folder are filtered. The filtering is what is used to substitute values in files such as `application.properties`, `index.html`, and `keycloak.json`. The `<profiles>` section properties can be used to override the main section properties by building maven with an extra `-P [profile_name]` flag. Profiles can also be selected automatically using an `<activations>` section. In the case of building on Linux, the `linux` profile is selected automatically because it's `<activation>` section specifies to activate the profile based on it being a `Linux` operating system. The effect of this profile is to change the startCaptureScript property to a Linux specific startCaptureScript, rather than the Windows one that's set in the main properties section.

Now, asside from Maven filtering, there's also Quarkus/Eclipse Microprofile settings. Project properties for Quarkus can be set in it's `application.properties` file. Properties in this file are used by the various Quarkus subsystems such as database persistence, or Open-ID connect (OIDC) integration. The can also be used to directly inject values into Java Bean fields using the `@ConfigProperty` annotation. Quarkus properties also have their own override mechanisms. For one, if a property starts with `%dev.`, `%test.`, or `%prod.`, these properties are only active when Quarkus is running in dev, test or prod mode. This allows you to set a property differently for testing or development, than you would for production. This is important for databases, because typically you don't want testing to affect either your development or production databases. In fact, it's quite common to use an in-memory database that simply gets thrown away after the test.

But Quarkus also has an environment variable override mechanism. If you take a property name, and convert it all to upper case, and change periods and dashes to underscore (`_`) characters, and you set this in the environment, it will override the property in the `application.properties` file. This is useful for deployment to Docker containers, Kubernetes, etc. There's also a place to put environment variable overrides in your project folder called the `.env` file.

So to fully understand how properties overrides effect the project, first consider the maven property substitutions. That happens *BEFORE* compile time. It also affects what gets deployed into containers. Next, consider what the `%dev.`, `%test.`, and `%prod.` prefixes are going to do when the container is running. And finally consider the effect of your environment settings and the environment variables in your projects `.env` file. But also consider that the `.env` file and your `$HOME/.mvn/settings.xml` file are not checked-in to the git repository. So changes you make in these files are only for you as a developer. When the application is being setup by developers they should have a look at the `.env` and `settings.xml` file samples later in this document as guidance.

Personally, I put generic, non-site-specific values in the `pom.xml` and `application.properties` files. Then I override the generic values with things like our keycloak server, and database type, url, and credentials. The overrides can be either in `settings.xml` - for things you'd like baked-in to the `application.properites` file as a default, or I use the `.env` file for more changable things. One of the things you need to understand about Quarkus is that when files that are in the project are edited, Quarkus compiles them on-the-fly, then hot-replaces them in the running Quarkus container. For Java class files, this works great usually, but for `application.properties` and `index.html`, this doesn't work very well at all. The reason being, that it copies the file from `src/main/resources` directly into `target/classes` without performing the Maven subsitutions on it. Then quarkus reloads, and finds some `${variables}` in it that it can't resolve, because they were supposed to be replaced before the compile. Unfortunately, this is one of those situations where the inner workings of maven and other build tools falls outside the scope of the Quarkus project. Perhaps though, there's some kind of Quarkus integration trick that can be done to somehow call-out to an external script to deal with these subsitution issues - but I'm not an expert on this. Perhaps someone else can solve this problem?

## Database Configuration

The src`/main/resources/application.properties` file contains a value called `quarkus.hibernate-orm.database.generation` which is
set to `drop-and-create`. This is a useful development setting that drops the database on every startup. However, you'll more than
likely want to change this to `none` instead once you have your database up and going, otherwise you'll have inconsistencies
between what's been captured and what's listed in the database. The way I'd recommend to do this is to set `QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION=none` for production, or you can use update for development. The tricky part about production is that you'll need to copy a fully provisioned database file into the docker image, unless you want to connect to an external production database like MySQL or Postgres (recommended).

# Database Schema Upgrades

Sometimes a database schema upgrade may be necessary, and I haven't integrated flyway or another database migration tool at this time. If you get database errors on startup, you should try using the `update` setting for `QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION`, or delete the databases, and clear up any capture files manually. If there's enough demand I'll integrate a proper schema migration tool, and upgrades will be automatic. The H2 database files end with the `.db` extension.

## The .env File (stored in your project's top level folder)

(This file configures the back-end web service, it doesn't affect the web client.)

```shell script
# These are Quarkus environment overrides, they have nothing to do with Apache Maven.
# They are used at runtime to override properties in application.properties.
QUARKUS_OIDC_AUTH_SERVER_URL=https://your.keycloak.server/auth/realms/quarkus
QUARKUS_OIDC_CLIENT_ID=backend-service
QUARKUS_OIDC_CREDENTIALS_SECRET=your-oidc-credentials-secret
QUARKUS_OIDC_TLS_VERIFICATION=required
TEST_USER_NAME=alice
TEST_USER_PASSWORD=alice
# This is the root CA certificate of your enterprise CA. If you're using a certificate
# signed by a public CA, you can ignore this. Alternatively, you can run the application
# on a non-SSL port to get the filters added. The CA cert is only needed here for the
# HttPie (http) program, which is a Python program, and doesn't use the Windows certificate
# store.
CA_CERT=root.crt
API_SERVER=https://your.installed.application
```

The `QUARKUS_OIDC_CREDENTIALS_SECRET` must match the `Keycloak -> Quarkus Realm -> Clients -> Backend-service -> Credentials -> Secret`. For security you should regenerate the secret. The frontend-client does not have a credentials secret because it's configured with "Access Type" set to "public". This is necessary because JavaScript based clients have no secure way to store the credentials. It's necessary to take additional security precautions for this reason. In particular, you should make sure the `Valid Redirect URIs` field is as specific as possible (so don't use `*` by itself for instance).

## Maven settings.xml example:

```xml
<settings 
    xmlns="http://maven.apache.org/SETTINGS/1.2.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 http://maven.apache.org/xsd/settings-1.2.0.xsd"
>

    <profiles>
        <profile>
            <id>keycloak</id>
            <properties>
                <auth.server-url>https://your.keycloak.server</auth.server-url>
                <auth.realm>your-keycloak-realm</auth.realm>
                
                <!--<auth.frontend.ssl-required>external</auth.frontend.ssl-required>-->
                <!--<auth.frontend.client-id>frontend-client</auth.frontend.client-id>-->
                <auth.frontend.client-confidential-port>443</auth.frontend.client-confidential-port>
                
                <!--<auth.backend.client-id>backend-service</auth.backend.client-id>-->
                <auth.backend.secret>your-keycloak-backend-secret</auth.backend.secret>
                <!--<auth.backend.tls-verification>required</auth.backend.tls-verification>-->
            </properties>
        </profile>
    </profiles>

    <activeProfiles>
        <activeProfile>keycloak</activeProfile>
    </activeProfiles>

</settings>
```

You'll of course want to change the value of `https://your.keycloak.server`, and probably also `your-keycloak-backend-secret` to whatever your Keycloak server URL is using.

The token replacements are done to help you get started quickly, and also to remove dependencies on the project location, and site specific URLs. However, there is one side effect; the tokens don't get replaced at runtime by the quarkus hot-deploy stuff, and this often causes server crashes. A workaround is to put variables that are developer specific in your`$HOME/.m2/settings.xml` file and `${project.basedir}/.env` files.The `settings.xml` file is used for maven variable replacements during compile time, and the `.env` file holds environment variables that Quarkus reads during startup. The environment variables can be used to override settings in the `src/main/resources/application.properties` file. You can also override these properties with `-Dpropname=propvalue` on the command line.

# Configuring the Web client in Keycloak

You'll need to login to your Keycloak server as a user with the `admin` role and switch to the Quarkus realm. The Keycloak server
that's run by `quarkus:dev` uses `admin` as a username, and `admin` as the password. Once you're logged in, create a new client
called `frontend-client` using `openid-connect` as the client protocol. In the `Valid Redirect URIs` field, type `http://localhost:8080/*`,
or whatever your web service URL is running under. This tells the Keycloak server that it's okay to redirect the user back to
`http://localhost:8080/*` after successful authentication. This is very important for security that it match the web client URL. Also, for
the `Web Origins` section, add a single line containing only the `+` sign. Of course later when you deploy this to production, you'll need
to change these URL's to a public, or at least corporate facing URL.

There used to be a file called keycloak.json that had to be configured for the client. Now that file is generated automatically and is available
at /api/res/configjson. It's a public URL, and is not protected, and it doesn't need to be.

## Authentication and Authorization

The application is now fully integrated with authorization servers supporting the `Open-ID Connect` standard (`OIDC` for short).
If you're running docker on your development workstation, all you need to do is run `quarkus:dev`. It will run a keycloak
server in the background, that's fully provisioned and setup for testing. Be warned, the web client doesn't work with this
unless you manually edit the web client configuration file (`keycloak.json`), and the `index.html` file to point to the keycloak
server. It may be easier to run keycloak from docker manually, and import the realm file below if you want to use the web client.

If you wish to setup a standalone keycloak server, you can import this realm as a quick-start:

`https://github.com/quarkusio/quarkus-quickstarts/blob/main/security-openid-connect-quickstart/config/quarkus-realm.json`

The file creates a realm with some default passwords that the application has configured in it's `application.properties` file.
These passwords are very bad, so you might want to update them right away. There's also a client secret that should be
regenerated.

# Windows Service Deployment

Create an empty folder called win64service, or something like that, and copy these files into it:

```
jre\  (A renamed OpenJDK 11 installation)
dumpcap-ws.exe (Renamed from WinSW-x64.exe)
dumpcap-ws.xml
dumpcap-ws-1.0.0-runner.jar (from target\ folder)
startCaptureScript.ps1  (from src/main/resources. You'll likely need to update the network interface name in this file)
ssl-cert-and-key.pfx (This should contain the SSL certificate and key file, stored in PKCS12 format)
```

I'm using Windows Service wrapper program called: "Windows Service Wrapper". Who would have guessed? It's available at `https://github.com/winsw/winsw`. I'm using `WinSW v2.11.0`, released on March 17, 2021. It's very simple to use. There's a config file generated under `src/target/classes/win64svc` that has most of the fields filled in based on your `settings.xml` and `pom.xml` properties. You'll need to change the `${my.cert.with.private.key}` value to a valid pfx or .p12 file containing your server key and SSL certificate. You can then download the `WinSW-x64.exe` file from `https://github.com/winsw/winsw/releases` and rename it to the same name as your configuration file. Once all the properties are filled out in your configuration file, you can just run (as in our case) `dumpcap-ws.exe install`, followed by `dumpcap.exe start` to start the service. If you want to stop and uninstall the service, just run the same `dumpcap-ws.exe stop`, and `dumpcap-ws.exe uninstall`.

Here's a copy of our configuration file (with a few things changed for security):

```
<!--
  MIT License

  Copyright (c) 2008-2020 Kohsuke Kawaguchi, Sun Microsystems, Inc., CloudBees,
  Inc., Oleg Nenashev and other contributors

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.
-->

<service>
  
  <!-- ID of the service. It should be unique across the Windows system-->
  <id>PCapSvc</id>
  <!-- Display name of the service -->
  <name>Packet Capture Service</name>
  <!-- Service description -->
  <description>Run dumpcap or tcpdump program from a Windows service.</description>
  
  <!-- This next line must match the layout of the OpenJDK folder that you've renamed to jre. -->
  <!-- Make sure the wrapper can find Java! -->
  <executable>%BASE%\jre\bin\java.exe</executable>
  <arguments>-jar dumpcap-ws-1.0.0-runner.jar</arguments>
  <env name="QUARKUS_OIDC_AUTH_SERVER_URL" value="https://your.keycloak.server/auth/realms/your_keycloak_realm" />
  <env name="QUARKUS_OIDC_CLIENT_ID" value="backend-service"/>
  <env name="QUARKUS_OIDC_CREDENTIALS_SECRET" value="your_keycloak_secret"/>
  <env name="QUARKUS_OIDC_TLS_VERIFICATION" value="required"/>
  <env name="START_CAPTURE_SCRIPT" value="./startCaptureScript.ps1"/>
  <env name="QUARKUS_DATASOURCE_DB_KIND" value="h2"/>
  <env name="QUARKUS_DATASOURCE_JDBC_URL" value="jdbc:h2:file:./h2db"/>
  <env name="QUARKUS_HTTP_PORT" value="80"/>
  <env name="QUARKUS_HTTP_TEST_PORT" value="0"/>
  <env name="QUARKUS_HTTP_SSL_PORT" value="443"/>
  <env name="QUARKUS_HTTP_INSECURE_REQUESTS" value="redirect"/>
  <env name="QUARKUS_HTTP_SSL_CERTIFICATE_KEY_STORE_FILE" value="./your.certificate.and.key.pfx"/>
  <env name="QUARKUS_HTTP_SSL_CERTIFICATE_KEY_STORE_PASSWORD" value="changeme"/>

</service>
```

### Testing

- The program can be tested with `.\mvnw clean compile test`, or you can run `.\mvnw clean compile quarkus:dev` and press `<o>` to show the test output, followed by `<r>` to run the tests.
- An alternative way of testing that involves the Keycloak (OIDC) server is to run the `test_access.ps1` script. There are now also separate scripts to start/stop/download and delete.

(Install the `jq` JSON query client using one of the preferred methods. I used the chocolatey package manager to install mine.)

Using the `HttPie` client to get an access token from the test keycloak server:

- Getting the access token from the Keycloak server returns it's response in JSON format, so you need to use jq to extract the access_token, or you need to cut and paste yourself. However, the access tokens only last 5 minutes, so be quick!
- There's a `test_access.ps1` PowerShell script in the top level directory which can be used for testing, or as an example for developing your own scripts.

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

## Creating a native executable (Requires GraalVM instead of the OpenJDK)

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/dumpcap-ws-1.0.0-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.

## Related Guides

- RESTEasy JAX-RS ([guide](https://quarkus.io/guides/rest-json)): REST endpoint framework implementing JAX-RS and more

## Provided Code

### RESTEasy JAX-RS

Easily start your RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started#the-jax-rs-resources)
