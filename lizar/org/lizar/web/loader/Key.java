package org.lizar.web.loader;

import org.lizar.json.JObject;
import org.lizar.json.JSON;
import org.lizar.web.config.Keys;

/**
 * An entry of group config.
 * 
 * @see Keys
 *
 */
public class Key {
	public String name;
	
	public Object value;
	
	public String desc;
	
	public  int _int(){
		return (Integer)value;
	}
	
	public  long _long(){
		return (Long)value;
	}
	
	public  double _double(){
		return (Double)value;
	}
	
	public  boolean _bool(){
		return (Boolean)value;
	}
	
	public String _str(){
		return value.toString();
	}
	
	public Key clone(){
		Key k=new Key();
		k.name=name;
		k.value=value;
		k.desc=desc;
		return k;
	}
	
	public static Key to_key(JSON e){
		if(e==null)return null;
		Key k=new Key();
		k.name=e._string("name");
		k.value=e.get("value");
		k.desc=e._string("desc");
		return k;
	}
	
	public JSON to_json(){
		JSON e=new JObject();
		e.put("name", name);
		e.put("value", value);
		e.put("desc", desc);
		return e;
	}

}
