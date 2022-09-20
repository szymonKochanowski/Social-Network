# Use open jdk as the base image
FROM openjdk:11
# Define a variable
ARG JAR_FILE=target/social_network-0.0.1-SNAPSHOT.jar
# Copy jar file as social_network.jar
COPY ${JAR_FILE} social_network.jar
# Listen application on port 8080
EXPOSE 8080
# run app by entrypoint
ENTRYPOINT ["java", "-jar", "/social_network.jar"]

# Build docker image with tag in console command:
# docker build -t social_network:1.0 .


