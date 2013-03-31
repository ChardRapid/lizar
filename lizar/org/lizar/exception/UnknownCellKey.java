package org.lizar.exception;

import org.lizar.web.loader.Cell;

public class UnknownCellKey extends RuntimeException {
	private static final long serialVersionUID = -7780149164665729686L;

	public UnknownCellKey(String lizar_key) {
		super("Bean:"+lizar_key+" can not find in Engine container.");
	}

	
	
}
