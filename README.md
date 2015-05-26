An example of an Ambari View with a simple UI for polling Phoenix and displaying realtime metrics.

To install the pre-built jar:
```
cd rt-view/
sudo cp target/rt-view-1.0-SNAPSHOT-view.jar /var/lib/ambari-server/resources/views/
sudo service ambari-server restart
```

![RT View](/sshots/dashboard.png?raw=true)

To manually build and deploy this project, you'll need maven, and bower (npm install -g bower) installed.
***Warning:*** deploy.sh WILL NOT WORK without internet access, maven, nodejs & npm (https://nodejs.org/), and bower installed!
```
cd rt-view/
sh deploy.sh
```
