#FROM eclipse-temurin:21.0.10_7-jre-ubi10-minimal - no known vulnerabilties
#eclipse-temurin:21-jre with vulnerabilities here is just to check how CI/CD stuff works. Change it later
FROM eclipse-temurin:21-jre
WORKDIR /app

# (Опціонально) безпечніше не під root
RUN useradd -r -u 10001 appuser
USER appuser

COPY **/target/app.jar /app/app.jar

EXPOSE 8080

# Мінімальні sane defaults для контейнера
#ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]