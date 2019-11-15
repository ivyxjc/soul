FROM gradle:jdk11 as builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
#RUN gradle  --no-daemon fatJar --full-stacktrace
RUN cd /home/gradle/src/build/libs \
    && ls

FROM openjdk:8-jre-slim
EXPOSE 8080
COPY --from=builder /home/gradle/src/build/libs/soul-1.0-SNAPSHOT.jar /usr
ENTRYPOINT ["java","-Dfile.encoding=UTF-8","-cp","/usr/soul-1.0-SNAPSHOT.jar","com.ivyxjc.soul.ServerKt"]