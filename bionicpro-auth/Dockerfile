FROM gradle:jdk21-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle/
RUN chmod +x gradlew
COPY build.gradle.kts settings.gradle.kts ./

RUN ./gradlew dependencies --no-daemon


COPY src src

RUN ./gradlew clean build -x test --no-daemon

FROM alpine/java:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","app.jar"]
