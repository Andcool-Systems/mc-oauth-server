FROM openjdk:17-jdk-slim
WORKDIR /app

COPY . .

CMD ["java", "-jar", "test_auth.jar"]

EXPOSE 8089
EXPOSE 25565