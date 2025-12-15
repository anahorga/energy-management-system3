FROM maven:3.9.7-eclipse-temurin-21 AS build

ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

COPY ./pom.xml /loadbalancerservice/pom.xml
WORKDIR /loadbalancerservice
RUN mvn dependency:go-offline
COPY ./src/ /loadbalancerservice/src
WORKDIR /loadbalancerservice
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /loadbalancerservice
COPY --from=build /loadbalancerservice/target/*.jar /loadbalancerservice/loadbalancerservice.jar
EXPOSE 8086
CMD ["java", "-jar", "loadbalancerservice.jar"]
