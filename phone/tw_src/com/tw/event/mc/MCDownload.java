package com.tw.event.mc;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;


import org.bson.types.ObjectId;

import tw.search.persistence.MovieIndexDao;

import com.lizar.util.StringHelper;
import com.lizar.web.Web;
import com.lizar.web.config.Config;
import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;
import com.mongodb.Entity;
import com.mongodb.EntityList;
import com.mongodb.MEntity;
import com.tw.domain.movie.DDownload;
import com.tw.domain.uc.Session;
import com.tw.persistence.AdminDao;
import com.tw.persistence.download.DownloadDao;
import com.tw.persistence.download.TopDownloadDao;
import com.tw.persistence.movie.MovieDao;

public class MCDownload extends Event{
	private AdminDao admin_dao;
	private MovieDao movie_dao;
	private DownloadDao download_dao;
	private TopDownloadDao top_download_dao;
	private MovieIndexDao movie_update_dao;
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		admin_dao=Web.get(AdminDao.class);
		movie_dao=Web.get(MovieDao.class);
		download_dao=Web.get(DownloadDao.class);
		top_download_dao=Web.get(TopDownloadDao.class);
		movie_update_dao=Web.get(MovieIndexDao.class);
	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/mc/download/*";
	}

	@Override
	public void handle(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Session user=el.get_attr("user",null);
		if(user==null||!admin_dao.is_super(user.getUser_id())){
			el.response(Config.xpath_str("server_info.root"));
			return;
		}
		if(el.request_path(2).equals("")){
			index(el);
		}else if(el.request_path(2).equals("add")){
			add(el);
		}else if(el.request_path(2).equals("recent")){
			recent(el);
		}else if(el.request_path(2).equals("top")){
			if(el.request_path(3).equals("cancel"))cancel_top(el);
			else top(el);
		}else if(el.request_path(2).equals("delete")){
			delete(el);
		}else if(el.request_path(2).equals("update")){
			index(el);
		}
		
	}
	
	private void cancel_top(EventLoader el) throws IOException, ServletException{
		String id=el._str("id");
		if(!ObjectId.isValid(id)){
			el.set_attr("msg", "id is not valid");
		}else{
			top_download_dao.remove(id);
			download_dao.cancel_top(id);
		}
		index(el);
	}
	
	private void top(EventLoader el) throws IOException, ServletException{
		String id=el._str("id");
		if(!ObjectId.isValid(id)){
			el.set_attr("msg", "id is not valid");
		}else{
			Entity e=download_dao.top(id);
			if(e==null){
				el.set_attr("msg", "id is not valid");
			}else if(e._bool("top")){
				el.set_attr("msg", "resource already top");
			}else{
				e.put("top", true);
				e.put("index", top_download_dao.max_index()+1);
				top_download_dao.insert(e);
				el.set_attr("msg", "top successfully.");
			}
		}
		recent(el);
	}
	
	private void delete(EventLoader el) throws IOException, ServletException{
		String id=el._str("id");
		String str=el.request().getHeader("referer");
		String to="index";
		if(str!=null&&str.endsWith("/recent")){
			to="recent";
		}
		if(!ObjectId.isValid(id)){
			el.set_attr("msg", "id is not valid");
		}else{
			top_download_dao.remove(id);
			download_dao.remove(id);
			el.set_attr("msg", "delete successfully.");
		}
		if(to.equals("recent"))recent(el);
		else index(el);
	}

	private void recent(EventLoader el) throws IOException, ServletException{
		int timeline=el._int("timeline");
		EntityList list=null;
		if(timeline==0)list=download_dao.find_recent(50);
		else list=download_dao.find(timeline,50);
		if(list==null||list.size()<50){
			el.set_attr("pre", timeline);
			el.set_attr("next", 0);
		}else {
			el.set_attr("pre", timeline);
			el.set_attr("next", ((Entity)list.get(list.size()-1))._long("create_time"));
		}
		el.set_attr("list", list);
		el.template("/WEB-INF/template/mc/download/recent.vm");
	}
	
	private void index(EventLoader el) throws IOException, ServletException{
		int start_index=el._int("start_index");
		EntityList list=null;
		if(start_index==0) list=top_download_dao.first(50);
		else top_download_dao.find(start_index,50);
		if(list==null||list.size()<50){
			el.set_attr("pre", start_index);
			el.set_attr("next", 0);
		}else 	{
			el.set_attr("pre", start_index);
			el.set_attr("next", ((Entity)list.get(list.size()-1))._long("index"));
		}
		el.set_attr("list", list);
		el.template("/WEB-INF/template/mc/download/index.vm");
	}

	private void add(EventLoader el) throws IOException, ServletException{
		long movie_id=el._long("movie_id");
		if(movie_id==0){
			el.template("/WEB-INF/template/mc/download/add.vm");
			return;
		}
		if(!movie_dao.exists(movie_id)){
			el.set_attr("msg", "movie id :"+movie_id+" is not exists.");
			el.template("/WEB-INF/template/mc/download/add.vm");
			return;
		}
		int quality=el._int("quality",-1);
		if(!DDownload.validate_quality(quality)){
			el.set_attr("msg", "wrong quality type.");
			el.template("/WEB-INF/template/mc/download/add.vm");
			return;
		}
		String links=el._str("links");
		if(StringHelper.isNull(links)){
			el.set_attr("msg", "download links is needed");
			el.template("/WEB-INF/template/mc/download/add.vm");
			return;
		}
		double size=el._double("size");
		int size_type=el._int("size_type",-1);
		if(!DDownload.validate_size_type(size_type)){
			el.set_attr("msg", "wrong size type.");
			el.template("/WEB-INF/template/mc/download/add.vm");
			return;
		}
		
		int subtitle=el._int("subtitle",-1);
		if(subtitle==-1){
			el.set_attr("msg", "subtitle type wrong.");
			el.template("/WEB-INF/template/mc/download/add.vm");
			return;
		}
		
		String title=el._str("title");
		if(StringHelper.isNull(title)){
			el.set_attr("msg", "title cannot be null");
			el.template("/WEB-INF/template/mc/download/add.vm");
			return;
		}
		String screenshot=el._str("screenshot");
		String comments=el._str("comments");
		Entity download=new MEntity();
		download.put("movie_id", movie_id);
		download.put("quality", quality);
		download.put("title", title);
		download.put("links", DDownload.split_links(links));
		download.put("screenshot",  DDownload.split_links(screenshot));
		download.put("comments",  DDownload.split_links(comments));
		download.put("size", size);
		download.put("size_type", size_type);
		download.put("create_time", System.currentTimeMillis());
		download.put("num", new Random().nextInt(7000)+700);
		download_dao.insert(download);
		movie_dao.update_top_effect(movie_id,quality);
		movie_update_dao.upsert(movie_id);
		el.set_attr("msg", "data insert successfully.");
		el.template("/WEB-INF/template/mc/download/add.vm");
		return;
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
