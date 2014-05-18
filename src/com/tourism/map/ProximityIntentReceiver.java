package com.tourism.map;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;

public class ProximityIntentReceiver extends BroadcastReceiver {
     
    private static final int NOTIFICATION_ID = 1000;
    BluetoothOutput output;

    @Override
    public void onReceive(Context context, Intent intent) {
		String key = LocationManager.KEY_PROXIMITY_ENTERING;
		Boolean entering = intent.getBooleanExtra(key, false);
		if(entering){
	    	String t = intent.getStringExtra("title");
	    	String description = intent.getStringExtra("description");
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
			Notification notification = createNotification();
			CharSequence title = t;
			CharSequence seq = description;
			notification.setLatestEventInfo(context,title,seq, pendingIntent);
			notificationManager.notify(NOTIFICATION_ID, notification);
			Intent myIntent = new Intent(context, BluetoothOutput.class);
			myIntent.putExtra("outputReproduce", seq.toString());
			context.startActivity(myIntent);
		}
    }
    
    protected void onDestroy(){
    	
    }
    
    private Notification createNotification() {
    	Notification notification = new Notification();
    	notification.icon = R.drawable.tourism;
    	notification.when = System.currentTimeMillis();     
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;     
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.defaults |= Notification.DEFAULT_LIGHTS;     
		notification.ledARGB = Color.WHITE;
		notification.ledOnMS = 1500;
		notification.ledOffMS = 1500;     
    	return notification;
    } 
}