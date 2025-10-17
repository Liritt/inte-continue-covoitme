FROM maven:3.8.6-openjdk-18 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY nb-configuration.xml ./

RUN mvn clean package

FROM tomcat:10-jdk17-openjdk-slim

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=build /app/target/Covoitme-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
COPY --from=build /app/src/main/webapp/WEB-INF/sql/init.sql /docker-entrypoint-initdb.d/init.sql

ENV CATALINA_OPTS="-Xms512m -Xmx1024m -XX:+UseParallelGC"
ENV JAVA_OPTS="-Djava.awt.headless=true -Dfile.encoding=UTF-8"

EXPOSE 8080

CMD ["catalina.sh", "run"]