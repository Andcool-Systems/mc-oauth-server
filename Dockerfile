FROM openjdk:21
WORKDIR /app

COPY . .

CMD ["java", "-jar", "mc-oauth-all.jar"]