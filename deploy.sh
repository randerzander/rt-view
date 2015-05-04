# Install UI dependencies via bower
cd src/main/resources/ui
bower install
cd ../../../../

# Build, install view, restart ambari-server
mvn clean package
sudo rm -rf /var/lib/ambari-server/resources/views/work/RT\{0.1.0\}/
sudo cp target/*-view.jar /var/lib/ambari-server/resources/views/
sudo service ambari-server restart
