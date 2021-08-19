
.\mvnw clean compile package -Pcontainer
docker build -f src/main/docker/Dockerfile.alpine -t quarkus/dumpcap-ws-jvm .
