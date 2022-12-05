# PCapture Project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

- The idea for the project is to use the Wireshark `dumpcap.exe` (or `tcpdump`) program to capture packets into a temporary .pcapng file,
  then retrieve the capture at an arbitrary later time.
- The project is secured using an `OpenID-connect` compatible server like `Keycloak`.
- There are 5 primary API commands: `start`, `stop`, `read`, `delete` and `list`.
- The start command also has a capture type parameter that allows you to select different capture filters.
- The capture filters are registered in the database, and can be added or removed using the filter API by a users with the `filter_admin` role.
- Users with the `admin` role can stop, download and delete captures made by other users.
- The start command returns a unique UUID (Universally Unique Identifier), which is then used as a path parameter for the other primary API commands (except list).

## Developer Quckstart - Build Environment Preparation

### Install Docker for Windows

- Download and install the Linux Kernel Update package for WSL2 (Windows Subsystem For Linux): https://wslstorestorage.blob.core.windows.net/wslblob/wsl_update_x64.msi
- Download and install Docker Desktop for Windows: https://www.docker.com/products/docker-desktop

### Install and Configure OpenJDK 11

- Download and run the OpenJDK 11 installer from: https://adoptopenjdk.net/

### Install and Configure Apache Maven

- Make sure that your JAVA_HOME is set to the Java installation folder, and your PATH contains %JAVA_HOME%\bin. If it isn't, you may need to reboot. If it still isn't set the environment variables manually.
- Download Apache Maven from https://maven.apache.org/download.cgi
- Unzip maven into a directory and set the M3_HOME environment variable to point to the install directory. Then add %M3_HOME%\bin to the PATH environment variable.
- Open a new command shell. If running mvn or java doesn't work, you may need to reboot.

### Install & Run Keycloak (Requires Docker)

- run `keycloakFirstRunOnly.ps1`script. It will lauch a docker container running keycloak at this URL: `http://localhost:8180`
- A realm called `pcapture` will be setup, the roles and clients will be configured. There are no users configured, because Keycloak doen't support exporting or importing users.
- Wait for the container to come up. You can monitor this from the Docker Desktop application. It should take about 2 minutes.
- Create a test user called `alice`. Go to the credential tab, and set the password to `alice` and turn temporary off. Go to the `Role Mappings` tab and add the `user` role. You may need to scroll down to see the `user` role.
- Create a test user called `admin`. Go to the credential tab, and set the password to `admin` and turn temporary off. Go to the `Role Mappings` tab and add the `user`, `admin` and `filter_admin` roles.
- If the container is restarted, it *WILL* retain the added users.
- Restart the container with `keycloakRestart.ps1` or simply use `docker restart keycloak` or the Docker Desktop application to start/stop/restart it.

### settings.xml

- Create a new file called `settings.xml` in your `%USERPROFILE%\.m2` folder, and copy the contents of the example `settings.xml` file below into it.
- Adjust the name of the `ethernet-network-interface` value in your new `settings.xml` file to suit your network configuration. Rename your network interface in Windows if there are any spaces in the name.
- The network interfaces can be found on Windows 10 computers by clicking on the start menu and typing `network` in the search menu, then clicking on the `View Network Connections` item.
- The name of the network connection must exactly match the value you enter into your `settings.xml` file. I recommend renaming the network connection in Windows if it has spaces in it.

Note: the `secret` value below under the `auth.backend.secret`, it must match the `backend-service` client credentials in the Keycloak realm. If you didn't regenerate the `backend-service` secret, this will match what is in the file below.

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
                <auth.server-url>http://localhost:8180</auth.server-url>
                <auth.realm>pcapture</auth.realm>
                <auth.backend.secret>secret</auth.backend.secret>
            </properties>
        </profile>

        <profile>
            <id>network</id>
            <properties>
                <ethernet-interface-name>Ethernet</ethernet-interface-name>
            </properties>
        </profile>
        
    </profiles>

    <activeProfiles>
        <activeProfile>keycloak</activeProfile>
        <activeProfile>network</activeProfile>
    </activeProfiles>

</settings>
```

### The .env File

- Checkout the project with git, if you haven't already. Or if you want, you can download one of the release files and unzip it.
- Copy the contents of the file below into a file called `.env` in your checked-out project folder.
- Edit the ETHERNET_INTERFACE_NAME to match the value of the `ethernet-interface-name` value you used in your `settings.xml`.

```shell
AUTH_SERVER_URL=http://localhost:8180
AUTH_REALM=pcapture
QUARKUS_OIDC_AUTH_SERVER_URL=http://localhost:8180/auth/realms/pcapture
QUARKUS_OIDC_CREDENTIALS_SECRET=secret
QUARKUS_KEYCLOAK_DEVSERVICES_REALM_NAME=pcapture
ETHERNET_INTERFACE_NAME=Ethernet

# Used for scripts

ADMIN_USER_NAME=admin
ADMIN_USER_PASSWORD=admin
API_SERVER=http://localhost:8080

KEYCLOAK_CA_CERT=
API_CA_CERT=
```

### Now You're ready to start building

```shell
./mvnw clean compile quarkus:dev
```

- This may take a while the first time you build. It needs to download lots of dependencies, not just for my project, but also for the Quarkus application server.
- Once it's done, you should have a working version of the app running on `http://localhost:8080`, and Keycloak will be running on `http://localhost:8180`.
- If you go to `http://localhost:8080`, it should redirect you to the Keycloak server's `pcapture` realm page, if you've done everything correctly, and I haven't missed anything ;)
- Login using the username `alice` and the password `alice`. Start a capture, but don't stop it, and log out.
- Login using the username `admin` and the password `admin`. Click on the `Toggle All Users` button. You should see that the `admin` user can see the captures the `alice` user captured.

### Cosmetic Settings

- You can change the `Display Name` under `Realm Settings` to change the login banner. You can also change the title and heading on the web page by adding these settings to your `settings.xml` under the keycloak properties.

```xml
<branding.title>PCapture</branding.title>
<branding.description>A web frontend for command-line based packet capture tools.</branding.description>
```

### Remember-me

- Remember-me can be turned on or off in the `Realm Settings` section.

## Status and Future Directions

- At this point the web service must tie in with a an `OpenID-connect` authentication server like `Keycloak`.
- There is now a rudimentary web-based user interface that allows you to start/stop/download/delete captures.
- There is also now an `admin` mode for people that have been assigned an `admin` role in Keycloak. Admin users can see the captures of other people, stop them, download them and delete them.
- Non-admin users can only start/stop/delete/download their own captures.
- I suggest that you create a settings.xml file in your maven `$HOME/.m2` directory to store the `${auth.server-url}`, `${auth.realm}` and `${auth.backend.secret}` properties that point to your local Keycloak server and allow the backend-service (API) to authenticate to it.
- I also suggest you create a `.env` file in the top level project directory. This holds environment settings for Quarkus when running from the local folder (does *NOT* affect production jar).
- The program now works on both Windows and Linux, and also from a Docker container hosted on Linux (with some caveats: See --net=host and --privilged)
- The docker `--net=host` flag is a problem on Windows. At this point in time, it doesn't work at all because there's no bridging between the Docker Linux VM and the Windows physical interface.
  As a workaround, I've removed the `--net=host` and added `-p 8080:8080` flags. However, while you can connect to the interface and capture packets, you're only able to do it on the Docker private
  network - which isn't really useful.
- I'm now using an `Alpine Linux` based `Docker` image, and use `tcpdump` rather than Wireshark's `dumpcap` program. This takes *MUCH* less time to build, and produces a much smaller Docker image than the Ubuntu based image I was using before.
- I can always switch back to Wireshark's `dumpcap` if we need it, but that would greatly increase the Docker image size. This is mostly due to the fact that dumpcap isn't packaged separately from Wireshark, and Wireshark has a lot of graphical user interface dependencies.
- It's unlikely that you'll be able to run the Docker image inside a `Kubernetes` cluster, because `Kubernetes` uses separate internal Pod networks. Most traffic is redirected into Kubernetes clusters via LoadBalancer and Ingress resources.
- You may want to write a separate standalone client web application to furthur separate the `frontend-client` from the `backend-service` in terms of security.
- There's now an `add_filter.ps1` script that *MUST* be used after installation to add/remove capture filters. The add_filters.ps1 script, like all the other PowerShell scripts in the directory reads the `.env` file for environment
  settings, so you may need to adjust some values there for them to work (in particular the ADMIN_USER_* variables and possibly the CA_CERT variable.)

## Understanding Project Settings

In this Quarkus project I'm using two kinds of configuration variable overrides. First, there's the Apache Maven `pom.xml` file. It contains properties in two different places; up near the top in the properties section, and down near the bottom in the `<profiles>` section. The properties up top are used all the time, unless overridden by something in the profiles section, or by another Maven file called `settings.xml` that's stored in `$HOME/.m2/`. There's also a `<resources>` section in the `pom.xml` file which controls how files in the `src/main/resources` folder are filtered. The filtering is what is used to substitute values in files such as `application.properties`. The `<profiles>` section properties can be used to override the main section properties by building maven with an extra `-P [profile_name]` flag. Profiles can also be activated automatically using an `<activations>` section. In the case of building on Linux, the `linux` profile is selected automatically because it's `<activation>` section specifies to activate the profile based on it being a `Linux` operating system. The effect of this profile is to change the startCaptureScript property to a Linux specific startCaptureScript, rather than the Windows one that's set in the main properties section.

Now, asside from Maven filtering, there's also Quarkus/Eclipse Microprofile settings. Project properties for Quarkus can be set in it's `application.properties` file. Properties in this file are used by the various Quarkus subsystems such as database persistence, or Open-ID connect (OIDC) integration. The can also be used to directly inject values into Java Bean fields using a `@ConfigProperty` annotation. Quarkus properties also have their own override mechanisms. For one, if a property starts with `%dev.`, `%test.`, or `%prod.`, these properties are only active when Quarkus is running in dev, test or prod mode. This allows you to set a property differently for testing or development, than you would for production. This is important for databases, because typically you don't want testing to affect either your development or production databases. In fact, it's quite common to use an in-memory database that simply gets thrown away after the test.

But Quarkus also has an environment variable override mechanism. If you take a property name, and convert it all to upper case, and change periods and dashes to underscore (`_`) characters, and you set this in the environment, it will override the property in the `application.properties` file. This is useful for deployment to Docker containers, Kubernetes, etc. There's also a place to put environment variable overrides in your project folder called the `.env` file.

So to fully understand how properties overrides effect the project, first consider the maven property substitutions. That happens *BEFORE* compile time. It also affects what gets deployed into containers. Next, consider what the `%dev.`, `%test.`, and `%prod.` prefixes are going to do when the container is running. And finally consider the effect of your environment settings and the environment variables in your projects `.env` file. But also consider that the `.env` file and your `$HOME/.mvn/settings.xml` file are not checked-in to the git repository. So changes you make in these files are only for you as a developer. When the application is being setup by developers they should have a look at the `.env` and `settings.xml` file samples later in this document as guidance.

Personally, I put generic, non-site-specific values in the `pom.xml` and `application.properties` files. Then I override the generic values with things like our keycloak server, and database type, url, and credentials. The overrides can be either in `settings.xml` - for things you'd like baked-in to the `application.properites` file as a default, or I use the `.env` file for more changable things. One of the things you need to understand about Quarkus is that when files that are in the project are edited, Quarkus compiles them on-the-fly, then hot-replaces them in the running Quarkus container. For Java class files, this works great usually, but for `application.properties` and `index.html`, this doesn't work very well at all. The reason being, that it copies the file from `src/main/resources` directly into `target/classes` without performing the Maven subsitutions on it. Then quarkus reloads, and finds some `${variables}` in it that it can't resolve, because they were supposed to be replaced before the compile. Unfortunately, this is one of those situations where the inner workings of maven and other build tools falls outside the scope of the Quarkus project. Perhaps though, there's some kind of Quarkus integration trick that can be done to somehow call-out to an external script to deal with these subsitution issues - but I'm not an expert on this. Perhaps someone else can solve this problem?

## Database Configuration

The src`/main/resources/application.properties` file contains a value called `quarkus.hibernate-orm.database.generation` which is set to `drop-and-create`. This is a useful development setting that drops the database on every startup. However, you'll more than likely want to change this to `none` instead once you have your database up and going, otherwise you'll have inconsistencies between what's been captured and what's listed in the database. The way I'd recommend to do this is to set `QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION=none` for production, or you can use update for development. The tricky part about production is that you'll need to copy a fully provisioned database file into the docker image, unless you want to connect to an external production database like MySQL or Postgres (recommended).

# Database Schema Upgrades

Sometimes a database schema upgrade may be necessary, and I haven't integrated flyway or another database migration tool at this time. If you get database errors on startup, you should try using the `update` setting for `QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION`, or delete the databases, and clear up any capture files manually. If there's enough demand I'll integrate a proper schema migration tool, and upgrades will be automatic. The H2 database files end with the `.db` extension.

## The .env File (stored in your project's top level folder)

This file is read by Quarkus to configure the back-end web service, it doesn't affect the web client. It's also used by the PowerShell scripts in the user_scripts and admin_scripts directories.

```shell script
# These directly override properties in application.properties. This gets done after
# maven has performed it's subsitutions, so even though AUTH_SERVER_URL and QUARKUS_OIDC_AUTH_SERVER_URL
# may seem redundant, they are in fact, separate properties. One is used for the @Inject annotations in
# PcapApplication, and the other is used to override Quarkus' OIDC behaviour.

AUTH_SERVER_URL=https://your.keycloak.server
AUTH_REALM=your_keycloak_realm
QUARKUS_OIDC_AUTH_SERVER_URL=https://your_keycloak_server/auth/realms/your_keycloak_realm
QUARKUS_OIDC_CREDENTIALS_SECRET=your_keycloak_backend_service_credentials
QUARKUS_KEYCLOAK_DEVSERVICES_REALM_NAME=your_keycloak_realm

# Used for scripts - you'll likely want to change these.

ADMIN_USER_NAME=alice
ADMIN_USER_PASSWORD=alice
API_SERVER=http://your.api.server:port

# The CA cert is only needed if you're using an internal Certificate Authority's CA certificate that has no
# trust chain to a public CA, or is otherwise unrecognized by the HttPie tool (or probably the underlying python)
# If you're just working in development mode, and your API and Keycloak servers aren't using SSL, then this isn't
# necessary. If you're not using an internal enterprise CA, then set both of these to nothing (leave the assignment
# empty). In our case, we use a public CA for our Keycloak server, so it's left empty. And we use a enterprise
# certificate for our API server, so it's normally set. However, when I'm connecting to localhost for development,
# I also leave the API_CA_CERT as an empty value, or just comment it out.
#
# Beware, that this script has an effect on the environment after it runs. So if you comment out a variable, instead
# of setting it to an empty value, the previously set value from this file will still be in effect. So either leave
# the variables in, and set them to empty, or else start another PowerShell window.

KEYCLOAK_CA_CERT=C:/full/path/to/pem/encoded/ca_certificate_for_keycloak.crt
API_CA_CERT=C:/full/path/to/pem/encoded/ca_certificate_for_api.crt
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
                <auth.realm>your_keycloak_realm</auth.realm>
                <auth.backend.secret>your_keycloak_backend-service_credentials</auth.backend.secret>
            </properties>
        </profile>
        
        <profile>
            <id>network</id>
            <properties>
                <ethernet-interface-name>Ethernet</ethernet-interface-name>
            </properties>
        </profile>
        
    </profiles>

    <activeProfiles>
        <activeProfile>keycloak</activeProfile>
        <activeProfile>network</activeProfile>
    </activeProfiles>

</settings>
```

You'll of course want to change the value of `https://your.keycloak.server`, and probably also `your-keycloak-backend-secret` to whatever your Keycloak server URL is using.

The token replacements are done to help you get started quickly, and also to remove dependencies on the project location, and site specific URLs. However, there is one side effect; the tokens don't get replaced at runtime by the quarkus hot-deploy stuff, and this often causes server crashes. A workaround is to put variables that are developer specific in your`$HOME/.m2/settings.xml` file and `${project.basedir}/.env` files.The `settings.xml` file is used for maven variable replacements during compile time, and the `.env` file holds environment variables that Quarkus reads during startup. The environment variables can be used to override settings in the `src/main/resources/application.properties` file. You can also override these properties with `-Dpropname=propvalue` on the command line.

# Configuring the Web client in Keycloak

You'll need to login to your Keycloak server as a user with the `admin` role and switch to the Quarkus realm. The Keycloak server that's run by `quarkus:dev` uses `admin` as a username, and `admin` as the password. Once you're logged in, create a new client called `frontend-client` using `openid-connect` as the client protocol. In the `Valid Redirect URIs` field, type `http://localhost:8080/*`, or whatever your web service URL is running under. This tells the Keycloak server that it's okay to redirect the user back to `http://localhost:8080/*` after successful authentication. This is very important for security that it match the web client URL. Also, for the `Web Origins` section, add a single line containing only the `+` sign. Of course later when you deploy this to production, you'll need to change these URL's to a public, or at least corporate facing URL.

There used to be a file called keycloak.json that had to be configured for the client. Now that file is generated automatically and is available at /api/res/configjson. It's a public URL, and is not protected, and it doesn't need to be.

## Authentication and Authorization

The application is now fully integrated with authorization servers supporting the `Open-ID Connect` standard (`OIDC` for short). If you're running docker on your development workstation, all you need to do is run `quarkus:dev`. It will run a keycloak server in the background, that's fully provisioned and setup for testing. Be warned, the web client doesn't work with this unless you manually edit the web client configuration file (`keycloak.json`), and the `index.html` file to point to the keycloak server. It may be easier to run keycloak from docker manually, and import the realm file below if you want to use the web client.

If you wish to setup a standalone keycloak server, you can import this realm as a quick-start:

`https://github.com/quarkusio/quarkus-quickstarts/blob/main/security-openid-connect-quickstart/config/quarkus-realm.json`

The file creates a realm with some default passwords that the application has configured in it's `application.properties` file. These passwords are very bad, so you might want to update them right away. There's also a client secret that should be regenerated.

In a production deployment, you'll probably use the User Federation section to synchronize users from Active Directory, and also add a `group-ldap-mapper` to map AD groups to Keycloak groups. Then you'll want to assign roles to the newly imported Keycloak groups in the group's `Role Mappings` tab.

Update: You'll now need to create a new role called `filter_admin` under the `pcapture` realm provided in the above realm import. That's in addition to the `admin` and `user` roles that should already be there if you used the quickstart link above to create the realm. I also suggest you rename your realm to something more appropriate like `pcapture`. If you do rename your realm though, make sure that you change it in the `settings.xml` and `.env` files above. After you've created the role, you should assign it to one of the test users under the users section. This is done under the `Role Mappings` tab on the user's page.

# Windows Service Deployment

- Run `.\mvnw clean compile package`

In a PowerShell window:

```shell
cd target\win64svc
.\pre-install.ps1
.\install-and-run.ps1
```

After the pre-install, the installation folder is essentially complete. You can then copy the win64svc folder to another PC before you install and run the service with the `install-and-run.ps1` script.

This will get a service running on your local PC, running on `http://localhost:8080`. For production deployment, you'll need to uncomment the lines at the bottom of the `pcapture.xml` file, and provide an SSL certificate and key saved in PKCS12 format (`.pfx` or `.p12`) called `server-cert-and-key.pfx`.

There's also a file called `stop-and-uninstall.ps1` which can be used to stop uninstall the service. You'll need to at least stop the service if you want to update the jar file used by the running service.

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

You can then execute your native executable with: `./target/pcapture-[version]-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.

## Related Guides

- RESTEasy JAX-RS ([guide](https://quarkus.io/guides/rest-json)): REST endpoint framework implementing JAX-RS and more

## Provided Code

### RESTEasy JAX-RS

Easily start your RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started#the-jax-rs-resources)
