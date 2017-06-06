package socket.adaptor.j0504;

import socket.adaptor.j0504.func.*;

import java.text.DecimalFormat;
import java.util.*;

import org.json.JSONObject;
import org.apache.log4j.Logger;  
import org.apache.log4j.PropertyConfigurator;

public class Datas {

	CRC checker = new CRC();	
	Logger logger = Logger.getLogger(Datas.class); 

	public boolean lead_check_data(String[] check_data){

		boolean check=true;
		String lead_data=check_data[0]+check_data[1];
		if(!lead_data.equals("7878") && !lead_data.equals("7979")){
			check=false;
			logger.warn("[Lead data Fail]:"+lead_data);}

	return check;}

	public boolean crc_check_data(String[] check_data){

		boolean check=true;
		String crc_data=check_data[check_data.length-2]+check_data[check_data.length-1];
		String verify_crc=checker.crc_general(Arrays.copyOfRange(check_data,0,check_data.length-2));
		if(!crc_data.equals(verify_crc)){
			check=false;
			logger.warn("[CRC data Fail]:"+crc_data);}

	return check;}

	public String[] common_data(String[] common_data){

		String deviceID_data=common_data[0]+common_data[1]+common_data[2]+common_data[3]+common_data[4]+common_data[5]+common_data[6]+common_data[7];
		String imsi_data=common_data[8]+common_data[9]+common_data[10]+common_data[11]+common_data[12]+common_data[13]+common_data[14]+common_data[15];
		String deviceModel_data=common_data[16]+common_data[17];
		String protocol=common_data[18];

		String deviceID=String.valueOf(Long.parseLong(deviceID_data));

		String[] head={deviceID,deviceModel_data,imsi_data,"DEFAULT_AdaptorID","DEFAULT_SocketID"};

	return head;}

	public String time_data(String[] time_data){

		String date=time_data[0]+time_data[1]+time_data[2]+time_data[3]+time_data[4]+time_data[5];
		//time,並未使用
		String NoUse=null;

	return NoUse;}

	public String field_data(String[] field_data){

		String field=field_data[0]+field_data[1];
		//Language,並未使用
		String NoUse=null;

	return NoUse;}

	public JSONObject gps_data(String[] gps_data, JSONObject jsonObj_gps_data){

		String satellites=gps_data[0];
		String lat_data=gps_data[1]+gps_data[2]+gps_data[3]+gps_data[4];
		String lng_data=gps_data[5]+gps_data[6]+gps_data[7]+gps_data[8];
		String speed_data=gps_data[9];
		String course=gps_data[10]+gps_data[11];
		//換算成座標
		double lat_ori=((double)Integer.parseInt(lat_data,16))/30000/60;
		double lng_ori=((double)Integer.parseInt(lng_data,16))/30000/60;
		//定格式 取小數點第六
		DecimalFormat gps_fromat=new DecimalFormat("##.000000");
		//轉換為格式 並且在轉為字串
		String lat=String.valueOf(Double.parseDouble(gps_fromat.format(lat_ori)));
		String lng=String.valueOf(Double.parseDouble(gps_fromat.format(lng_ori)));
		String speed=String.valueOf(Integer.parseInt(speed_data,16));
		try {
			jsonObj_gps_data.put("Lat", lat);
			jsonObj_gps_data.put("Lng", lng);
			jsonObj_gps_data.put("Speed", speed);
			jsonObj_gps_data.put("LocationType", "0");
		}catch(Exception e){
			logger.warn("Error: " + e.getMessage());}

	return jsonObj_gps_data;}

	public JSONObject device_data(String[] device_data, JSONObject jsonObj_device_data){

		String deviceStatus_data=device_data[0];
		String voltageLevel_data=device_data[1];
		String gsmLevel_data=device_data[2];

		//16轉2並補位  接著轉10
		String deviceStatus=String.valueOf(Integer.parseInt(String.format("%08d",Integer.parseInt(Integer.toBinaryString(Integer.parseInt(deviceStatus_data,16)))).substring(2,5),2));
		String voltageLevel=String.valueOf(Integer.parseInt(voltageLevel_data,16));
		String gsmLevel=String.valueOf(Integer.parseInt(gsmLevel_data,16));

		try {
			jsonObj_device_data.put("DeviceStatus", deviceStatus);
			jsonObj_device_data.put("VoltageLevel", voltageLevel);
			jsonObj_device_data.put("GSMLevel", gsmLevel);
		}catch(Exception e){
			logger.warn("Error: " + e.getMessage());}
 
	return jsonObj_device_data;}

	public JSONObject warn_data(String[] warn_data, JSONObject jsonObj_warn_data){

		String warning_data=warn_data[0];
		String geofenceID_data=warn_data[1];

		String warning=String.valueOf(Integer.parseInt(warning_data));
		String geofenceID= geofenceID_data.substring(0,1);

		try {
			jsonObj_warn_data.put("Warning", warning);
			jsonObj_warn_data.put("GeoFenceID", geofenceID);
		}catch(Exception e){
			logger.warn("Error: " + e.getMessage());}
 
	return jsonObj_warn_data;}

	public JSONObject lbs_data(String[] lbs_data, JSONObject jsonObj_lbs_data){

		String mcc_data=lbs_data[0]+lbs_data[1];
		String mnc_data=lbs_data[2];
		String lac_data=lbs_data[3]+lbs_data[4];
		String ci_data=lbs_data[5]+lbs_data[6]+lbs_data[7];

		String mcc=String.valueOf(Integer.parseInt(mcc_data,16));
		String mnc=String.valueOf(Integer.parseInt(mnc_data,16));
		String lac=String.valueOf(Integer.parseInt(lac_data,16));
		String ci=String.valueOf(Integer.parseInt(ci_data,16));

		List<JSONObject> lbsList= new LinkedList<JSONObject>();
		JSONObject jsonObj_lbs = new JSONObject();

		try {
			jsonObj_lbs.put("MCC", mcc);
			jsonObj_lbs.put("MNC", mnc);
			jsonObj_lbs.put("LAC", lac);
			jsonObj_lbs.put("CI", ci);
			lbsList.add(jsonObj_lbs);
			jsonObj_lbs_data.put("LBS", lbsList);

		}catch(Exception e){
			logger.warn("Error: " + e.getMessage());}

	return jsonObj_lbs_data;}

	public JSONObject lbs_array_data(String[] lbs_array_data, JSONObject jsonObj_lbs_array_data){

		String mcc_data=lbs_array_data[0]+lbs_array_data[1];
		String mnc_data=lbs_array_data[2];
		String lac_data=lbs_array_data[3]+lbs_array_data[4];
		String ci_data=lbs_array_data[5]+lbs_array_data[6]+lbs_array_data[7];
		String rssi_data=lbs_array_data[8];

		String mcc=String.valueOf(Integer.parseInt(mcc_data,16));
		String mnc=String.valueOf(Integer.parseInt(mnc_data,16));
		String lac=String.valueOf(Integer.parseInt(lac_data,16));
		String ci=String.valueOf(Integer.parseInt(ci_data,16));
		String rssi=String.valueOf(Integer.parseInt(rssi_data,16));

		List<JSONObject> lbsList= new LinkedList<JSONObject>();
		JSONObject jsonObj_lbs_big = new JSONObject();

		try {
			jsonObj_lbs_big.put("MCC", mcc);
			jsonObj_lbs_big.put("MNC", mnc);
			jsonObj_lbs_big.put("LAC", lac);
			jsonObj_lbs_big.put("CI", ci);
			jsonObj_lbs_big.put("RSSI", rssi);
			lbsList.add(jsonObj_lbs_big);
			lbsList.add(lbs_small_data(Arrays.copyOfRange(lbs_array_data,8,14)));
			lbsList.add(lbs_small_data(Arrays.copyOfRange(lbs_array_data,14,20)));
			lbsList.add(lbs_small_data(Arrays.copyOfRange(lbs_array_data,20,26)));
			lbsList.add(lbs_small_data(Arrays.copyOfRange(lbs_array_data,26,32)));
			lbsList.add(lbs_small_data(Arrays.copyOfRange(lbs_array_data,32,38)));
			lbsList.add(lbs_small_data(Arrays.copyOfRange(lbs_array_data,38,44)));
			jsonObj_lbs_array_data.put("LBS", lbsList);

		}catch(Exception e){
			logger.warn("Error: " + e.getMessage());}

	return jsonObj_lbs_array_data;}

	public JSONObject lbs_small_data(String[] lbs_small_data){

		String nlac_data=lbs_small_data[0]+lbs_small_data[1];
		String nci_data=lbs_small_data[2]+lbs_small_data[3]+lbs_small_data[4];
		String nrssi_data=lbs_small_data[5];

		String nlac=String.valueOf(Integer.parseInt(nlac_data,16));
		String nci=String.valueOf(Integer.parseInt(nci_data,16));
		String nrssi=String.valueOf(Integer.parseInt(nrssi_data,16));

		JSONObject jsonObj_lbs_small = new JSONObject();

		try {
			jsonObj_lbs_small.put("LAC", nlac);
			jsonObj_lbs_small.put("CI", nci);
			jsonObj_lbs_small.put("RSSI", nrssi);


		}catch(Exception e){
			logger.warn("Error: " + e.getMessage());}

	return jsonObj_lbs_small;}

	public JSONObject config_data(String[] config_data, JSONObject jsonObj_config_data){

		String identifier_data=config_data[0]+config_data[1]+config_data[2]+config_data[3];
		String[] instruction_data=Arrays.copyOfRange(config_data,4,config_data.length);
		//ASCII編碼還原
		String instruction=String.valueOf((char)Integer.parseInt(instruction_data[0],16));
		for(int i=1;i<instruction_data.length;i++){
			instruction=instruction+(char)Integer.parseInt(instruction_data[i],16);}

		try {
			jsonObj_config_data.put("Identifier", identifier_data);
			jsonObj_config_data.put("Instruction", instruction);

		}catch(Exception e){
			logger.warn("Error: " + e.getMessage());}

	return jsonObj_config_data;}
}
