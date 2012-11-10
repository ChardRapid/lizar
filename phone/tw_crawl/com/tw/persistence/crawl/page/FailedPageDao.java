package com.tw.persistence.crawl.page;

import com.lizar.util.MyMath;
import com.lizar.web.loader.Cell;
import com.mongodb.Entity;
import com.mongodb.MEntity;
import com.tw.persistence.MongoDao;

public class FailedPageDao extends MongoDao implements Cell{

	public void init_property() throws Exception {
		this.init("failed_page");
		this.create_index("md5", "md5_index", -1, true);
	}
	
	public void upsert(String url,String msg){
		Entity e=new MEntity();
		e.put("url", url);
		e.put("md5", MyMath.encryptionWithMD5(url));
		e.put("msg", msg);
		Entity update=new MEntity();
		update.put("$set", e);
		update.put("$inc", new MEntity("num",1));
		this.collection.update(new MEntity("md5",e._string("md5")),update,true,false );
	}
	
	public long count(){
		return this.collection.count();
	}
	
	public boolean exists(String url){
		return this.collection.findOne(new MEntity("md5",MyMath.encryptionWithMD5(url)),new MEntity("_id",1))!=null;
	}
	
}
