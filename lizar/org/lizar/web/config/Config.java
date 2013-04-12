package org.lizar.web.config;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletContext;

import org.lizar.exception.CellKeyIsNotJsonObject;
import org.lizar.exception.ConfigKeyLoopTooDeep;
import org.lizar.exception.ConfigKeysPathIllegal;
import org.lizar.exception.CoreConfigJsonIsNull;
import org.lizar.exception.LifecycleException;
import org.lizar.json.JList;
import org.lizar.json.JObject;
import org.lizar.json.JSON;
import org.lizar.json.util.JSONParser;
import org.lizar.log.Log;
import org.lizar.log.Logger;
import org.lizar.util.FileTool;
import org.lizar.util.StringHelper;
import org.lizar.util.Time;
import org.lizar.web.Controller;
import org.lizar.web.Lifecycle;
import org.lizar.web.Web;
import org.lizar.web.controller.ConfigManager;
import org.lizar.web.controller.VelocitySupport;


/**
 * <pre>
 * Member of config architecture.
 * 
 * task 1 
 * 			Initialize system global config 
 * task 2 
 * 			Update whole config architecture while config cell changes periodically.
 * 
 * 
 * Config goes in "/WEB-INF/lizar.json".
 * Config architecture consists of all config cells.
 * Config cells contains Config, Group, Keys and I18Msg.
 * 
 * config architecture EL syntax:
 * 			${}					invoke self's config
 * 			${cfg:xxx}			invoke Config's config
 * 			${key:xxx}			invoke Kyes's config
 * 			${group:xxx.xxx}	invoke Group's config
 * 
 * support:
 * 		${}
 * 
 * rule 1
 * 			every config cell can invoke self's config by ${}. 
 * 
 * rule 2
 * 			Config is the top which can't invoke config from I18Msg, Group and Keys
 * 			but on the contrary the rest can invoke Config by ${cfg:xxx} such as ${cfg:server_info.root}.
 * 
 * rule 3
 * 			if file "/WEB-INF/lizar/keys.json" doesn't exist, create file and put all keys in it.
 * 			if file exists but a key configured in system source code doesn't exist in file, put the key in it.
 * 			if a key both exists in file and system source code, but has different value,  value in file is prior.
 * 
 * Config architecture make system hot configurable which provides great convenience for administrator to maintain system.
 * </pre>
 * */
public class Config implements Lifecycle{

	private static Log log=Logger.newInstance(Config.class);
	
	private static Config cfg;
	
	Translator translator = new LizarTranslator();
	
	/**
	 * config contains EL expression.
	 */
	private JSON org_config;
	
	/**
	 * config with EL expression translated
	 */
	private JSON run_time_config;
	
	private boolean ready=false;
	
	/**
	 * All config goes "/WEB-INF/lizar.json".
	 */
	public static final String FILE_PATH="/WEB-INF/lizar.json";
	
	/**
	 * config file
	 */
	private File file;
	
	/**
	 * config file's last modify time 
	 * 
	 * use to check whether config file has changed.
	 */
	private long last_modify_time;
	
	private static ServletContext context;
	
	private boolean started=false;
	
	private Thread checker;
	public void ready_to_check_changes(){
		ready=true;
	}
	
	private Config(){
	}
	
	public static Config instance(){
		if(cfg==null){
			cfg=new Config();
			context=Web.context;
			try {
				cfg._init();
			} catch (IOException e) {
				log.error("an error has happened with lizar config init pls check the /WEB-INF/lizar.json file.", e);
			}
		}
		return cfg;
	}
	
	/**
	 * Invoked when system closes.
	 * @throws LifecycleException if the component has not been started.
	 */
	public void stop() throws LifecycleException{
		log.info(this.getClass().getSimpleName()+" stopping");
		if(!started) throw new LifecycleException(this.getClass().getSimpleName()+" has stopped.");
		checker.interrupt();
		started=false;
	}
	
	/**
	 * Check and update all config(Config,Keys,Group and I18Msg) periodically which will work immediately.
	 * 
	 * File checked: "/WEB-INF/lizar.json", "/WEB-INF/lizar/keys.json", "/WEB-INF/lizar/group.json" and "/WEB-INF/lizar/locale/i18n.txt"
	 */
	public void start() throws LifecycleException{
		log.info(this.getClass().getSimpleName()+" starting");
		if(started) throw new LifecycleException(this.getClass().getSimpleName()+" has started.");
		
		checker=new Thread(){
			public void run() {
				last_modify_time=file.lastModified();
				try {
					sleep(xpath_time("server_info.config_check_inverval"));
				} catch (InterruptedException e) {
					log.debug("config checker sleep  has been interrupted as the thread is going to close.");
				}
				while(started){
					if(ready){
						if(!file.exists()){
							try {
								FileTool.write_to_file(org_config,file);
							} catch (IOException e) {
								log.error("an error has happened with "+file.getPath()+" config file.\n System will check it again after 5 minutes", e);
								try {
									sleep(Time.translate_time("2m"));
								} catch (InterruptedException e1) {
									log.debug("config checker sleep  has been interrupted as the thread is going to close.");
								}
							}
							last_modify_time=file.lastModified();
						}else if(file.lastModified()!=last_modify_time){
							log.info("detected lizar.json has been changed. Apply changes.....");
							try{
								last_modify_time=file.lastModified();
								update_changes();
							}catch(Exception e){
								log.error("an error has happened with "+file.getPath()+" config file.\n System will check it again after 5 minutes", e);
								try {
									sleep(Time.translate_time("2m"));
								} catch (InterruptedException e1) {
									log.debug("config checker sleep  has been interrupted as the thread is going to close.");
								}
							}
						}
						//check and update other config cell
						try {
							Web.keys.check();
						} catch (IOException e) {
							log.error("an error has happened with Web.keys component check file /WEB-INF/lizar/keys.json \n System will check it again after 2 minutes", e);
							try {
								sleep(Time.translate_time("2m"));
							} catch (InterruptedException e1) {
								log.debug("config checker sleep  has been interrupted as the thread is going to close.");
							}
						}
						try {
							Web.i18.check();
						} catch (IOException e) {
							log.error("an error has happened with Web.keys component check dir /WEB-INF/lizar/locale \n System will check it again after 2 minutes", e);
							try {
								sleep(Time.translate_time("2m"));
							} catch (InterruptedException e1) {
								log.debug("config checker sleep  has been interrupted as the thread is going to close.");
							}
						}
						try {
							Web.group.check();
						} catch (IOException e) {
							log.error("an error has happened with Web.keys component check file /WEB-INF/lizar/group.json \n System will check it again after 2 minutes", e);
							try {
								sleep(Time.translate_time("2m"));
							} catch (InterruptedException e1) {
								log.debug("config checker sleep  has been interrupted as the thread is going to close.");
							}
						}
//						try{
//							Web.plugin_manager.check();
//						}catch(Exception e){
//							log.error("an error has happened with Web.plugin component check files under /WEB-INF/lizar/plugin \n System will check it again after 2 minutes", e);
//							try {
//								sleep(Time.translate_time("2m"));
//							} catch (InterruptedException e1) {
//								log.debug("config checker sleep  has been interrupted as the thread is going to close.");
//							}
//						}
						Web.controller._check_disable_event();
						
					}
					try {
						sleep(xpath_time("server_info.config_check_inverval"));
					} catch (InterruptedException e) {
						log.debug("config checker sleep  has been interrupted as the thread is going to close.");
					}
					
				}
				log.info("..............................................................................");
				log.info("\t\t\tConfig Checker has been successfully terminated.");
				log.info("..............................................................................");
			}
		};
		checker.start();
		started=true;
	}
	
	private void update_changes(){
		JSON changes=config_data();
		if(changes==null)throw new CoreConfigJsonIsNull(file.getPath());
		check_server_info(changes);
		check_container(changes);
		check_db_config(changes);
		check_muti_language(changes);
		check_event(changes);
		check_template(changes);
		check_static_resource(changes);
		org_config=changes;
		run_time_config=(JSON)JSONParser.parse(org_config.toString());
		translator.translate();
		apply_server_info_changes();
		apply_multi_language_changes();
		apply_event_changes();
		apply_template_changes();
		apply_static_resource_changes();
	}
	
	
	private void apply_server_info_changes(){
		ConfigManager.user_name= xpath_str("config_manager.user_name");
		ConfigManager.password= xpath_str("config_manager.password");
		Web.controller._reload_encode_type(xpath_str("server_info.encode_type", "utf-8"));
	}
	
	private void check_server_info(JSON entity){
		JSON server_info=entity._entity("server_info");
		if(server_info==null){
			entity.put("server_info", org_config.get("server_info"));
			return;
		}
		server_info.put("name", server_info._string("name",context.getContextPath()));
		server_info.put("root", server_info._string("root","http://localhost"+context.getContextPath()));
		server_info.put("encode_type", server_info._string("encode_type", "utf-8"));
		server_info.put("description", server_info._string("description", "hot run app"));
		server_info.put("config_check_inverval", Time.translate_time(server_info._string("config_check_inverval", "3s")));
		server_info.put("enable_config_manager", server_info._bool("enable_config_manager", true));
		_check_config_manager(entity);
		entity.put("server_info", server_info);
		
		
	}
	
	private void _check_config_manager(JSON entity){
		JSON config_manager=entity._entity("config_manager");
		if(config_manager==null){
			entity.put("config_manager", org_config.get("config_manager"));
			return;
		}
		config_manager.put("user_name", config_manager._string("user_name","lizar"));
		config_manager.put("password", config_manager._string("password","password"));
		entity.put("config_manager", config_manager);
	}
	
	private void check_container(JSON entity){
		//do nothing
		entity.put("container", org_config.get("container"));
	}
	
	private void check_db_config(JSON entity){
		if(entity.get("db_config")==null)entity.put("db_config", org_config.get("db_config"));
	}
	
	private void apply_multi_language_changes(){
		Web.i18._check_i18msg_setting(xpath_str("muti_language.var","m"),xpath_str("muti_language.default_locale",""));
	}
	
	private void check_muti_language(JSON entity){
		JSON muti_lan=entity._entity("muti_language");
		if(muti_lan==null){
			entity.put("muti_language", org_config.get("muti_language"));
			return;
		}
		muti_lan.put("var",muti_lan._string("var","m"));
		muti_lan.put("default_locale",muti_lan._string("default_locale",""));
		entity.put("muti_language", muti_lan);
	}
	
	private void apply_event_changes(){
		log.info("interceptor:"+xpath_str("event.interceptor", ""));
		Web.controller._check_interceptor(xpath_str("event.interceptor", ""));
		Web.controller._check_vars(xpath_entity("event.global_var"));
		Web.controller._check_exception_recorder(xpath_str("event.exception_recorder", ""));
	}
	
	private void check_event(JSON entity){
		JSON event=entity._entity("event");
		if(event==null){
			entity.put("event", org_config.get("event"));
			return;
		}
		event.put("interceptor",event._string("interceptor",""));
		event.put("exception_handler",event._string("exception_recorder",""));
		if(event.get("global_var")==null)event.put("global_var", new JObject());
		entity.put("event", event);
	}
	
	private void apply_template_changes(){
		Web.controller._check_template_setting();
		
	}
	
	private void check_template(JSON entity){
		JSON template=entity._entity("template");
		if(template==null){
			entity.put("template", org_config.get("template"));
			return;
		}
		template.put("class",template._string("class", VelocitySupport.class.getName()));
		JList listen=template._list("listen");
		if(listen==null){
			listen=new JList();
			listen.add("vm");
			listen.add("tpl");
		}
		template.put("listen",listen);
		if(template._entity("params")==null)template.put("params",new JObject());
		entity.put("template", template);
		
	}
	
	private void apply_static_resource_changes(){
		Web.controller._check_static_resource_setting(Config.xpath_time("static_resource.delay_load"),Config.xpath_file_size("static_resource.file_max_size"),Config.xpath_int("static_resource.file_cache_min_uses"));
	}
	
	private void check_static_resource(JSON entity){
		JSON static_resource=entity._entity("static_resource");
		if(static_resource==null){
			entity.put("static_resource", org_config.get("static_resource"));
			return;
		}
		static_resource.put("delay_load", static_resource._string("delay_load","3s"));
		static_resource.put("file_max_size",static_resource._string("file_max_size","10MB"));
		static_resource.put("file_cache_min_uses",static_resource._int("file_cache_min_uses",1));
		entity.put("static_resource", static_resource);
	}
	
	
	
	private void _init() throws IOException{
		org_config=config_data();
		boolean config_file_exist=true;
		if(org_config==null){
			log.info("System will use the default config.");
			org_config=new JObject();
			config_file_exist=false;
		}
		
		_init_server_info_config();
		_init_container_config();
		_init_plugin_config();
		_init_event_config();
		_init_template_config();
		_init_static_resource_config();
		_init_muti_language_config();
		_init_keys_config();
		_init_group_config();
		if(org_config.get("db_config")==null)org_config.put("db_config", new JObject());
		if(!config_file_exist){
			file=new File(path_filter(FILE_PATH));
			FileTool.write_to_file(org_config,file);
		}
		run_time_config=(JSON)JSONParser.parse(org_config.toString());
		translator.translate();
		log.info("config data info :");
		log.info(run_time_config.toString());
	}
	

	
	private JSON config_data(){
		file=new File(path_filter(FILE_PATH));
		if(file.exists())return FileTool.read_json(file);
		return null;
		
	}
	
	
	
	
	
	public static Object xpath(String key){
		JSON cf=cfg.run_time_config;
		String[] keys=key.split("\\.");
		int i=0;
		Object obj=null;
		while(i<keys.length){
			if(StringHelper.isNull(keys[i]))throw new ConfigKeysPathIllegal(key);
			int index=retrieve_list(keys[i]);
			if(index==-1){
				if(i==keys.length-1){
					return cf.get(keys[i]);
				}else {
					obj=cf.get(keys[i]);
					if(obj==null)return null;
					if(obj instanceof JSON){
						cf=(JSON)obj;
						i++;
					}else throw new CellKeyIsNotJsonObject(keys[i]);
				}
			}else{
				JList l=(JList)cf.get(keys[i]);
				if(l==null)return null;
				if(l.size()<index){
					return null;
				}
				if(i==keys.length-1){
					return l.get(index);
				}else {
					if(l.get(index) instanceof JSON){
						cf=(JSON)l.get(index);
						i++;
					}else throw new CellKeyIsNotJsonObject(keys[i]);
				}
			}
		}
		return obj;
	}
	
	
	

	
	private void _init_container_config(){
		JSON container=org_config._entity("container");
		if(container==null){
			container=new JObject();
		}
		JList list=container._list("dirs");
		if(list==null)list=new JList();
		if(list.isEmpty()){
			File f=new File(path_filter("/WEB-INF/classes"));
			if(f.isDirectory()){
				list.add(f.getPath());
			}
		}
		container.put("dirs",list);
		org_config.put("container", container);
	}
	
	/**
	 * Create file used to store cell configurations mainly plugin configurations.
	 */
	private void _init_keys_config() throws IOException{
		
	}
	
	private void _init_group_config() throws IOException{
		
	}
	
	private void _init_server_info_config(){
		JSON server_info=org_config._entity("server_info");
		if(server_info==null){
			server_info=new JObject();
		}
		server_info.put("name", server_info._string("name",context.getContextPath()));
		server_info.put("root", server_info._string("root","http://localhost"+context.getContextPath()));
		server_info.put("encode_type", server_info._string("encode_type", "utf-8"));
		server_info.put("description", server_info._string("description", "hot run app"));
		server_info.put("config_check_inverval", server_info._string("config_check_inverval", "3s"));
		server_info.put("enable_config_manager", server_info._bool("enable_config_manager", true));
		_init_config_manager();
		Controller._init_encode_type(server_info._string("encode_type", "utf-8"));
		org_config.put("server_info", server_info);
	}
	
	private void _init_config_manager(){
		JSON config_manager=org_config._entity("config_manager");
		if(config_manager==null){
			config_manager=new JObject();
		}
		config_manager.put("user_name", config_manager._string("user_name","lizar"));
		config_manager.put("password", config_manager._string("password","password"));
		org_config.put("config_manager", config_manager);
	}
	
	private void _init_static_resource_config(){
		JSON static_resource=org_config._entity("static_resource");
		if(static_resource==null)static_resource=new JObject();
		static_resource.put("delay_load", static_resource._string("delay_load","3s"));
		static_resource.put("file_max_size",static_resource._string("file_max_size","10MB"));
		static_resource.put("file_cache_min_uses",static_resource._int("file_cache_min_uses",1));
		org_config.put("static_resource", static_resource);
	}
	
	
	
	private void _init_plugin_config(){
		JSON plugin=org_config._entity("plugin");
		if(plugin==null)plugin=new JObject();
		org_config.put("plugin", plugin);
	}
	
	public static String path_filter(String path){
		if(StringHelper.isNull(path))return context.getRealPath("");
		if(path.length()>9&&StringHelper.equals(path.substring(0, 9).toLowerCase(), "absolute:")){
			path=path.substring(9, path.length());
		}else path=context.getRealPath(path);
		return path;
	}
	
	
	public static String path_filter(String path,String default_path){
		if(StringHelper.isNull(path))return context.getRealPath(default_path);
		if(path.length()>9&&StringHelper.equals(path.substring(0, 9).toLowerCase(), "absolute:")){
			path=path.substring(9, path.length());
		}else path=context.getRealPath(path);
		return path;
	}

	public static long xpath_time(String key){
		Object r= xpath(key);
		if(r==null)return 0;
		return Time.translate_time(r.toString());
	}
	
	public static String xpath_path(String key){
		Object r= xpath(key);
		if(r==null)return context.getRealPath("");
		return path_filter(r.toString(),"");
	}
	
	public static String xpath_str(String key){
		Object r= xpath(key);
		if(r==null)return null;
		return r.toString();
	}
	
	public static String xpath_str(String key,String _default){
		Object r= xpath(key);
		if(r==null)return _default;
		return r.toString();
	}
	
	public static boolean xpath_bool(String key){
		Object r= xpath(key);
		if(r==null)return false;
		if(r instanceof String) return Boolean.parseBoolean(r.toString());
		if(r instanceof Boolean) return (Boolean)r;
		return false;
	}
	
	
	public static JList xpath_list(String key){
		return (JList) xpath(key);
	}
	
	public static int xpath_int(String key){
		return (Integer) xpath(key);
	}
	
	public static Long xpath_long(String key){
		Object o=(Long) xpath(key);
		if(o instanceof Long)return (Long)o;
		if(o instanceof Integer) return Long.parseLong(o.toString());
		if(o instanceof String&&StringHelper.isLong(o.toString())) return Long.parseLong(o.toString());
		if(o instanceof Double)return Long.parseLong(o.toString());
		if(o instanceof Float)return Long.parseLong(o.toString());
		return 0l;
	}
	
	public static double xpath_double(String key){
		Object o=(Long) xpath(key);
		if(o instanceof Long)return (Double)o;
		if(o instanceof Integer) return (Integer)o;
		if(o instanceof String&&StringHelper.isDouble(o.toString())) return Double.parseDouble(o.toString());
		if(o instanceof Double)return (Double)o;
		if(o instanceof Float)return (Float)o;
		return 0;
	}
	
	public static long xpath_file_size(String key){
		Object o=xpath(key);
		if(o==null)return 0;
		return FileTool.file_size(o.toString());
	}
	
	public static JSON xpath_entity(String key){
		return (JSON) xpath(key);
	}
	
	public static String xpath_path(String key,String default_path){
		Object r= xpath(key);
		default_path=path_filter(default_path,"");
		if(r==null)return default_path;
		return path_filter(r.toString(),default_path);
	}
	
	
	/**
	 * tell if the key is represent a list like: var[0]
	 * 
	 * and if it is , the index will return or return -1
	 * */
	private static int retrieve_list(String key){
		int end=key.lastIndexOf("]");
		if(end!=key.length()-1)return -1;
		int start=key.lastIndexOf("[");
		if(start>=end)return -1;
		String value=key.substring(start+1, end);
		if(StringHelper.isInteger(value)){
			return Integer.parseInt(value);
		}
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T xpath(String key,T t){
		JSON cf=cfg.run_time_config;
		String[] keys=key.split("\\.");
		int i=0;
		Object obj=null;
		while(i<keys.length){
			if(StringHelper.isNull(keys[i]))throw new ConfigKeysPathIllegal(key);
			int index=retrieve_list(keys[i]);
			if(index==-1){
				if(i==keys.length-1){
					return (T)cf.get(keys[i]);
				}else {
					obj=cf.get(keys[i]);
					if(obj==null)return null;
					if(obj instanceof JSON){
						cf=(JSON)obj;
						i++;
					}else throw new CellKeyIsNotJsonObject(keys[i]);
				}
			}else{
				JList l=(JList)cf.get(keys[i]);
				if(l==null)return null;
				if(l.size()<index){
					return null;
				}
				if(i==keys.length-1){
					return (T)l.get(index);
				}else {
					if(l.get(index) instanceof JSON){
						cf=(JSON)l.get(index);
						i++;
					}
					else throw new CellKeyIsNotJsonObject(keys[i]);
				}
			}
		}
		return null;
		
	}
	
	private void _init_template_config(){
		JSON template=org_config._entity("template");
		if(template==null){
			template=new JObject();
		}
		template.put("class",template._string("class", VelocitySupport.class.getName()));
		JList listen=template._list("listen");
		if(listen==null){
			listen=new JList();
			listen.add("vm");
			listen.add("tpl");
		}
		template.put("listen",listen);
		if(template._entity("params")==null)template.put("params",new JObject());
		org_config.put("template", template);
	}
	
	private void _init_event_config(){
		JSON event=org_config._entity("event");
		if(event==null){
			event=new JObject();
		}
		event.put("interceptor",event._string("interceptor",""));
		event.put("exception_recorder",event._string("exception_recorder",""));
		if(event.get("global_var")==null)event.put("global_var", new JObject());
		org_config.put("event", event);
	}
	
	
	
	private void _init_muti_language_config() throws IOException{
		JSON muti_lan=org_config._entity("muti_language");
		if(muti_lan==null){
			muti_lan=new JObject();
		}
		muti_lan.put("var",muti_lan._string("var","m"));
		muti_lan.put("default_locale",muti_lan._string("default_locale",""));
		File f=new File(path_filter(I18Msg.DIR));
		FileTool.check_dir(f);
		f=new File(path_filter(I18Msg.DIR+"/i18.txt"));
		if(!f.exists()){
			FileTool.write_to_file( "#this is default locale i18 msg file, you can name different locale as i18.en.txt",f);
		}
		org_config.put("muti_language", muti_lan);
	}
	
	
	
	private class LizarTranslator extends Translator{
		
		@Override
		public void translate() {
			// TODO Auto-generated method stub
			_parse_entity(run_time_config);
		}
		
		/**
		 * 
		 * Translate the configuration from EL expression to string expression.
		 * 
		 * */
		@SuppressWarnings("unchecked")
		private JSON _parse_entity(JSON e){
			for(Entry<String,Object> en:(Set<Entry<String,Object>>)e.toMap().entrySet()){
				if(en.getValue() instanceof String){
					e.put(en.getKey(), express(en.getValue().toString()));
				}else if(en.getValue() instanceof JList){
					e.put(en.getKey(), _parse_list((JList)en.getValue()));
				}else if(en.getValue() instanceof JSON){
					e.put(en.getKey(), _parse_entity((JSON)en.getValue()));
				}
			}
			return e;
		}
		
		private JList _parse_list(JList list){
			Object o=null;
			for(int i=0;i<list.size();i++){
				 o=list.get(i);
				 if(o instanceof String){
					 list.remove(i);
					 list.add(i, express(o.toString()));
				 }else if(o instanceof JList){
					 list.remove(i);
					 list.add(i, _parse_list((JList)o));
				 }else if(o instanceof JSON){
					 list.remove(i);
					 list.add(i, _parse_entity((JSON)o)); 
				 }
			}
			return list;
		}
		
		
		
		/**
		 * Parse an expression which may contain "${}" to string,int,long and so on.
		 */
		@Override
		protected Object express(Object value) {
			// TODO Auto-generated method stub
			if(value==null)return null;
			if(value instanceof String){
				int i=0;
				Object o;
				String k=value.toString();
				while(true){
					if(i>=20){
						throw new ConfigKeyLoopTooDeep(FILE_PATH,value.toString());
					}
					i++;
					if(!Translator.check(k))return k;
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
		
		/**
		 * Get data from cfg.run_time_config by string searcher.<br/>
		 * This method will translate string format searcher to json format which java can parse.
		 */
		private Object explain(String e){
			String[] params=e.split("\\.");
			JSON cf=cfg.run_time_config;
			if(params.length==1)return cf.get(params[0]);
			for(int i=0;i<params.length;i++){
				if(StringHelper.isNotNull(params[i].trim())){
					if(i==params.length-1){
						return cf.get(params[i]);
					}else{
						int pos=params[i].indexOf("[");
						int index=0;
						if(pos!=-1){
							index=Integer.parseInt(params[i].substring(pos+1,params[i].length()-1));
							params[i]=params[i].substring(0,pos);
						}
						Object o=cf.get(params[i]);
						if(o instanceof JList){
							return ((JList)o).get(index);
						}
						cf=(JSON)o;
					}
				}else return cf;
			}
			return null;
		}
		
		
	}
	
}
