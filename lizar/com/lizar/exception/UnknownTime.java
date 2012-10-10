package com.lizar.exception;

import com.lizar.web.loader.Cell;

public class UnknownTime extends RuntimeException {
	private static final long serialVersionUID = -7780149164665729686L;

	public UnknownTime(String input_time) {
		super("Unknown Time :"+input_time+"\n input time should be like 100,100ms,100h,100d,100s");
	}

	
	
}
