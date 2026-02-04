############################
# 1) Build stage
############################
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY app/pom.xml app/pom.xml
COPY common/pom.xml common/pom.xml
COPY contract-api/pom.xml contract-api/pom.xml
COPY identity/pom.xml identity/pom.xml
COPY media/pom.xml media/pom.xml
COPY reactions/pom.xml reactions/pom.xml
COPY comments/pom.xml comments/pom.xml
COPY views/pom.xml views/pom.xml

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests dependency:go-offline

COPY . .

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests -pl app -am clean package

############################
# 2) Runtime stage
############################
FROM eclipse-temurin:21-jre
WORKDIR /app

# (Опціонально) безпечніше не під root
RUN useradd -r -u 10001 appuser
USER appuser

COPY --from=build /workspace/app/target/*.jar /app/app.jar

EXPOSE 8080

# Мінімальні sane defaults для контейнера
#ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]