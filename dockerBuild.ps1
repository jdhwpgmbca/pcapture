
\.mvnw clean compile package -DskipTests -Pcontainer
docker build -f src/main/docker/Dockerfile.ubuntu -t quarkus/dumpcap-ws-jvm .
