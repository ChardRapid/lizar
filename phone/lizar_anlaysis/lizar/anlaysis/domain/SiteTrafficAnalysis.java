package lizar.anlaysis.domain;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import lizar.anlaysis.persistence.DailyTrafficDao;
import lizar.anlaysis.persistence.IPTrafficDao;
import lizar.anlaysis.persistence.RefererDao;

import com.lizar.log.Log;
import com.lizar.log.Logger;
import com.lizar.util.StringHelper;
import com.lizar.util.http.Http;
import com.lizar.web.Web;
import com.lizar.web.config.Keys;
import com.lizar.web.controller.EventLoader;
import com.lizar.web.loader.Plugin;
import com.mongodb.Entity;
import com.mongodb.EntityList;

public class SiteTrafficAnalysis extends Plugin {
	private IPTrafficDao traffic_dao;
	private DailyTrafficDao daily_dao;
	private RefererDao referer_dao;
	private Log log=Logger.newInstance(this.getClass());
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		traffic_dao=Web.get(IPTrafficDao.class);
		daily_dao=Web.get(DailyTrafficDao.class);
		referer_dao=Web.get(RefererDao.class);
		Keys.set("lizar.traffic_open", true);
	}
	
	public boolean traffic_open(){
		return Keys._bool("lizar.traffic_open");
	}

	public void site_traffic(EventLoader event_loader){
		pv_map(event_loader);
		ip_traffic(event_loader);
		refer_record(event_loader);
	}
	
	private void refer_record(EventLoader event_loader){
		String record=event_loader.request().getHeader("referer");
		if(StringHelper.isNotNull(record)){
			 record=Http.getWholeDomain(record);
			 referer_dao.update(event_loader.request_path(),record);
		}
	}
	
	private void ip_traffic(EventLoader event_loader){
		String id=event_loader.client_ip();
		if(!StringHelper.equals("0:0:0:0:0:0:0:1", id)){
			traffic_dao.update(event_loader.request_path(),id);
		}
	}
	
	private void pv_map(EventLoader event_loader){
		daily_dao.inc_pv(event_loader.request_path(), Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime())));
	}
	
	@Override
	public void pre_run() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		log.info(".............................................................................");
		log.info("\t\t\t\t ready to update site traffic information.");
		int time=Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime()));
		EntityList list=daily_dao.get(time);
		Entity e;
		if(list!=null){
			for(Object o:list){
				e=(Entity)o;
				long num=traffic_dao.get_traffic(e._string("uri"));
				EntityList refer_list=referer_dao.get(e._string("uri"));
				daily_dao.update_ip(e._string("uri"),time,num,refer_list);
			}
		}
		traffic_dao.clean();
		referer_dao.clean();
		log.info(".............................................................................");
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
		return "1d";
	}

	@Override
	public String set_delay_time() {
		// TODO Auto-generated method stub
		return null;
	}

}
