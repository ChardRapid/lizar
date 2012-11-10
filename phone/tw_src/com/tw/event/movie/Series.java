package com.tw.event.movie;

import java.io.IOException;

import javax.servlet.ServletException;

import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;

public class Series extends Event{

	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/series/*";
	}

	@Override
	public void handle(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}
	private void series(String uname,String series,EventLoader el) throws IOException, ServletException{
		el.template("/WEB-INF/template/series/detail.vm");
	}
	
	private void series(String uname,EventLoader el) throws IOException, ServletException{
		el.template("/WEB-INF/template/series/index.vm");
	}
	
	
	@Override
	public void handle_jsonp(EventLoader el) throws ServletException,
			IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handle_json(EventLoader el) throws ServletException,
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
