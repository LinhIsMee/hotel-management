FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
# Tải trước tất cả dependencies để sử dụng layer caching trong Docker
RUN mvn dependency:go-offline -B

COPY src ./src
COPY .mvn ./.mvn
COPY mvnw .
COPY mvnw.cmd .
# Biên dịch và đóng gói ứng dụng
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
# Sao chép jar file từ build stage
COPY --from=build /app/target/hotel-management.jar hotel-management.jar
# Biến môi trường cấu hình
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=9000

EXPOSE 9000
ENTRYPOINT ["java", "-jar", "/app/hotel-management.jar"] 