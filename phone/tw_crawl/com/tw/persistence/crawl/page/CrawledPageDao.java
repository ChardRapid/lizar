package com.tw.persistence.crawl.page;

import com.lizar.util.MyMath;
import com.lizar.web.loader.Cell;
import com.mongodb.Entity;
import com.mongodb.MEntity;
import com.tw.persistence.MongoDao;

public class CrawledPageDao extends MongoDao implements Cell{
	
	@Override
	public void init_property() throws Exception {
		this.init("crawled_page");
		this.create_index("md5", "md5_index", -1, true);
	}
	
	public boolean exists(String url){
		return this.collection.findOne(new MEntity("md5",MyMath.encryptionWithMD5(url)),new MEntity("_id",1))!=null;
	}
	
	public long count(){
		return this.collection.count();
	}
	
	public void insert(String url,int num,int movie_num,int celebrity_num){
		Entity e=new MEntity();
		e.put("url", url);
		e.put("md5", MyMath.encryptionWithMD5(url));
		e.put("num", num);
		e.put("movie_num", movie_num);
		e.put("celebrity_num", celebrity_num);
		this.collection.insert(e);
	}
}
