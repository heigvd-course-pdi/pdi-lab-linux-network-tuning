FROM eclipse-temurin:24-jdk

RUN apt-get update && apt-get install -y maven

WORKDIR /app
COPY pom.xml .
COPY src/ ./src/
RUN mvn compile package -q
RUN cp target/java-server-1.0.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]

