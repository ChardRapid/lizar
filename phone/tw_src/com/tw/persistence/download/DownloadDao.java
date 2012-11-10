package com.tw.persistence.download;

import org.bson.types.ObjectId;

import com.lizar.web.loader.Cell;
import com.mongodb.DBCursor;
import com.mongodb.Entity;
import com.mongodb.EntityList;
import com.mongodb.MEntity;
import com.tw.persistence.MongoDao;

/**
 * 
 * 
 * 
 * */
public class DownloadDao extends MongoDao implements Cell{
	@Override
	public void init_property() throws Exception {
		this.init("download");
		this.create_index("movie_id", "movie_id", 1, false);
		this.create_index("create_time", "create_time", -1, true);
	}
	
	public Entity top(String id){
		return this.collection.findAndModify(new MEntity("_id",new ObjectId(id)), new MEntity("$set",new MEntity("top",true)));
	}
	
	public void cancel_top(String id){
		 this.collection.update(new MEntity("_id",new ObjectId(id)), new MEntity("$set",new MEntity("top",false)));
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
	
	public EntityList find_recent(int size){
		DBCursor cur=this.collection.find().sort(new MEntity("create_time",-1)).limit(size);
		if(cur.hasNext()){
			EntityList list=new EntityList();
			while(cur.hasNext()){
				list.add(cur.next());
			}
			return list;
		}
		return null;
	}
	
	public EntityList find(long timeline,int size){
		DBCursor cur=this.collection.find(new MEntity("create_time",new MEntity("$lt",timeline))).sort(new MEntity("create_time",-1)).limit(size);
		if(cur.hasNext()){
			EntityList list=new EntityList();
			while(cur.hasNext()){
				list.add(cur.next());
			}
			return list;
		}
		return null;
	}
	
}
