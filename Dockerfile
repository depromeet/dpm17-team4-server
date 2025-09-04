FROM eclipse-temurin:21-jdk
ARG JAR_FILE=build/libs/app.jar

COPY ${JAR_FILE} app.jar

RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && echo "Asia/Seoul" > /etc/timezone

ENTRYPOINT ["java","-jar","/app.jar"]
