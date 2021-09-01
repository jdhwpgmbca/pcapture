# Developer Quckstart - Build Environment Preparation

### Install Docker for Windows

- Download and install the Linux Kernel Update package for WSL2 (Windows Subsystem For Linux): https://wslstorestorage.blob.core.windows.net/wslblob/wsl_update_x64.msi
- Download and install Docker Desktop for Windows: https://www.docker.com/products/docker-desktop

### Install and Configure OpenJDK 11

- Download and run the OpenJDK 11 installer from: https://adoptopenjdk.net/

### Install and Configure Apache Maven

- Make sure that your JAVA_HOME is set to the installation folder, and your PATH contains %JAVA_HOME%\bin. If it isn't, you may need to reboot. If it still isn't set the environment variables manually.
- Download Apache Maven from https://maven.apache.org/download.cgi
- Unzip maven into a directory and set the M3_HOME environment variable to point to the install directory. Then add %M3_HOME%\bin to the PATH environment variable.
- Open a new command shell. If running mvn or java doesn't work, you may need to reboot.

### Install Keycloak (Requires Docker)

- Use the docker command below to start a local Keycloak server instance.

```shell
docker run --rm --name keycloak -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin -p 8180:8080 jboss/keycloak
```

### Configuring Keycloak

- Login to the admin console at `http://localhost:8081` using username `admin` and password `admin`.
- Import the quarkus realm from the `realm-export.json` file in the top of the project folder.
- The import should default to using the `quarkus` realm. You should stick to that for now. This local keycloak container will not retain it's data once it's stopped.

### settings.xml

- Create a new file called `settings.xml` in your `%USERPROFILE%\.m2` folder. If the folder doesn't exist, create it.
- Copy the contents of the `settings.xml` file below into your new `settings.xml` file in your `.m2` folder.

Note: the `secret` value below under the `auth.backend.secret`, it must match the `backend-service` client credentials.

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
                <auth.realm>quarkus</auth.realm>
                <auth.backend.secret>secret</auth.backend.secret>
            </properties>
        </profile>
        
    </profiles>

    <activeProfiles>
        <activeProfile>keycloak</activeProfile>
    </activeProfiles>

</settings>
```

### The .env File

- Checkout the project with git, if you haven't already. Or if you want, you can download one of the release files and unzip it.
- Copy the contents of the file below into a file called `.env` in your checked-out project folder.

```shell script
AUTH_SERVER_URL=http://localhost:8180
AUTH_REALM=quarkus
QUARKUS_OIDC_AUTH_SERVER_URL=http://localhost:8180/auth/realms/quarkus
QUARKUS_OIDC_CREDENTIALS_SECRET=secret
QUARKUS_KEYCLOAK_DEVSERVICES_REALM_NAME=quarkus
ADMIN_USER_NAME=alice
ADMIN_USER_PASSWORD=alice
API_SERVER=http://localhost:8080
```

# Rename The Network Interface (If Necessary)

The src/main/resources/startCaptureScript.ps1 file contains a network interface name called `vEthernetBridge`. That needst to be changed to the network interface you have on your PC, or the PC that will be running PCapture. If you go to your start menu, and search for `Network Connections`, it will show a window with your network adapter names. The interface name must match one of those names. If there are spaces in the names, this might complicate things. I suggest that if you have spaces in the names, that you rename the network interface that you want to use so that it doesn't have spaces in it's name.

### Now You're ready to start building

```shell
./mvnw clean compile quarkus:dev
```

- This may take a while the first time you build. It needs to download lots of dependencies, not just for my project, but also for the Quarkus application server.
- Once it's done, you should have a working version of the app running on http://localhost:8080.
- If you go to http://localhost:8080, it should redirect you to the Keycloak server's `quarkus` realm page, if you've done everything correctly, and I haven't missed anything ;)
- Login using the username `alice` and the password `alice`, or perhaps `admin` and `admin` or `jdoe` and `jdoe`.

## Cosmetic Settings

- You can change the `Display Name` under `Realm Settings` to change the login banner. You can also change the title and heading on the web page by adding these settings to your `settings.xml` under the keycloak properties.

```xml
<branding.title>PCapture</branding.title>
<branding.description>A web frontend for command-line based packet capture tools.</branding.description>
```

## Remember-me

- Remember-me can be turned on in the `Realm Settings` section.

