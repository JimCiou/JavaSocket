package socket.adaptor.j0504;

import socket.adaptor.j0504.system.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;  
import org.apache.log4j.PropertyConfigurator;

public class Backend {

	String url_default = null;

	Property propuser = new Property();
	Logger logger = Logger.getLogger(Backend.class); 

	// Read Config file
	public void readConfig(String properties) {
		this.url_default = propuser.loadproperties(properties,"BackendURL");
	}

	// HTTP GET request
	public void sendGet(String[] head, String url) {

		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url_default+url);

		// add request header
		request.addHeader("DeviceID", head[0]);
		request.addHeader("DeviceModel", head[1]);
		request.addHeader("IMSI", head[2]);
		request.addHeader("AdaptorID", head[3]);
		request.addHeader("Socket", head[4]);

		try {
			HttpResponse response = client.execute(request);

			logger.warn("Update to Backend:[URL]------------------------------------------------");
			logger.warn("Sending 'GET' request to: " + url_default+url);
			logger.warn("=======================================================================");
			logger.warn("Response from Backend:[URL]--------------------------------------------");
			logger.warn("Response Code : "+response.getStatusLine().getStatusCode());

			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();

			String line = "";
			while ((line = rd.readLine()) != null) {result.append(line);}

			logger.warn(result.toString());
			logger.warn("=======================================================================");

		}catch(Exception e){
			logger.warn("Error: " + e.getMessage());}
	}

	// HTTP POST request
	public void sendPost(String[] head, String body, String url) {

		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url_default+url);

		// add header
		post.setHeader("DeviceID", head[0]);
		post.setHeader("DeviceModel", head[1]);
		post.setHeader("IMSI", head[2]);
		post.setHeader("AdaptorID", head[3]);
		post.setHeader("Socket", head[4]);

		try {
			StringEntity params =new StringEntity(body);
			post.setEntity(params);
			post.setHeader("Content-type","application/json");

			HttpResponse response = client.execute(post);

			logger.warn("Update to Backend:[URL]------------------------------------------------");
			logger.warn("Sending 'POST' request to: " + url_default+url);
			logger.warn("=======================================================================");
//			logger.warn("Post parameters : " + post.getEntity());
			logger.warn("Response from Backend:[URL]--------------------------------------------");
			logger.warn(response.getStatusLine().getStatusCode());

			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();

			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			logger.warn(result.toString());
			logger.warn("=======================================================================");

		}catch(Exception e){
			logger.warn("Error: " + e.getMessage());}
	}

	// HTTP PUT request
	public void sendPut(String[] head, String body, String url) {

		HttpClient client = new DefaultHttpClient();
		HttpPut put = new HttpPut(url_default+url);

		// add header
		put.setHeader("DeviceID", head[0]);
		put.setHeader("DeviceModel", head[1]);
		put.setHeader("IMSI", head[2]);
		put.setHeader("AdaptorID", head[3]);
		put.setHeader("Socket", head[4]);

		try {
			StringEntity params =new StringEntity(body);
			put.setEntity(params);
			put.setHeader("Content-type","application/json");

			HttpResponse response = client.execute(put);

			logger.warn("Update to Backend:[URL]------------------------------------------------");
			logger.warn("Sending 'PUT' request to: " + url_default+url);
//			logger.warn("Put parameters : " + put.getEntity());
			logger.warn("=======================================================================");
			logger.warn("Response from Backend:[URL]--------------------------------------------");
			logger.warn(response.getStatusLine().getStatusCode());

			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();

			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			logger.warn(result.toString());
			logger.warn("=======================================================================");
		}catch(Exception e){
			logger.warn("Error: " + e.getMessage());}
	}
}
