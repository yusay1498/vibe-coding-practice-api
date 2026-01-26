FROM amazoncorretto:25

ARG JAR_FILE=target/*.jar

COPY $JAR_FILE /opt/app.jar

ENTRYPOINT ["java", "-jar", "/opt/app.jar"]
