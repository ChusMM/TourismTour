package com.tourism.map;

import android.graphics.Bitmap;

public class Util {

    public static Bitmap ScaleBitmap(Bitmap bm, float scalingFactor) {
        int scaleHeight = (int) (bm.getHeight() * scalingFactor);
        int scaleWidth = (int) (bm.getWidth() * scalingFactor);

        return Bitmap.createScaledBitmap(bm, scaleWidth, scaleHeight, true);
    }
    
    public static String formatCoordinate(String value) throws IndexOutOfBoundsException {
    	return value.substring(0, Math.min(9, value.length()));
    }
}
