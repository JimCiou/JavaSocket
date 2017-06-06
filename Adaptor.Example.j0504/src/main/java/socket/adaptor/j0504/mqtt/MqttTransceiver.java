package socket.adaptor.j0504.mqtt;

import socket.adaptor.j0504.system.*;
import socket.adaptor.j0504.*;

import java.net.Socket;

import java.sql.Timestamp;
import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.apache.log4j.Logger; 

public class MqttTransceiver implements MqttCallback {

	ADconf adconfer = new ADconf();
        Logger logger = Logger.getLogger(MqttTransceiver.class); 
   
	private MqttClient 			client;
	private String 				brokerUrl;
	private boolean 			quietMode;
	private MqttConnectOptions 	conOpt;
	private boolean 			clean;
	private String password;
	private String userName;

	//BackDoor for Set Socket/////////
	public void mqttSetSocket(String socketID, Socket socket) {
		adconfer.setSocket(socketID, socket);}
	public void mqttRemoveSocket(String socketID) {adconfer.removeSocket(socketID);}
	//////////////////////////////////

	public void Stores(String brokerUrl, String clientId, boolean cleanSession, boolean quietMode, String userName, String password) throws MqttException {
		this.brokerUrl = brokerUrl;
		this.quietMode = quietMode;
		this.clean 	   = cleanSession;
		this.password = password;
		this.userName = userName;

		String tmpDir = System.getProperty("java.io.tmpdir");
		MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);

		try {
			conOpt = new MqttConnectOptions();
			conOpt.setCleanSession(clean);
		if(password != null ) {conOpt.setPassword(this.password.toCharArray());}
	    	if(userName != null) {conOpt.setUserName(this.userName);}

			// Construct an MQTT blocking mode client
			client = new MqttClient(this.brokerUrl,clientId, dataStore);
			// Set this wrapper as the callback handler
			client.setCallback(this);

		} catch (MqttException e) {
			e.printStackTrace();
			logger.warn("Unable to set up client: "+e.toString());
			System.exit(1);
		}
	}

	public void publish(String topicName, int qos, byte[] payload) throws MqttException {

		logger.warn("Connecting to "+brokerUrl + " with client ID "+client.getClientId());
		client.connect(conOpt);
		logger.warn("Connected");

		String time = new Timestamp(System.currentTimeMillis()).toString();
		logger.warn("Publishing at: "+time+ " to topic \""+topicName+"\" qos "+qos);

		// Create and configure a message
 		MqttMessage message = new MqttMessage(payload);
		message.setQos(qos);
		// Main function
    		client.publish(topicName, message);

    		client.disconnect();
 		logger.warn("Disconnected");
	}

	public void subscribe(String act, String topicName, int qos) throws MqttException {

		if(act.equals("ON")){

                	// Connect to the MQTT server
			client.connect(conOpt);		
			logger.warn("Connected to "+brokerUrl+" with client ID "+client.getClientId());
			logger.warn("Subscribing to topic \""+topicName+"\" qos "+qos);

			// Main function
			client.subscribe(topicName, qos);
		}else if(act.equals("OFF")){

			// Disconnect the client from the server
			logger.warn("Disconnected");
			client.disconnect();
		}
    	}

	/****************************************************************/
	/* Methods to implement the MqttCallback interface              */
	/****************************************************************/

	public void connectionLost(Throwable cause) {

		logger.warn("Connection to " + brokerUrl + " lost!" + cause);
		System.exit(1);
	}

	public void deliveryComplete(IMqttDeliveryToken token) {}

	public void messageArrived(String topic, MqttMessage message) throws MqttException {
		// Called when a message arrives from the server that matches any
		// subscription made by the client
		String time = new Timestamp(System.currentTimeMillis()).toString();
//		System.out.println("Time:\t" +time +
//                           "  Topic:\t" + topic +
//                           "  Message:\t" + new String(message.getPayload()) +
//                           "  QoS:\t" + message.getQos());

		//BackDoor for Set Socket
		logger.warn("Config to Device:[MSG]+++++++++++++++++++++++++++++++++++++++++++++++++");
		logger.warn("Received from MQTT: "+new String(message.getPayload()));
		adconfer.getMSG(new String(message.getPayload()));
		logger.warn("#######################################################################");
	}

	/****************************************************************/
	/* End of MqttCallback methods                                  */
	/****************************************************************/

}
