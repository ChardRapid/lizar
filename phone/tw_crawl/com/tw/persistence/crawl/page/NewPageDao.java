package com.tw.persistence.crawl.page;

import com.lizar.util.MyMath;
import com.lizar.web.loader.Cell;
import com.mongodb.Entity;
import com.mongodb.MEntity;
import com.tw.persistence.MongoDao;

public class NewPageDao extends MongoDao implements Cell{
	@Override
	public void init_property() throws Exception {
		this.init("new_page");
		this.create_index("md5", "md5_index", -1, true);
	}
	
	public boolean exists(String url){
		return this.collection.findOne(new MEntity("md5",MyMath.encryptionWithMD5(url)),new MEntity("_id",1))!=null;
	}
	
	public long count(){
		return this.collection.count();
	}
	
	public String find_one(){
		Entity e=this.collection.findOne(null,new MEntity("url",1));
		if(e!=null)return e._string("url");
		return null;
	}
	
	public void delete(String url){
		this.collection.remove(new MEntity("md5", MyMath.encryptionWithMD5(url)));
	}
	
	public void insert(String url,long time){
		Entity e=new MEntity();
		e.put("url", url);
		e.put("time", time);
		e.put("md5", MyMath.encryptionWithMD5(url));
		this.collection.update(new MEntity("md5",e._string("md5")),e,true,false );
	}
}
