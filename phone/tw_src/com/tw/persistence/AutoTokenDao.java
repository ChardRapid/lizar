package com.tw.persistence;

import org.bson.types.ObjectId;

import com.lizar.web.loader.Cell;
import com.mongodb.Entity;
import com.mongodb.MEntity;

public class AutoTokenDao extends MongoDao implements Cell {

	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		this.init("auto_token");
	}

	public void insert(ObjectId user_id,String token,long timeout){
		Entity e=new MEntity();
		e.put("_id",user_id);
		e.put("token", token);
		e.put("timeout",timeout);
		this.collection.update(new MEntity("_id",user_id), e, true,false);
	}
	
	public ObjectId check(String token){
		Entity e=this.collection.findOne(new MEntity("token",token));
		if(e==null)return null;
		return e._obj_id("_id");
	}
	
	public void delete_by_id(ObjectId user_id){
		this.collection.remove(new MEntity("_id",user_id));
	}
	
	public void logout(ObjectId user_id){
		this.collection.remove(new MEntity("_id",user_id));
	}
	
	public void clean(){
		collection.remove(new MEntity("timeout",new MEntity("$lt",System.currentTimeMillis())));
	}
	
}
