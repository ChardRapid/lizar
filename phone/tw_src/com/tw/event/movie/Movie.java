package com.tw.event.movie;

import java.io.IOException;

import javax.servlet.ServletException;

import com.lizar.util.StringHelper;
import com.lizar.web.Web;
import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;
import com.mongodb.Entity;
import com.mongodb.EntityList;
import com.tw.persistence.download.DownloadDao;
import com.tw.persistence.download.TopDownloadDao;
import com.tw.persistence.movie.MovieDao;

public class Movie extends Event{

	private MovieDao movie_dao;
	private TopDownloadDao top_download_dao;
	private DownloadDao download_dao; 
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		movie_dao=Web.get(MovieDao.class);
		download_dao=Web.get(DownloadDao.class);
		top_download_dao=Web.get(TopDownloadDao.class);
	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/movie/*";
	}

	@Override
	public void handle(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//电影列表（1！10000）
		//单个电影
		//电影评论
		//最新上映
		//经典系列
		//热门电影
		if(StringHelper.isLong(el.request_path(1))){
			if(el.request_path(2).equals("")){
				movie(el);
			}
		}
	}
	
	private void movie(EventLoader el) throws IOException, ServletException{
		long id=Long.parseLong(el.request_path(1));
		Entity e=movie_dao.get(id);
		if(e==null){
			el.html("/WEB-INF/lizar/404.html");
			return;
		}
		el.set_attr("movie", e);
		EntityList links=top_download_dao.get(id);
		if(links==null){
			links=download_dao.get(id);
		}
		el.set_attr("dlinks", links);
		el.template("/WEB-INF/template/movie/index.vm");
	}
	
	private void profile(String uname,String id,EventLoader el) throws IOException, ServletException{
		el.template("/WEB-INF/template/record/index.vm");
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
	public void handle_xml(EventLoader event_loader) throws ServletException,
			IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void before(EventLoader event_loader) throws ServletException,
			IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void after(EventLoader event_loader) throws ServletException,
			IOException {
		// TODO Auto-generated method stub
		
	}

	public static void main(String[] args) {
		
	}

}
