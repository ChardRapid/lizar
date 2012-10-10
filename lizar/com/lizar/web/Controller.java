package com.lizar.web;



import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lizar.exception.EventHandlingPathIsNotSupported;
import com.lizar.json.JList;
import com.lizar.json.JSON;
import com.lizar.log.Log;
import com.lizar.log.Logger;
import com.lizar.util.FileTool;
import com.lizar.util.StringHelper;
import com.lizar.web.config.Config;
import com.lizar.web.controller.ContentType;
import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventInterceptor;
import com.lizar.web.controller.EventLoader;
import com.lizar.web.controller.StaticResource;
import com.lizar.web.controller.TemplateSupport;
import com.lizar.web.loader.I18Resource;

/**
 * <p>
 * @task dispatch requests to proper event
 * 
 * @caution1 event's type and priority fields are set to promote retrieval efficiency.
 * @caution2 throws all exceptions
 * @caution3 all redirections happen here
 * </p>
 */
public class Controller{
	private Log log=Logger.newInstance(this.getClass());
	public static TemplateSupport template;
	
	private  Event top;
	
	public static Map<String,Event> level_one_map;
	
	public static Map<String,Event> simple_event_map;

	/**
	 * <event_name,event>
	 */
	public static Map<String,Event> event_map;
	
	public static Map<String,Object> global_vars;	
	
	private static EventInterceptor int_cpt;
	
	private static Controller controller;
	
	public static final String EVENT_DISABLE_FILE="/WEB-INF/lizar/disable_event.json";

    private static Event default_handler;
    
    public static String encode_type;
    
    public static JList disable_list;
    
    public static long disable_file_last_modify;
    
    public static StaticResource static_src;
    
    /**
	 * 
	 * URL *
	 * 
	 * */
	public static final int SEARCH_PRIORITY_TOP=10;
	
	/**
	 * 
	 * URL /ADFA*
	 * 
	 * */
	public static final int SEARCH_PRIORITY_HIGHT=7;
	
	/**
	 * URL /
	 * 
	 * */
	public static final int SEARCH_PRIORITY_MINOR=3;
	
	public static Controller instance(){
		if(controller==null){
			controller=new Controller();
		}
		return controller;
	}
	
	private Controller(){
		if(event_map==null)event_map=Web.events;
		level_one_map=new HashMap<String,Event>();
		simple_event_map=new HashMap<String,Event>();
		disable_list=new JList();
		Map<String,Event> start_map=check_events();
		log.info("..............................................................................");
		log.info("\t\t\tTotally "+start_map.size()+" Event is going to load.");
		log.info("..............................................................................");
		_reload_encode_type(Config.xpath_str("server_info.encode_type"));
		pre_handle(start_map);
		_init_default_event(start_map);
		_init_tpl_vars();
		_init_int_cpt();
		_load_template_support(Config.xpath_str("template.class"),Config.xpath_list("template.listen"),Config.xpath_entity("template.params"));
		ContentType.init();
		static_src=StaticResource.instance(Web.context.getRealPath(""),Config.xpath_time("static_resource.delay_load"),Config.xpath_file_size("static_resource.file_max_size"),Config.xpath_int("static_resource.file_cache_min_uses"));
		EventLoader.context=Web.context;
		reload_disable_map();
	}
	
	private void reload_disable_map(){
		disable_list.clear();
		File f=new File(Config.path_filter(EVENT_DISABLE_FILE));
		if(!f.exists()){
			disable_file_last_modify=f.lastModified();
			try {
				FileTool.write_to_file(disable_list.toString(), new File(Config.path_filter(EVENT_DISABLE_FILE)));
			} catch (IOException e) {
				log.error("an error with   Controller disable file, pls check",e);
			}
			for(Entry<String,Event> entry:event_map.entrySet()){
				entry.getValue().enable=true;
			}
			return;
		}
		if(f.lastModified()!=disable_file_last_modify){
			JSON e=null;
			try{
				e=FileTool.read_json(f);
			}catch(Exception ex){
				log.error("error format of Controller disable file, pls check the json grammar in "+EVENT_DISABLE_FILE);
				return;
			}
			if(e==null){
				log.info(" controller disable file turns to be null, all the diable event will be enable.");
				for(Entry<String,Event> entry:event_map.entrySet()){
					entry.getValue().enable=true;
				}
				disable_file_last_modify=f.lastModified();
				return;
			}
			if(!(e instanceof JList)){
				log.error("Json of "+EVENT_DISABLE_FILE+" file must be an json array.");
				return;
			}
			JList list=(JList)e;
			for(Entry<String,Event> entry:event_map.entrySet()){
				entry.getValue().enable=true;
			}
			for(Object o:list){
				Event event=event_map.get(o.toString());
				if(event!=null){
					event.enable=false;
				}
			}
			disable_file_last_modify=f.lastModified();
		}
	}
	
	public  void _check_disable_event(){
		reload_disable_map();
	}
	
	
	public void _reload_encode_type(String encode_type){
		Controller.encode_type=Config.xpath_str("server_info.encode_type","utf-8");
		Event.encode_type=Config.xpath_str("server_info.encode_type","utf-8");
	}
	
	
	
	private Map<String,Event> check_events(){
		Map<String,Event> start_map =Web.container.get_sub_cells_of(Event.class);
		return start_map;
	}
	
	public void _check_interceptor(String interceptor){
		if(int_cpt==null){
			if(StringHelper.isNotNull(interceptor)){
				_init_int_cpt(interceptor);
			}
			return;
		}
		if(StringHelper.isNull(interceptor)){
			int_cpt=null;
			return;
		}
		if(!int_cpt.getClass().getName().equals(interceptor)){
			_init_int_cpt(interceptor);	
		}
	}
	
	public void _check_default_event(String default_event){

		if(default_handler==null){
			if(StringHelper.isNotNull(default_event))_init_default_event(event_map);
			return;
		}
		if(StringHelper.isNull(default_event)){
			default_handler=null;
			return;
		}
		if(!default_handler.getClass().getName().equals(default_event)){
			_init_default_event(event_map);
		}
	}
	
	public void _check_vars(JSON vars){
    	if(vars!=null){
    		global_vars.clear();
    		for(Entry e:(Set<Entry>)vars.toMap().entrySet()){
    			global_vars.put(e.getKey().toString(),e.getValue());
    		}
    	} 
	}
	

	public void _check_template_setting(){
		String template_class=Config.xpath_str("template.class");
		if(StringHelper.isNull(template_class)){
			template=null;
			return;
		}
		JSON params=Config.xpath_entity("template.params");
		JList listen=Config.xpath_list("template.listen");
		if(template==null){
			_load_template_support(template_class,listen,params);
			return;
		}
	   if(!template.getClass().getName().equals(template_class)){
		   _load_template_support(template_class,listen,params);
			return;
	   }
	   template.listen(listen);
	   if(params!=null&&!params.toMap().isEmpty()){
		   try {
			template.init(Web.context, params);
		} catch (ServletException e) {
			log.warn("Template Support Class "+template_class+" init failed.", e);
		}
	   }
	}
	
	public void _check_static_resource_setting(long delay_load,long file_max_size,int file_cache_min_uses){
		static_src.delay_load=delay_load;
		static_src.file_max_size=file_max_size;
		static_src.file_cache_min_uses=file_cache_min_uses;
		
	}
	
	private void _load_template_support(String template_class,JList listen,JSON params){
		if(StringHelper.isNull(template_class))return;
		try{
			Class t=Class.forName(template_class);
			try{
			t.asSubclass(TemplateSupport.class);
			}catch(Exception e){
				log.debug("Template Support Class "+template_class+" must extends com.lizar.web.controller.TemplateSupport", e);
				return;
			}
			template=(TemplateSupport)t.newInstance();
			template.init(Web.context,params);
			template.listen(listen);
			log.info("..............................................................................");
			log.info("\t\t\tUse template "+template_class+" with listen post fix "+template.listen());
			log.info("..............................................................................");
		}catch(Exception e){
			log.warn("Template Support Class "+template_class+" init failed.", e);
		}
	}
	
	private void pre_handle(Map<String,Event> start_map){
		for(Entry<String,Event> e:start_map.entrySet()){
			_express_event_listener(e.getKey(),e.getValue());
		}
		
	}
	
	/**
	 * 
	 * global Event intercepter init
	 * 
	 * */
	private void _init_int_cpt(){
		_init_int_cpt(Config.xpath("event.interceptor",""));
	}
	
	/**
	 * 
	 * global Event intercepter init
	 * 
	 * */
	private void _init_int_cpt(String interceptor){
		if(StringHelper.isNotNull(interceptor)){
			Object obj=null;
			try {
				obj=Class.forName(interceptor).newInstance();
			} catch (Throwable e){
				System.err.println(e);
				return;
			}
			log.info("..............................................................................");
			log.info("\t\t\tUse Event Listener "+interceptor+" to handle before and after event ");
			log.info("..............................................................................");
			if(obj!=null)this.int_cpt=(EventInterceptor)obj;
		}
	}
	
	
	
	private void _init_default_event(Map<String,Event> start_map) {
		String event=Config.xpath("event.default_event","");
		if(StringHelper.isNotNull(event)){
			if(event.indexOf(".")!=-1){
				for(Event e:start_map.values()){
					if(e.getClass().getName().equals(event)){
						default_handler=e;
						log.info("..............................................................................");
						log.info("\t\t\tdefault event use "+event+" as default event");
						log.info("..............................................................................");
						break;
					}
				}
				if(default_handler==null){
					log.info("..............................................................................");
					log.info("\t\t\tDefault Event :"+event+" can not found in event map pls check.");
					log.info("..............................................................................");
				}
			}else{
				default_handler=start_map.get(event);
				if(default_handler==null){
					log.info("..............................................................................");
					log.info("\t\t\tDefault Event :"+event+" can not found in event map pls check.");
					log.info("..............................................................................");
				}
			}
		}
	}
	
	
	private void _init_tpl_vars(){
		global_vars=new HashMap<String,Object>();
		JSON vars=Config.xpath_entity("event.global_var");
    	if(vars!=null){
    		for(Entry e:(Set<Entry>)vars.toMap().entrySet()){
    			global_vars.put(e.getKey().toString(),e.getValue());
    		}
    	}
	}
	
	
	
   
    private void pre_init(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException{
    	request.setCharacterEncoding(encode_type);
		response.setCharacterEncoding(encode_type);
		pre_tpl_global_var(request);
		pre_locale_map(request);
    }

   
    
    private void pre_locale_map(HttpServletRequest request){
    	I18Resource res=Web.i18.get(request.getLocale().getLanguage());
    	if(res!=null)  	request.setAttribute(Web.i18.var, res.map);
    }
    
    private void pre_tpl_global_var(HttpServletRequest request){
    	if(global_vars!=null){
    		for(Entry<String,Object> e:global_vars.entrySet()){
    			request.setAttribute(e.getKey(), e.getValue());
    		}
    	}
    }
    


	public static String level_zero(String path){
		int i=path.indexOf("/",1);
		if(i==-1)return path;
		return path.substring(0,i);
	}

	
	private void translate_handling_path(Event e){
		if(e.priority()==SEARCH_PRIORITY_HIGHT){
			if(e.handling_path().endsWith("/*"))e.handling_path(e.handling_path().substring(0,e.handling_path().length()-2));
			else e.handling_path(e.handling_path().substring(0,e.handling_path().length()-1));
		}else if(e.priority()==SEARCH_PRIORITY_MINOR){
			if(StringHelper.isNull(e.handling_path())||e.handling_path().equals("/"))e.handling_path("/");
			else if(e.handling_path().endsWith("/"))e.handling_path(e.handling_path().substring(0,e.handling_path().length()-1));
		}
	}
	
	
	


	/**
	 * @task1 init an event
	 * @task2 set an event's priority
	 * @task3 assign value to queue and maps.
	 * 
	 * @caution  there is only one default event in system.
	 */
	private  void _express_event_listener(String key,Event event) {
	   event.handling_path(StringHelper.isNull(event.default_handling_path())?"":event.default_handling_path().toLowerCase().trim());
	   event.priority(get_event_priority(event.handling_path().trim()));
	   translate_handling_path(event);
	   if(event.priority()==SEARCH_PRIORITY_TOP){
		   top=event;
		   log.warn("System detect /* path event :"+event.getClass().getName()+", the rest event can not take effect.");
	   }else if(event.priority()==SEARCH_PRIORITY_HIGHT) {
		   level_one_map.put(level_zero(event.handling_path()),event);
		   log.info("Event "+event.getClass().getName()+" with key "+key+" has been loaded in complex event map with high level.");
	   } else if(event.priority()==SEARCH_PRIORITY_MINOR){
		   simple_event_map.put(event.handling_path(), event);
		   log.info("Event "+event.getClass().getName()+" with key "+key+" has been loaded in simple event map.");
		  
	   }
	   event.set_servlet_context(Web.context);
	   event_map.put(key, event);
	}

	
	public static int event_level(String visit_uri){
		if(visit_uri.length()==1)return 0;
		visit_uri=visit_uri.substring(1,visit_uri.length());
		int i=0;
		for(String s:visit_uri.split("/")){
			if(StringHelper.isNotNull(s))i++;
		}
		return i-1;
	}
	

	
	/**
	 * Set a event's priority according its visit_uri.
	 * @param visit_uri
	 * @return
	 * @throws Exception 
	 */
	public static int get_event_priority(String visit_uri)  {
		if(StringHelper.isNull(visit_uri)||visit_uri.indexOf("*")==-1)return SEARCH_PRIORITY_MINOR;
		if(visit_uri.equals("*")||visit_uri.equals("/*")) return SEARCH_PRIORITY_TOP;
		if(visit_uri.endsWith("*"))return SEARCH_PRIORITY_HIGHT;
		throw new EventHandlingPathIsNotSupported(visit_uri+" is currently not support in chard controller uri interceptor pattern");
	}
    
	private void _handle(Event handler,String path,EventLoader event_loader) throws ServletException, IOException{
		if(path.indexOf(".")==-1){
			handler.before(event_loader);
			handler.handle(event_loader);
			handler.after(event_loader);
		}else if(path.endsWith(".json")){
			handler.before(event_loader);
			handler.handle_json(event_loader);
			handler.after(event_loader);
		}else if(path.endsWith(".xml")){
			handler.before(event_loader);
			handler.handle_xml(event_loader);
			handler.after(event_loader);
		}else if(path.endsWith(".jsonp")){
			handler.before(event_loader);
			handler.handle_jsonp(event_loader);
			handler.after(event_loader);
		}else{
			StaticResource.handle(event_loader, path, true);
		}
		if(int_cpt!=null)int_cpt.after(event_loader);
	}
    
	public static String postfix(String path){
		if(StringHelper.isNull(path))return "";
		int p=path.lastIndexOf(".");
		if(p==-1)return "";
		return path.substring(p+1, path.length());
	}
	
	private String path_filter(String path){
		if(StringHelper.isNull(path))return "/";
		if(path.length()>1&&path.endsWith("/"))return path.substring(0, path.length()-1);
		return path;
	}
	
	public void handle_event(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException{
		String path="/";
		if(!StringHelper.equals(request.getContextPath()+"/", request.getRequestURI()))path=path_filter(request.getServletPath());
		pre_init(request,response);
		String post_fix=postfix(path);
		EventLoader event_loader = new EventLoader( path,request, response);
		if(int_cpt!=null)int_cpt.before(event_loader);
		event_loader.postfix(post_fix);
		event_loader.lan=request.getLocale().getLanguage();
		if(StringHelper.isNotNull(post_fix)&&template!=null&&template.listen().indexOf(post_fix)!=-1){
			template.handle(event_loader);
			if(int_cpt!=null)int_cpt.after(event_loader);
			return;
		}
		if(top!=null&&top.enable){
			try{
				_handle(top,path,event_loader);
			}catch(Exception e){
				log.error("request:"+path+" throw a exception in "+top.getClass().getName(),e);
				handle_500(event_loader);
			}
			return;
		}
		Event event=seek_event_handler(path.toLowerCase());
		if(event==null||!event.enable){
			if(default_handler==null){
				handle_404(event_loader);
				return;
			}else{
				try{
					_handle(default_handler,path,event_loader);
				}catch(Exception e){
					log.error("request:"+path+" throw a exception in "+event.getClass().getName(),e);
					handle_500(event_loader);
				}
			}
		}else{
			try{
				_handle(event,path,event_loader);
			}catch(Exception e){
				log.error("request:"+path+" throw a exception in "+event.getClass().getName(),e);
				handle_500(event_loader);
			}
		}
	}
	
	private void handle_404(EventLoader event_loader) throws IOException, ServletException{
		HttpServletRequest request=event_loader.request();
		String path=path_filter(request.getServletPath());
		String post_fix=postfix(path);
		String full_path=Config.path_filter("/WEB-INF/lizar/404."+post_fix, "");
		File _404=new File(full_path);
		if(_404.isFile()){
			StaticResource.handle_abs_file(event_loader, full_path, true,HttpServletResponse.SC_NOT_FOUND);
		}else{
			event_loader.postfix("html");
			full_path=Config.path_filter("/WEB-INF/lizar/404.html");
			if(!_404.exists()){
				_404=new File(full_path);
				FileTool.write_to_file( "The event you are looking for is not exists, if you want to change the 404 content, pls replace the file /WEB-INF/lizar/404.html",_404);
			}else{
				FileTool.delete_sub_files(_404);
				_404.delete();
				_404=new File(full_path);
				FileTool.write_to_file("The event you are looking for is not exists, if you want to change the 404 content, pls replace the file /WEB-INF/lizar/404.html",_404);
			}
			StaticResource.handle_abs_file(event_loader, full_path, true,HttpServletResponse.SC_NOT_FOUND);
		}
		if(int_cpt!=null)int_cpt.after(event_loader);
	}

	private void handle_500(EventLoader event_loader) throws IOException, ServletException{
		HttpServletRequest request=event_loader.request();
		String path=path_filter(request.getServletPath());
		String post_fix=postfix(path);
		String full_path=Config.path_filter("/WEB-INF/lizar/500."+post_fix, "");
		File _500=new File(full_path);
		if(_500.isFile()){
			StaticResource.handle_abs_file(event_loader, full_path, true,HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}else{
			event_loader.postfix("html");
			full_path=Config.path_filter("/WEB-INF/lizar/500.html");
			if(!_500.exists()){
				_500=new File(full_path);
				FileTool.write_to_file("The event you visit get a server internal exception, if you want to change the 500 content, pls replace the file /WEB-INF/lizar/500.html",_500);
			}else{
				FileTool.delete_sub_files(_500);
				_500.delete();
				_500=new File(full_path);
				FileTool.write_to_file( "The event you visit get a server internal exception, if you want to change the 500 content, pls replace the file /WEB-INF/lizar/500.html",_500);
			}
			StaticResource.handle_abs_file(event_loader, full_path, true,HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		if(int_cpt!=null)int_cpt.after(event_loader);
	}
	

	/**
	 * @param path such as /search /search.json /search/blog.json
	 */
	private Event seek_event_handler(String path){
		int second=path.indexOf("/", 1);
		if(second!=-1)path=path.substring(0, second);
		else if(path.contains("."))path=path.substring(0, path.indexOf("."));
		Event e=simple_event_map.get(path);
		if(e!=null)return e;
		String l_0=level_zero(path);
		e=level_one_map.get(l_0);
		if(e!=null){
			if(e.priority()==SEARCH_PRIORITY_HIGHT)return e;
			if(e.handling_path().equals(path))return e;
		}
		return null;
	}

	
	
	public void destroy(){
		static_src.destroy();
	}




}
