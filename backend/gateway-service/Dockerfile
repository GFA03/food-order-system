FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy the entire backend project to ensure Maven finds all required submodule definitions
COPY . .

# Build the generic parent and the specific child module
RUN ./mvnw clean package -pl gateway-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# We use a wildcard as the Spring Boot maven plugin generates a distinctly versioned JAR
COPY --from=builder /app/gateway-service/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
