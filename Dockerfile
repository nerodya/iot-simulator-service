FROM gradle:8.7-jdk17 AS build
WORKDIR /src
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle* settings.gradle* gradle.properties* ./
COPY src ./src
RUN ./gradlew bootJar -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
EXPOSE 8085
COPY --from=build /src/build/libs/*[^plain].jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]