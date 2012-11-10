package com.tw.event.download;

import java.io.IOException;

import javax.servlet.ServletException;

import org.bson.types.ObjectId;

import com.lizar.web.Web;
import com.lizar.web.config.Config;
import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;
import com.mongodb.Entity;
import com.tw.persistence.download.DownloadDao;
import com.tw.persistence.download.TopDownloadDao;
import com.tw.persistence.movie.MovieDao;

public class Download extends Event{

	private TopDownloadDao top_download_dao;
	private DownloadDao download_dao;
	private MovieDao movie_dao;
	
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		download_dao=Web.get(DownloadDao.class);
		top_download_dao=Web.get(TopDownloadDao.class);
		movie_dao=Web.get(MovieDao.class);
	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/download/*";
	}

	@Override
	public void handle(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String id=el.request_path(1);
		if(ObjectId.isValid(id)){
			download_links(id,el);
		}else if(id.equals("")){
			
		}
	}

	private void download_links(String id,EventLoader el) throws IOException, ServletException{
		Entity e=top_download_dao.get(id);
		if(e==null){
			e=download_dao.get(id);
		}
		if(e==null){
			el.response(Config.xpath_str("server_info.root"));
			return;
		}
		Entity movie_info=movie_dao.get(e._long("movie_id"));
		el.set_attr("dinfo", e);
		el.set_attr("minfo", movie_info);
		el.template("/WEB-INF/template/movie/download.vm");
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

}
