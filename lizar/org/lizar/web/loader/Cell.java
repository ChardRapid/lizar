package org.lizar.web.loader;

public interface Cell {
	
	/**
	 * init cell and return a string represent this cell in container.
	 * 
	 * you can call it key or DNA
	 * 
	 * after the System load, you can get your cell everywhere use Web.get(key) or Container.get(key)
	 * 
	 * 
	 * */
	void init_property()throws Exception;
}
