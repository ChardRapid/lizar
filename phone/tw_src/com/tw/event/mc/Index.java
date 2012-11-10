package com.tw.event.mc;

import java.io.IOException;

import javax.servlet.ServletException;

import com.lizar.web.Web;
import com.lizar.web.config.Config;
import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;
import com.tw.domain.uc.Session;
import com.tw.persistence.AdminDao;

public class Index extends Event {

	private AdminDao admin_dao;
	
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		admin_dao=Web.get(AdminDao.class);
	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/mc";
	}

	@Override
	public void handle(EventLoader el) throws ServletException, IOException {
		Session user=el.get_attr("user",null);
		if(user==null||!admin_dao.is_super(user.getUser_id())){
			el.response(Config.xpath_str("server_info.root"));
			return;
		}
		el.template("/WEB-INF/template/mc/index.vm");
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
