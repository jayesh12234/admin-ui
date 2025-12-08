# Use a Java 21 runtime image
FROM eclipse-temurin:21-jre

# Install required packages as root
USER root
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      ca-certificates curl wget unzip gnupg2 apt-transport-https \
      fonts-liberation libasound2 libatk1.0-0 libatk-bridge2.0-0 \
      libc6 libgdk-pixbuf2.0-0 libnspr4 libnss3 libx11-6 libx11-xcb1 \
      libxcb1 libxcomposite1 libxcursor1 libxdamage1 libxrandr2 libxss1 \
      libxtst6 libgbm1 libglib2.0-0 libgtk-3-0 libgconf-2-4 \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Download and install google-chrome-stable .deb (stable channel)
RUN wget -q -O /tmp/google-chrome-stable_current_amd64.deb \
    https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb \
 && apt-get update \
 && apt-get install -y --no-install-recommends /tmp/google-chrome-stable_current_amd64.deb \
 && rm -f /tmp/google-chrome-stable_current_amd64.deb \
 && apt-get -y --purge autoremove \
 && rm -rf /var/lib/apt/lists/*

# Detect installed Chrome version and download matching chromedriver
RUN CHROME_VER="$(google-chrome --product-version 2>/dev/null || google-chrome-stable --product-version 2>/dev/null)" \
 && echo "Installed Chrome version: ${CHROME_VER}" \
 && CHROME_MAJOR="$(echo ${CHROME_VER} | cut -d. -f1)" \
 && echo "Chrome major version: ${CHROME_MAJOR}" \
 # Query latest compatible chromedriver for the chrome major version
 && LATEST_DRIVER_VERSION="$(curl -sS https://chromedriver.storage.googleapis.com/LATEST_RELEASE_${CHROME_MAJOR})" \
 && if [ -z "$LATEST_DRIVER_VERSION" ]; then echo "Could not detect chromedriver for major ${CHROME_MAJOR}"; exit 1; fi \
 && echo "Matching ChromeDriver version: ${LATEST_DRIVER_VERSION}" \
 && wget -q -O /tmp/chromedriver_linux64.zip "https://chromedriver.storage.googleapis.com/${LATEST_DRIVER_VERSION}/chromedriver_linux64.zip" \
 && unzip /tmp/chromedriver_linux64.zip -d /tmp/ \
 && mv /tmp/chromedriver /usr/local/bin/chromedriver \
 && chmod +x /usr/local/bin/chromedriver \
 && rm -f /tmp/chromedriver_linux64.zip

# Create runtime user
ARG container_user=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001
ENV work_dir=/home/${container_user}

RUN groupadd -g ${container_user_gid} ${container_user} \
 && useradd -m -u ${container_user_uid} -g ${container_user_gid} -s /bin/bash ${container_user} \
 && mkdir -p ${work_dir} \
 && chown -R ${container_user}:${container_user} ${work_dir}

# Switch to non-root user for runtime
USER ${container_user}
WORKDIR ${work_dir}

# Copy application artifacts (adjust source paths as needed)
# NOTE: If your build system doesn't support --chown, copy as root then chown in a RUN command
COPY --chown=${container_user}:${container_user} entrypoint.sh ${work_dir}/entrypoint.sh
COPY --chown=${container_user}:${container_user} target/*.jar ${work_dir}/app.jar
COPY --chown=${container_user}:${container_user} src/main/resources/ ${work_dir}/resources/

RUN chmod +x ${work_dir}/entrypoint.sh || true

# Expose any ports if needed (example)
# EXPOSE 8080

ENTRYPOINT ["./entrypoint.sh"]
