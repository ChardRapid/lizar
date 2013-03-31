package org.lizar.exception;

import org.lizar.web.loader.Cell;

public class CellIsNull extends RuntimeException {
	private static final long serialVersionUID = -7780149164665729686L;

	public CellIsNull(String bean_key) {
		super("Bean:"+bean_key+" is null.");
	}

	
	
}
