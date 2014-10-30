package com.tourism.map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class ListenDialog extends AlertDialog {
	private View mView;
	private final DialogInterface.OnClickListener mListener;
	
	private String mImageDescription;
	private String mPlaceTitle;
	
	public ListenDialog(Context context, String imageDescription, String placeTitle, DialogInterface.OnClickListener listener) {
		super(context);
		mImageDescription = imageDescription;
		mPlaceTitle = placeTitle;
        mListener = listener;
	}
	
	@Override
    protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		mView = getLayoutInflater().inflate(R.layout.listen_dialog, null);
		setTitle("");
		
		setView(mView);
		setInverseBackgroundForced(true);
		Context context = getContext();
		
		TextView description  = (TextView) findViewById(R.id.description);

		setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(R.string.cancel), mListener);
		setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok), mListener);

		mView.findViewById(R.id.listen_layout).setVisibility(View.VISIBLE);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		
		description.setText(mPlaceTitle);
		setImagePlace();
	}
	
	private void setImagePlace() {
		byte[] decodedString = Base64.decode(mImageDescription, Base64.DEFAULT);
		ImageView image = (ImageView) findViewById(R.id.placeImage);
		image.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
		image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	}

}
