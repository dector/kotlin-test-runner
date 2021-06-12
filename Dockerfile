# === Build builder image ===

FROM gradle:6.8-jdk11 AS build

WORKDIR /home/builder

# Prepare required project files
COPY src ./src
COPY build.gradle.kts ./
COPY settings.gradle.kts ./

# Build test runner
RUN gradle --no-daemon -i shadowJar \
    && cp build/libs/autotest-runner.jar .

# Warm-up cache
COPY cache-warmup/ ./
RUN cd cache-warmup \
  && gradle --no-daemon resolveDependencies

# === Build runtime image ===

FROM gradle:6.8-jdk11
ARG WORKDIR="/opt/test-runner"

# Copy binary and launcher script
COPY bin/run.sh ${WORKDIR}/run.sh
COPY --from=build /home/builder/autotest-runner.jar ${WORKDIR}

COPY --from=build /home/gradle/.gradle/ /home/gradle/.gradle/

# Cache Kotlin dependencies
# COPY cache-warmup/ /opt/cache-warmup/
# RUN cd /opt/cache-warmup/ \
#   && GRADLE_HOME=/opt/cache-warmup/.gradle gradle --console plain --no-daemon resolveDependencies \
#   && cp -r .gradle/ /opt/test-runner/.gradle/

# ENV GRADLE_HOME=/opt/test-runner/.gradle

# RUN ls /opt/test-runner/.gradle

WORKDIR $WORKDIR

ENTRYPOINT ["sh", "/opt/test-runner/run.sh"]
