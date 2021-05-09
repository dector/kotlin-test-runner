# === Build builder image ===

FROM adoptopenjdk/openjdk11:jdk-11.0.5_10-alpine-slim AS build
RUN apk add --no-cache tar bash procps

WORKDIR /home/builder

# Download gradle (used by current auto-tester implementation)
ARG GRADLE_DISTRIB_NAME="gradle-7.0"
ARG GRADLE_DISTRIB_FILE="${GRADLE_DISTRIB_NAME}-bin.zip"
ARG GRADLE_HOME="/home/gradle"

RUN wget https://services.gradle.org/distributions/${GRADLE_DISTRIB_FILE}
RUN unzip ${GRADLE_DISTRIB_FILE}
RUN mv ${GRADLE_DISTRIB_NAME} ${GRADLE_HOME}

# Prepare required project files
COPY src ./src
COPY build.gradle.kts ./

# Build test runner
RUN ${GRADLE_HOME}/bin/gradle -i shadowJar
RUN cp build/libs/autotest-runner.jar .

# List result files
#RUN rm -r build
#RUN apk add --no-cache tree
#RUN tree .

# === Build runtime image ===

FROM adoptopenjdk/openjdk11:jdk-11.0.5_10-alpine-slim
ARG WORKDIR="/opt/test-runner/bin"

COPY bin/run.sh ${WORKDIR}/run.sh
COPY --from=build ${GRADLE_HOME} ${GRADLE_HOME}
COPY --from=build /home/builder/autotest-runner.jar ${WORKDIR}/

ENV PATH "${PATH}:${GRADLE_HOME}/bin"

WORKDIR /opt/test-runner

ENTRYPOINT ["sh", "/opt/test-runner/bin/run.sh"]
