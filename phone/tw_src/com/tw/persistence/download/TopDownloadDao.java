package com.tw.persistence.download;

import org.bson.types.ObjectId;

import com.lizar.web.loader.Cell;
import com.mongodb.DBCursor;
import com.mongodb.Entity;
import com.mongodb.EntityList;
import com.mongodb.MEntity;
import com.tw.domain.movie.DDownload;
import com.tw.persistence.MongoDao;

/**
 * 
 * 置顶推荐，首页最新的下载
 * 
 * */
public class TopDownloadDao extends MongoDao implements Cell {
	public void init_property() throws Exception {
		this.init("top_download");
		this.create_index("movie_id", "movie_id", 1, false);
		this.create_index("index", "index", -1, true);
	}
	
	public long max_index(){
		DBCursor cur=this.collection.find(null,new MEntity("index",1)).sort(new MEntity("index",-1)).skip(0).limit(1);
		if(cur.hasNext())return cur.next()._long("index");
		return 0;
	}
	
	public void remove(String id){
		this.collection.remove(new MEntity("_id",new ObjectId(id)));
	}
	
	public void insert(Entity e){
		this.collection.insert(e);
	}
	
	public Entity get(String _id){
		return this.collection.findOne(new MEntity("_id",new ObjectId(_id)));
	}
	
	public EntityList get(long movie_id){
		DBCursor cur= this.collection.find(new MEntity("movie_id",movie_id));
		if(cur.hasNext()){
			EntityList list=new EntityList();
			while(cur.hasNext()){
				list.add(cur.next());
			}
			return list;
		}
		return null;
	}
	public EntityList first(int size){
		DBCursor cur=this.collection.find().sort(new MEntity("index",-1)).limit(size);
		if(cur.hasNext()){
			EntityList list=new EntityList();
			Entity e;
			while(cur.hasNext()){
				e=cur.next();
				e.put("rest_time",DDownload.rest_time_translate(e._long("create_time")));
				list.add(e);
			}
			return list;
		}
		return null;
	}
	public EntityList find(int start_index,int size){
		DBCursor cur=this.collection.find(new MEntity("index",new MEntity("$lt",start_index))).sort(new MEntity("index",-1)).limit(size);
		if(cur.hasNext()){
			EntityList list=new EntityList();
			while(cur.hasNext()){
				list.add(cur.next());
			}
			return list;
		}
		return null;
	}
	
	public long count(){
		return this.collection.count();
	}
	
}
