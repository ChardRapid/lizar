package com.tw.event.mc;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import org.bson.types.ObjectId;

import com.lizar.web.Web;
import com.lizar.web.config.Config;
import com.lizar.web.config.Keys;
import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;
import com.mongodb.Entity;
import com.mongodb.MEntity;
import com.tw.domain.crawl.CrawlLoader;
import com.tw.domain.uc.Session;
import com.tw.persistence.AdminDao;
import com.tw.persistence.crawl.page.CrawlInfoDao;
import com.tw.persistence.crawl.page.CrawledPageDao;
import com.tw.persistence.crawl.page.DoubanCelebrityDao;
import com.tw.persistence.crawl.page.DoubanMovieDao;
import com.tw.persistence.crawl.page.FailedPageDao;
import com.tw.persistence.crawl.page.NewPageDao;

public class PageCrawl extends Event{

	private CrawlLoader crawl_loader;
	
	private static boolean crawl_started=false;

	private static Entity last_crawl;
	
	private  CrawlInfoDao crawl_info_dao;
	private AdminDao admin_dao;
	private FailedPageDao failed_page_dao;
	private CrawledPageDao crawled_page_dao;
	private NewPageDao new_page_dao;
	private DoubanMovieDao douban_movie_dao;
	private DoubanCelebrityDao celebrity_dao;
	@Override
	public void init_property() throws Exception {
		crawl_info_dao=Web.get(CrawlInfoDao.class);
		admin_dao=Web.get(AdminDao.class);
		crawl_loader=Web.get(CrawlLoader.class);
		failed_page_dao=Web.get(FailedPageDao.class);
		crawled_page_dao=Web.get(CrawledPageDao.class);
		new_page_dao=Web.get(NewPageDao.class);
		douban_movie_dao=Web.get(DoubanMovieDao.class);
		celebrity_dao=Web.get(DoubanCelebrityDao.class);
		last_crawl=crawl_info_dao.get_last();
		System.out.println(last_crawl);
		Keys.set("crawl.page_crawl_detected_interval", 10000);
		Thread crawl=new Thread(){
			@Override
			public void run() {
				while(true){
					if(crawl_started){
						System.out.println("crawl is going to begin");
						crawl();
						System.out.println("crawl is going to end");
						crawl_started=false;
						crawl_info_dao.upsert_last_crawl(last_crawl);
					}else{
						try {
							this.sleep(Keys._int("crawl.page_crawl_detected_interval"));
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		};
		crawl.start();
	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/mc_page_crawl/*";
	}

	@Override
	public void handle(EventLoader el) throws ServletException, IOException {
		Session user=el.get_attr("user",null);
		if(user==null||!admin_dao.is_super(user.getUser_id())){
			el.response(Config.xpath_str("server_info.root"));
			return;
		}
		el.set_attr("crawl_started", crawl_started);
		el.set_attr("movie_num", douban_movie_dao.count());
		el.set_attr("page_num", crawled_page_dao.count());
		el.set_attr("failed_page_num", failed_page_dao.count());
		el.set_attr("new_page_num", new_page_dao.count());
		el.set_attr("celebrity_num", celebrity_dao.count());
		if(el.request_path(1).equals("")){
			index(el);
		}else if(el.request_path(1).equals("start")){
			if(crawl_started){
				el.set_attr("msg", "crawl is already started.");
				el.set_attr("last_crawl",last_crawl);
				el.template("/WEB-INF/template/mc/crawl/index.vm");
			}else{
				crawl_started=true;
				CrawlLoader.run=true;
				last_crawl=new MEntity();
				last_crawl.put("start_time", new Date(System.currentTimeMillis()));
				last_crawl.put("max_threads", 1);
				last_crawl.put("_id", new ObjectId());
				crawl_info_dao.insert(last_crawl);
				el.set_attr("last_crawl",last_crawl);
				el.set_attr("crawl_started", crawl_started);
				el.set_attr("msg", "crawl is going to running within "+Keys._int("crawl.page_crawl_detected_interval")+" miliseconds");
				el.template("/WEB-INF/template/mc/crawl/index.vm");
			}
		}else if(el.request_path(1).equals("stop")){
			if(crawl_started&&!CrawlLoader.run){
				el.set_attr("msg","crawl is going to end");
			}else if(!crawl_started){
				el.set_attr("msg","crawl is already ended.");
			}else{
				CrawlLoader.run=false;
				el.set_attr("msg","crawl is going to end");
			}
			el.set_attr("crawl_started", crawl_started);
			index(el);
		}
	}

	private void index(EventLoader el) throws IOException, ServletException{
		el.set_attr("last_crawl",last_crawl);
		el.template("/WEB-INF/template/mc/crawl/index.vm");
	}
	
	private void crawl(){
		crawl_loader.begin(last_crawl);
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
	public void handle_xml(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void before(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void after(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

}
