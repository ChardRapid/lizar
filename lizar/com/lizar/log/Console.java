package com.lizar.log;

import com.lizar.web.Web;


public class Console implements Log {

	private String _t;
	
	public Console(Class t){
		_t=t.getName();
	}
	
	@Override
	public void debug(String message) {
		if(Web.debug)System.out.println("[Console debug] "+_t+" "+message);
		
	}

	@Override
	public void debug(String message, Throwable t) {
		// TODO Auto-generated method stub
		if(Web.debug){
			System.out.println("[Console debug] "+_t+" "+message);
			t.printStackTrace();
		}
		
	}

	@Override
	public void error(String message) {
		// TODO Auto-generated method stub
		System.out.println("[Console error] "+_t+" "+message);
	}

	@Override
	public void error(String message, Throwable t) {
		// TODO Auto-generated method stub
		System.out.println("[Console error] "+_t+" "+message);
		t.printStackTrace();
	}

	@Override
	public void info(String message) {
		// TODO Auto-generated method stub
		System.out.println("[Console info] "+_t+" "+message);
	}

	@Override
	public void info(String message, Throwable t) {
		// TODO Auto-generated method stub
		System.out.println("[Console info] "+_t+" "+message);
		t.printStackTrace();
	}

	@Override
	public void warn(String message) {
		// TODO Auto-generated method stub
		System.out.println("[Console warn] "+_t+" "+message);
	}

	@Override
	public void warn(String message, Throwable t) {
		// TODO Auto-generated method stub
		System.out.println("[Console warn] "+_t+" "+message);
		t.printStackTrace();
	}

	public static void main(String[] args){
		String[] arr="sdf.".split("\\.");
		System.out.println(arr.length);
		System.out.println(arr[0]);
		System.out.println(arr[1]);
	}

	@Override
	public void debug(Throwable t) {
		System.out.println("[Console debug]"+_t);
		t.printStackTrace();
	}

	@Override
	public void error(Throwable t) {
		System.out.println("[Console error]"+_t);
		t.printStackTrace();
	}

	@Override
	public void info(Object obj) {
		System.out.println("[Console info]"+_t);
		System.out.println(obj);
	}

	@Override
	public void info(Throwable t) {
		System.out.println("[Console info]"+_t);
		t.printStackTrace();
	}

	@Override
	public void warn(Throwable t) {
		System.out.println("[Console warn]"+_t);
		t.printStackTrace();
	}

}
