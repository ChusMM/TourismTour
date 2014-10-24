package com.tourism.map;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.MapActivity;
import com.tourism.map.exceptions.PlaceBuilderException;
import com.tourism.map.exceptions.ProximityLoadException;
import com.tourism.map.exceptions.ServerConnectionException;

public class MainMap extends MapActivity implements LocationListener ,TextToSpeech.OnInitListener, MapFinals, DialogInterface.OnClickListener, DialogInterface.OnDismissListener {
	private static String TAG = "com.tourism.map.MainMap";

	private MapView mapView;
	private MapController mapController;
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
	private List<ProximityReceiver> recieverList;
	private ListenDialog listenDialog;
	private boolean wifichanged;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainmap);

		createMap();
		recieverList = new ArrayList<ProximityReceiver>();
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		tts = new TextToSpeech(this, this);

		latitude = (TextView) findViewById(R.id.latitude_value);
		longitude = (TextView) findViewById(R.id.longitude_value);

		wifichanged = false;
		WifiManager wifiMan = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		//TODO: Action @Oscar:  Por qué forzamos a habilitar el wifi?
		//TODO: Answer:
		if (wifiMan.isWifiEnabled() == false)
		{
			wifiMan.setWifiEnabled(true);
			wifichanged = true;
		}

		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		}
		
		try {
			setLocationProvider();
			setMapLocation();
			createProximityIntents();
			getCoordinates();
			mapView.postInvalidate();
			zoomToMyLocation(); 
		} catch (CancellationException e) {
			Log.e(TAG, e.toString());
		} catch (ExecutionException	e) {
			Log.e(TAG, e.toString());
		} catch (InterruptedException e	) {
			Log.e(TAG, e.toString());
		} catch (IllegalStateException	e) {
			Log.e(TAG, e.toString());
		}catch (NullPointerException e) {
			Log.e(TAG, e.toString());
		} catch (ProximityLoadException e) {
			Log.e(TAG, e.toString());
		} catch (PlaceBuilderException e) {
			Log.e(TAG, e.toString());
		}
	}
	
	/** Register for the updates when Activity is in foreground */
	@Override
	protected void onResume() {
		super.onResume();
		locationOverlay.enableMyLocation();
	}

	/** Stop the updates when Activity is paused */
	@Override
	protected void onPause() {
		super.onPause();
		locationOverlay.disableMyLocation();
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
	
	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) { }
		else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) { }
	}        

	private void createMap() {
		mapView = (MapView) findViewById (R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapController = mapView.getController();
	}


	private void createProximityIntents() throws ExecutionException, InterruptedException, ProximityLoadException, IllegalStateException, NullPointerException, PlaceBuilderException {

		ProximityConnection proxConn = new ProximityConnection();
		proxConn.execute();
		// Consejo
		// Poco sentido tiene hacer una asyncTask  para luego bloquearen el hilo principal hasta que termina la ejecución
		String descr = proxConn.get();
		if (descr == null) {
			throw proxConn.getProxException();
		}

		StringBuffer sb = new StringBuffer();
		Matcher match = Pattern.compile("\\\\u([0-9A-Fa-f]{4})").matcher(descr);
		while (match.find()) {
			try {
				int cp = Integer.parseInt(match.group(1), 16);
				match.appendReplacement(sb, "");
				sb.appendCodePoint(cp);
			} catch (NumberFormatException e) {
				Log.e(TAG, e.toString());
				// TODO: Action Chus & Oscar. int cp = DefaultLocationOnError ?????
			}
		}
		match.appendTail(sb);

		descr = sb.toString();
		descr = descr.replaceAll(OPEN_SQUAREBK, "");
		descr = descr.replaceAll(CLOSED_SQUAREBK, "");
		String [] d = descr.split(OPEN_BRACKET + "," + CLOSED_BRACKET);

		for (int i = 0; i < d.length; i ++) {
			d[i] = d[i].replaceAll(OPEN_BRACKET, "");
			d[i] = d[i].replaceAll(CLOSED_BRACKET, "");

			String [] values = d[i].split(",");
			Place place = new Place(values);

			Log.i(TAG,"Description: " + place.getDescription());
			Log.i(TAG, "Description Lenght: " + place.getDescription().length());

			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			intent.putExtra(ProximityReceiver.TITLE, place.getTitle());
			intent.putExtra(ProximityReceiver.DESCRIPTION, place.getDescription());

			PendingIntent proximityIntent = PendingIntent.getBroadcast(this, i, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			locationManager.addProximityAlert(place.getY(), place.getX(), place.getRadius(), -1, proximityIntent);
		}

		IntentFilter filter = new IntentFilter(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		ProximityReceiver reciever = new ProximityReceiver();
		registerReceiver(reciever, filter);
		recieverList.add(reciever);
	}

	private void setLocationProvider() {
		// TODO: @Oscar: Me parece que sobra esto de Criteria, es local a la función. No lo necesitan ni los listeners ni el manager 
		Criteria crit = new Criteria();
		crit.setAccuracy(Criteria.ACCURACY_FINE);

		locationListenerNet = locationListenerGps = new TourLocationListener();
		/*new LocationListener() {
			public void onLocationChanged(Location newLoc) {
				newLocation(newLoc);
			}

			public void onProviderDisabled(String provider) { }

			public void onProviderEnabled(String provider) { }

			public void onStatusChanged(String provider, int status, Bundle extras) { }
        };*/
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 15000, 0, locationListenerNet, Looper.myLooper());
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 0, locationListenerGps, Looper.myLooper());
	}

	// TODO: @Oscar: Nuevo  locationListener
	private class TourLocationListener implements LocationListener {
		public void onLocationChanged(Location newLoc) {
			try {
				newLocation(newLoc);
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IndexOutOfBoundsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServerConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void onProviderDisabled(String provider) { }

		public void onProviderEnabled(String provider) { }

		public void onStatusChanged(String provider, int status, Bundle extras) { } 
	}

	private void setMapLocation() {
		locationOverlay = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(locationOverlay);
		locationOverlay.enableMyLocation();
	}

	private void getCoordinates() {
		Location lastKnownLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastKnownLoc != null) {
			int longTemp = (int) (lastKnownLoc.getLongitude() * 1000000);
			int latTemp  = (int) (lastKnownLoc.getLatitude()  * 1000000);
			mapController.animateTo(new GeoPoint(latTemp, longTemp));

			String lat = Double.valueOf(lastKnownLoc.getLatitude()).toString();
			String len = Double.valueOf(lastKnownLoc.getLongitude()).toString();

			latitude.setText(Util.formatCoordinate(lat));
			longitude.setText(Util.formatCoordinate(len));
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

	public void onLocationChanged(Location loc) {
		try {
			newLocation(loc);
		} catch(NullPointerException e) {
			// TODO: Action @Oscar
		} catch (IndexOutOfBoundsException e) {
			
		} catch (InterruptedException e) {
			
		} catch (ExecutionException e) {
			
		} catch (ServerConnectionException e) {
			
		}
	}

	public void onProviderDisabled(String provider) {
		System.out.println("PASA POR ONPROVIDERDISABLED");
	}

	public void onProviderEnabled(String provider) { }

	public void onStatusChanged(String provider, int status, Bundle extras) { }

	private void newLocation(Location loc) throws NullPointerException, IndexOutOfBoundsException, InterruptedException, ExecutionException, ServerConnectionException {
		if (!tts.isSpeaking() && loc != null) { // If the system is not reproducing the description of the place and a location is gotten.
			String lat = Double.valueOf(loc.getLatitude()).toString();
			String len = Double.valueOf(loc.getLongitude()).toString();
			latitude.setText(Util.formatCoordinate(lat));
			longitude.setText(Util.formatCoordinate(len));

			GeoPoint point = new GeoPoint((int) (loc.getLatitude() * BASE_EXP6), (int) (loc.getLongitude() * BASE_EXP6));
			ServerConnection conn = new ServerConnection((float) loc.getLatitude(), (float) loc.getLongitude());
			conn.execute();
			String d = conn.get();
			if (d == null) {
				throw conn.getServException();
			}
			
			d = d.replaceAll("null", DOUBLE_QUOTES);
			String[] array = d.split(QUOTE_COMA);
			array[0] = array[0].substring(1);
			for(int i = 0; i < array.length; i ++) {
				array[i] = array[i].replaceAll(QUOTE, "");
			}

			if(!array[0].equals(placeTitle) && !array[0].equals("")) {
				placeTitle = array[0];
				placeTitle.toUpperCase(Locale.ENGLISH);
				// TODO: Mal rollo
				placeDescription = array[1];
				imageDescription = array[3];
				sendVibration();
				acceptRequest(this.getCurrentFocus());
			}
			updateMap(point);
		}
	}

	private void updateMap(GeoPoint p) {
		mapController.animateTo(p);
		mapController.setZoom(250);
		mapView.invalidate();
	}

	private void sendVibration() {
		NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); 
		Notification n = new Notification();
		n.vibrate = new long[] {100, 200, 100, 200, 100, 200, 100, 200, 100, 200, 100, 200, 100, 200, 100, 200, 100, 200, 100, 200}; 
		nManager.notify(0, n);
	}

	public void acceptRequest(View view) {
		// Crear un dialogo sin guarrear el código de la clase que lo  lanza
		listenDialog = new ListenDialog(getBaseContext(), imageDescription, placeTitle, this);
		listenDialog.setOnDismissListener(this);
		listenDialog.show();
	}
	
	public void onClick(DialogInterface dialog, int which) {
		if (dialog == listenDialog && which == DialogInterface.BUTTON_POSITIVE) {
			tts.speak(placeDescription, TextToSpeech.QUEUE_FLUSH, null);
			while(tts.isSpeaking()){}
		}
	}
	
	public void onDismiss(DialogInterface dialog) { }

	@Override
	protected boolean isRouteDisplayed() {
		return true;
	}

	public void onInit(int arg0) { }

	//para mostrar el menú de la aplicación
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mainmenu, menu);
		return true;
	}

	//código para cada opción de menú
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.exit:
			finish();
		}
		return true;
	}
}
