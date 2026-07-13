FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /workspace

ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx768m -Dorg.gradle.daemon=false -Dfile.encoding=UTF-8"

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

COPY src ./src

RUN ./gradlew clean bootJar --no-daemon --max-workers=1 \
    && JAR_FILE="$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | head -n 1)" \
    && test -n "$JAR_FILE" \
    && cp "$JAR_FILE" /workspace/app.jar


FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

RUN groupadd --system spring \
    && useradd --system \
        --gid spring \
        --home-dir /app \
        --shell /usr/sbin/nologin \
        spring

COPY --from=builder --chown=spring:spring /workspace/app.jar /app/app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]