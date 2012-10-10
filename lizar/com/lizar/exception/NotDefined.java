package com.lizar.exception;

public class NotDefined extends RuntimeException{
	
	
	public NotDefined() {
		super();
	}
	
	public NotDefined(String msg){
		super(msg);
	}
	
	
	public NotDefined(String message, Throwable cause) {
	        super(message, cause);
	}
	
	  public NotDefined(Throwable cause) {
	        super(cause);
	 }
}
