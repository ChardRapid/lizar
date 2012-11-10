package com.tw.event.mc;

import java.io.IOException;

import javax.servlet.ServletException;

import tw.search.domain.DIndex;
import tw.search.persistence.CelebrityIndexDao;
import tw.search.persistence.IndexInfoDao;
import tw.search.persistence.MovieIndexDao;

import com.lizar.web.Web;
import com.lizar.web.config.Keys;
import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;
import com.mongodb.Entity;
import com.mongodb.MEntity;

public class MCIndex extends Event{

	private IndexInfoDao index_info_dao;
	private MovieIndexDao movie_update_dao;
	private CelebrityIndexDao celebrity_update_dao;
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		index_info_dao=Web.get(IndexInfoDao.class);
		movie_update_dao=Web.get(MovieIndexDao.class);
		celebrity_update_dao=Web.get(CelebrityIndexDao.class);
		Keys.set("index.movie_index_dir", "/WEB-INF/movie_index/");
		Keys.set("index.celebrity_index_dir", "/WEB-INF/celebrity_index/");
		Keys.set("index.thread_interval", "10ms");
	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/mc/index/*";
	}

	@Override
	public void handle(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(el.request_path(2).equals("")){
			index(el);
		}else if(el.request_path(2).equals("movie_index_syn")){
			movie_index_syn(el);
		}else if(el.request_path(2).equals("celebrity_index_syn")){
			celebrity_index_syn(el);
		}else if(el.request_path(2).equals("syn_stop")){
			syn_stop(el);
		}
	}
	
	private void syn_stop(EventLoader el) throws IOException, ServletException{
		if(!DIndex.run){
			el.set_attr("run",false);
			el.set_attr("msg", "index service is not running");
			index(el);
		}else{
			DIndex.run=false;
			el.set_attr("run",DIndex.run);
			el.set_attr("msg", "index service is going to stop");
			index(el);
		}
	}

	private void celebrity_index_syn(EventLoader el) throws IOException, ServletException{
		if(DIndex.run){
			el.set_attr("run",DIndex.run);
			if("movie_index_syn".equals(DIndex.event)){
				el.set_attr("msg", "movie index service is already running............");
				
			}else if("celebrity_index_syn".equals(DIndex.event)){
				el.set_attr("msg", "celebrity index service is already running............");
			}
		}else{
			long count=celebrity_update_dao.count();
			if(count>0l){
				DIndex index=new DIndex();
				DIndex.run=true;
				el.set_attr("run",DIndex.run);
				DIndex.event="celebrity_index_syn";
				Entity event=new MEntity();
				DIndex.start_time=System.currentTimeMillis();
				event.put("start_time", DIndex.start_time);
				event.put("event", "celebrity_index_syn");
				event.put("start_num",count);
				el.set_attr("msg", "celebrity index service is running............");
				index_info_dao.insert(event);
				index.start();
			}else{
				el.set_attr("run",false);
				el.set_attr("msg", "there is no celebrity  info need to update............");
			}
		}
		index(el);
	}
	
	private void movie_index_syn(EventLoader el) throws IOException, ServletException{
		if(DIndex.run){
			el.set_attr("run",DIndex.run);
			if("movie_index_syn".equals(DIndex.event)){
				el.set_attr("msg", "movie index service is already running............");
				
			}else if("celebrity_index_syn".equals(DIndex.event)){
				el.set_attr("msg", "celebrity index service is already running............");
			}
		}else{
			long count=movie_update_dao.count();
			if(count>0l){
				DIndex index=new DIndex();
				DIndex.run=true;
				el.set_attr("run",DIndex.run);
				DIndex.event="movie_index_syn";
				Entity event=new MEntity();
				DIndex.start_time=System.currentTimeMillis();
				event.put("start_time", DIndex.start_time);
				event.put("event", "movie_index_syn");
				event.put("start_num", count);
				el.set_attr("msg", "movie index service is running............");
				index_info_dao.insert(event);
				index.start();
			}else{
				el.set_attr("run",false);
				el.set_attr("msg", "there is no movie info need to update............");
			}
		}
		index(el);
	}
	
	private void index(EventLoader el) throws IOException, ServletException{
		el.set_attr("recent_index",index_info_dao.recent(10));
		el.set_attr("rest_movie_num", movie_update_dao.count());
		el.set_attr("rest_celebrity_num", celebrity_update_dao.count());
		el.template("/WEB-INF/template/mc/search/index.vm");
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
