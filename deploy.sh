# Install UI dependencies via bower
cd src/main/resources/ui
bower install
cd ../../../../

# Build, install view, restart ambari-server
rm -rf target/
mvn clean package
sudo rm -rf /var/lib/ambari-server/resources/views/work/RT*
sudo rm -rf /var/lib/ambari-server/resources/views/rt-*.jar
sudo cp target/*-SNAPSHOT-view.jar /var/lib/ambari-server/resources/views/
sudo rm /var/log/ambari-server/ambari-server.log
sudo service ambari-server restart
