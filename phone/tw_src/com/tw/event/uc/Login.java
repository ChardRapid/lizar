package com.tw.event.uc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import com.lizar.util.MyMath;
import com.lizar.util.StringHelper;
import com.lizar.web.Web;
import com.lizar.web.config.Config;
import com.lizar.web.config.Keys;
import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;
import com.mongodb.Entity;
import com.tw.domain.uc.Session;
import com.tw.domain.uc.UserCenter;
import com.tw.domain.uc.element.Email;
import com.tw.domain.uc.element.Password;
import com.tw.persistence.AutoTokenDao;
import com.tw.persistence.RegisterDao;
import com.tw.persistence.UserDetailDao;

public class Login extends Event {
	private UserCenter ucenter;
	private UserDetailDao user_detail_dao;
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		ucenter=Web.get(UserCenter.class);
		user_detail_dao=Web.get(UserDetailDao.class);
	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/login/*";
	}

	@Override
	public void handle(EventLoader event_loader) throws ServletException,
			IOException {
		if(event_loader.get_attr("uc")!=null){
			event_loader.response(Config.xpath_str("server_info.root"));
		}else{
			String email=event_loader._str("email","").toLowerCase().trim();
			String password=event_loader._str("password","").trim();
			int timeout=event_loader._int("timeout", 3600*24);
			if(StringHelper.isNull(email)){
				event_loader.template("/WEB-INF/template/login.vm");
				return;
			}
			String msg=login(event_loader.lan,email,password, timeout, event_loader);
			if(StringHelper.isNotNull(msg)){
				event_loader.set_attr("msg", msg);
				event_loader.set_attr("email", email);
				event_loader.template("/WEB-INF/template/login.vm");
			}
		}
	}
	
	private String login(String lan,String email,String password,int timeout,EventLoader el) throws IOException{
		String msg = Password.login_validate(lan,password);
		if (StringHelper.isNotNull(msg))return msg;
		msg = Email.validate_email_without_db_verify(lan, email);
		if (StringHelper.isNotNull(msg))return msg;
		Entity user=user_detail_dao.get_by_email( email,null);
		if(user==null){
			return Web.i18.get(lan, "login.password_invalid");
		}
		if(!user._string("password").equals(MyMath.encryptionWithMD5(password))){
			return Web.i18.get(lan, "login.password_invalid");
		}
		if(timeout<0)timeout=3600*24;
		Session session=ucenter.login(el, user, System.currentTimeMillis()+timeout*1000);
		Cookie c=new Cookie("token",session.get_id());
		c.setMaxAge(timeout);
		el.response(Config.xpath_str("server_info.root"), c);
		return msg;
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

	}

	@Override
	public void handle_xml(EventLoader event_loader) throws ServletException,
			IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void before(EventLoader event_loader) throws ServletException,
			IOException {
		
	}

	@Override
	public void after(EventLoader event_loader) throws ServletException,
			IOException {
		// TODO Auto-generated method stub

	}

}
