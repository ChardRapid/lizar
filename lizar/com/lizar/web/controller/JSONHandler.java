package com.lizar.web.controller;

import java.io.IOException;

import javax.servlet.ServletException;

public interface JSONHandler {
	/**
	 * @task invoked by controller,handle user request
	 * @param el resource and function owned by each request
	 */
	abstract  void handle_json(EventLoader el)throws ServletException, IOException ;
	
}
