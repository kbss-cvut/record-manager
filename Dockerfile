FROM tomcat:9.0-jdk8-slim

EXPOSE 8080

COPY /target/record-manager-0.*.war /usr/local/tomcat/webapps/record-manager.war

CMD ["catalina.sh","run"]
