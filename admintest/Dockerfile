FROM -d -p 4444:4444 --shm-size="2g" selenium/standalone-chrome:latest

USER root

# Define build-time arguments
ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME

# Set labels for metadata
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

# Define build-time arguments for user and group
ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001

# Set working directory for the user
ENV work_dir=/home/${container_user}/

# Install packages and create user
RUN groupadd -g ${container_user_gid} ${container_user_group} \
    && useradd -u ${container_user_uid} -g ${container_user_group} -s /bin/bash -m ${container_user} -d ${work_dir}

# Switch to the specified user for the subsequent commands
USER ${container_user_uid}:${container_user_gid}

# Copy files
COPY --chown=${container_user_uid}:${container_user} --chmod=771 ./entrypoint.sh ${work_dir}/entrypoint.sh
COPY --chown=${container_user}:${container_user} ./src/main/resources/ ${work_dir}/resources/
COPY --chown=${container_user_uid}:${container_user} ./target/*.jar ${work_dir}

# Set working directory
WORKDIR ${work_dir}

# Expose port 4444 (this is where Selenium Grid will be accessible)
EXPOSE 4444

# Entrypoint for the container
ENTRYPOINT ["/bin/bash", "-c", "./entrypoint.sh"]
