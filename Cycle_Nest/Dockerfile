# 1. Use Tomcat 9 with JDK 21
FROM tomcat:9.0-jdk21-temurin

# 2. Maintainer Info
LABEL maintainer="Faiz Lawan <N1258521@my.ntu.ac.uk>"

# 3. Clean up default apps to keep it lightweight
RUN rm -rf /usr/local/tomcat/webapps/*

# 4. Copy your .war file
COPY target/Cycle_Nest.war /usr/local/tomcat/webapps/CycleNestOrchestrator.war

# 5. Expose the port
EXPOSE 8080

# 6. Start Tomcat
CMD ["catalina.sh", "run"]