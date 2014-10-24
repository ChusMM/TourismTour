package com.tourism.map.exceptions;

public class ServerConnectionException extends Exception {
	private static final long serialVersionUID = -7419058098884552181L;
	
	public ServerConnectionException(String msg) {
		super(msg);
	}
}