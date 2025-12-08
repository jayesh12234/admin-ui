# Start with a Java 21 runtime
FROM eclipse-temurin:21-jre

# Build-time metadata (CI expects these ARG/LABEL lines)
ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME

LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

# --- Install required packages and Chrome & dependencies as root ---
USER root
ENV DEBIAN_FRONTEND=noninteractive

# Install general dependencies needed by Chrome and for downloads
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      ca-certificates curl wget unzip gnupg2 apt-transport-https \
      fonts-liberation libasound2 libatk1.0-0 libatk-bridge2.0-0 \
      libc6 libgdk-pixbuf2.0-0 libnspr4 libnss3 libx11-6 libx11-xcb1 \
      libxcb1 libxcomposite1 libxcursor1 libxdamage1 libxrandr2 libxss1 \
      libxtst6 libgbm1 libglib2.0-0 libgtk-3-0 libgconf-2-4 \
      xvfb procps \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Download and install google-chrome-stable (amd64 .deb)
RUN wget -q -O /tmp/google-chrome-stable_current_amd64.deb \
      https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb \
 && apt-get update \
 && apt-get install -y --no-install-recommends /tmp/google-chrome-stable_current_amd64.deb \
 && rm -f /tmp/google-chrome-stable_current_amd64.deb \
 && apt-get -y --purge autoremove \
 && rm -rf /var/lib/apt/lists/*

# Install kubectl (stable)
RUN curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" \
 && chmod +x kubectl \
 && mv kubectl /usr/local/bin/kubectl

# Install matching chromedriver for the installed Chrome
RUN set -eux; \
    CHROME_VER="$(google-chrome --product-version 2>/dev/null || google-chrome-stable --product-version 2>/dev/null)"; \
    echo "Detected Chrome version: ${CHROME_VER}"; \
    CHROME_MAJOR="$(echo ${CHROME_VER} | cut -d. -f1)"; \
    if [ -z "${CHROME_MAJOR}" ]; then echo "ERROR: Couldn't detect Chrome major version"; exit 1; fi; \
    LATEST_DRIVER="$(curl -sS https://chromedriver.storage.googleapis.com/LATEST_RELEASE_${CHROME_MAJOR})"; \
    if [ -z "${LATEST_DRIVER}" ]; then echo "ERROR: No matching chromedriver for Chrome major ${CHROME_MAJOR}"; exit 1; fi; \
    echo "Matching chromedriver: ${LATEST_DRIVER}"; \
    wget -q -O /tmp/chromedriver_linux64.zip "https://chromedriver.storage.googleapis.com/${LATEST_DRIVER}/chromedriver_linux64.zip"; \
    unzip -q /tmp/chromedriver_linux64.zip -d /tmp; \
    mv /tmp/chromedriver /usr/local/bin/chromedriver; \
    chmod +x /usr/local/bin/chromedriver; \
    rm -f /tmp/chromedriver_linux64.zip

# --- Create runtime user (non-root) ---
ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001
ENV WORK_DIR=/home/${container_user}

RUN groupadd -g ${container_user_gid} ${container_user_group} || true \
 && useradd -m -u ${container_user_uid} -g ${container_user_gid} -s /bin/bash ${container_user} || true \
 && mkdir -p ${WORK_DIR} \
 && chown -R ${container_user}:${container_user} ${WORK_DIR}

# --- Copy application artifacts as root (robust for CI) ---
# Expected files in build context:
#  - entrypoint.sh
#  - target/*.jar  (your application jar)
#  - src/main/resources/  (optional resources)
COPY entrypoint.sh /tmp/entrypoint.sh
COPY target/*.jar /tmp/app.jar
COPY src/main/resources/ /tmp/resources/ || true

# Set ownership and permissions, then move to work dir
RUN mv /tmp/app.jar ${WORK_DIR}/app.jar || true \
 && mv /tmp/entrypoint.sh ${WORK_DIR}/entrypoint.sh || true \
 && mv /tmp/resources ${WORK_DIR}/resources || true \
 && chown -R ${container_user}:${container_user} ${WORK_DIR} \
 && chmod +x ${WORK_DIR}/entrypoint.sh || true

# Expose port if your app needs (uncomment and adjust)
# EXPOSE 8080

# Switch to non-root user and set workdir
USER ${container_user}
WORKDIR ${WORK_DIR}

# Default entrypoint
ENTRYPOINT ["./entrypoint.sh"]
