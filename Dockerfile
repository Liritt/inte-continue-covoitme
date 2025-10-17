FROM tomcat:10-jdk17-openjdk-slim

COPY target/*.war /usr/local/tomcat/webapps/ROOT.war