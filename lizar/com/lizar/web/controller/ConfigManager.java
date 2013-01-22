package com.lizar.web.controller;

import java.io.IOException;

import javax.servlet.ServletException;

public class ConfigManager extends Event {

	public static String user_name="";
	
	public static String password="";
	
	
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/config_manager/*";
	}

	@Override
	public void handle(EventLoader el) throws ServletException,
			IOException {
		// TODO Auto-generated method stub
		String path=el.request_path();
		
	}





}
