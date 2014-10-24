package com.tourism.map;

import com.tourism.map.exceptions.PlaceBuilderException;

public class Place implements MapFinals {
	private static String TAG = "com.tourism.map.Place";
	
	double x;
	double y;
	float radius;
	String title;
	String description;
	
	public Place(String[] values) throws PlaceBuilderException {
		try {
			setX(values[0]);
			setY(values[1]);
			setRadius(values[2]);
			setTitle(values[3]); 
			setDescription(values[4]); 
		} catch (IndexOutOfBoundsException e) {
			throw new PlaceBuilderException(TAG + " " + e.toString());
		}
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}

	public float getRadius() {
		return radius;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public void setRadius(float radius) {
		this.radius = radius;
	}
	
	public void setX(String value) throws PlaceBuilderException {
		try {
			String strX = value.replaceAll(QUOTE, "").split(COLON)[1]; // TODO: Coges siempre la derecha ':' ? [1]
			this.x = Double.parseDouble(strX);
		} catch(IndexOutOfBoundsException e) {
			throw new PlaceBuilderException("There's no X coordinate value in expression");
		} catch (NumberFormatException e) {
			throw new PlaceBuilderException("X coordinate " + e.getMessage());
		}
	}

	public void setY(String value) throws PlaceBuilderException {
		try {
			String strY = value.replaceAll(QUOTE, "").split(COLON)[1];
			this.y = Double.parseDouble(strY);
		} catch(IndexOutOfBoundsException e) {
			throw new PlaceBuilderException("There's no Y coordinate value in expression");
		} catch (NumberFormatException e) {
			throw new PlaceBuilderException("Y coordinate " + e.getMessage());
		}
	}

	public void setRadius(String value) throws PlaceBuilderException {
		try {
			String strRad = value.replaceAll(QUOTE, "").split(COLON)[1];
			this.radius = Float.parseFloat(strRad);
		} catch(IndexOutOfBoundsException e) {
			throw new PlaceBuilderException("There's no radius value in expression");
		} catch (NumberFormatException e) {
			throw new PlaceBuilderException("Radius " + e.getMessage());
		}
	}
	
	public void setTitle(String value) throws PlaceBuilderException {
		try {
			this.title = value.replaceAll(QUOTE, "").split(COLON)[1];
		} catch(IndexOutOfBoundsException e) {
			throw new PlaceBuilderException("There's no titles value in expression");
		}
	}
	
	public void setDescription(String value) throws PlaceBuilderException {
		try {
			this.description = value.replaceAll(QUOTE, "").split(COLON)[1];
		} catch(IndexOutOfBoundsException e) {
			throw new PlaceBuilderException("There's no description value in expression");
		}
	}
}
