package com.lizar.web.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.lizar.exception.ConfigKeyLoopTooDeep;
import com.lizar.exception.KeyIsNotExists;
import com.lizar.json.JList;
import com.lizar.json.JSON;
import com.lizar.log.Log;
import com.lizar.log.Logger;
import com.lizar.util.FileTool;
import com.lizar.web.loader.ConfigFile;
import com.lizar.web.loader.Key;

/**
 * 
 * support:
 * 
 * 	${}
 *  ${cfg:xxx}
 * 
 * 	do not support ${i18:xxx} or ${status:xxx}
 * 
 * 	this is mainly for the System core property settings.
 * 
 * */
public class Keys {
	private Log log=Logger.newInstance(this.getClass());
	private static Keys keys;
	public static final String FILE_PATH="/WEB-INF/lizar/keys.json";
	private Map<String,Key> map;
	private Map<String,Key> store_map;
	private int size=0;
	public ConfigFile cnf;
	public static Keys instance(){
		if(keys==null)keys=new Keys();
		return keys;
	}
	
	
	public static void set(String key,Object o){
		Key k=keys.map.get(key);
		if(k==null){
			k=new Key();
			k.name=key;
			k.value=o;
			keys.map.put(key, k);
			keys.store_map.put(key, k.clone());
		}else{
			keys.log.info("Key "+key+" will not set into keys container, as there already one from file or other set");
		}
		
	}
	
	public static void set(String key,Object o,String desc){
		Key k=keys.map.get(key);
		if(k==null){
			 k=new Key();
			 k.name=key;
			 k.value=o;
			 k.desc=desc;
			 keys.map.put(key, k);
			 keys.store_map.put(key, k.clone());
		}else{
			keys.log.info("Key "+key+" will not set into keys container, as there already one from file or other set");
		}
	}

	public static int _int(String key){
		Key k=keys.map.get(key);
		if(k==null)throw new KeyIsNotExists(key);
		return k._int();
	}
	
	public static long _long(String key){
		Key k=keys.map.get(key);
		if(k==null)throw new KeyIsNotExists(key);
		return k._long();
	}
	
	public static double _double(String key){
		Key k=keys.map.get(key);
		if(k==null)throw new KeyIsNotExists(key);
		return k._double();
	}
	
	
	/**
	 * 
	 * get property with string value
	 * 
	 * */
	public static String _str(String key) {
		Key k=keys.map.get(key);
		if(k==null)throw new KeyIsNotExists(key);
		return k._str();
	}
	
	public static boolean exists(String key){
		return keys.map.get(key)!=null;
	}
	
	public static Key get(String key){
		Key k= keys.map.get(key);
		if(k==null)throw new KeyIsNotExists(key);
		return k;
	}
	
	private Keys(){
		map=new HashMap<String,Key>();
		store_map=new LinkedHashMap<String,Key>();
	} 
	
	public void persistence() throws IOException{
		if(cnf==null){
			cnf=new ConfigFile();
			cnf.file=new File(Config.path_filter(FILE_PATH));
		}
		if(cnf.file.exists()){
			JList list=(JList)FileTool.read_json(cnf.file);
			if(list!=null){
				JSON e;
				for(Object o:list){
					e=(JSON)o;
					Key k=Key.to_key(e);
					map.put(k.name,k);
					store_map.put(k.name,k.clone());
				}
				translate();
			}
		}
		size=map.size();
		FileTool.write_to_file(to_store_list(), cnf.file);
		cnf.last_modify=cnf.file.lastModified();
	}

	private void translate(){
		Key k;
		for(Object obj:map.values()){
			k=(Key)obj;
			try{
				k.value=express(k.value);
			}catch(Exception e){
				log.error("Translate "+k.value+" value failed.",e);
			}
		}
	}
	
	private static Object explain(String key){
		if(key.startsWith("cfg:")){
			if(key.length()==4)return "";
			return Config.xpath(key.substring(4, key.length()));
		}
		return keys.map.get(key).value;
	}
	
	public static Object express(Object value){
		if(value==null)return null;
		if(value instanceof String){
			int i=0;
			Object o;
			String k=value.toString();
			while(true){
				if(i>=20){
					throw new ConfigKeyLoopTooDeep(Config.path_filter(FILE_PATH),value.toString());
				}
				i++;
				if(!Config.check(k))return k;
				int start=k.indexOf("${");
				int end=k.indexOf("}");
				if(start==0&&end==k.length()-1){
					o=explain(k.substring(2,k.length()-1));
					if(o instanceof String){
						k=o.toString();
						continue;
					}
					return o;
				}
				Object result=explain(k.substring(start+2, end));
				if(result==null) result=" ";
				if(start==0){
					k= result.toString()+k.substring(end+1,k.length());
					continue;
				}
				if(end==k.length()-1){
					k=k.substring(0,start)+result;
					continue;
				}
				k=k.substring(0,start)+result+k.substring(end+1,k.length());
			}
		}else return value;
	}
	
	private JList to_store_list(){
		JList list=new JList();
		Key key;
		for(Object o:store_map.values()){
			key=(Key)o;
			list.add(key.to_json());
		}
		return list;
	}
	
	public void check() throws IOException{
		if(!cnf.file.exists()){
			FileTool.write_to_file(to_store_list(), cnf.file);
			cnf.last_modify=cnf.file.lastModified();
			return;
		}
		if(cnf.file.lastModified()!=cnf.last_modify){
			JList list=(JList)FileTool.read_json(cnf.file);
			if(list!=null){
				JSON e;
				for(Object o:list){
					e=(JSON)o;
					Key k=Key.to_key(e);
					map.put(k.name,k);
					store_map.put(k.name,k.clone());
				}
				translate();
			}
			FileTool.write_to_file(to_store_list(), cnf.file);
			cnf.last_modify=cnf.file.lastModified();
		}else if(size!=map.size()){
			translate();
			FileTool.write_to_file(to_store_list(), cnf.file);
			cnf.last_modify=cnf.file.lastModified();
		}
	}
	 
	
	
}
