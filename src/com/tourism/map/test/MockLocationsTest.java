package com.tourism.map.test;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.widget.TextView;

public class MockLocationsTest extends AsyncTask<String,String,String>{

	private Location mockLocation;
	private MapView map;
	private MapController mc;
	private TextView latitude;
	private TextView longitude;
	private GeoPoint p;
	
	public MockLocationsTest(Location loc, MapView mapView,MapController mapController,TextView lat, TextView lon){
		map = mapView;
		mc = mapController;
		latitude = lat;
		longitude = lon;
		mockLocation = loc;
	}
	
	public GeoPoint getPoint(){
		return p;
	}
	
	@Override
	protected String doInBackground(String... arg0) {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		getCoordinates(mockLocation);
		mc.animateTo(p);
		mc.setZoom(10);
		return "";
	}
	@Override
	protected void onPostExecute(String arg){
		map.invalidate();
	}
	
	@Override
    protected void onProgressUpdate(String... values) {
        map.invalidate();
    }
	
	private void getCoordinates(Location mockLocation)
	{
		  int longTemp = (int)(mockLocation.getLongitude()* 1000000);
		  int latTemp = (int)(mockLocation.getLatitude() * 1000000);
		  p = new GeoPoint(latTemp, longTemp);
		  //mc.setZoom(10);
		  String lat = Double.valueOf(mockLocation.getLatitude()).toString();
		  String len = Double.valueOf(mockLocation.getLongitude()).toString();
		  latitude.setText(lat.substring(0,Math.min(9, lat.length())));
  		  longitude.setText(len.substring(0,Math.min(9, len.length())));
	}
}
