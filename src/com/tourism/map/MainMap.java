package com.tourism.map;


import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentFilter;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.MapActivity;

public class MainMap extends MapActivity implements LocationListener ,TextToSpeech.OnInitListener
{
	private MapView mapView;
	private MapController mc;
	private LocationManager locationManager;
	private MyLocationOverlay locationOverlay;
	private TextView longitude;
	private TextView latitude;
	private TextToSpeech tts;
	private String placeTitle = "";
	private String placeDescription;
	private String imageDescription;
	private LocationListener locationListenerGps;
	private LocationListener locationListenerNet;
	private LinkedList<ProximityIntentReceiver> recieverList;
	private Dialog dialog;
	private boolean wifichanged;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		createMap();
		recieverList = new LinkedList<ProximityIntentReceiver>();
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		tts = new TextToSpeech(this, this);
		latitude = (TextView)findViewById(R.id.latitude_value);
		longitude = (TextView)findViewById(R.id.longitude_value);
		wifichanged = false;
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled() == false)
        {
            wifi.setWifiEnabled(true);
            wifichanged = true;
        }   
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		}
		setLocationProvider();
		setLocationinMap();
		createProximityIntents();
		getCoordinates();
		mapView.postInvalidate();
        zoomToMyLocation(); // Zoom in the map to the current location
	}
	
	private void createProximityIntents(){
		
		ProximityConnection prox;
		String x = null;
		String y = null;
		String radius = null;
		String title = null;
		String description = null;
		try {
			prox = new ProximityConnection();
			prox.execute();
			String descr = prox.get();
		    StringBuffer buf = new StringBuffer();
		    Matcher m = Pattern.compile("\\\\u([0-9A-Fa-f]{4})").matcher(descr);
		    while (m.find()) {
		        try {
		            int cp = Integer.parseInt(m.group(1), 16);
		            m.appendReplacement(buf, "");
		            buf.appendCodePoint(cp);
		        } catch (NumberFormatException e) {
		        }
		    }
		    m.appendTail(buf);
		    descr = buf.toString();
			descr = descr.replaceAll("\\[", "");
			descr = descr.replaceAll("\\]", "");
			String [] d = descr.split("\\},\\{");
			for(int i = 0;i<d.length;i++){
				d[i] = d[i].replaceAll("\\{","");
				d[i] = d[i].replaceAll("\\}","");
				String [] values = d[i].split(",");
				x = values[0].replaceAll("\"","").split(":")[1];
				y = values[1].replaceAll("\"","").split(":")[1];
				radius = values[2].replaceAll("\"","").split(":")[1];
				title = values[3].replaceAll("\"","").split(":")[1];
				description = values[4].replaceAll("\"","").split(":")[1];
				System.out.println("Description: " + description);
				System.out.println("Description Lenght: " + description.length());
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				intent.putExtra("title",title);
				intent.putExtra("description", description);
				PendingIntent proximityIntent = PendingIntent.getBroadcast(this, i, intent, PendingIntent.FLAG_CANCEL_CURRENT);
				locationManager.addProximityAlert(Double.parseDouble(y), Double.parseDouble(x), Float.parseFloat(radius), -1, proximityIntent);
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Excepcion: " + e.getMessage());
		}
		IntentFilter filter = new IntentFilter(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		ProximityIntentReceiver reciever = new ProximityIntentReceiver();
		registerReceiver(reciever,filter);
		recieverList.add(reciever);
	}
	
	
	@Override
	protected void onDestroy() {
	    //Close the Text to Speech Library
		for(int i = 0;i<recieverList.size();i++){
			unregisterReceiver(recieverList.get(i));
		}
		String provider = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if(provider.contains("gps") && provider.contains("net")){ //if gps is enabled
			locationManager.removeUpdates(locationListenerGps);
			locationManager.removeUpdates(locationListenerNet);
			Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
			intent.putExtra("enabled", false);
			sendBroadcast(intent);
	    }
		if(wifichanged){
			WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			wifi.setWifiEnabled(false);
		}
	    if(tts != null) {
	        tts.stop();
	        tts.shutdown();
	    }
	    super.onDestroy();
	}
	
	private void createMap()
	{
		setContentView(R.layout.mainmap);
		mapView = (MapView) findViewById (R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mc = mapView.getController();
	}
	
	@Override
    public void onBackPressed() {
		moveTaskToBack(true);
	}
	
	private void setLocationProvider()
	{
		Criteria crit = new Criteria();
		crit.setAccuracy(Criteria.ACCURACY_FINE);
		locationListenerNet = locationListenerGps = new LocationListener() {
			public void onLocationChanged(Location arg0) {
				newLocation(arg0);
			}

			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub
				
			}
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 15000, 0,locationListenerNet, Looper.myLooper());
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 0, locationListenerGps,Looper.myLooper());
	}
	
	private void setLocationinMap()
	{
		locationOverlay= new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(locationOverlay);
		locationOverlay.enableMyLocation();
	}
	
	private void getCoordinates()
	{
		
		Location lastKnownLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastKnownLoc != null){
		  int longTemp = (int)(lastKnownLoc.getLongitude()* 1000000);
		  int latTemp = (int)(lastKnownLoc.getLatitude() * 1000000);
		  mc.animateTo(new GeoPoint(latTemp, longTemp));
		  String lat = Double.valueOf(lastKnownLoc.getLatitude()).toString();
		  String len = Double.valueOf(lastKnownLoc.getLongitude()).toString();
		  latitude.setText(lat.substring(0,Math.min(9, lat.length())));
		  longitude.setText(len.substring(0,Math.min(9, len.length())));
		}
	}
	
    /**
     * This method zooms to the user's location with a zoom level of 10.
     */
    private void zoomToMyLocation() {
            GeoPoint myLocationGeoPoint = locationOverlay.getMyLocation();
            if(myLocationGeoPoint != null) {
                    mapView.getController().animateTo(myLocationGeoPoint);
                    mapView.getController().setZoom(10);
            }
            else {
                    Toast.makeText(this, "Cannot determine location", Toast.LENGTH_SHORT).show();
            }
    }
	

	 /** Register for the updates when Activity is in foreground */
    @Override
    protected void onResume() 
    {
        super.onResume();
        locationOverlay.enableMyLocation();
    }
    
    public void onLocationChanged(Location loc) 
	{
    	newLocation(loc);
	    
	}
    
    private void newLocation(Location loc)
    {
    	try{
	    	if (!tts.isSpeaking() && loc != null) { // If the system is not reproducing the description of the place and a location is gotten.
	    		String lat = Double.valueOf(loc.getLatitude()).toString();
	  		  	String len = Double.valueOf(loc.getLongitude()).toString();
	  		  	latitude.setText(lat.substring(0,Math.min(9, lat.length())));
	  		  	longitude.setText(len.substring(0,Math.min(9, len.length())));
	            GeoPoint p = new GeoPoint((int) (loc.getLatitude() * 1E6), (int) (loc.getLongitude() * 1E6));
	            ServerConnection conn = new ServerConnection((float)loc.getLatitude(),(float)loc.getLongitude());
	            conn.execute();
	            String d = conn.get();
	            d = d.replaceAll("null", "\"\"");
	            String[] array = d.split("\",");
	            array[0] = array[0].substring(1);
	            for(int i = 0;i<array.length;i++) {
	            	array[i] = array[i].replaceAll("\"","");
	            }
	        	if(!array[0].equals(placeTitle) && !array[0].equals("")) {
	        		placeTitle = array[0];
	        		placeTitle.toUpperCase();
	        		placeDescription = array[1];
	        		imageDescription = array[3];
	        		sendVibration();
	        		acceptRequest(this.getCurrentFocus());
	        	}
	        	updateMap(p);
	        }
	    }
	    catch(Exception e) {
	    	
	    }
    }
    
    private void updateMap(GeoPoint p)
    {
        mc.animateTo(p);
        mc.setZoom(250);
        mapView.invalidate();
    }
    
    private void sendVibration()
    {
    	NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); 
    	Notification n = new Notification();
    	n.vibrate = new long[]{100, 200, 100, 200,100,200,100,200,100,200,100,200,100,200,100,200,100,200,100,200}; 
    	nManager.notify(0, n);
    }
    
    public void acceptRequest(View view) {
    	if(dialog != null)
    		dialog.dismiss();
    	dialog = new Dialog(this);
    	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	dialog.setCancelable(true);
    	dialog.setContentView(R.layout.requestdialog);
    	Button accept = (Button) dialog.findViewById(R.id.accept);
    	TextView des = (TextView)dialog.findViewById(R.id.description);
    	setPlaceImage(dialog);
    	des.setText(placeTitle);
    	accept.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				tts.speak(placeDescription, TextToSpeech.QUEUE_FLUSH, null);
				while(tts.isSpeaking()){}
				dialog.dismiss();
			}
		});
    	Button cancel = (Button) dialog.findViewById(R.id.cancel);
    	cancel.setOnClickListener(new OnClickListener() {
           public void onClick(View v) {
        	   dialog.dismiss();
           }
       	});
		dialog.show();
    }
    
    private void setPlaceImage(Dialog dialog)
    {
    	byte[] decodedString = Base64.decode(imageDescription, Base64.DEFAULT);
    	ImageView image = (ImageView)dialog.findViewById(R.id.placeImage);
    	image.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
    	image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {

      super.onConfigurationChanged(newConfig);
      if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
          //your code
      } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
         //your code
      }
    }

	public void onProviderDisabled(String provider) {
	    // TODO Auto-generated method stub
		System.out.println("PASA POR ONPROVIDERDISABLED");

	}

	public void onProviderEnabled(String provider) {
	    // TODO Auto-generated method stub
	}

	public void onStatusChanged(String provider, int status,
	                Bundle extras) 
	{
	    // TODO Auto-generated method stub
	}        

    /** Stop the updates when Activity is paused */
    @Override
    protected void onPause() 
    {
        super.onPause();
        locationOverlay.disableMyLocation();
    }
	
	@Override
	protected boolean isRouteDisplayed()
	{
		return true;
	}

	public void onInit(int arg0) {
		// TODO Auto-generated method stub
		
	}
	
	//para mostrar el menú de la aplicación
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }
    
    //código para cada opción de menú
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId()) 
        {
            case R.id.exit:
            	finish();
        }
        return true;
    }
}
