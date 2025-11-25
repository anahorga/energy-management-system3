FROM maven:3.9.7-eclipse-temurin-21 AS build

ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

COPY ./pom.xml /deviceservice/pom.xml
WORKDIR /deviceservice
RUN mvn dependency:go-offline
COPY ./src/ /deviceservice/src
WORKDIR /deviceservice
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /deviceservice
COPY --from=build /deviceservice/target/*.jar /deviceservice/deviceservice.jar
EXPOSE 8082
CMD ["java", "-jar", "deviceservice.jar"]
