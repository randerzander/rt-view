An example of an Ambari View with a simple UI for polling Phoenix and displaying realtime metrics.

To install the pre-built jar:
```
cd rt-view/
sudo cp target/rt-view-1.0-SNAPSHOT-view.jar /var/lib/ambari-server/resources/views/
sudo service ambari-server restart
```

(/sshots/dashboard.png)

To manually build and deploy this project, you'll need maven, and bower (npm install -g brunch) installed.
```
cd rt-view/
sh deploy.sh
```
