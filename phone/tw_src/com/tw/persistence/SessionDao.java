package com.tw.persistence;

import java.util.Date;

import org.bson.types.ObjectId;

import com.lizar.util.Time;
import com.lizar.web.Web;
import com.lizar.web.loader.Cell;
import com.mongodb.Entity;
import com.mongodb.MEntity;
import com.tw.domain.uc.UserCenter;

public class SessionDao extends MongoDao implements Cell {

	private UserCenter ucenter;
	
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		this.init("session");
		ucenter=Web.get(UserCenter.class);
	}
	
	public Entity get(String session_id,String fields){
		return this.collection.findOne(new MEntity("_id",session_id),translate_fiels(fields));
	}

	public void logout(ObjectId user_id){
		this.collection.remove(new MEntity("user_id",user_id));
	}
	
	public void insert(Entity e){
		this.collection.insert(e);
	}
	
	public void clean(){
		collection.remove(new MEntity("last_visit_time",new MEntity("$lt",System.currentTimeMillis()-Time.translate_time(ucenter.interval_time))));
	}
	
	public static void main(String[] args){
		
		
		System.out.println((201-8)/60);
		
	}
	
}
