package com.tw.event.mc;

import java.io.IOException;

import javax.servlet.ServletException;

import tw.search.persistence.MovieIndexDao;

import com.lizar.web.Web;
import com.lizar.web.config.Config;
import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;
import com.mongodb.Entity;
import com.mongodb.EntityList;
import com.tw.domain.crawl.MovieSyn;
import com.tw.domain.uc.Session;
import com.tw.persistence.AdminDao;
import com.tw.persistence.crawl.page.DoubanMovieDao;
import com.tw.persistence.movie.MovieDao;


public class MCMovie extends Event {
	private DoubanMovieDao douban_movie_dao;
	private MovieDao movie_dao;
	private AdminDao admin_dao;
	private MovieIndexDao movie_index_dao;
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		douban_movie_dao=Web.get(DoubanMovieDao.class);
		movie_dao=Web.get(MovieDao.class);
		admin_dao=Web.get(AdminDao.class);
		movie_index_dao=Web.get(MovieIndexDao.class);
	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/mc/movie/*";
	}

	@Override
	public void handle(EventLoader el) throws ServletException, IOException {
		Session user=el.get_attr("user",null);
		if(user==null||!admin_dao.is_super(user.getUser_id())){
			el.response(Config.xpath_str("server_info.root"));
			return;
		}
		if(el.request_path().equals("/mc/movie")){
			index(el);
		}else if(el.request_path(3).equals("add")){
			add(el);
		}else if(el.request_path(3).equals("modify")){
			el.template("/WEB-INF/template/mc/movie/modify.vm");
		}
	}
	
	private void add(EventLoader el) throws IOException, ServletException{
		long id=el._long("id");
		Object o=MovieSyn.get_movie_info(id);
		if(o instanceof String){
			el.set_attr("msg", o.toString());
			index(el);
		}else{
			movie_dao.upsert((Entity)o);
			movie_index_dao.upsert(id);
		}
		index(el);
	}

	private void index(EventLoader el) throws IOException, ServletException{
		int year=el._int("year", 0);
		EntityList list=null;
		if(year>0){
			list=movie_dao.get_by_year(year);
			cut_summary(list);
			el.set_attr("year", year);
			el.set_attr("list", list);
		}
		el.set_attr("douban_movie_num", douban_movie_dao.count());
		el.set_attr("movie_profile_num", movie_dao.count());
		el.template("/WEB-INF/template/mc/movie/index.vm");
	}
	
	private void cut_summary(EntityList list){
		if(list!=null){
			Entity e;
			for(Object o:list){
				e=(Entity)o;
				if(e._string("summary").length()>100)e.put("summary",e._string("summary").substring(0, 100)+"...");
			}
		}
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
