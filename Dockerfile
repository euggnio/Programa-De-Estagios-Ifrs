# === Build Stage ===
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src/ src/
RUN ./mvnw package -DskipTests -B

# === Runtime Stage ===
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /app/tokens && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]
