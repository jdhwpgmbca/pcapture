
.\mvnw clean compile package -Pcontainer
$ENV:DOCKER_BUILDKIT=1
docker build -t pcapture:latest -t jdhwpgmbca/pcapture .
