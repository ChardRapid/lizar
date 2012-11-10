package com.tw.domain.uc;

import java.util.Date;

import com.lizar.web.Web;
import com.lizar.web.config.Keys;
import com.lizar.web.loader.Plugin;
import com.tw.persistence.AutoTokenDao;

public class AutoTokenClean extends Plugin {

	private AutoTokenDao auto_token_dao;
	
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		auto_token_dao=Web.get(AutoTokenDao.class);

		Keys.set("session_auto_time_out", 3600*24);
		Keys.set("session_max_auto_time_out", 3600*24*30);
	}

	@Override
	public void pre_run() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		auto_token_dao.clean();
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
		return "6h";
	}

	@Override
	public String set_delay_time() {
		// TODO Auto-generated method stub
		return null;
	}

}
