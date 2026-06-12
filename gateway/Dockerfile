# Etapa de build — maven com JDK 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Copia todos os poms declarados no root pom.xml para que o Maven
# consiga resolver o reactor sem falhar em modulo ausente
COPY pom.xml ./
COPY commons/pom.xml commons/
COPY administrativo/pom.xml administrativo/
COPY agendamento/pom.xml agendamento/
COPY atendimento/pom.xml atendimento/
COPY gateway/pom.xml gateway/

RUN mvn dependency:go-offline -B --no-transfer-progress -pl gateway

COPY gateway/src gateway/src

RUN mvn clean package -B --no-transfer-progress -pl gateway -DskipTests

# Etapa de runtime — eclipse-temurin:17-jre (jammy) pq o alpine nao tem build pra ARM
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /build/gateway/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
