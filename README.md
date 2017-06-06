# JavaSocket
These are only for Jim's backup, the final version has not been tested.

Hint: This application is installed in the Karaf container, the background environment needs to have Java and Maven.
## Need several third-party bundles
Use command "bundle:install file:/home/../.."

- org.apache.httpcomponents.httpcore_X.X.X.jar
- org.apache.httpcomponents.httpclient_X.X.X.jar
- org.eclipse.paho.client.mqttv3-X.X.X.jar

## Adaptor config file
Change config file, then put it under apache-karaf-X.X.X/etc/

```
#Use MQTT to receive Backend's order, then send packet to device by TCP/IP.
MQqos=2
MQport=1883
MQIP=localhost
MQuserName=socket
MQpassword=12345
MQtopic=/adaptor/#

#Get device packet by TCP/IP, then use HTTP to upload devices information.
BackendURL=http://localhost:8181/cxf/example 
```
- adaptor.example.j0504.properties

## Common config file
File already under apache-karaf-X.X.X/etc/

- users.properties, for Users.
- org.apache.karaf.shell.cfg, for SSH login.
- org.ops4j.pax.logging.cfg, for Logger.
