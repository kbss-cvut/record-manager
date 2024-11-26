FROM maven:3-eclipse-temurin-17 as build

WORKDIR /record-manager

COPY pom.xml pom.xml

RUN mvn -B de.qaware.maven:go-offline-maven-plugin:resolve-dependencies

COPY src src

RUN mkdir /record-manager/doc

RUN mvn package -B -DskipTests=true

FROM eclipse-temurin:17-jdk-alpine as runtime
COPY --from=build  /record-manager/target/record-manager.jar record-manager.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/record-manager.jar"]
