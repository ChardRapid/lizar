package com.tw.event.mc;

import java.io.IOException;

import javax.servlet.ServletException;

import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;

public class MCUser extends Event {

	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/mc/user/*";
	}

	@Override
	public void handle(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(el.request_path().equals("/mc/user")){
			index(el);
		}else if(el.request_path(2).equals("disable")){
			el.template("/WEB-INF/template/mc/user/modify.vm");
		}
	}
	
	private void index(EventLoader el) throws IOException, ServletException{
		
		
		
		el.template("/WEB-INF/template/mc/user/index.vm");
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
	public void handle_xml(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void before(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void after(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

}
