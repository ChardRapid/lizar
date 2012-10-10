package com.lizar.web.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.lizar.exception.ConfigKeyLoopTooDeep;
import com.lizar.exception.GroupCodeIsNotExists;
import com.lizar.exception.InvalidFullGroupCode;
import com.lizar.json.JList;
import com.lizar.json.JObject;
import com.lizar.json.JSON;
import com.lizar.util.FileTool;
import com.lizar.util.StringHelper;
import com.lizar.web.loader.ConfigFile;
import com.lizar.web.loader.Explain;
import com.lizar.web.loader.Key;

/**
 * 
 * Status will load all the Status from /WEB-INF/status.json
 * and check any changes from there.
 * 
 * you can define the status file location at status.status_file
 * 
 * the default is related path and with "absolute:", system will regard it as absolute path.
 * 
 * Each status format:
 * 
 * 	group: *
 * 			Integer
 * 			which means a group of status
 *  code:  *
 *  		Integer
 *  		which means the error code of the status group
 *  msg:   *
 *  		String
 *  		which means the status msg of the group
 *  		the ${} fromat is supported.
 *  		${xxx}, you can get the msg of the status(full_code)
 *  		${cfg:xxx},   you can get the config of the config value.
 *  		${key:xxx},   you can get the running keys of the Keys module
 * 
 * */
public class Group {

	private Map<String,Explain> code_map=new HashMap<String,Explain>();
	
	private Map<String,List<Explain>> group_map=new HashMap<String,List<Explain>>();
	
	public static final String FILE_PATH="/WEB-INF/lizar/group.json";
	
	public ConfigFile cnf;
	
	private int size;
	
	private static Group group;
	
	private Group(){
		
	}
	public static Group instance(){
		if(group==null)group=new Group();
		return group;
	}
	

	private void translate(){
		Explain k;
		for(Object obj:code_map.values()){
			k=(Explain)obj;
			k.value=express(k.value);
		}
	}
	
	private static Object explain(String key){
		if(key.startsWith("cfg:")){
			if(key.length()==4)return "";
			return Config.xpath(key.substring(4, key.length()));
		}else if(key.startsWith("key:")){
			if(key.length()==4)return "";
			return Keys.get(key.substring(4, key.length()));
		}
		Explain e=group.code_map.get(key);
		if(e==null)throw new GroupCodeIsNotExists(key);
		return e.value;
	}
	
	/**
	 * If value is an instance of string, translate EL value to string value.<br/>
	 * Example: key background's value is  ${color} and color is red and then return red.
	 * @param value may contain EL expression ${}
	 */
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
				if(k.indexOf(".")==-1)throw new InvalidFullGroupCode(k);
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
	
	private JSON to_group_entity(){
		JList list;
		JSON data=new JObject();
		JSON exp;
		for(String k:group_map.keySet()){
			List<Explain> exp_list=group_map.get(k);
			list=new JList();
			for(Explain e:exp_list){
				exp=new JObject();
				exp.put("code",e.code);
				exp.put("value",e.value);
				list.add(exp);
			}
			data.put(k+"", list);
		}
		return data;
	}
	
	public void persistence() throws IOException{
		if(cnf==null){
			cnf=new ConfigFile();
			cnf.file=new File(Config.path_filter(FILE_PATH));
		}
		if(cnf.file.exists()){
			JSON entity=FileTool.read_json(cnf.file);
			if(entity!=null){
				Explain exp;
				JList list;
				JSON st;
				for(String key:entity.keySet()){
					list=entity._list(key);
					for(Object o:list){
						st=(JSON)o;
						exp=new Explain();
						exp.group=key;
						exp.code=st._string("code");
						exp.value=st.get("value");
						set_to_list(exp);
					}
				}
				translate();
			}
		}
		size=code_map.size();
		FileTool.write_to_file(to_group_entity(), cnf.file);
		cnf.last_modify=cnf.file.lastModified();
	}
	
	private static void set_to_list(Explain exp){
		Explain e=group.code_map.get(exp.full_code());
		List<Explain> list=group.group_map.get(exp.group);
		if(list==null)list=new LinkedList<Explain>();
		if(e==null){
			list.add(exp);
		}else{
			for(int i=0;i<list.size();i++){
				if(list.get(i).full_code().equals(exp.full_code())){
					list.remove(i);
					break;
				}
			}
			list.add(exp);
		}
		group.code_map.put(exp.full_code(), exp);
		group.group_map.put(exp.group,list);
	}
	
	public static void set_up(String _group,String code,Object value){
		Explain exp=new Explain();
		exp.group=_group;
		exp.code=code;
		exp.value=value;
		set_to_list(exp);
	}

	public void check() throws IOException{
		if(!cnf.file.exists()){
			FileTool.write_to_file(to_group_entity(), cnf.file);
			cnf.last_modify=cnf.file.lastModified();
			return;
		}
		if(cnf.file.lastModified()!=cnf.last_modify){
			JSON entity=FileTool.read_json(cnf.file);
			if(entity!=null){
				Explain exp;
				JList list;
				JSON st;
				for(String key:entity.keySet()){
					list=entity._list(key);
					for(Object o:list){
						st=(JSON)o;
						exp=new Explain();
						exp.group=key;
						exp.code=st._string("code");
						exp.value=st.get("value");
						set_to_list(exp);
					}
				}
				translate();
			}
			FileTool.write_to_file(to_group_entity(), cnf.file);
			cnf.last_modify=cnf.file.lastModified();
		}else if(size!=code_map.size()){
			translate();
			FileTool.write_to_file(to_group_entity(), cnf.file);
			cnf.last_modify=cnf.file.lastModified();
		}
	}
	
	public static List<Explain> group_of_codes(String _group){
		return group.group_map.get(_group);
	}

	public static Explain get(String full_code){
		return group.code_map.get(full_code);
	}

	
	public static Object get(String group,String code){
		Explain exp=get(group+"."+code);
		if(exp==null)throw new GroupCodeIsNotExists(group,code);
		return exp.value;
	}
	
	
	public static int _int(String group,String code){
		Explain exp=get(group+"."+code);
		if(exp==null)throw new GroupCodeIsNotExists(group,code);
		return (Integer)exp.value;
	}
	
	
	
	public static long _long(String group,String code){
		Explain exp=get(group+"."+code);
		if(exp==null)throw new GroupCodeIsNotExists(group,code);
		return (Long)exp.value;
	}
	
	
	public static double _double(String group,String code){
		Explain exp=get(group+"."+code);
		if(exp==null)throw new GroupCodeIsNotExists(group,code);
		return (Double)exp.value;
	}
	
	
	public static String _str(String group,String code){
		Explain exp=get(group+"."+code);
		if(exp==null)throw new GroupCodeIsNotExists(group,code);
		return (String)exp.value;
	}
	
}
