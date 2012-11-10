package com.tw.event.uc;

import java.io.IOException;

import javax.servlet.ServletException;

import com.lizar.web.Web;
import com.lizar.web.config.Config;
import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;
import com.tw.domain.uc.Session;
import com.tw.persistence.AutoTokenDao;
import com.tw.persistence.SessionDao;

public class Logout extends Event {

	private SessionDao session_dao;
	
	private AutoTokenDao auto_token_dao;
	
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		session_dao=Web.get(SessionDao.class);
		auto_token_dao=Web.get(AutoTokenDao.class);
	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/logout";
	}

	@Override
	public void handle(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Session user=el.get_attr("user",null);
		if(user!=null){
			session_dao.logout(user.getUser_id());
			auto_token_dao.logout(user.getUser_id());
		}
		el.response(Config.xpath_str("server_info.root"));
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
