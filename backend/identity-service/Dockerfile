FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy the entire backend project to ensure Maven finds all required submodule definitions
COPY . .

# Build the generic parent and the specific child module
RUN sed -i 's/\r//' mvnw && chmod +x mvnw && ./mvnw clean package -pl identity-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/identity-service/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
