ARG CAPTURE_SCRIPT=/deployments/startCaptureScript.sh
ARG DATA_DIR=/data
ARG DATASOURCE_DB_KIND=h2
ARG DATASOURCE_JDBC_URL=jdbc:h2:file:/data/h2db
ARG DATASOURCE_USERNAME=pcapdb-user
ARG DATASOURCE_PASSWORD=changeme
ARG AUTH_SERVER_URL=http://localhost:8180/auth/realms/pcapture
ARG OIDC_CLIENT_ID=backend-service
ARG OIDC_CREDENTIALS_SECRET=secret
ARG OIDC_TLS_VERIFICATION=required

FROM maven:3.8.2-openjdk-11-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package -Pcontainer

FROM alpine:3

VOLUME ["/data"]

ARG JAVA_PACKAGE=openjdk11
ARG RUN_JAVA_VERSION=1.3.8
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'

RUN apk add --no-cache shadow libcap tini bash curl ${JAVA_PACKAGE} \
    && mkdir -p /deployments && chown 1001:1001 /deployments && chmod 550 /deployments \
    && mkdir -p /data && chown 1001:1001 /data && chmod 770 /data \
    && curl https://repo1.maven.org/maven2/io/fabric8/run-java-sh/${RUN_JAVA_VERSION}/run-java-sh-${RUN_JAVA_VERSION}-sh.sh -o /deployments/run-java.sh \
    && chown 1001:1001 /deployments/run-java.sh \
    && chmod 550 /deployments/run-java.sh

# && echo "securerandom.source=file:/dev/urandom" >> /etc/alternatives/jre/conf/security/java.security

# Configure the JAVA_OPTIONS, you can add -XshowSettings:vm to also display the heap size.
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
# We make four distinct layers so if there are application changes the library layers can be re-used

RUN apk add --no-cache tcpdump

RUN groupadd -g 1001 quarkus \
    && useradd -u 1001 -g quarkus -d /deployments -s /bin/bash quarkus \
    && chgrp quarkus /usr/bin/tcpdump \
    && chmod a+x /usr/bin/tcpdump \
    && /usr/sbin/setcap cap_net_raw,cap_net_admin=eip /usr/bin/tcpdump

# This is for the thin-jar type. However, now I'm using a fat-jar. The difference is basically
# that the fat jar includes all the components of the thin jar, so you don't need to include
# all these directories. However, this probably increases the size of the Docker image because
# the fat jar is bigger. If you use a thin jar, you're only adding a little bit onto the image
# when you update the jar. But if the jar is larger, it's a larger update to the image, and the
# layer is bigger. So I'll probably go back to using thin jars soon. -jdh

# COPY --chown=quarkus target/quarkus-app/lib/ /deployments/lib/
# COPY --chown=quarkus target/quarkus-app/*.jar /deployments/
# COPY --chown=quarkus target/quarkus-app/app/ /deployments/app/
# COPY --chown=quarkus target/quarkus-app/quarkus/ /deployments/quarkus/

COPY --chown=quarkus --chmod=550 src/main/resources/startCaptureScriptTcpdumpForAlpineContainers.sh /deployments/startCaptureScript.sh
COPY --from=build --chown=quarkus --chmod=440 /home/app/target/*-runner.jar /deployments/

# The environment variables below are set from the ARG values above, which can optionally be overridden
# by the Continuous Integration environment.

ENV START_CAPTURE_SCRIPT=$START_CAPTURE_SCRIPT
ENV DATA_DIRECTORY=$DATA_DIR

ENV QUARKUS_DATASOURCE_DB_KIND=$DATASOURCE_DB_KIND
ENV QUARKUS_DATASOURCE_JDBC_URL=DATASOURCE_JDBC_URL
ENV QUARKUS_DATASOURCE_USERNAME=$DATASOURCE_USERNAME
ENV QUARKUS_DATASOURCE_PASSWORD=$DATASOURCE_PASSWORD

ENV QUARKUS_OIDC_AUTH_SERVER_URL=$AUTH_SERVER_URL
ENV QUARKUS_OIDC_CLIENT_ID=$OIDC_CLIENT_ID
ENV QUARKUS_OIDC_CREDENTIALS_SECRET=$OIDC_CREDENTIALS_SECRET
ENV QUARKUS_OIDC_TLS_VERIFICATION=$OIDC_TLS_VERIFICATION

EXPOSE 8080
USER 1001

# I've modified the entrypoint so I can use a different init system that has been specialized for containers.
# This init adds signal handlers to PID 1 to cleanup after process terminations. Before I did this, tcpdump
# was leaving zombie processes around after it terminated, and this doesn't release the process memory. You
# don't always need to do this sort of thing in a container, because you don't always need to launch any more
# processes. But in this case, since we are spawning tcpdump processes, this step is necessary.
# 
# As just a little more background on this behaviour: Linux usually installs signal handlers for other PIDs,
# just not for PID=1. And in a Docker container, the ENTRYPOINT will have a PID of 1.

ENTRYPOINT ["/sbin/tini", "--"]

CMD [ "/deployments/run-java.sh" ]
