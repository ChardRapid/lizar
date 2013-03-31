package org.lizar.exception;

import org.lizar.web.loader.Cell;

public class I18MsgPathIllegal extends RuntimeException {
	private static final long serialVersionUID = -7780149164665729686L;

	public I18MsgPathIllegal(String keys_path) {
		super("Config Keys path is illegal:"+keys_path+"\n input time should be like key\nkey1.key2");
	}


	
}
