FROM maven:3-eclipse-temurin-17 AS build

WORKDIR /record-manager

COPY pom.xml pom.xml

RUN mvn -B de.qaware.maven:go-offline-maven-plugin:resolve-dependencies

COPY src src

RUN mvn package -B -DskipTests=true


FROM eclipse-temurin:17-jdk-alpine AS runtime

COPY --from=build  /record-manager/target/record-manager.jar record-manager.jar

# create entrypoint script
COPY --chmod=755 <<'EOF' /entrypoint.sh
#!/bin/sh
exec java $JAVA_OPTS -jar /record-manager.jar
EOF


EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["/entrypoint.sh"]
