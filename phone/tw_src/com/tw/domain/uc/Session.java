package com.tw.domain.uc;

import org.bson.types.ObjectId;

import com.mongodb.Entity;
import com.mongodb.MEntity;

public class Session {
	
	private ObjectId user_id;
	
	private String _id;
	
	private Entity user_detail;
	
	private long last_visit_time;
	
	private Session(){
		
	}
	
	/**
	 * 
	 * transfer a user detail  to session obj
	 * 
	 * */
	public static Session new_session(String token,Entity user_detail){
		Session session=new Session();
		session.user_id=user_detail._obj_id("_id");
		session.user_detail=user_detail;
		session._id=token;
		session.last_visit_time=System.currentTimeMillis();
		return session;
	}
	
	/**
	 * 
	 * transfer a session entity to session obj
	 * 
	 * */
	public static Session load_session(Entity e){
		if(e==null)return null;
		Session session=new Session();
		session.user_id=e._obj_id("user_id");
		session.user_detail=e._entity("user_detail");
		session._id=e._string("_id");
		session.last_visit_time=System.currentTimeMillis();
		return session;
	}
	

	public  Entity to_entity(){
		Entity e=new MEntity();
		e.put("user_id",user_id);
		e.put("_id",_id);
		e.put("user_detail",user_detail);
		e.put("last_visit_time", last_visit_time);
		return e;
	}

	public ObjectId getUser_id() {
		return user_id;
	}

	public String get_id() {
		return _id;
	}

	public Entity getUser_detail() {
		return user_detail;
	}

	public long getLast_visit_time() {
		return last_visit_time;
	}

	
	
	
	
	
	
}
