package com.tw.event.uc;

import java.io.IOException;

import javax.servlet.ServletException;

import com.lizar.web.Web;
import com.lizar.web.config.Config;
import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;
import com.tw.domain.uc.Session;
import com.tw.domain.uc.UserCenter;

public class Setting extends Event {
	private UserCenter ucenter;
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		ucenter=Web.get(UserCenter.class);
	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/setting/*";
	}

	@Override
	public void handle(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Session u=el.get_attr("user",null);
		if(u==null){
			el.response(Config.xpath_str("server_info.root")+"/login");
		}
		String action=el.request_path(1);
		if(action.equals("")){
			index(el);
		}else if(action.equals("password")){
			password(el);
		}else if(action.equals("head")){
			head(el);
		}else{
			index(el);
		}
	}
	
	private void index(EventLoader el) throws IOException, ServletException{
		el.template("/WEB-INF/template/setting/index.vm");
	}
	
	private void password(EventLoader el) throws IOException, ServletException{
		el.template("/WEB-INF/template/setting/head.vm");
	}
	
	private void head(EventLoader el) throws IOException, ServletException{
		el.template("/WEB-INF/template/setting/head.vm");
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
