FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/assignment-escalation-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java","-jar","app.jar","--spring.profiles.active=docker"]
