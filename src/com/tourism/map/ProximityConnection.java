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
import org.apache.http.util.EntityUtils;

import com.tourism.map.exceptions.ProximityLoadException;

import android.os.AsyncTask;
import android.util.Log;
public class ProximityConnection extends AsyncTask<Void, Void, String> {
	private static final String TAG = "com.tourism.map.ProximityConnection";
	
	private static final String URL = "http://tourismtour.esy.es//coordinates.php";
	private ProximityLoadException mProxException = new ProximityLoadException("Fatal unexpected Proximity exception");
	
	public ProximityLoadException getProxException() {
		return this.mProxException;
	}
	
	@Override
	protected String doInBackground(Void... args) {
		try {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(URL);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity, "ISO-8859-1");
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.toString());
			mProxException = new ProximityLoadException(e.getMessage());
			return null;
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			mProxException = new ProximityLoadException(e.getMessage());
			return null;
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			mProxException = new ProximityLoadException(e.getMessage());
			return null;
		}
	}
}
