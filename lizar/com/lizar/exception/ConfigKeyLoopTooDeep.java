package com.lizar.exception;


public class ConfigKeyLoopTooDeep extends RuntimeException {
	private static final long serialVersionUID = -7780149164665729686L;

	public ConfigKeyLoopTooDeep(String config_file,String key) {
		super("Config file:"+config_file+" with the key "+key+" express too deep, more than 20, pls try to simplify it.");
	}

	
	
}
