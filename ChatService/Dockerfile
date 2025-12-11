FROM maven:3.9.7-eclipse-temurin-21 AS build
ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

COPY ./pom.xml /chatservice/pom.xml
WORKDIR /chatservice
RUN mvn dependency:go-offline
COPY ./src/ /chatservice/src
WORKDIR /chatservice
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /chatservice
COPY --from=build /chatservice/target/*.jar /chatservice/chatservice.jar
EXPOSE 8089
CMD ["java", "-jar", "chatservice.jar"]
