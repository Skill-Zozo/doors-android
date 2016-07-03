package com.doors.thegrid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Connector {
	private CloseableHttpClient httpclient;

	public Connector() {
		
		httpclient = HttpClients.createDefault();
		
	}

	public String login(String usr, String pwd) {
		/**
		 * sends login request to server with 
		 * username=usr, password = pwd
		 * returns a negative message if there is no such user
		 * */
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("type", "login"));
		nvps.add(new BasicNameValuePair("username", usr));
		nvps.add(new BasicNameValuePair("password", pwd));
		JSONObject json = sendToServer(nvps);
		try {
			JSONArray jArray = (JSONArray) json.get("state");
			String state = (String) jArray.get(0);
			return state;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "failed for reasons unknown";
	}

	public Profile getUser(String usr, String pwd) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("type", "pull_data"));
		nvps.add(new BasicNameValuePair("username", usr));
		nvps.add(new BasicNameValuePair("password", pwd));
		JSONObject json = sendToServer(nvps);
		System.out.println(json);
		try {
			JSONArray json_block = (JSONArray)json.get("user");
			String response = (String) json_block.get(0);
			Profile user = buildUserData(usr,response);
			return user;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	

	private Profile buildUserData(String name, String arr) {
		Profile user = new Profile(name);
		String[] dbEntries = arr.split(";;");
		for(int i = 0; i < dbEntries.length; i++) {
			String[] attributes = dbEntries[i].split(",");
			Location loc;
			if(user.hasLocation(attributes[1])) {
				loc = user.getLocation(attributes[1]);
			} else {
				loc = new Location(user, attributes[1]);
			}
			Room room;
			if(loc.hasRoom(attributes[2])) {
				room = loc.getRoom(attributes[2]);
			} else {
				room = new Room(attributes[2], loc);
			}
			new Device(attributes[3], room);
		}
		return user;
	}

	private JSONObject sendToServer(List<NameValuePair> nvps) {
		JSONObject json = null;
		HttpEntity entity = null;
		try {
			HttpPost httpPost = new HttpPost(
					"http://localhost:8080/testshit/Sever");
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			CloseableHttpResponse response = httpclient.execute(httpPost);
			entity = response.getEntity();
			String content = EntityUtils.toString(entity, "UTF-8");
			json = new JSONObject(content);
			EntityUtils.consume(entity);
			response.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				EntityUtils.consume(entity);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return json;
	}
	
	public String addDeviceTo(String user, String pwd, Device dev) {
		Room room = dev.getParentRoom();
		Location loc = room.getPlace();
		List<NameValuePair> nvps = new ArrayList<>();
		nvps.add(new BasicNameValuePair("user", user));
		nvps.add(new BasicNameValuePair("password", pwd));
		nvps.add(new BasicNameValuePair("type", "add"));
		nvps.add(new BasicNameValuePair("device", dev.getNameofDevice()));
		nvps.add(new BasicNameValuePair("room", room.getName()));
		nvps.add(new BasicNameValuePair("location", loc.getName()));
		JSONObject json = sendToServer(nvps);
		JSONArray jArray;
		try {
			jArray = (JSONArray) json.get("state");
			String status = (String)jArray.get(0);
			return status;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "failed";
	}
	
	public String signup(String usr, String pwd) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("type", "signup"));
		nvps.add(new BasicNameValuePair("username", usr));
		nvps.add(new BasicNameValuePair("password", pwd));
		JSONObject json = sendToServer(nvps);
		JSONArray status_array;
		try {
			status_array = (JSONArray)json.get("state");
			return (String)(status_array.get(0));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "failed";
	}

	public static void main(String[] args) {
		Connector conn = new Connector();
		Profile user = new Profile("dev1");
		Location loc = new Location(user, "Work");
		Room room = new Room("office", loc);
		conn.addDeviceTo(user.getName(), "work", new Device("lamp", room));
	}
}