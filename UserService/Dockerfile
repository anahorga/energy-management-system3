FROM maven:3.9.7-eclipse-temurin-21 AS build

ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

COPY ./pom.xml /userservice/pom.xml
WORKDIR /userservice
RUN mvn dependency:go-offline
COPY ./src/ /userservice/src
WORKDIR /userservice
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /userservice
COPY --from=build /userservice/target/*.jar /userservice/userservice.jar
EXPOSE 8081
CMD ["java", "-jar", "userservice.jar"]
