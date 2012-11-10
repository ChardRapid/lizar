package com.tw.event.movie;

import java.io.IOException;

import javax.servlet.ServletException;

import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;

public class Celebrity extends Event {

	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/celebrity/*";
	}

	@Override
	public void handle(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}
	private void star(String uname,String star,EventLoader el) throws IOException, ServletException{
		el.template("/WEB-INF/template/star/detail.vm");
	}
	
	
	private void download(String uname,String id,EventLoader el) throws IOException, ServletException{
		el.template("/WEB-INF/template/record/download.vm");
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
