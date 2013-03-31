package org.lizar.web.config;

import org.lizar.exception.InvalidConfigKeyExpress;

/**
 * config architecture EL syntax translator
 * 
 * @see Config
 * @see Config.LizarTranslator
 * @see Keys.KeyTranslator
 * @see Group.GroupTranslator
 * 
 * @author wujianchao
 *
 */
public abstract class Translator {
	
	/**
	 * Translate the configuration from EL expression.
	 */
	public abstract void translate();
	
	/**
	 * Parse EL expression which may contain "${}" to string,int,long and so on.
	 * 
	 */
	protected abstract Object express(Object value );
	
	/**
	 * @task Check whether a string contains "${}"
	 * 
	 * @caution Key do not support Nested expression.
	 */
	public static boolean check(String e){
		int first=e.indexOf("${");
		if(first==-1)return false;
		int end=e.indexOf("}");
		if(end==-1)return false;
		int second=e.indexOf("${", first+2);
		if(second==-1||second>end)return true;
		throw new InvalidConfigKeyExpress(e+" is not valid express, express do not support Nested express in one key");
	}

}
