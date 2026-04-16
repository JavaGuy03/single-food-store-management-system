# Giai đoạn 1: Build mã nguồn bằng Maven
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build file jar và bỏ qua test cho nhanh
RUN mvn clean package -DskipTests

# Giai đoạn 2: Chạy ứng dụng với JRE (nhẹ hơn JDK rất nhiều)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy file jar từ giai đoạn 1 sang
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]