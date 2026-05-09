# Etapa de build — maven com JDK 17
# Uso eclipse-temurin porque e o que tem melhor suporte LTS no alpine
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Copia o pom raiz e os modulos pra resolver dependencias antes de copiar o codigo
# Isso aproveita o cache do Docker e nao baixa tudo do zero a cada mudanca
COPY pom.xml ./
COPY commons/pom.xml commons/
COPY administrativo/pom.xml administrativo/
COPY agendamento/pom.xml agendamento/
COPY atendimento/pom.xml atendimento/

RUN mvn dependency:go-offline -B --no-transfer-progress -pl administrativo -am

# Copia o codigo-fonte e builda so o que precisa
COPY commons/src commons/src
COPY administrativo/src administrativo/src

RUN mvn clean package -B --no-transfer-progress -pl administrativo -am -DskipTests

# Etapa de runtime — eclipse-temurin:17-jre (jammy) pq o alpine nao tem build pra ARM
# Importante pra rodar no Mac com chip M sem precisar de --platform
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /build/administrativo/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
