package com.tw.persistence;

import java.net.UnknownHostException;

import com.lizar.util.StringHelper;
import com.lizar.web.Web;
import com.lizar.web.config.Keys;
import com.lizar.web.loader.Cell;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Entity;
import com.mongodb.MEntity;
import com.mongodb.Mongo;
import com.mongodb.MongoException;


public class MongoDao implements Cell{
	public static Mongo	mongodb ;
	public  DB db;
	protected DBCollection collection;
	public static final int ASC=1;
	public static final int DESC=0;
	
	
	public void init(String schema_name) throws UnknownHostException, MongoException{
		if(mongodb==null){
			mongodb = new Mongo();
		}
		db = mongodb.getDB(Keys._str("mongodb.name"));
		collection = db.getCollection(schema_name);
	}
	
	public Entity translate_fiels(String fields){
		if(StringHelper.isNull(fields))return null;
		String[] keys=fields.split(",");
		Entity f=new MEntity();
		for(String k:keys){
			if(StringHelper.isNotNull(k.trim()))f.put(k, 1);
		}
		return f;
	}
	
	public void create_index(String column,String index_name,int sort_order,boolean unique) throws Exception {
		collection.ensureIndex(new MEntity(column, sort_order), index_name, unique);
	}
	
	public void create_index(Entity columns,String index_name,boolean unique) throws Exception {
		collection.ensureIndex( columns, index_name, unique);
	}

	@Override
	public void init_property() throws Exception {
		Keys.set("mongodb.name", "phone");
	}
	
	public void clean(){
		this.collection.remove(new MEntity());
	}
}
