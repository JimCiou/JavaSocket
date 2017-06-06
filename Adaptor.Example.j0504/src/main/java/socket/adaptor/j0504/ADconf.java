package socket.adaptor.j0504;

import socket.adaptor.j0504.func.*;

import java.util.*;

import org.json.JSONObject;
import java.net.Socket;
import java.io.OutputStream;
import org.apache.log4j.Logger;  
import org.apache.log4j.PropertyConfigurator;

public class ADconf {

	CRC crcer=new CRC();
    	Hashtable<String,Socket> socketArray = new Hashtable<String,Socket>();
	Logger logger = Logger.getLogger(ADconf.class);

	//存入Socket
	public void setSocket(String socketID, Socket socket) {
		socketArray.put(socketID, socket);
		logger.warn("Put SocketID: ["+socketID+"] in Adaptor Configer");}

	//移除Socket
	public void removeSocket(String socketID) {
		socketArray.remove(socketID);
		logger.warn("Remove SocketID: ["+socketID+"] in Adaptor Configer");}

	public void getMSG(String tx_config_data) {
		try {
			logger.warn("Received Config Order: "+tx_config_data);
			//Example:String identifier="87380883";String instruction="GFENCE,1,OFF,#";
			//為了接住輸入的資料  宣告JSONObject 
			JSONObject jsonObj_config_data = new JSONObject(tx_config_data);
			String identifier = jsonObj_config_data.get("Identifier").toString();
			String instruction = jsonObj_config_data.get("Instruction").toString();
			String socketID = jsonObj_config_data.get("Socket").toString();

			//取得Socket
			Socket socket = (Socket)socketArray.get(socketID);

			//製作0x80封包
			String tx_data="78 78 ";

			//Example1:長度為26 則填入1A >>78 78 '1A' [80 12 87 38 08 83 {47 46 45 4E 43 45 2C 31 2C 4F 46 46 2C 23} 00 00 00 01 94 EC] 0D 0A
			//Example2:長度為20 則填入14 >>78 78 '14' [80 0C 82 86 17 63 {47 46 45 4E 43 45 53 23} 00 00 00 01 38 2C] 0D 0A
			tx_data=tx_data+Integer.toHexString(2+4+instruction.length()+4+2);
			tx_data=tx_data+" 80 ";

			//Example1:長度為18 則填入12 >>78 78 1A 80 '12' [87 38 08 83 {47 46 45 4E 43 45 2C 31 2C 4F 46 46 2C 23}] 00 00 00 01 94 EC 0D 0A
			//Example2:長度為12 則填入0C >>78 78 14 80 '0C' [82 86 17 63 {47 46 45 4E 43 45 53 23}] 00 00 00 01 38 2C 0D 0A
			tx_data=tx_data+Integer.toHexString(4+instruction.length())+" ";

			for(int i=0;i<4;i++){
				tx_data=tx_data+identifier.substring(2*i+0,2*i+2)+" ";}
			for(int i=0;i<instruction.length();i++){
				tx_data=tx_data+Integer.toHexString((int)instruction.charAt(i)).toUpperCase()+" ";}

			tx_data=tx_data+"00 00 00 01 ";

			//CRC的部份
			String[] tx_data_tokens=tx_data.split(" ");
			String crc=crcer.crc_general(Arrays.copyOfRange(tx_data_tokens,2,tx_data_tokens.length));
			tx_data=tx_data+crc.substring(0,2)+" "+crc.substring(2,4);

			tx_data=tx_data+" 0D 0A";

			//傳送封包
			logger.warn("Send packet 0x80 to ["+socketID+"]:"+tx_data);
			OutputStream out=null;
			out = socket.getOutputStream();

			//需要回應,將String轉Byte
			String[] data_out_tokens=tx_data.split(" ");
			byte [] data_out_Byte=new byte[data_out_tokens.length];
			for(int i=0;i<data_out_tokens.length;i++){
				data_out_Byte[i]=(byte)Integer.parseInt(data_out_tokens[i],16);}

			out.write(data_out_Byte);
			out.flush();}
		catch(Exception e){
			logger.warn("Error: " + e.getMessage());}
	}
}
