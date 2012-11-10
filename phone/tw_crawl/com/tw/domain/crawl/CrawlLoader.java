package com.tw.domain.crawl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.lizar.log.Log;
import com.lizar.log.Logger;
import com.lizar.util.StringHelper;
import com.lizar.web.Web;
import com.lizar.web.config.Group;
import com.lizar.web.loader.Cell;
import com.mongodb.Entity;
import com.tw.persistence.crawl.page.CrawledPageDao;
import com.tw.persistence.crawl.page.FailedPageDao;
import com.tw.persistence.crawl.page.NewPageDao;

public class CrawlLoader implements Cell{
	private Log log=Logger.newInstance(this.getClass());
	private FailedPageDao failed_page_dao;
	private CrawledPageDao crawled_page_dao;
	private NewPageDao new_page_dao;
	
	private static final String START_URL="http://movie.douban.com";
	
	public static final int DEFAULT_MAX_THREADS=1;
	
	public static boolean run=true;
	
	private List<Crawl> crawls=new ArrayList<Crawl>();
	
	
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		failed_page_dao=Web.get(FailedPageDao.class);
		crawled_page_dao=Web.get(CrawledPageDao.class);
		new_page_dao=Web.get(NewPageDao.class);
		Group.set_up("crawl", "thread_sleep_interval", 5*1000);
		Group.set_up("crawl_disallow_map", "/subject_search", true);
		Group.set_up("crawl_disallow_map", "/amazon_search", true);
		Group.set_up("crawl_disallow_map", "/forum", true);
		Group.set_up("crawl_disallow_map", "/new_subject", true);
		Group.set_up("crawl_disallow_map", "/service/iframe", true);
		Group.set_up("crawl_disallow_map", "/j", true);
		Group.set_up("crawl_disallow_map", "/link2", true);
		Group.set_up("crawl_disallow_map", "/recommend", true);
		Group.set_up("crawl_disallow_map", "/mupload", true);
		Group.set_up("crawl_disallow_map", "/ticket", true);
	}

	/**
	 * 
	 * crawl_info includes max_threads and start_time
	 * 
	 * 
	 * 
	 * */
	public void begin(Entity crawl_info){
		int max_threads=crawl_info._int("max_threads");
		if(max_threads<=0)max_threads=DEFAULT_MAX_THREADS;
		if(!crawled_page_dao.exists(START_URL)){
			new_page_dao.insert(START_URL,0);
		}
		init_threads(max_threads);
		long start_new_page_num=new_page_dao.count();
		long start_page_num=crawled_page_dao.count();
		long start_failed_num=failed_page_dao.count();
		String url=new_page_dao.find_one();
		log.info(" start with new page num:"+start_new_page_num);
		while(run){
			if(StringHelper.isNotNull(url)){
				log.info(url);
				new_page_dao.delete(url);
				push_to(url);
				url=new_page_dao.find_one();
				if(min_size()>50){
					try {
						Thread.sleep(Group._int("crawl.thread_sleep_interval")*5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}	
				}
			}else{
				try {
					Thread.sleep(Group._int("crawl.thread_sleep_interval")*60);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				url=new_page_dao.find_one();
				if(StringHelper.isNull(url)&&max_size()==0){
					run=false;
				}
			}
		}
		run=false;
		crawl_info.put("start_new_page_num", start_new_page_num);
		crawl_info.put("end_new_page_num", new_page_dao.count());
		crawl_info.put("page_num", crawled_page_dao.count()-start_page_num);
		crawl_info.put("failed_num", failed_page_dao.count()-start_failed_num);
		crawl_info.put("end_time", new Date(System.currentTimeMillis()));
	}
	
	private int max_size(){
		int max=crawls.get(0).queue.size();
		int temp;
		for(int i=1;i<crawls.size();i++){
			temp=crawls.get(i).queue.size();
			if(temp>max)max=temp;
		}
		return max;
	}
	
	private int min_size(){
		int min=crawls.get(0).queue.size();
		int temp;
		for(int i=1;i<crawls.size();i++){
			temp=crawls.get(i).queue.size();
			if(temp<min)min=temp;
		}
		return min;
	}
	
	private void push_to(String url){
		int min=crawls.get(0).queue.size();
		int temp;
		int index=0;
		for(int i=1;i<crawls.size();i++){
			temp=crawls.get(i).queue.size();
			if(temp<min){
				min=temp;
				index=i;
			}
		}
		crawls.get(index).push_task(url);
	}
	
	private void init_threads(int max_threads){
		crawls.clear();
		for(int i=0;i<max_threads;i++){
			Crawl c=new Crawl();
			crawls.add(c);
			c.start();
		}
	}
	
}
