package com.tw.event;

import java.io.IOException;

import javax.servlet.ServletException;

import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;

public class About extends Event {

	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/about/*";
	}

	@Override
	public void handle(EventLoader event_loader) throws ServletException,
			IOException {
		// TODO Auto-generated method stub
		if(event_loader.request_path().equals("/about")){
			event_loader.request("/WEB-INF/template/about/about.vm");
		}else 	if(event_loader.request_path().equals("/about/agreement")){
			event_loader.request("/WEB-INF/template/about/agreement.vm");
		}
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
		System.out.println("handle json:"+event_loader.request_path());
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
