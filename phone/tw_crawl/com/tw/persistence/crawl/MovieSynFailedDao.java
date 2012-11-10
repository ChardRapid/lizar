package com.tw.persistence.crawl;

import java.util.Date;

import com.lizar.web.loader.Cell;
import com.mongodb.Entity;
import com.mongodb.MEntity;
import com.tw.persistence.MongoDao;

public class MovieSynFailedDao extends MongoDao implements Cell {
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		this.init("movie_syn_failed");
	}
	
	public void insert(long id,String msg){
		Entity e=new MEntity();
		e.put("id", id);
		e.put("msg", msg);
		e.put("start_time", new Date(System.currentTimeMillis()));
		this.collection.insert(e);
	}
	
}
