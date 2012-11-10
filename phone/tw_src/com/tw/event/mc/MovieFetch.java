package com.tw.event.mc;

import java.io.IOException;

import javax.servlet.ServletException;

import com.lizar.web.Web;
import com.lizar.web.config.Config;
import com.lizar.web.config.Group;
import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;
import com.tw.domain.crawl.MovieSyn;
import com.tw.domain.uc.Session;
import com.tw.persistence.AdminDao;
import com.tw.persistence.crawl.page.DoubanMovieDao;
import com.tw.persistence.movie.MovieDao;

public class MovieFetch extends Event{
	private AdminDao admin_dao;
	private DoubanMovieDao douban_movie_dao;
	private MovieDao movie_dao;
	public static boolean run=false;
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		admin_dao=Web.get(AdminDao.class);
		douban_movie_dao=Web.get(DoubanMovieDao.class);
		movie_dao=Web.get(MovieDao.class);
		Group.set_up("movie_fetch", "apikey", "0c3246ce56721e1b2496d46b03565a51");
		Group.set_up("movie_fetch", "api_interval", 1500);
	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/mc_movie_fetch/*";
	}

	@Override
	public void handle(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Session user=el.get_attr("user",null);
		if(user==null||!admin_dao.is_super(user.getUser_id())){
			el.response(Config.xpath_str("server_info.root"));
			return;
		}
		if(run){
			el.set_attr("msg", "正在更新电影信息。。。。。。。。。。。");
		}
		if(el.request_path(1).equals("syn")){
			syn(el);
		}else if(el.request_path(1).equals("cancel")){
			if(run){
				run=false;
				el.set_attr("msg", "电影信息更新任务已取消。。。。。。。。。。。");
			}else{
				el.set_attr("msg", "当前没有电影信息更新任务。。。。。。。。。。。");
			}
		}
		index(el);
	}

	private void syn(EventLoader el) throws IOException, ServletException{
		if(el.request_path(2).equals("new_detect")){
			if(!run){
				run=true;
				MovieSyn movie=new MovieSyn(MovieSyn.NEW_DETECT);
				movie.start();
				el.set_attr("msg", "转移新电影马上开始。。。。。。。。。。。");
			}else{
				el.set_attr("msg", "转移新电影正在进行中。。。。。。。。。。。");
			}
		}else if(el.request_path(2).equals("all")){
			if(!run){
				run=true;
				MovieSyn movie=new MovieSyn(MovieSyn.ALL);
				el.set_attr("msg", "即将更新所有电影数据.............");
				movie.start();
			}else{
				el.set_attr("msg", "正在更新所有电影数据正在进行中.............");
			}
		}
	}
	
	private void index(EventLoader el) throws IOException, ServletException{
		el.set_attr("run", run);
		el.set_attr("douban_movie_num", douban_movie_dao.count());
		el.set_attr("movie_profile_num", movie_dao.count());
		el.template("/WEB-INF/template/mc/movie/crawl.vm");
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
