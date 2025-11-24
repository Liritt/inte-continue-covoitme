FROM tomcat:10-jdk17-openjdk-slim

RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

COPY target/*.war /usr/local/tomcat/webapps/ROOT.war
COPY pom.xml /usr/local/tomcat/pom.xml
COPY src /usr/local/tomcat/src

RUN mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install-deps" \
    && mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"

# COPY --from=build /root/.cache/ms-playwright/ /root/.cache/ms-playwright/

ENV MAVEN_OPTS="-Xmx1024m"
ENV CI="true"

EXPOSE 8080

CMD ["catalina.sh", "run"]