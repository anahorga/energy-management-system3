FROM maven:3.9.7-eclipse-temurin-21 AS build

ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

COPY ./pom.xml /monitoringdevice/pom.xml
WORKDIR /monitoringdevice
RUN mvn dependency:go-offline
COPY ./src/ /monitoringdevice/src
WORKDIR /monitoringdevice
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /monitoringdevice
COPY --from=build /monitoringdevice/target/*.jar /monitoringdevice/monitoringdevice.jar
EXPOSE 8084
CMD ["java", "-jar", "monitoringdevice.jar"]
