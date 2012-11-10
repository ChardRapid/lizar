package com.tw.event;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import lizar.anlaysis.domain.SiteTrafficAnalysis;
import lizar.anlaysis.persistence.DailyTrafficDao;
import lizar.anlaysis.persistence.IPTrafficDao;

import com.lizar.log.Log;
import com.lizar.log.Logger;
import com.lizar.web.Web;
import com.lizar.web.config.Keys;
import com.lizar.web.controller.EventInterceptor;
import com.lizar.web.controller.EventLoader;
import com.tw.domain.uc.Session;
import com.tw.domain.uc.UserCenter;

public class Interceptor implements EventInterceptor {

	private UserCenter ucenter=Web.get(UserCenter.class);
	private SiteTrafficAnalysis site_traffic_analysis=Web.get(SiteTrafficAnalysis.class);
	private Log log=Logger.newInstance(this.getClass());
	@Override
	public void before(EventLoader event_loader) throws ServletException,
			IOException {
		Session u=ucenter.load_session(event_loader,null);
		event_loader.set_attr("user",u);
		if(site_traffic_analysis.traffic_open())site_traffic_analysis.site_traffic(event_loader);
	}

	
	
	@Override
	public void after(EventLoader event_loader) throws ServletException,
			IOException {
		// TODO Auto-generated method stub
	}

}
