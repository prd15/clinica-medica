# Etapa de build — maven com JDK 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Baixa dependencias antes de copiar o codigo pra aproveitar cache de layer
COPY pom.xml ./
COPY commons/pom.xml commons/
COPY administrativo/pom.xml administrativo/
COPY agendamento/pom.xml agendamento/
COPY atendimento/pom.xml atendimento/

RUN mvn dependency:go-offline -B --no-transfer-progress -pl atendimento -am

COPY commons/src commons/src
COPY atendimento/src atendimento/src

RUN mvn clean package -B --no-transfer-progress -pl atendimento -am -DskipTests

# Etapa de runtime — eclipse-temurin:17-jre (jammy) pq o alpine nao tem build pra ARM
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /build/atendimento/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
