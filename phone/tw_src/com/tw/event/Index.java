package com.tw.event;

import java.io.IOException;

import javax.servlet.ServletException;

import com.lizar.web.Web;
import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;
import com.tw.domain.uc.UserCenter;
import com.tw.persistence.download.TopDownloadDao;

public class Index extends Event {
	private UserCenter ucenter;
	private TopDownloadDao top_download_dao;
	@Override
	public void init_property() throws Exception {
		top_download_dao=Web.get(TopDownloadDao.class);
		ucenter=Web.get(UserCenter.class);
	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/";
	}

	@Override
	public void handle(EventLoader el) throws ServletException,
			IOException {
		index(el);
	}
	
	private void index(EventLoader event_loader) throws IOException, ServletException{
		event_loader.set_attr("links", top_download_dao.first(50));
		event_loader.template("/WEB-INF/template/index.vm");
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
		System.out.println(event_loader.request_path());
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
