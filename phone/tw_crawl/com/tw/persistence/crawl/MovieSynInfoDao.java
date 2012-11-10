package com.tw.persistence.crawl;

import com.lizar.web.loader.Cell;
import com.mongodb.Entity;
import com.mongodb.MEntity;
import com.tw.persistence.MongoDao;

public class MovieSynInfoDao extends MongoDao implements Cell{
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		this.init("movie_syn_info");
	}
	
	public void insert(long start,long end,int syn_type){
		Entity e=new MEntity();
		e.put("start_time", start);
		e.put("end_time", end);
		e.put("syn_type", syn_type);
		this.collection.insert(e);
	}
	
}
