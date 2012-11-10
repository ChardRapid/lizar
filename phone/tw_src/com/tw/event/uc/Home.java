package com.tw.event.uc;

import java.io.IOException;

import javax.servlet.ServletException;

import com.lizar.util.StringHelper;
import com.lizar.web.Web;
import com.lizar.web.config.Config;
import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;
import com.tw.domain.uc.Session;
import com.tw.domain.uc.UserCenter;

public class Home extends Event {
	private UserCenter ucenter;
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		ucenter=Web.get(UserCenter.class);
	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/home/*";
	}

	@Override
	public void handle(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Session u=el.get_attr("uc",null);
		if(u==null){
			el.response(Config.xpath_str("server_info.root")+"/login");
		}
		String action=el.request_path(1);
		if(action.equals("")){
			home(el);
		}else if(action.equals("remind")){
			remind(el);
		}else{
			home(el);
		}
	}
	

	private void remind(EventLoader el) throws IOException, ServletException{
		el.template("/WEB-INF/template/uc/remind.vm");
	}
	
	private void home(EventLoader el) throws IOException, ServletException{
		el.template("/WEB-INF/template/uc/index.vm");
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
