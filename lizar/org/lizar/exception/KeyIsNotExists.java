package org.lizar.exception;

import org.lizar.web.loader.Cell;

public class KeyIsNotExists extends RuntimeException {
	private static final long serialVersionUID = -7780149164665729686L;

	public KeyIsNotExists(String key) {
		super("Key:"+key+" is not exists, you must set the value before use it");
	}

	
	
}
