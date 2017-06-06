package socket.adaptor.j0504;

import socket.adaptor.j0504.mqtt.*;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.log4j.Logger;  
import org.apache.log4j.PropertyConfigurator;
 
public class Adaptor extends Thread {

	int serverport = 8180;
	String adaptorID = "gt350s";
	boolean serverflag=true;

	ServerSocket serverSocket;    //伺服端的Socket
	SocketAction flag = new SocketAction();;
	MqttActions mqttsample = new MqttActions();
	Logger logger = Logger.getLogger(Adaptor.class); 

	public void run(){
		while(serverflag){
 		int socketID = 1;  
			try { 
				serverSocket = new ServerSocket(serverport);
        			logger.warn("Adaptor-gt350s: 開始執行 & MQTT Subscribe"); 
				mqttsample.MqttSubscribe("etc/adaptor.example.j0504.properties");
 
				// 當Server運作中時
				while (!serverSocket.isClosed()) {  
					logger.warn("Adaptor-gt350s: 等待新的連線");
        	    			Socket socket = serverSocket.accept( );
					logger.warn("新的連線建立: SocketID = "+socketID);  
		        		new SocketServer_Thread(socket, Integer.toString(socketID), adaptorID, mqttsample, flag).start();
					socketID++;
				}} 
			catch (Exception e) {logger.warn("Exception in gt350sGo(): "+e);}
		}
	}
	//關閉的方法
	public void gt350sStop(){
        	mqttsample.mqttAction("OFF");
		flag.setAllStop();
		logger.warn("Adaptor-gt350s: 關閉MQTT Subscribe & 所有Socket關閉");
		try{
			this.serverflag=false;
			serverSocket.close();
			logger.warn("Adaptor-gt350s: 關閉執行");}
		catch (IOException e) {e.printStackTrace();}
	}
}

class SocketAction {

	boolean flag=true;

	public boolean getAction(){
	return flag;}

	public void setAllStop(){
		this.flag=false;}
}


class SocketServer_Thread extends Thread {

	Socket socket;
	String socketID;
	String adaptorID;
	MqttActions mqttsample;
	SocketAction flag;
	Packets packets = new Packets();
	Logger logger = Logger.getLogger(SocketServer_Thread.class); 

	public SocketServer_Thread(Socket socket, String socketID, String adaptorID, MqttActions mqttsample, SocketAction flag) {
		this.socket = socket;    
		this.socketID=socketID;
		this.adaptorID=adaptorID;  
		this.mqttsample=mqttsample; 
		this.flag=flag;} 
 
	public void run(){
		try { 
		//將SocketID & Socket丟給MQTT
		mqttsample.mqttSetSocket(socketID,socket);

		InputStream in=null;
		OutputStream out=null;
		socket.setSoTimeout(330*1000);//TimeOUT Setup 5m30s
		//一個Socket配給1024個Buffer當接收
		byte [] data_in_Byte=new byte[1024];
			// 當Socket已連接時連續執行 Flag必須為true
			while (socket.isConnected() && flag.getAction()) {
				in = socket.getInputStream();
				out = socket.getOutputStream();
				//讀取接收資料並存進Buffer中 讀取結果做確認是否斷線
				int read =in.read(data_in_Byte);
				if(read==-1 || read==-0){
					logger.warn("read buffer==(-1 or 0)");break;}
					
				//若資料有接收到..則開始解析
				//(1)取得封包長度,資訊位於 3 or 4Byte
				int packet_length_int=0;
				if((Integer.toHexString(data_in_Byte[0] & 0xff)+Integer.toHexString(data_in_Byte[0] & 0xff)).equals("7878")){
					packet_length_int=Integer.parseInt(Integer.toHexString(data_in_Byte[2] & 0xff),16)+5;}
				else{
					packet_length_int=Integer.parseInt(Integer.toHexString(data_in_Byte[2] & 0xff)+Integer.toHexString(data_in_Byte[3] & 0xff),16)+6;}

				//(2)取得封包內容,Byte轉String
				String tmp="";
				String tmp_byte="";
				for(int i=0;i<packet_length_int;i++){
					tmp_byte=Integer.toHexString(data_in_Byte[i] & 0xff).toUpperCase();
						//補零
						if(tmp_byte.length()==1){tmp_byte="0"+tmp_byte;}
					tmp += tmp_byte+" ";}

				//(3)紀錄於Log,並且清除Buffer
				logger.warn("Received from["+socketID+"]:"+tmp);
				Arrays.fill(data_in_Byte,(byte)0);

				//(4)上傳Backend並建立回傳
				String[] data_out=null;
				data_out = packets.rx_packet(tmp,socketID,adaptorID);

				//(5)是否需要回應 [0]:YES or NO ,[1]:packet
				if(data_out[0].equals("YES")){
					logger.warn("Need to responsed socket["+socketID+"]:"+data_out[1]);
					//需要回應,將String轉Byte
					String[] data_out_tokens=data_out[1].split(" ");
					byte [] data_out_Byte=new byte[data_out_tokens.length];
					for(int i=0;i<data_out_tokens.length;i++){
						data_out_Byte[i]=(byte)Integer.parseInt(data_out_tokens[i],16);}
					out.write(data_out_Byte);      
					out.flush();
				}
								
			}
		logger.warn("["+socketID+"] 已經正常斷線");
		in.close();
		out.close();
		socket.close();} 
		catch (Exception e) {
		e.printStackTrace();
		logger.warn("["+socketID+"] 非正常斷線");}
		finally	{
		mqttsample.mqttRemoveSocket(socketID);
		packets.disconnect(socketID);}       
	}   
}
