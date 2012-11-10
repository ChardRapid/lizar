package com.tw.domain;

import java.util.Date;

import com.lizar.web.Web;
import com.lizar.web.config.Group;
import com.lizar.web.loader.Plugin;
import com.tw.persistence.MongoDao;

public class ThreadClean extends Plugin {

	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void pre_run() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		MongoDao.mongodb.close();
	}

	@Override
	public Date set_start_time() {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public String set_interval_time() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String set_delay_time() {
		// TODO Auto-generated method stub
		return null;
	}

}
