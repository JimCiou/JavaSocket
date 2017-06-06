package socket.adaptor.j0504;

import socket.adaptor.j0504.func.*;

import java.util.*;
import org.json.JSONObject;
import org.apache.log4j.Logger;  
import org.apache.log4j.PropertyConfigurator;

public class Packets {

	enum Packet_enum{Packet_1F, Packet_10, Packet_17, Packet_18, Packet_13, Packet_16, Packet_19, Packet_81, Packet_80;
		public static Packet_enum conversion(String packetID){
			if(packetID != null){
				if (packetID.equalsIgnoreCase("1F")) {return Packet_enum.Packet_1F;}
				if (packetID.equalsIgnoreCase("10")) {return Packet_enum.Packet_10;}
				if (packetID.equalsIgnoreCase("17")) {return Packet_enum.Packet_17;}
				if (packetID.equalsIgnoreCase("18")) {return Packet_enum.Packet_18;}
				if (packetID.equalsIgnoreCase("13")) {return Packet_enum.Packet_13;}
				if (packetID.equalsIgnoreCase("16")) {return Packet_enum.Packet_16;}
				if (packetID.equalsIgnoreCase("19")) {return Packet_enum.Packet_19;}
				if (packetID.equalsIgnoreCase("81")) {return Packet_enum.Packet_81;}
				if (packetID.equalsIgnoreCase("80")) {return Packet_enum.Packet_80;}
			}
		return null;}
	}

	Datas datas=new Datas();
	Backend backender=new Backend();
	CRC crcer=new CRC();
	Logger logger = Logger.getLogger(Packets.class); 

	Hashtable<String,String> socketToDeviceID_Array = new Hashtable<String,String>();

	public String[] rx_packet(String rx_data, String socket, String adaptorID){

		String[] rx_data_tokens=rx_data.split(" ");
		//判斷是否有過長的封包
		boolean over_length_check=(rx_data_tokens[0]+rx_data_tokens[1]).equals("7979");
		boolean length_check=false;
		boolean crc_check=false;

		boolean lead_check=datas.lead_check_data(Arrays.copyOfRange(rx_data_tokens,0,2));
		if(over_length_check){
			//總長度需要-4-2 須等於 Packet的第3+4個Byte
			length_check=((rx_data_tokens.length-4-2)==Integer.parseInt(rx_data_tokens[2]+rx_data_tokens[3],16));
			crc_check=datas.crc_check_data(Arrays.copyOfRange(rx_data_tokens,2,rx_data_tokens.length-2));}
		else{
			//總長度需要-4-1 須等於 Packet的第3個Byte
			length_check=((rx_data_tokens.length-4-1)==Integer.parseInt(rx_data_tokens[2],16));
			crc_check=datas.crc_check_data(Arrays.copyOfRange(rx_data_tokens,2,rx_data_tokens.length-2));}

		//建立回應資料 預設為不回應
		String response_act="NO";
		String response_data="78 78 ";

		if(lead_check && length_check && crc_check){
			//共同項目
			String[] head={null,null,null,null,null};
			if(over_length_check){head=datas.common_data(Arrays.copyOfRange(rx_data_tokens,5,24));}
			else{head=datas.common_data(Arrays.copyOfRange(rx_data_tokens,4,23));}			
			head[3]=adaptorID;
			head[4]=socket;
			logger.warn("Update to Backend:[HEAD]-----------------------------------------------");
			logger.warn("DeviceID="+head[0]+" DeviceModel="+head[1]+" IMSI="+head[2]+" AdaptorID="+head[3]+" Socket="+head[4]);
			logger.warn("=======================================================================");
			socketToDeviceID_Array.put(socket,head[0]);
			String upload_data = "null";

			backender.readConfig("etc/adaptor.example.j0504.properties");

			Packet_enum packet_enum=null;
			if(over_length_check){packet_enum = Packet_enum.conversion(rx_data_tokens[4]);}
			else{packet_enum = Packet_enum.conversion(rx_data_tokens[3]);}
			
			switch(packet_enum){
				case Packet_1F:
					upload_data=rx_packet_0x1F(rx_data_tokens);
					response_data=response_data+"0B 1F 00 00 58 9A 00 00";
					response_act="YES";
				break;
				case Packet_10:
					upload_data=rx_packet_0x10(rx_data_tokens);
					backender.sendPut(head, upload_data, "/locations");
					response_data=response_data+"05 10";
				break;
				case Packet_17:
					upload_data=rx_packet_0x17(rx_data_tokens);
					backender.sendPut(head, upload_data, "/locations");
					response_data=response_data+"05 17";
					response_act="YES";
				break;
				case Packet_18:
					upload_data=rx_packet_0x18(rx_data_tokens);
					backender.sendPut(head, upload_data, "/locations");
					response_data=response_data+"05 18";
				break;
				case Packet_13:
					upload_data=rx_packet_0x13(rx_data_tokens);
					backender.sendPost(head, upload_data, "/devices");
					response_data=response_data+"05 13";
					response_act="YES";
				break;
				case Packet_16:
					upload_data=rx_packet_0x16(rx_data_tokens);
					backender.sendPut(head, upload_data, "/device/warnings");
					response_data=response_data+"05 16";
					response_act="YES";
				break;
				case Packet_19:
					upload_data=rx_packet_0x19(rx_data_tokens);
					backender.sendPut(head, upload_data, "/device/warnings");
					response_data=response_data+"05 19";
					response_act="YES";
				break;
				case Packet_81:
					upload_data=rx_packet_0x81(over_length_check,rx_data_tokens);
					backender.sendPost(head, upload_data, "/device/settings");
				break;
				case Packet_80:
					//No act
				break;
				default:
			}
		logger.warn("Update to Backend:[BODY]-----------------------------------------------");
		logger.warn(upload_data);
		logger.warn("=======================================================================");
		}
		//CRC的部份
		if(response_act.equals("YES")){
		response_data=response_data+" 00 00 ";
		String[] response_data_tokens=response_data.split(" ");
		String crc=crcer.crc_general(Arrays.copyOfRange(response_data_tokens,2,response_data_tokens.length));
		response_data=response_data+crc.substring(0,2)+" "+crc.substring(2,4);
		response_data=response_data+" 0D 0A";}

		String[] response={response_act,response_data};

	return response;}

	//用來告訴Backend Device斷線
	public void disconnect(String socket){

		String DeviceID = (String)socketToDeviceID_Array.get(socket);

		String[] head={DeviceID,null,null,null,null};
		logger.warn("Update to Backend:[HEAD]-----------------------------------------------");
		logger.warn("DeviceID="+head[0]+" DeviceModel="+head[1]+" IMSI="+head[2]+" AdaptorID="+head[3]+" Socket="+head[4]);
		logger.warn("=======================================================================");
		String upload_data = "null";
		try {backender.sendPost(head, upload_data, "/device/disconnect");}
		catch(Exception e){System.err.println("Error: " + e.getMessage());}
		socketToDeviceID_Array.remove(socket);
	}

	public String rx_packet_0x1F(String[] rx_data_tokens){

		//Sync資料的起始位置,Non-完成
		JSONObject jsonObj_0x1F = new JSONObject();
		//特定封包(時間)
		datas.time_data(Arrays.copyOfRange(rx_data_tokens,23,29));
		//特定封包(field)
		datas.field_data(Arrays.copyOfRange(rx_data_tokens,29,31));

	return jsonObj_0x1F.toString();}

	public String rx_packet_0x10(String[] rx_data_tokens){

		int g=29;//GPS資料的起始位置

		JSONObject jsonObj_0x10 = new JSONObject();
		//特定封包(時間)
		datas.time_data(Arrays.copyOfRange(rx_data_tokens,23,29));
		//特定封包(GPS)
		jsonObj_0x10=datas.gps_data(Arrays.copyOfRange(rx_data_tokens, g, g+12), jsonObj_0x10);
		//特定封包(field)
		datas.field_data(Arrays.copyOfRange(rx_data_tokens,41,43));

	return jsonObj_0x10.toString();}

	public String rx_packet_0x17(String[] rx_data_tokens){

		int l=23;//LBS資料的起始位置

		JSONObject jsonObj_0x17 = new JSONObject();
		//特定封包(LBS)
		jsonObj_0x17=datas.lbs_data(Arrays.copyOfRange(rx_data_tokens, l, l+8), jsonObj_0x17);

	return jsonObj_0x17.toString();}


	public String rx_packet_0x18(String[] rx_data_tokens){

		int lr=29;//LBS+RSSI資料的起始位置

		JSONObject jsonObj_0x18 = new JSONObject();
		//特定封包(時間)
		datas.time_data(Arrays.copyOfRange(rx_data_tokens,23,29));
		//特定封包(LBS+RSSI)
		jsonObj_0x18=datas.lbs_array_data(Arrays.copyOfRange(rx_data_tokens, lr, lr+45), jsonObj_0x18);

		try {
			jsonObj_0x18.put("LocationType", "1");
		}catch(Exception e){
			System.err.println("Error: " + e.getMessage());}

		//特定封包(field)
		datas.field_data(Arrays.copyOfRange(rx_data_tokens,74,76));

	return jsonObj_0x18.toString();}

	public String rx_packet_0x13(String[] rx_data_tokens){

		int d=23;//Device資料的起始位置

		JSONObject jsonObj_0x13 = new JSONObject();
		//特定封包(DeviceStatus)
		jsonObj_0x13=datas.device_data(Arrays.copyOfRange(rx_data_tokens, d, d+3), jsonObj_0x13);

	return jsonObj_0x13.toString();}

	public String rx_packet_0x16(String[] rx_data_tokens){

		int g=29;//GPS資料的起始位置
		int l=42;//LBS資料的起始位置
		int d=50;//Device資料的起始位置
		int f=53;//FenceWarn資料的起始位置

		JSONObject jsonObj_0x16 = new JSONObject();
		//特定封包(時間)
		datas.time_data(Arrays.copyOfRange(rx_data_tokens,23,29));
		//特定封包(GPS)
		jsonObj_0x16=datas.gps_data(Arrays.copyOfRange(rx_data_tokens, g, g+12), jsonObj_0x16);
		//特定封包(LBS)
		jsonObj_0x16=datas.lbs_data(Arrays.copyOfRange(rx_data_tokens, l, l+8), jsonObj_0x16);
		//特定封包(DeviceStatus)
		jsonObj_0x16=datas.device_data(Arrays.copyOfRange(rx_data_tokens, d, d+3), jsonObj_0x16);
		//特定封包(FenceWarn)
		jsonObj_0x16=datas.warn_data(Arrays.copyOfRange(rx_data_tokens, f, f+2), jsonObj_0x16);

		try {
			jsonObj_0x16.put("LocationType", "1");
		}catch(Exception e){
			System.err.println("Error: " + e.getMessage());}

		//特定封包(LBS length),未使用
		String lbs_length=rx_data_tokens[41];


	return jsonObj_0x16.toString();}


	public String rx_packet_0x19(String[] rx_data_tokens){

		int l=29;//LBS資料的起始位置
		int d=37;//Device資料的起始位置
		int f=40;//FenceWarn資料的起始位置

		JSONObject jsonObj_0x19 = new JSONObject();
		//特定封包(時間)
		datas.time_data(Arrays.copyOfRange(rx_data_tokens,23,29));
		//特定封包(LBS)
		jsonObj_0x19=datas.lbs_data(Arrays.copyOfRange(rx_data_tokens, l, l+8), jsonObj_0x19);
		//特定封包(DeviceStatus)
		jsonObj_0x19=datas.device_data(Arrays.copyOfRange(rx_data_tokens, d, d+3), jsonObj_0x19);
		//特定封包(FenceWarn)
		jsonObj_0x19=datas.warn_data(Arrays.copyOfRange(rx_data_tokens, f, f+2), jsonObj_0x19);

		try {
			jsonObj_0x19.put("LocationType", "1");
		}catch(Exception e){
			System.err.println("Error: " + e.getMessage());}
	
	return jsonObj_0x19.toString();}

	public String rx_packet_0x81(boolean over_length_check, String[] rx_data_tokens){

		int c=24;//Config資料的起始位置
		if(over_length_check){c=26;}//7979 Config資料的起始位置

		JSONObject jsonObj_0x81 = new JSONObject();
		//特定封包(Config)
		jsonObj_0x81=datas.config_data(Arrays.copyOfRange(rx_data_tokens, c, rx_data_tokens.length-10), jsonObj_0x81);

	return jsonObj_0x81.toString();}

}
