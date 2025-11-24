FROM maven:3.8.7-openjdk-18 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY nb-configuration.xml ./

RUN mvn clean package -DskipTests \
    && mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install-deps" \
    && mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"

FROM tomcat:10-jdk17-openjdk-slim

RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war
COPY --from=build /app/src /usr/local/tomcat/src
COPY --from=build /app/pom.xml /usr/local/tomcat/pom.xml

# COPY --from=build /root/.cache/ms-playwright/ /root/.cache/ms-playwright/

ENV MAVEN_OPTS="-Xmx1024m"
ENV CI="true"

EXPOSE 8080

CMD ["catalina.sh", "run"]