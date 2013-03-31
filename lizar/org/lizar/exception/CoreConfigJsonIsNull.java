package org.lizar.exception;

import org.lizar.web.loader.Cell;

public class CoreConfigJsonIsNull extends RuntimeException {
	private static final long serialVersionUID = -7780149164665729686L;

	public CoreConfigJsonIsNull(String file_path) {
		super("Core Config:"+file_path+" is null");
	}

	
	
}
