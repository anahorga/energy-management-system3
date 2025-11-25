FROM maven:3.9.7-eclipse-temurin-21 AS build

ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

COPY ./pom.xml /authenticationservice/pom.xml
WORKDIR /authenticationservice
RUN mvn dependency:go-offline
COPY ./src/ /authenticationservice/src
WORKDIR /authenticationservice
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /authenticationservice
COPY --from=build /authenticationservice/target/*.jar /authenticationservice/authenticationservice.jar
EXPOSE 8083
CMD ["java", "-jar", "authenticationservice.jar"]
