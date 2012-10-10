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
	public void handle(EventLoader event_loader) throws ServletException,
			IOException {
		// TODO Auto-generated method stub
		String path=event_loader.request_path();
		
	}

	@Override
	public void handle_jsonp(EventLoader event_loader) throws ServletException,
			IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void handle_json(EventLoader event_loader) throws ServletException,
			IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void handle_xml(EventLoader event_loader) throws ServletException,
			IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void before(EventLoader event_loader) throws ServletException,
			IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void after(EventLoader event_loader) throws ServletException,
			IOException {
		// TODO Auto-generated method stub

	}


}
