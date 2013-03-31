package org.lizar.exception;


public class InvalidFullGroupCode extends RuntimeException {
	private static final long serialVersionUID = -7780149164665729686L;


	public InvalidFullGroupCode(String fullcode) {
		super("GroupCode:"+fullcode+" is not valid, it must like this \"group.code\"");
	}
	
}
