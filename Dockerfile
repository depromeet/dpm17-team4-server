FROM eclipse-temurin:21-jre
ARG JAR_FILE=build/libs/server-*.jar

COPY ${JAR_FILE} /app.jar

ENV TZ=Asia/Seoul \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -XX:+UseStringDeduplication"

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo "$TZ" > /etc/timezone \
 && addgroup --system app && adduser --system --ingroup app app \
 && chown app:app /app.jar \

USER app

HEALTHCHECK --interval=30s --timeout=3s --retries=3 CMD [ "sh", "-c", "exec 3<>/dev/tcp/127.0.0.1/8080 || exit 1" ]

ENTRYPOINT ["java","-jar","/app.jar"]
