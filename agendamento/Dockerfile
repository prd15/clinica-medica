# Etapa de build — maven com JDK 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Baixa dependencias antes de copiar o codigo pra aproveitar cache de layer
COPY pom.xml ./
COPY commons/pom.xml commons/
COPY administrativo/pom.xml administrativo/
COPY agendamento/pom.xml agendamento/
COPY atendimento/pom.xml atendimento/
COPY gateway/pom.xml gateway/

RUN mvn dependency:go-offline -B --no-transfer-progress -pl agendamento -am

COPY commons/src commons/src
COPY agendamento/src agendamento/src

RUN mvn clean package -B --no-transfer-progress -pl agendamento -am -DskipTests

# Etapa de runtime — eclipse-temurin:17-jre (jammy) pq o alpine nao tem build pra ARM
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /build/agendamento/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
