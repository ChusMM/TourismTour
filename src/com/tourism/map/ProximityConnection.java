package com.tourism.map;

import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;


public class ProximityConnection extends AsyncTask<String,String,String>
{
	
	@Override
	protected String doInBackground(String... params) {
	 try
	    {
		 	ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		 	HttpClient httpclient = new DefaultHttpClient();
	        HttpPost httppost = new HttpPost("http://tourapp.zz.mu//coordinates.php");
	        httppost.setEntity(new UrlEncodedFormEntity(
	        nameValuePairs));
	        HttpResponse response = httpclient.execute(httppost);
	        HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity,"ISO-8859-1");
	     }
	     catch(Exception e){
	         return "Network problem";
	     }
	
	}
}
