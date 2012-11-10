package com.tw.domain.crawl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import tw.search.persistence.MovieIndexDao;

import com.lizar.log.Log;
import com.lizar.log.Logger;
import com.lizar.util.StringHelper;
import com.lizar.util.http.Http;
import com.lizar.web.Web;
import com.lizar.web.config.Group;
import com.mongodb.Entity;
import com.mongodb.EntityList;
import com.mongodb.MEntity;
import com.mongodb.util.JSON;
import com.tw.event.mc.MovieFetch;
import com.tw.persistence.crawl.MovieSynFailedDao;
import com.tw.persistence.crawl.MovieSynInfoDao;
import com.tw.persistence.crawl.page.DoubanMovieDao;
import com.tw.persistence.movie.MovieDao;
import com.tw.util.XML2JSON;

public class MovieSyn extends Thread {
	private Log log=Logger.newInstance(this.getClass());
	private DoubanMovieDao douban_movie_dao=Web.get(DoubanMovieDao.class);
	private MovieDao movie_dao=Web.get(MovieDao.class);
	private MovieSynFailedDao movie_syn_failed_dao=Web.get(MovieSynFailedDao.class);
	private MovieSynInfoDao movie_syn_info_dao=Web.get(MovieSynInfoDao.class);
	private MovieIndexDao movie_index_dao=Web.get(MovieIndexDao.class);
	/**
	 * 
	 * 更新新检测到 douban_movie中的电影
	 * 
	 * */
	public static final int NEW_DETECT=1;
	
	/**
	 * 
	 * 更新所有电影信息
	 * 
	 * */
	public static final int ALL=3;
	
	private int syn_type =ALL;
	
	public MovieSyn(int syn){
		this.syn_type=syn;
	}
	
	@Override
	public void run() {
		log.info("movie syn is going to begin with syn_type:"+syn_type);
		long start_time=System.currentTimeMillis();
		if(syn_type==NEW_DETECT){
			new_detect();
		}else if(syn_type==ALL){
			update();
			new_detect();
		}
		log.info("movie syn is going to end");
		MovieFetch.run=false;
		movie_syn_info_dao.insert(start_time,System.currentTimeMillis(),syn_type);
	}
	
	private void new_detect(){
		int num=0;
		long id=0;
		while(MovieFetch.run){
			id=douban_movie_dao.get();
			transfer(id);
			douban_movie_dao.remove(id);
			if(id==0)break;
			try {
				Thread.sleep(Group._int("movie_fetch.api_interval"));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			num++;
		}
		log.info("totally handle "+num+" movie information transfer.");
	}
	
	private void transfer(long id){
		log.info("ready to update movie : "+id);
		Entity e=null;
		try{
			e=retreive_movie(id);
		}catch(Exception e1){
			e1.printStackTrace();
			movie_syn_failed_dao.insert(id,e1.getLocalizedMessage());
			return;
		}
		if(e!=null){
			movie_dao.upsert(e);
			movie_index_dao.upsert(id);
		}
	}
	
	
	private static void pick_up_image(Entity json){
		EntityList list=json._list("link");
		if(list!=null){
			for(Object o:list){
				Entity j=(Entity)o;
				if(StringHelper.equals(j._string("@rel"), "image")){
					json.put("image", j._string("@href").replace("spic", "lpic"));
					json.removeField("link");
					return;
				}
			}
		}
	}
	
	private static void pick_up_attribute(Entity json){
		EntityList list=json._list("attribute");
		if(list!=null){
			for(Object o:list){
				Entity j=(Entity)o;
				if(StringHelper.equals(j._string("@name"), "year")){
					try{
						json.put("year", Integer.parseInt(j._string("#text")));
					}catch(Exception e){
						json.put("year", 0);
					}
				}else if(StringHelper.equals(j._string("@name"), "movie_duration")){
					json.put("movie_duration", j._string("#text"));
				}else if(StringHelper.equals(j._string("@name"), "country")){
					json.put("country", j._string("#text"));
				}else if(StringHelper.equals(j._string("@name"), "language")){
					json.put("language", j._string("#text"));
				}else if(StringHelper.equals(j._string("@name"), "pubdate")){
					json.put("pubdate", j._string("#text"));
				}else if(StringHelper.equals(j._string("@name"), "website")){
					json.put("website", j._string("#text"));
				}else if(StringHelper.equals(j._string("@name"), "imdb")){
					json.put("imdb", j._string("#text"));
				}else if(StringHelper.equals(j._string("@name"), "episodes")){
					try{
						json.put("episodes", Integer.parseInt(j._string("#text","0")));
					}catch(Exception e){
						json.put("episodes", 0);
					}
				}else if(StringHelper.equals(j._string("@name"), "writer")){
					EntityList writer=json._list("writer");
						if(writer==null){
						writer=new EntityList();
					}
					writer.add(j._string("#text"));
					json.put("writer", writer);
				}else if(StringHelper.equals(j._string("@name"), "movie_type")){
					EntityList movie_type=json._list("movie_type");
					if(movie_type==null){
						movie_type=new EntityList();
					}
					movie_type.add(j._string("#text"));
					json.put("movie_type", movie_type);
				}else if(StringHelper.equals(j._string("@name"), "director")){
					EntityList director=json._list("director");
					if(director==null){
						director=new EntityList();
					}
					director.add(j._string("#text"));
					json.put("director", director);
				}else if(StringHelper.equals(j._string("@name"), "aka")&&StringHelper.isNotNull(j._string("#text"))){
					if(StringHelper.equals(j._string("@lang"), "zh_CN")){
						json.put("title_cn", j._string("#text"));
						continue;
					}
					EntityList writer=json._list("keys");
					if(writer==null){
						writer=new EntityList();
					}
					writer.add(j._string("#text").toLowerCase());
					json.put("keys", writer);
				}else if(StringHelper.equals(j._string("@name"), "cast")){
					EntityList cast=json._list("cast");
					if(cast==null){
						cast=new EntityList();
					}
					cast.add(j._string("#text"));
					json.put("cast", cast);
				}
			}
			json.removeField("attribute");
		}
		
	}
	
	public static Object get_movie_info(long id){
		String result=null;
		try {
			result=Http.get("http://api.douban.com/movie/subject/"+id+"?apikey="+Group._str("movie_fetch.apikey"), 10000);
		} catch (IOException e) {
			e.printStackTrace();
			return e.getLocalizedMessage();
		}
		if(StringHelper.isNull(result)){
			return "no response msg get from :"+"http://api.douban.com/movie/subject/"+id+"?apikey="+Group._str("movie_fetch.apikey");
		}
		XML2JSON test=new XML2JSON();
		result=result.replaceAll("db:attribute", "attribute").replaceAll("db:tag", "tag").replaceAll("gd:rating", "rating");
		Entity json=null;
		try {
			json = (Entity)JSON.parse(test.xml2json(result));
		} catch (IOException e) {
			e.printStackTrace();
			return e.getLocalizedMessage();
		}
		json.removeField("@xmlns:opensearch");
		json.removeField("@xmlns:openSearch");
		json.removeField("@xmlns");
		json.removeField("@xmlns:db");
		json.removeField("category");
		json.removeField("@xmlns:gd");
		json.removeField("author");
		json.put("id", id);
		pick_up_image(json);
		pick_up_attribute(json);
		transfer_rating(json);
		transfer_tag(json);
		transfer_keys(json);
		return json;
	}
	
	public  Entity retreive_movie(long id){
		String result=null;
		try {
			result=Http.get("http://api.douban.com/movie/subject/"+id+"?apikey="+Group._str("movie_fetch.apikey"), 10000);
		} catch (IOException e) {
			e.printStackTrace();
			movie_syn_failed_dao.insert(id,e.getLocalizedMessage());
			return null;
		}
		if(StringHelper.isNull(result)){
			movie_syn_failed_dao.insert(id,"result is null");
			return null;
		}
		XML2JSON test=new XML2JSON();
		result=result.replaceAll("db:attribute", "attribute").replaceAll("db:tag", "tag").replaceAll("gd:rating", "rating");
		Entity json=null;
		try {
			json = (Entity)JSON.parse(test.xml2json(result));
		} catch (IOException e) {
			e.printStackTrace();
			movie_syn_failed_dao.insert(id,e.getLocalizedMessage());
			return null;
		}
		json.removeField("@xmlns:opensearch");
		json.removeField("@xmlns:openSearch");
		json.removeField("@xmlns");
		json.removeField("@xmlns:db");
		json.removeField("category");
		json.removeField("@xmlns:gd");
		json.removeField("author");
		json.put("id", id);
		pick_up_image(json);
		pick_up_attribute(json);
		transfer_rating(json);
		transfer_tag(json);
		transfer_keys(json);
		return json;
	}
	
	private static void transfer_keys(Entity json){
		EntityList aka=json._list("keys");
		if(StringHelper.isNotNull(json._string("title"))){
			aka.add(json._string("title").toLowerCase());
		}
		if(StringHelper.isNotNull(json._string("title_cn"))){
			aka.add(json._string("title_cn").toLowerCase());
		}
		json.put("keys", aka);
	}
	
	private static void transfer_rating(Entity json){
		Entity rating=json._entity("rating");
		if(rating!=null){
			rating.removeField("@min");
			rating.removeField("@max");
			rating.put("average", rating.removeField("@average"));
			rating.put("numRaters", rating.removeField("@numRaters"));
		}
	}
	
	private static void transfer_tag(Entity json){
		if(json.get("tag")==null)return;
		EntityList tag_list;
		if(json.get("tag") instanceof MEntity){
			tag_list=new EntityList();
			tag_list.add(json._entity("tag")._string("@name"));
			json.put("tag", tag_list);
		}else if(json.get("tag") instanceof EntityList){
			EntityList tags=json._list("tag");
			if(tags!=null){
				tag_list=new EntityList();
				Entity tag;
				for(Object o:tags){
					tag=(Entity)o;
					tag_list.add(tag.get("@name"));
				}
				json.put("tag", tag_list);
			}
		}else json.removeField("tag");
	}
	
	
	
	private  void update(){
		long num=movie_dao.count();
		long id=movie_dao.get_min_id();
		if(id==0){
			log.info("totally handle "+num+" movie information transfer.");
			return;
		}
		while(MovieFetch.run){
			transfer(id);
			id=movie_dao.get_next_id(id);
			if(id==0)break;
			try {
				Thread.sleep(Group._int("movie_fetch.api_interval"));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		log.info("totally handle "+num+" movie information transfer.");
	}
	
}
