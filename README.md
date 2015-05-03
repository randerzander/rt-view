An example of an Ambari View with a simple UI for displaying Kafka messages & Phoenix query results.

To install the pre-built jar:
```
sudo rm -rf /var/lib/ambari-server/resources/views/work/example\{1.0.0\}
sudo cp target/rt-view-1.0-SNAPSHOT-view.jar /var/lib/ambari-server/resources/views/
```

To manually build and deploy this project, you'll need maven, and bower (npm install -g brunch) installed.
```
cd rt-view/src/main/resources/ui
bower install
cd ~/rt-view/
mvn package
```
