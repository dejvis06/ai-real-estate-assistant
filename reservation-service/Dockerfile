FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q
COPY src/ ./src
RUN ./mvnw clean package -DskipTests -q

FROM eclipse-temurin:25-jdk
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8082 5006
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006", "-jar", "app.jar"]
