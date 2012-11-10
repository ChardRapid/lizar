package com.tw.domain.uc;

import java.util.Date;

import javax.servlet.http.Cookie;

import org.bson.types.ObjectId;

import com.lizar.util.MyMath;
import com.lizar.util.StringHelper;
import com.lizar.web.PluginManager;
import com.lizar.web.Web;
import com.lizar.web.config.Group;
import com.lizar.web.config.Keys;
import com.lizar.web.controller.EventLoader;
import com.lizar.web.loader.Plugin;
import com.mongodb.Entity;
import com.tw.domain.ThreadClean;
import com.tw.persistence.AutoTokenDao;
import com.tw.persistence.SessionDao;
import com.tw.persistence.UserDetailDao;

public class UserCenter extends Plugin {

	
	private SessionDao session_dao;
	private UserDetailDao user_detail_dao;
	private AutoTokenDao auto_token_dao;
	
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		session_dao=Web.get(SessionDao.class);
		user_detail_dao=Web.get(UserDetailDao.class);
		auto_token_dao=Web.get(AutoTokenDao.class);
		ThreadClean uc=PluginManager.getPlugin(ThreadClean.class);
		Group.set_up("session_dao", "timeout", "");
		Group.set_up("session_dao", "next", "pp");
	}

	@Override
	public void pre_run() throws Exception {
		auto_token_dao.clean();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		session_dao.clean();
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public Date set_start_time() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String set_interval_time() {
		// TODO Auto-generated method stub
		return "30m";
	}

	@Override
	public String set_delay_time() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Session load_session(EventLoader el,String fields){
		Cookie token=el.get_cookie("token");
		if(token==null)return null;
		Session s=null;
		if(StringHelper.isNotNull(token.getValue())){
			s=Session.load_session(session_dao.get(token.getValue(), fields));
		}
		if(s==null){
			ObjectId _id=auto_token_dao.check(token.getValue());
			if(_id==null)return null;
			s=Session.new_session(token.getValue(),user_detail_dao.get(_id));
			if(s==null){
				auto_token_dao.delete_by_id(_id);
			}else{
				session_dao.insert(s.to_entity());
			}
		}
		return s;
	}
	
	public Session login(EventLoader el,Entity user_detail,long timeout){
		String session_id= MyMath.encryptionWithMD5(el.session_id()+System.currentTimeMillis());
		Session s=Session.new_session(session_id,user_detail);
		session_dao.insert(s.to_entity());
		auto_token_dao.insert(user_detail._obj_id("_id"),session_id, timeout);
		return s;
	}

	

	public Session register_login(String session_id,Entity user){
		Session session=Session.new_session(session_id,user);
		session_dao.insert(session.to_entity());
		return session;
	}
	
	
}
