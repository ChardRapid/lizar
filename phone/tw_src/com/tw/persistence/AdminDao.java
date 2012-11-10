package com.tw.persistence;

import org.bson.types.ObjectId;

import com.mongodb.Entity;
import com.mongodb.MEntity;
import com.tw.domain.mc.Admin;

public class AdminDao extends MongoDao {
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		this.init("admin");
	}
	
	public void insert(ObjectId _id,int type){
		Entity e=new MEntity();
		e.put("_id", _id);
		e.put("type", type);
		this.collection.insert(e);
	}
	
	public boolean is_super(ObjectId _id){
		Entity e=new MEntity();
		e.put("_id", _id);
		e.put("type", Admin.SUPER);
		return this.collection.findOne(e)!=null;
	}
	
}
