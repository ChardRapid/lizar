package com.lizar.util;

import java.util.Map.Entry;

public class Pair<T,T1> implements Entry<T,T1>{

	private T key;
	
	private T1 value;
	
	public Pair() {
	}
	
	public Pair(T key,T1 value){
		this.key=key;
		this.value=value;
	}
	
	
	public T getKey() {
		return key;
	}

	
	public T1 getValue() {
		// TODO Auto-generated method stub
		return value;
	}

	
	public T1 setValue(T1 value) {
		this.value=value;
		return value;
	}

	public T setKey(T key) {
		this.key=key;
		return key;
	}

	public static void main(String[] args){
		System.out.println(1023/1024);
	}
}
