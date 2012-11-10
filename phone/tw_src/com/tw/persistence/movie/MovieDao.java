package com.tw.persistence.movie;

import com.lizar.web.loader.Cell;
import com.mongodb.DBCursor;
import com.mongodb.Entity;
import com.mongodb.EntityList;
import com.mongodb.MEntity;
import com.tw.persistence.MongoDao;

/**
 * 
 * 作品
 * 
 * */
public class MovieDao extends MongoDao implements Cell{

	@Override
	public void init_property() throws Exception {
		this.init("movie");
	}
	
	public EntityList get_by_year(int year){
		DBCursor cur=this.collection.find(new MEntity("year",year)).sort(new MEntity("rating.average",-1));
		if(cur!=null){
			EntityList list=new EntityList();
			for(;cur.hasNext();){
				list.add(cur.next());
			}
			return list;
		}
		return null;
	}
	
	public void update_top_effect(long id,int top_effect){
		Entity query=new MEntity();
		query.put("_id", id);
		query.put("top_effect", new MEntity("$lt",top_effect));
		this.collection.update(query, new MEntity("top_effect",top_effect));
	}
	
	public long count(){
		return this.collection.count();
	}
	
	public boolean exists(long movie_id){
		return this.collection.findOne(new MEntity("_id",movie_id),new MEntity("_id",1))!=null;
	}
	
	public void upsert(Entity e){
		this.collection.update(new MEntity("_id",e.get("_id")), new MEntity("$set",e),true,false);
	}
	
	public Entity get(long id){
		return this.collection.findOne(new MEntity("_id",id));
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
	
	public EntityList search(String keys){
		DBCursor cur=this.collection.find(new MEntity("keys",keys)).sort(new MEntity("rating.average",-1));
		if(cur!=null){
			EntityList list=new EntityList();
			for(;cur.hasNext();){
				list.add(cur.next());
			}
			return list;
		}
		return null;
	}
}
