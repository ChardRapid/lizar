package com.lizar.exception;

public class FatalError extends Exception{
	
	
	public FatalError() {
		super();
	}
	
	public FatalError(String msg){
		super(msg);
	}
	
	
	public FatalError(String message, Throwable cause) {
	        super(message, cause);
	}
	
	  public FatalError(Throwable cause) {
	        super(cause);
	 }
	  
	  public static void main(String[] args){
		  System.out.println(FatalError.class.getSimpleName());
	  }
}
