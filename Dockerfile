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