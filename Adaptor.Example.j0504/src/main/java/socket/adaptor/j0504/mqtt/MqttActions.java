package socket.adaptor.j0504.mqtt;

import socket.adaptor.j0504.system.*;

import java.net.Socket;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.apache.log4j.Logger;  
import org.apache.log4j.PropertyConfigurator;

public class MqttActions extends Thread{

	MqttTransceiver MqttClient = new MqttTransceiver();
        Property propuser = new Property();
	Logger logger = Logger.getLogger(MqttActions.class); 

	static int qos 			= 2;
	static String broker 		= "localhost";
	static int port 		= 1883;
	static String subTopic		= null;
	static String pubTopic 	        = null;
	static String password = null;
	static String userName = null;

	static String action 		= "Default";
	static String message 		= "Default";

	static boolean ssl = false;
	static String protocol = "tcp://";
	static String clientId 	= null;
	static boolean cleanSession = true;
	static boolean quietMode 	= false;

	//BackDoor for Set Socket/////////
	public void mqttSetSocket(String socketID, Socket socket) {
		MqttClient.mqttSetSocket(socketID, socket);}
	public void mqttRemoveSocket(String socketID) {MqttClient.mqttRemoveSocket(socketID);}
	//////////////////////////////////

	public void MqttPublish(String properties, String pubtopic,String pubmessage) {
		this.action="publish";

		this.qos = Integer.parseInt(propuser.loadproperties(properties,"MQqos"));;
		this.broker = propuser.loadproperties(properties,"MQIP");
		this.port = Integer.parseInt(propuser.loadproperties(properties,"MQport"));
		this.userName = propuser.loadproperties(properties,"MQuserName");
		this.password = propuser.loadproperties(properties,"MQpassword");

		this.pubTopic = pubtopic;
		this.message=pubmessage;

		mqttStore();
		mqttAction("ON");
	}

	public void MqttSubscribe(String properties) {
		this.action="subscribe";

		this.qos = Integer.parseInt(propuser.loadproperties(properties,"MQqos"));
		this.broker = propuser.loadproperties(properties,"MQIP");
		this.port = Integer.parseInt(propuser.loadproperties(properties,"MQport"));
		this.subTopic = propuser.loadproperties(properties,"MQtopic");
		this.userName = propuser.loadproperties(properties,"MQuserName");
		this.password = propuser.loadproperties(properties,"MQpassword");

		mqttStore();
		mqttAction("ON");
	}
		
	public void mqttStore() {
	
    		if (ssl) {protocol = "ssl://";}
		String url = protocol + broker + ":" + port;

		if (clientId == null || clientId.equals("")) {clientId = "SampleJavaV3_"+action;}

		try {
			MqttClient.Stores(url, clientId, cleanSession, quietMode,userName,password);
		} catch(MqttException me) {
			// Display full details of any exception that occurs
			logger.warn("reason "+me.getReasonCode());
			logger.warn("msg "+me.getMessage());
			logger.warn("loc "+me.getLocalizedMessage());
			logger.warn("cause "+me.getCause());
			logger.warn("excep "+me);
			me.printStackTrace();
		}
	}

	public void mqttAction(String act) {

		// Default settings:
		String topic 		= "";
		
		// Validate the provided arguments
		if (!action.equals("publish") && !action.equals("subscribe")) {
			logger.warn("Invalid action: "+action);return;}
		if (qos < 0 || qos > 2) {
			logger.warn("Invalid QoS: "+qos);return;}
		if (topic.equals("")) {
			// Set the default topic according to the specified action
			if (action.equals("publish")) {
				topic = pubTopic;
			} else {
				topic = subTopic;
			}
		}

		// With a valid set of arguments, the real work of
		// driving the client API can begin
		try {
			// Perform the requested action
			if (action.equals("publish")) {
				MqttClient.publish(topic,qos,message.getBytes());
			} else if (action.equals("subscribe")) {
				MqttClient.subscribe(act,topic,qos);
			}
		} catch(MqttException me) {
			// Display full details of any exception that occurs
			logger.warn("reason "+me.getReasonCode());
			logger.warn("msg "+me.getMessage());
			logger.warn("loc "+me.getLocalizedMessage());
			logger.warn("cause "+me.getCause());
			logger.warn("excep "+me);
			me.printStackTrace();
		}
	}
}
