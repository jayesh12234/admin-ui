#!/bin/bash

java --version


#!/bin/bash

# Start Selenium Hub
java -jar /opt/selenium/selenium-server-standalone.jar -role hub &
sleep 5  # Wait for Selenium Hub to start

# Start Selenium Node with Chrome
java -Dwebdriver.chrome.driver=/usr/bin/chromedriver -jar /opt/selenium/selenium-server-standalone.jar -role node -hub http://localhost:4444/grid/register -browser "browserName=chrome,maxInstances=5" &

# Wait for a while to allow nodes to register
sleep 10

# Run your test or other application
java -jar adminui-*-jar-with-dependencies.jar
