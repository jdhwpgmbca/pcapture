
.\mvnw clean compile package -Pcontainer
$ENV:DOCKER_BUILDKIT=1
docker build -f target/docker/Dockerfile.alpine -t quarkus/pcapture-jvm .
