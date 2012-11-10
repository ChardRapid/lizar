package com.lizar.web.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.lizar.exception.ConfigKeyLoopTooDeep;
import com.lizar.log.Log;
import com.lizar.log.Logger;
import com.lizar.util.FileTool;
import com.lizar.util.StringHelper;
import com.lizar.web.Controller;
import com.lizar.web.loader.I18Resource;

/**
 * 
 * support:
 * 
 * 	${}
 *  ${cfg:xxx}
 *  ${key:xxx}
 *  ${group:group.code}
 * 	do not support ${status:xxx}
 * 
 * 	this is mainly for the i18 msg change.
 * 
 * */
public class I18Msg {
	private Log log=Logger.newInstance(this.getClass());
	private Map<String,I18Resource> res=new HashMap<String,I18Resource>();
	
	public I18Resource _default;
	
	public static final String DIR="/WEB-INF/lizar/locale";
	
	public String var;
	
	private static I18Msg i18;
	
	public static I18Msg instance(){
		if(i18==null)i18=new I18Msg();
		return i18;
	}
	
	
	private I18Msg(){
	}
	
	private void clean(){
		for(I18Resource i:res.values()){
			i.clean();
		}
		res.clear();
		_default=null;
	}
	
	public void _check_i18msg_setting(String var,String lan){
		this.var=var;
		if(_default==null||StringHelper.isNull(_default.lan)){
			if(StringHelper.isNotNull(lan)){
				renew_default(lan);
			}
		}else if(!StringHelper.equals(lan, _default.lan)){
			renew_default(lan);
		}
	}
	
	private void renew_default(String lan){
		I18Resource ir=res.get(lan);
		if(ir!=null)_default=ir;
		else{
			File f=new File(DIR+"/i18."+lan+".txt");
			if(!f.exists()){
				try {
					FileTool.write_to_file("#this is default locale i18 msg file, you can name different locale as i18."+lan+".txt", f);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			ir= _load(f);
			if(ir==null){
				log.warn("The config update with muti_language.default_locale :"+lan+", but can not find the match file:"+f.getPath());
				return;
			}
			ir.lan=lan;
			res.put(lan,ir);
			_default=ir;
			translate(ir);
		} 
	}
	
	public void persistence() throws IOException{
		var=Config.xpath_str("muti_language.var");
		File l=new File(Config.path_filter(DIR));
		if(l.isFile()){
			FileTool.check_dir(l);
			l=new File(Config.path_filter(DIR+"/i18.txt"));
			if(!l.exists()){
				FileTool.write_to_file("#this is default locale i18 msg file, you can name different locale as i18.en.txt",l);
			}
			return;
		}
		if(l.exists()){
			File[] f_arr=l.listFiles();
			I18Resource ir;
			for(File _f:f_arr){
				String name=_f.getName().toLowerCase();
				if(_f.isFile()&&name.startsWith("i18.")&&name.endsWith(".txt")){
					if(name.equals("i18.txt")){
						ir=_load(_f);
						ir.lan="";
						_default=ir;	continue;
					}
					String lan=name.substring(4,name.length()-4);
					if(lan.equals("")||lan.indexOf(".")!=-1){
						log.error("Muti-Language load failed msg file must in fixed format : msg.txt\nmsg.zh.txt\n a format like this (msg..txt) or (msg.zh..txt) is not support");
						continue;
					}
					if(_f.canRead()){
						if(lan.equals(Config.xpath("muti_language.default_locale"))){
							ir=_load(_f);
							ir.lan="";
							_default=ir;
						}else{
							ir=_load(_f);
							ir.lan="";
							res.put(lan, ir);
						}
					}else{
						log.error("Muti-Language load msg from "+_f.getName()+" failed. caused by the file cannot be able to read by the application");
					}
				}
			}
			translate();
		}else{
			l.mkdirs();
			l=new File(Config.path_filter(DIR+"/i18.txt"));
			if(!l.exists()){
				FileTool.write_to_file( "#this is default locale i18 msg file, you can name different locale as i18.en.txt",l);
			}
			return;
		}
	}
	
	private void translate(){
		if(_default!=null){
			for(Entry<String,String> e:_default.map.entrySet()){
				e.setValue(express(_default,e.getValue()));
			}
		}
		for(I18Resource irs:res.values()){
			for(Entry<String,String> e:irs.map.entrySet()){
				e.setValue(express(irs,e.getValue()));
			}
		}
	}
	
	private void translate(I18Resource irs){
		for(Entry<String,String> e:irs.map.entrySet()){
			e.setValue(express(irs,e.getValue()));
		}
	}
	
	private void translate(List<I18Resource> list){
		for(I18Resource irs:list){
			for(Entry<String,String> e:irs.map.entrySet()){
				e.setValue(express(irs,e.getValue()));
			}
		}
	}
	
	private Object explain(Map<String,String> map,String key){
		if(key.startsWith("cfg:")){
			if(key.length()==4)return "";
			return Config.xpath(key.substring(4, key.length()));
		}else if(key.startsWith("key:")){
			if(key.length()==4)return "";
			return Keys.get(key.substring(4, key.length()));
		}else if(key.startsWith("group:")){
			if(key.length()==6)return "";
			return Group.get(key.substring(6, key.length()));
		}
		return map.get(key);
	}
	
	private String express(I18Resource res,String value){
		if(StringHelper.isNull(value))return "";
		int i=0;
		Object o;
		String k=value.toString();
		while(true){
			if(i>=20){
				throw new ConfigKeyLoopTooDeep(res.file.getPath(),value);
			}
			i++;
			if(!Config.check(k))return k;
			int start=k.indexOf("${");
			int end=k.indexOf("}");
			if(start==0&&end==k.length()-1){
				o=explain(res.map,k.substring(2,k.length()-1));
				if(o instanceof String){
					k=o.toString();
					continue;
				}
				if(o!=null)return o.toString();
				return "";
			}
			Object result=explain(res.map,k.substring(start+2, end));
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
	}
	
	
	private I18Resource _load(File _f){
		Map<String,String> res=new HashMap<String,String>();
		BufferedReader reader=null;
        try {
        	reader = new BufferedReader(new InputStreamReader(new FileInputStream(_f),Controller.encode_type));
            String tempString = null;
            int i=0;
            String[] arr=null;
            
            while ((tempString = reader.readLine()) != null) {
            	if(!tempString.trim().equals("")&&!tempString.trim().startsWith("#")){
        			arr=tempString.split("=");
        			if(arr.length>2){
        				System.err.println("Muti-Language read line "+i+" error in "+_f.getName()+" file. this line get more than one '='.");
        			}else if(arr.length==2){
        				res.put(arr[0], arr[1]);
        			}else{
        				log.info("msg:"+tempString+" is not a void  <key=value> format in "+_f.getPath());
        			}
        			i++;
        		}
                
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        I18Resource i18=new I18Resource();
        i18.file=_f;
        i18.last_modify=_f.lastModified();
        i18.map=res;
       return i18;
	}

	
	private void  update(I18Resource i18){
		BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(i18.file));
            String tempString = null;
            int i=0;
            String[] arr=null;
            while ((tempString = reader.readLine()) != null) {
            	if(!tempString.trim().equals("")){
        			arr=tempString.split("=");
        			if(arr.length>2){
        				System.err.println("Muti-Language read line "+i+" error in "+i18.file.getName()+" file. this line get more than one '='.");
        			}else{
        				i18.map.put(arr[0], arr[1]);
        			}
        			i++;
        		}
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        i18.last_modify=i18.file.lastModified();
	}
	
	public void check() throws IOException{
		var=Config.xpath_str("muti_language.var");
		File l=new File(DIR);
		if(l.isFile()){
			FileTool.check_dir(l);
			l=new File(Config.path_filter(DIR+"/i18.txt"));
			if(!l.exists()){
				FileTool.write_to_file( "#this is default locale i18 msg file, you can name different locale as i18.en.txt",l);
			}
			return;
		}
		if(l.exists()){
			File[] f_arr=l.listFiles();
			I18Resource ir;
			List<I18Resource> modify_list=new LinkedList<I18Resource>();
			for(File _f:f_arr){
				String name=_f.getName().toLowerCase();
				if(_f.isFile()&&name.startsWith("i18.")&&name.endsWith(".txt")){
					if(name.equals("i18.txt")){
						if(_default.file.lastModified()!=_default.last_modify){
							update(_default);
							modify_list.add(_default);
						}
						continue;
					}
					String lan=name.substring(4,name.length()-4);
					if(lan.equals("")||lan.indexOf(".")!=-1){
						log.error("Muti-Language load failed msg file must in fixed format : msg.txt\nmsg.zh.txt\n a format like this (msg..txt) or (msg.zh..txt) is not support");
						continue;
					}
					if(_f.canRead()){
						if(lan.equals(Config.xpath("muti_language.default_locale"))){
							if(_default!=null){
								if(StringHelper.equals(_default.file, _f.getPath())){
									if(_default.file.lastModified()!=_default.last_modify){
										update(_default);
										modify_list.add(_default);
									}
								}else{
									log.warn("Default locale "+_default.file.getPath()+" has been droped, and set "+_f.getPath()+" as default locale file.");
									_default=_load(_f);
									modify_list.add(_default);
								}
							}else{
								_default=_load(_f);
								modify_list.add(_default);
							}
						}else{
							ir=res.get(lan);
							if(ir==null){
								ir= _load(_f);
								ir.lan=lan;
								res.put(lan,ir);
								modify_list.add(res.get(lan));
							}else {
								if(ir.file.lastModified()!=ir.last_modify){
									update(ir);
									modify_list.add(ir);
								}
							}
						}
					}else{
						log.error("Muti-Language load msg from "+_f.getName()+" failed. caused by the file cannot be able to read by the application");
					}
				}
			}
			translate(modify_list);
		}else{
			l.mkdirs();
			return;
		}
		
		
	}
	
	public String get(String lan,String key){
		if(lan==null||lan.equals(""))return _default.map.get(key);
		I18Resource rs=res.get(lan);
		if(rs==null)rs= _default;
		return rs.map.get(key);
	}
	
	public I18Resource get(String lan){
		I18Resource rs=res.get(lan);
		if(rs==null)rs= _default;
		return rs;
	}

	
	public static void main(String[] args){
		System.out.println(Integer.parseInt("3.26456"));
	}

	
}
