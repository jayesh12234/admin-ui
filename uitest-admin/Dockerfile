# Microsoft's official Playwright Java image — ships the matching browser binaries and all
# OS-level dependencies for the Playwright version uitest-commons depends on (playwright.version
# in mosip-functional-tests/uitestrig-platform/pom.xml). Keep this tag in sync with that property.
FROM mcr.microsoft.com/playwright/java:v1.44.0-jammy

USER root

# Maven isn't bundled in the Playwright base image.
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME

LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001

ENV work_dir=/home/${container_user}/

RUN groupadd -g ${container_user_gid} ${container_user_group} \
    && useradd -u ${container_user_uid} -g ${container_user_group} -s /bin/bash -m ${container_user} -d ${work_dir} \
    && chown -R ${container_user}:${container_user} /home/${container_user}/

USER ${container_user_uid}:${container_user_gid}
WORKDIR ${work_dir}

# Source only — no fat jar. The suite runs via `mvn test` (Surefire → JUnit5 Platform →
# AdminTestRunner → Cucumber), same as a local run.
COPY --chown=${container_user_uid}:${container_user_group} pom.xml ${work_dir}
COPY --chown=${container_user_uid}:${container_user_group} src/ ${work_dir}src/
COPY --chown=${container_user_uid}:${container_user_group} entrypoint.sh ${work_dir}

# Resolves and caches all dependencies (including uitest-commons/apitest-commons) into this
# image's own .m2 and compiles the test sources, so a container start only has to run the suite.
RUN mvn -B test-compile -Dgpg.skip=true

ENTRYPOINT ["/bin/bash", "./entrypoint.sh"]
