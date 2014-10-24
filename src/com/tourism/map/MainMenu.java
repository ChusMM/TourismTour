package com.tourism.map;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class MainMenu extends Activity{

	SharedPreferences prefs;
	Locale l;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Perhaps set content view here
		prefs = getSharedPreferences("com.tourist.map.MainMenu", MODE_PRIVATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (prefs.getBoolean("firstrun", true)) {
			// Do first run stuff here then set 'firstrun' as false
			// using the following line to edit/commit prefs

		} else {
			Intent intentSecond=new Intent();
			intentSecond.setClass(this, MainMap.class);
			startActivity(intentSecond);
		}
	}

	@ Override
	public void onDestroy() {
		super.onDestroy();
	}
}
