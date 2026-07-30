# ---------- BUILD STAGE ----------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml for dependency caching
COPY backend/hesco/pom.xml ./pom.xml
RUN mvn -q -e -DskipTests dependency:go-offline

# Copy source
COPY backend/hesco/src ./src

# Build jar
RUN mvn -q -DskipTests clean package

# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy jar
COPY --from=builder /app/target/*.jar app.jar

# Expose correct port
EXPOSE 8084

# Start app
ENTRYPOINT ["java","-Xms256m","-Xmx512m","-jar","/app/app.jar"]