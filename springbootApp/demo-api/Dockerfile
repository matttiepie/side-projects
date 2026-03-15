FROM gradle:8.14.4-jdk21 AS build
WORKDIR /home/gradle/src

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
COPY src ./src

RUN ./gradlew --no-daemon clean bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]