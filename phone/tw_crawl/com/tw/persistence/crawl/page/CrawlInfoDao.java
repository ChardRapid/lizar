package com.tw.persistence.crawl.page;

import org.bson.types.ObjectId;

import com.lizar.web.loader.Cell;
import com.mongodb.DBCursor;
import com.mongodb.Entity;
import com.mongodb.MEntity;
import com.tw.persistence.MongoDao;

public class CrawlInfoDao extends MongoDao implements Cell {

	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		this.init("crawl_info");
	}
	
	public void insert(Entity crawl){
		this.collection.insert(crawl);
	}
	
	public void upsert_last_crawl(Entity craw_info){
		ObjectId id=(ObjectId)craw_info.removeField("_id");
		this.collection.update(new MEntity("_id",id),new MEntity("$set",craw_info));
	}
	
	public Entity get_last(){
		DBCursor cur=this.collection.find().sort(new MEntity("start_time",-1)).skip(0).limit(1);
		if(cur.hasNext()){
			return cur.next();
		}
		return null;
	}

}
