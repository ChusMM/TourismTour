package com.tourism.map;

import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;

public class ServerConnection extends AsyncTask<String,String,String>{

	private float latitude;
	private float longuitude;
	
	public ServerConnection(float lat, float lon) throws Exception
	{
		latitude = lat;
		longuitude = lon;
	}
	
	@Override
	protected String doInBackground(String... params) {
		 try
		    {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	    	nameValuePairs.add(new BasicNameValuePair("PosY", (Float.valueOf(latitude)).toString()));
	    	nameValuePairs.add(new BasicNameValuePair("PosX", (Float.valueOf(longuitude)).toString()));
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpPost httppost = new HttpPost("http://tourapp.zz.mu//main.php");
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        HttpResponse response = httpclient.execute(httppost);
	        HttpEntity entity = response.getEntity();
	        return EntityUtils.toString(entity);
	    }
		 catch(Exception e){
			 return "Network problem";
		 }
	}
}
