package com.tw.persistence;

import com.lizar.web.loader.Cell;
import com.mongodb.Entity;
import com.mongodb.MEntity;

public class RegisterDao extends MongoDao implements Cell {

	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		this.init("register");
	}
	
	public void upsert(Entity e){
		collection.update(new MEntity("email",e._string("email")), e, true,false);
	}

	public Entity find_and_remove_by_code(String code){
		return collection.findAndRemove(new MEntity("code",code));
		
	}
	
}
