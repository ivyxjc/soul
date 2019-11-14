FROM gradle:jdk8 as builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle fatJar
RUN cd /home/gradle/src/build/libs \
    && ls

FROM openjdk:8-jre-slim
EXPOSE 8080
COPY --from=builder /home/gradle/src/build/libs/soul-1.0-SNAPSHOT.jar /usr
ENTRYPOINT ["/bin/bash"]

