package com.tw.persistence.crawl.page;

import com.lizar.web.loader.Cell;
import com.mongodb.DBCursor;
import com.mongodb.Entity;
import com.mongodb.MEntity;
import com.tw.persistence.MongoDao;

public class DoubanMovieDao extends MongoDao implements Cell{
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		this.init("douban_movie");
	}
	
	public void remove(long id){
		this.collection.remove(new MEntity("_id",id));
	}
	
	public long get(){
		Entity e=this.collection.findOne();
		if(e!=null)return e._long("_id");
		return 0;
	}
	
	public long get_min_id(){
		DBCursor cur=this.collection.find(null,new MEntity("_id",1)).sort(new MEntity("_id",1)).skip(0).limit(1);
		if(cur.hasNext())return cur.next()._long("_id");
		return 0;
	}
	
	public long  get_next_id(long id){
		DBCursor cur=this.collection.find(new MEntity("_id",new MEntity("$gt",id)),new MEntity("_id",1)).sort(new MEntity("_id",1)).skip(0).limit(1);
		if(cur.hasNext())return cur.next()._long("_id");
		return 0;
	}
	
	public void insert(long id){
		Entity e=new MEntity();
		e.put("_id", id);
		this.collection.insert(e);
	}
	
	public long count(){
		return this.collection.count();
	}
	
}
