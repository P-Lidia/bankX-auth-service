# Этап 1: Сборка JAR с Maven (внутри Docker)
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
# Копируем pom.xml сначала (для кэширования зависимостей)
COPY pom.xml .
# Скачиваем зависимости (быстрее, если pom не меняется)
RUN mvn dependency:go-offline -B
# Копируем исходный код
COPY src ./src
# Собираем JAR (clean + package, без тестов для скорости)
RUN mvn clean package -DskipTests -Dmaven.javadoc.skip=true -Dmaven.source.skip=true

# Этап 2: Runtime (лёгкий образ с Java, без Maven)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# Копируем JAR из этапа build
COPY --from=build /app/target/bankX-auth-service-1.0-SNAPSHOT.jar app.jar
# Запуск
ENTRYPOINT ["java", "-jar", "app.jar"]