#!/bin/bash
set -e

java --version
mvn --version

# Rancher mounts the equivalent of adminui.properties (URLs, secrets) as a ConfigMap/Secret file —
# not baked into the image, since the tracked file only holds blank defaults. Drop it where
# Maven's resource handling expects it, so `mvn test` picks it up exactly like a local run.
ADMINUI_CONFIG_PATH="${ADMINUI_CONFIG_PATH:-/home/mosip/config/adminui.properties}"
if [ -f "$ADMINUI_CONFIG_PATH" ]; then
    cp "$ADMINUI_CONFIG_PATH" src/test/resources/config/adminui.properties
    echo "Mounted config applied from $ADMINUI_CONFIG_PATH"
else
    echo "No mounted config found at $ADMINUI_CONFIG_PATH — using adminui.properties from the image plus environment variables."
fi

# -o: dependencies were already resolved into this image's .m2 at build time.
# Exit code propagates mvn's own (non-zero on any test failure), so Rancher sees a failed Job.
mvn -o test -Dgpg.skip=true
