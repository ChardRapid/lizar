package org.lizar.web.controller;

import java.io.IOException;

import javax.servlet.ServletException;

public interface XMLHandler {


	/**
	 * @task invoked by controller,handle user request
	 * @param el resource and function owned by each request
	 */
	abstract void handle_xml(EventLoader el)throws ServletException, IOException ;
	
}
