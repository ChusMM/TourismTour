package com.tourism.map;

//import java.io.File;
import java.io.FileInputStream;
//import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
//import java.util.Set; 

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;

public class BluetoothOutput extends Activity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener{

	private BluetoothAdapter mBluetoothAdapter;
	private List<BluetoothDevice> mArrayAdapter;
	private boolean hasActiveBluetooth;
	private String textToReproduce;
	private String destinationFileName;
	private TextToSpeech tts;
	private HashMap<String,String> myHashRender;
	private AudioManager audioManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//FileWriter writer = null;
		//try{
			//writer = new FileWriter(new File("/storage/sdcard0/traces.txt"),true);
			textToReproduce = getIntent().getStringExtra("outputReproduce");
			//writer.write("Text to reproduce: " + textToReproduce); 
			hasActiveBluetooth = false;
			mArrayAdapter = new ArrayList<BluetoothDevice>();
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if(!mBluetoothAdapter.isEnabled()){
				//writer.write("Bluetooth adapter is not enabled\n");
				hasActiveBluetooth = true;
				mBluetoothAdapter.enable();
			}
			while (!mBluetoothAdapter.isEnabled()) { // Wait till discovery starts
				//writer.write("Bluetooth adapter is not enabled yet\n");
		    }
			//writer.write("Bluetooth adapter after discovering\n");
			
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
			filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			filter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED);
			//writer.write("Before registering filter\n");
			registerReceiver(reciever, filter); // Don't forget to unregister during onDestroy
			mBluetoothAdapter.startDiscovery();
			while (!mBluetoothAdapter.isDiscovering()) { // Wait till discovery starts
				System.out.println("Bluetooth adapter is discovering devices\n");
		    }
			//writer.write("After registering filter\n");
		//}
		//catch(IOException ioe){
			
		//}
		//finally{
		//	try {
		//		writer.close();
		//	} catch (IOException e) {
				// TODO Auto-generated catch block
		//		e.printStackTrace();
		//	}
		//}
	}
	
	public boolean checkBluetoothAdapter(){
		//FileWriter writer = null;
		boolean ret = false;
		//try{
			//writer = new FileWriter(new File("/storage/sdcard0/traces.txt"),true);
			if (mBluetoothAdapter == null){
				//writer.write("ADAPTER IS NULL\n");
			    ret = false;
			}
			else{
				//writer.write("ADAPTER IS NOT NULL");
				ret =  true;
			}
		return ret;
	}
	
	public boolean checkBluetoothEnabled(){
		if (!mBluetoothAdapter.isEnabled()) {
		    mBluetoothAdapter.enable();
		    return true;
		}
		return true;
	}
	
	public List<BluetoothDevice> getArrayAdapter(){
		return mArrayAdapter;
	}
	
	protected void onDestroy(){
		super.onDestroy();
		unregisterBroadcastReciever();
		if(hasActiveBluetooth){
			mBluetoothAdapter.disable();
		}
	}
	
	
	private void unregisterBroadcastReciever(){
		unregisterReceiver(reciever);
	}
	
	public void finish() {
	    super.finish();
	};
	
	BroadcastReceiver reciever = new BroadcastReceiver() {
		@Override
	    public void onReceive(Context context, Intent intent) {
			
		        String action = intent.getAction();
		        int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
		        // When discovery finds a device
		        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
		            // Get the BluetoothDevice object from the Intent
		            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		            // Add the name and address to an array adapter to show in a ListView
		            if(device != null){
		            	mArrayAdapter.add(device);
		            }
		        }
		        else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
		        {
		        	if(mArrayAdapter.size() == 0){ // No devices found
		        		finish();
		        	}
		        	else{
		        		writeTTStoFile(textToReproduce);
		        	}
		        }
		        else if (action.equals(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED))
		        {
		        	
			            int scoAudioState = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
			            switch (scoAudioState)
			            {
			                case AudioManager.SCO_AUDIO_STATE_CONNECTED:
			                {
			                	FileInputStream stream = null;
				        	    try{
				        	    	stream = new FileInputStream(destinationFileName);
				        	    	MediaPlayer mPlayer = new MediaPlayer();
				        	    	mPlayer.setDataSource(stream.getFD());
				                    mPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
				                    mPlayer.prepare();
				                    mPlayer.start();
				                    while(mPlayer.isPlaying()){

				                    }

				                    mPlayer.stop();
				                    mPlayer.release();
				                    audioManager.setBluetoothScoOn(false);
				                    audioManager.stopBluetoothSco();
				                    audioManager.setMode(AudioManager.MODE_NORMAL);
				        	    }
				        	    catch(Exception fnfe){
				        	    	
				        	    }
				        	    finally{
				        	    	try {
				        	    		if(stream != null){
				        	    			stream.close();
				        	    		}
				        			} catch (IOException e) {

				        			}
				        	    }
				        	    finish();
			                    break;
			                }
			                case AudioManager.SCO_AUDIO_STATE_ERROR:
			                {
			                	finish();
			                	break;
			                }
			            }
		        }
		        else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
		        	//writer.write("Discovery started. Nothing to do\n");
		        	System.out.println("Se estan empezando a descubrir dispositivos");
		        }
		        else if (AudioManager.SCO_AUDIO_STATE_DISCONNECTED == state) {
		        	//writer.write("There is nothing to do\n");
		        }
		        else {
		        	finish();
		        }
	    }
	};

	private void writeTTStoFile(String text) {
		myHashRender = new HashMap<String,String>();
		String textToConvert = text;
		destinationFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.mp3";
		myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, textToConvert);
		tts = new TextToSpeech(this, this);
	}
	
	private void sendtoHeadSet(){
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		audioManager.setMode(AudioManager.MODE_NORMAL);
		audioManager.setBluetoothScoOn(true);
		audioManager.startBluetoothSco();
	}

	public void onInit(int status) {
		// check for successful instantiation
        if (status == TextToSpeech.SUCCESS) {
        	tts.setOnUtteranceCompletedListener(this);
        	tts.synthesizeToFile(textToReproduce, myHashRender, destinationFileName);
        }
	}
	
	// It's callback
	public void onUtteranceCompleted(String utteranceId) {
		tts.stop();
    	tts.shutdown();
    	sendtoHeadSet();
	}
}

