# Imagem runtime-only, compartilhada pelos 4 microsservicos.
# Os JARs sao buildados ANTES (mvn package -DskipTests) — no host ou no CI —
# e o .dockerignore libera apenas */target/*.jar para o contexto de build.
# Sem etapa Maven aqui: build muito mais rapido e imagem menor que o multi-stage.
FROM eclipse-temurin:17-jre
# eclipse-temurin:17-jre (jammy) em vez de alpine: tem build para ARM (Mac M).

ARG MODULE=administrativo
WORKDIR /app

COPY ${MODULE}/target/${MODULE}-*.jar app.jar

# MaxRAMPercentage limita o heap a 75% do limite do container (evita estourar a RAM
# do notebook); egd=urandom acelera a inicializacao do SecureRandom.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# exec → o java vira PID 1; o SIGTERM do Docker chega no Spring Boot e o shutdown
# acontece de forma limpa.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
