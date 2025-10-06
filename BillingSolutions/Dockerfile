# Stage 1: Build the application using a Maven and Java 17 image
# This stage compiles your code and creates the JAR file.
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Create the final, lightweight production image
# This stage takes only the compiled JAR file and the Java runtime.
FROM openjdk:17-jre-slim
WORKDIR /app

# Copy the JAR file created in the 'build' stage
COPY --from=build /app/target/BillingSolutions-0.0.1-SNAPSHOT.jar app.jar

# Tell Docker that the container will listen on port 8080
EXPOSE 8080

# The command to run your application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]