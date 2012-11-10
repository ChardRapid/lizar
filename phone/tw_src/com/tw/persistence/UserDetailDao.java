package com.tw.persistence;

import org.bson.types.ObjectId;

import com.lizar.web.loader.Cell;
import com.mongodb.Entity;
import com.mongodb.MEntity;

public class UserDetailDao extends MongoDao implements Cell{

	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		this.init("user_detail");
	}
	
	public boolean email_exists(String email){
		return collection.findOne(new MEntity("email",email),new MEntity("_id",1))!=null;
	}
	
	public void insert(Entity user){
		collection.insert(user);
	}
	
	public Entity get_by_token(String token){
		return collection.findOne(new MEntity("token",token));
	}
	
	
	public Entity get(ObjectId _id,String fields){
		return this.collection.findOne(new MEntity("_id",_id),translate_fiels(fields));
	}
	
	public Entity get_by_email(String email,String fields){
		return this.collection.findOne(new MEntity("email",email),translate_fiels(fields));
	}
	
	public Entity get(ObjectId _id){
		return this.collection.findOne(new MEntity("_id",_id));
	}
}
