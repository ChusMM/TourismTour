package com.tourism.map;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.tourism.map.exceptions.ServerConnectionException;

import android.os.AsyncTask;
import android.util.Log;

public class ServerConnection extends AsyncTask<String,String,String>{
	private static final String TAG = "com.tourism.map.ServerConnection";
	private static final String URL = "http://tourapp.esy.es//main.php";
	
	private float latitude;
	private float longuitude;
	private ServerConnectionException mServException = new ServerConnectionException("Fatal unexpected Proximity exception");
	
	public ServerConnection(float lat, float lon) {
		this.latitude = lat;
		this.longuitude = lon;
	}
	
	public ServerConnectionException getServException() {
		return this.mServException;
	}
	
	@Override
	protected String doInBackground(String... params) {
		try {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("PosY", (Float.valueOf(latitude)).toString()));
			nameValuePairs.add(new BasicNameValuePair("PosX", (Float.valueOf(longuitude)).toString()));

			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(URL);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity);
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.toString());
			mServException = new ServerConnectionException(e.getMessage());
			return null;
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			mServException = new ServerConnectionException(e.getMessage());
			return null;
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			mServException = new ServerConnectionException(e.getMessage());
			return null;
		}
	}
}
