package org.lizar.exception;

import org.lizar.web.loader.Cell;

public class CellKeyIsNotJsonObject extends RuntimeException {
	private static final long serialVersionUID = -7780149164665729686L;

	public CellKeyIsNotJsonObject(String key) {
		super("Bean Key:"+key+" is not refer to a json object, as it has sub property");
	}

	
	
}
