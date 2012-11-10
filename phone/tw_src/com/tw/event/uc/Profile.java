package com.tw.event.uc;

import java.io.IOException;

import javax.servlet.ServletException;

import com.lizar.util.StringHelper;
import com.lizar.web.config.Config;
import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;

public class Profile extends Event {

	
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/user/*";
	}

	@Override
	public void handle(EventLoader el) throws ServletException,
			IOException {
		// TODO Auto-generated method stub
		String id=el.request_path(1);
		if(StringHelper.isLong(id)){
			profile(id,el);
		}else{
			el.response(Config.xpath_str("server_info.root"));
		}
	}
	
	private void profile(String id,EventLoader el) throws IOException, ServletException{
		el.template("/WEB-INF/template/profile/index.vm");
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
	public void handle_xml(EventLoader el) throws ServletException,
			IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void before(EventLoader el) throws ServletException,
			IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void after(EventLoader el) throws ServletException,
			IOException {
		// TODO Auto-generated method stub

	}

}
