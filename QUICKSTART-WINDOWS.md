# Developer Quckstart - Build Environment Preparation

## Install Docker for Windows

- Download and install the Linux Kernel Update package for WSL2 (Windows Subsystem For Linux): https://wslstorestorage.blob.core.windows.net/wslblob/wsl_update_x64.msi
- Download and install Docker Desktop for Windows: https://www.docker.com/products/docker-desktop

## Install and Configure OpenJDK 11

- Download and run the OpenJDK 11 installer from: https://adoptopenjdk.net/

## Install and Configure Apache Maven

- Make sure that your JAVA_HOME is set to the installation folder, and your PATH contains %JAVA_HOME%\bin. If it isn't, you may need to reboot. If it still isn't set the environment variables manually.
- Download Apache Maven from https://maven.apache.org/download.cgi
- Unzip maven into a directory and set the M3_HOME environment variable to point to the install directory. Then add %M3_HOME%\bin to the PATH environment variable.
- Open a new command shell. If running mvn or java doesn't work, you may need to reboot.

## Install & Run Keycloak (Requires Docker)

- run `keycloakRun.ps1` script. It will lauch a docker container running keycloak at this URL: `http://localhost:8180`
- A realm called `pcapture` will be setup, the roles and clients will be configured. There are no users configured, because Keycloak doen't support exporting or importing users.
- Wait for the container to come up. You can monitor this from the Docker Desktop application. It should take about 2 minutes.
- Create a test user called `alice`. Go to the credential tab, and set the password to `alice` and turn temporary off. Go to the `Role Mappings` tab and add the `user` role. You may need to scroll down to see the `user` role.
- Create a test user called `admin`. Go to the credential tab, and set the password to `admin` and turn temporary off. Go to the `Role Mappings` tab and add the `user`, `admin` and `filter_admin` roles.
- If the container is restarted, it *WILL* retain the added users.
- Restart the container with `keycloakRestart.ps1` or simply use `docker restart keycloak` or the Docker Desktop application to start/stop/restart it.

## settings.xml

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

## The .env File

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

## Now You're ready to start building

```shell
./mvnw clean compile quarkus:dev
```

- This may take a while the first time you build. It needs to download lots of dependencies, not just for my project, but also for the Quarkus application server.
- Once it's done, you should have a working version of the app running on `http://localhost:8080`, and Keycloak will be running on `http://localhost:8180`.
- If you go to `http://localhost:8080`, it should redirect you to the Keycloak server's `pcapture` realm page, if you've done everything correctly, and I haven't missed anything ;)
- Login using the username `alice` and the password `alice`. Start a capture, but don't stop it, and log out.
- Login using the username `admin` and the password `admin`. Click on the `Toggle All Users` button. You should see that the `admin` user can see the captures the `alice` user captured.

## Cosmetic Settings

- You can change the `Display Name` under `Realm Settings` to change the login banner. You can also change the title and heading on the web page by adding these settings to your `settings.xml` under the keycloak properties.

```xml
<branding.title>PCapture</branding.title>
<branding.description>A web frontend for command-line based packet capture tools.</branding.description>
```

## Remember-me

- Remember-me can be turned on or off in the `Realm Settings` section.
