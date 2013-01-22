package com.lizar.web;



import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
import com.lizar.web.controller.After;
import com.lizar.web.controller.Before;
import com.lizar.web.controller.ContentType;
import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventInterceptor;
import com.lizar.web.controller.EventLoader;
import com.lizar.web.controller.JSONHandler;
import com.lizar.web.controller.JSONPHandler;
import com.lizar.web.controller.StaticResource;
import com.lizar.web.controller.TemplateSupport;
import com.lizar.web.controller.XMLHandler;
import com.lizar.web.loader.I18Resource;

/**
 * <p>
 * @task Intercept and dispatch requests to proper event
 * 
 * Event handlers with unsupported handle path will not be put into event_handler_map and will not handle request.
 * 			Unsupported handle path(Event's handle_path field) :
 * 								1 who is not start with "/"
 * 								2 who contains illegal chars such as '.'
 * 								3 null and ""
 * 								4 multi-"/" such as "/xxx//user"
 * 
 * 			Intercepting steps : 
 * 								1 if a template exists and listens the file type, the handle the request 
 * 
 * 								2 if a event handler in minor_event_handler_map can catch the request then handle it.
 * 
 * 								3 if all of above fail, get handler from high_event_handler_map
 * 
 * 
 * 			How a request visit path match an event handler in high_event_handler_map:
 * 								Firstly eliminating postfix like ".json" or "/" of visit path,but visit path is "/" do nothing.
 * 								Secondly,get from high_event_handler_map, if not match, eliminate last "/xxx" from visit path
 * 								then get from high_event_handler_map, if matches return the event handler else eliminate last "/xxx" 
 * 								from visit path and search again until find proper event handler or visit path is null.
 * 
 * 
 * @caution1 Event's priority field is set to promote retrieve efficiency.
 * 
 * @caution2 If "/xxx" and "/xxx/*" both are events' handle path, "/xxx"'s priority is higher.
 * 			 if "/xxx/*" and "/xxx/user/*" both are events' handle path, "/xxx/user/*"'s priority is higher.
 * 
 * @caution3 If an event handler is configured as default event handler, its handle path doesn't work 
 * 				and t is not the top event handler any more now, if it was.
 * 
 * @caution4 "/*" can intercept all request but its priority is the lowest in system.
 * 
 * </p>
 */
public class Controller{
	private Log log=Logger.newInstance(this.getClass());
	public static TemplateSupport template;
	
	
	/**
	 * 
	 * store event handler with high priority
	 * 
	 * <path,event>
	 * 
	 * Path is used to seek event handler an it is event's handle path without "/*" as end.
	 * If you want to intercept all request put <"/",event>
	 * 
	 */
	public static Map<String,Event> high_event_handler_map;
	
	/**
	 * 
	 * store event handler with minor priority
	 * 
	 * <path,event>
	 * 
	 * Path is used to seek event handler an it is event's handle path without "/" as end.
	 * If handle path is "/" put <"/",event>
	 * 
	 */
	public static Map<String,Event> minor_event_handler_map;

	/**
	 * <event_full_class_name,event>
	 * 
	 * all event handlers go here.
	 */
	public static Map<String,Event> event_map;
	
	/**
	 * configured in lizar.json which is set to response.
	 */
	public static Map<String,Object> global_vars;
	
	private static EventInterceptor int_cpt;
	
	private static Controller controller;
	
	public static final String EVENT_DISABLE_FILE="/WEB-INF/lizar/disable_event.json";

    public static String encode_type;
    
    public static JList disable_list;
    
    public static long disable_file_last_modify;
    
    public static StaticResource static_src;
    
	
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
		high_event_handler_map=new HashMap<String,Event>();
		minor_event_handler_map=new HashMap<String,Event>();
		disable_list=new JList();
		Map<String,Event> start_map=check_events();
		log.info("..............................................................................");
		log.info("\t\t\tTotally "+start_map.size()+" Event is going to load.");
		log.info("..............................................................................");
		pre_handle(start_map);
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
	
	public static void _init_encode_type(String encode_type){
		Controller.encode_type=encode_type;
		Event.encode_type=encode_type;
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
	
	public void _check_vars(JSON vars){
    	if(vars!=null){
    		global_vars.clear();
    		for(Entry e:(Set<Entry>)vars.toMap().entrySet()){
    			global_vars.put(e.getKey().toString(),e.getValue());
    		}
    	} 
	}
	
	/**
	 * Applying template config from config file lizar.json and 
	 * 
	 * this allow system apply and config template dynamically.
	 * 
	 */
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
			if(obj!=null) int_cpt=(EventInterceptor)obj;
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

	/**
	 * If event's priority is high there is two cases "/*" and "/mc/user/*"
	 * 									
	 * If event's priority is minor there is three cases  "/", /mc" and "/mc/"					
	 */
	private void translate_handling_path(Event e){
		if(e.priority()==SEARCH_PRIORITY_HIGHT){
			if(e.handling_path().length()>2&&e.handling_path().endsWith("/*"))e.handling_path(e.handling_path().substring(0,e.handling_path().length()-2));
			else e.handling_path(e.handling_path().substring(0,e.handling_path().length()-1));
		}else if(e.priority()==SEARCH_PRIORITY_MINOR){
			if(e.handling_path().equals("/"))e.handling_path("/");
			else if(e.handling_path().endsWith("/"))e.handling_path(e.handling_path().substring(0,e.handling_path().length()-1));
			else e.handling_path(e.handling_path());
		}
	}
	
	
	


	/**
	 * @task1 set an event's priority
	 * @task2 set and an event's priority
	 * @task3 assign values to maps.
	 * 
	 * @caution1 if event.default_handling_path() is not supported, it will not be put into event_handler_map.
	 * 
	 * @param key event's full class name
	 * 
	 */
	private  void _express_event_listener(String key,Event event) {
	   event.handling_path(StringHelper.isNull(event.default_handling_path())?"":event.default_handling_path().toLowerCase().trim());
	   event.priority(get_event_priority(event.handling_path().trim()));
	   translate_handling_path(event);
	   if(event.priority()==SEARCH_PRIORITY_HIGHT) {
		   high_event_handler_map.put(event.handling_path(), event);
		   log.info("High level event handler "+event.getClass().getName()+" with key "+key+" has been loaded to high_event_handler_map.");
	   }else if(event.priority()==SEARCH_PRIORITY_MINOR){
		   minor_event_handler_map.put(event.handling_path(), event);
		   log.info("Minor level event handler "+event.getClass().getName()+" with key "+key+" has been loaded to minor_event_handler_map.");
	   }
	   event.set_servlet_context(Web.context);
	   event_map.put(key, event);
	}

	/**
	 * @returns 0 if request top event
	 * 
	 * @param visit_uri such as /search/blog.json
	 */
	public static int visit_path_level(String visit_uri){
		if(StringHelper.isNull(visit_uri))return 0;
		visit_uri=visit_uri.substring(1,visit_uri.length());
		int i=0;
		for(String s:visit_uri.split("/")){
			if(StringHelper.isNotNull(s))i++;
		}
		return i;
	}
	

	
	/**
	 * <p>
	 * Get a event's priority according its handle path.
	 * 
	 * priority:
	 * 			high : if visit_uri is end with "/*" 
	 * 			minor : if visit_uri doesn't contain "*"
	 * 
	 * @caution visit_uri must start with "/".
	 * </p>
	 * 
	 * @param visit_uri event's handle_path
	 * 
	 * @throws EventHandlingPathIsNotSupported if visit_uri format is not supported.
	 */
	public static int get_event_priority(String visit_uri){
		if(StringHelper.isNotNull(visit_uri)&&visit_uri.startsWith("/")){
			if(visit_uri.endsWith("/*"))return SEARCH_PRIORITY_HIGHT;
			if(visit_uri.indexOf("*")==-1)return SEARCH_PRIORITY_MINOR;
		}
		
		throw new EventHandlingPathIsNotSupported(visit_uri+" is currently not supported in  controller uri interceptor pattern");
	}
    
	private void _handle(Event handler,String path,EventLoader el) throws ServletException, IOException{
		if(path.indexOf(".")==-1){
			if(handler instanceof Before)((Before)handler).before(el);
			handler.handle(el);
			if(handler instanceof After)((After)handler).after(el);
		}else if(path.endsWith(".json")){
			if(handler instanceof Before)((Before)handler).before(el);
			if(handler instanceof JSONHandler)((JSONHandler)handler).handle_json(el);
			if(handler instanceof After)((After)handler).after(el);
		}else if(path.endsWith(".xml")){
			if(handler instanceof Before)((Before)handler).before(el);
			if(handler instanceof XMLHandler)((XMLHandler)handler).handle_xml(el);
			if(handler instanceof After)((After)handler).after(el);
		}else if(path.endsWith(".jsonp")){
			if(handler instanceof Before)((Before)handler).before(el);
			if(handler instanceof JSONPHandler)((JSONPHandler)handler).handle_jsonp(el);
			if(handler instanceof After)((After)handler).after(el);
		}else{
			StaticResource.handle(el, path, true);
		}
		if(int_cpt!=null&&el.need_after){
			int_cpt.after(el);
			el.need_after=false;
		}
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
		String path=StringHelper.isNull(request.getPathInfo())?"/":path_filter(request.getPathInfo());
		pre_init(request,response);
		String post_fix=postfix(path);
		boolean need_before=true;
		EventLoader el =(EventLoader)request.getAttribute("com.lizar.web.controller.eventloader");
		if(el==null){
			el=new EventLoader( path,request, response);
			request.setAttribute("com.lizar.web.controller.eventloader", el);
		}else need_before=false;
		if(int_cpt!=null&&need_before)int_cpt.before(el);
		el.postfix(post_fix);
		el.lan=request.getLocale().getLanguage();
		if(StringHelper.isNotNull(post_fix)&&template!=null&&template.listen().indexOf(post_fix)!=-1){
			template.handle(path,el);
			if(int_cpt!=null&&el.need_after){
				int_cpt.after(el);
				el.need_after=false;
			}
			return;
		}
		Event event=seek_event_handler(path.toLowerCase());
		if(event==null||!event.enable){
			handle_404(el);
		} else{
			try{
				_handle(event,path,el);
			}catch(Exception e){
				log.error("request:"+path+" throw a exception in "+event.getClass().getName(),e);
				handle_500(el);
			}
		}
	}
	
	private void handle_404(EventLoader el) throws IOException, ServletException{
		String path=el.request_path();
		String post_fix=postfix(path);
		String full_path=Config.path_filter("/WEB-INF/lizar/404."+post_fix, "");
		File _404=new File(full_path);
		if(_404.isFile()){
			StaticResource.handle_abs_file(el, full_path, true,HttpServletResponse.SC_NOT_FOUND);
		}else{
			el.postfix("html");
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
			StaticResource.handle_abs_file(el, full_path, true,HttpServletResponse.SC_NOT_FOUND);
		}
		if(int_cpt!=null)int_cpt.after(el);
	}

	private void handle_500(EventLoader el) throws IOException, ServletException{
		String path=el.request_path();
		String post_fix=postfix(path);
		String full_path=Config.path_filter("/WEB-INF/lizar/500."+post_fix, "");
		File _500=new File(full_path);
		if(_500.isFile()){
			StaticResource.handle_abs_file(el, full_path, true,HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}else{
			el.postfix("html");
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
			StaticResource.handle_abs_file(el, full_path, true,HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		if(int_cpt!=null)int_cpt.after(el);
	}
	

	/**
	 * @param path such as /search /search.json /search/blog.json
	 */
	private Event seek_event_handler(String path){
		if(path.contains("."))path=path.substring(0, path.indexOf("."));
		if(!StringHelper.equals(path, "/")&&path.endsWith("/"))path=path.substring(0, path.length()-1);
		Event e=null;
		e=minor_event_handler_map.get(path);
		if(e!=null)return e;
		int level = visit_path_level(path);
		for(int l=0;l<level;l++){
			e=high_event_handler_map.get(path);
			if(e!=null)return e;
			path=path.substring(0, path.lastIndexOf("/"));
		}
		e=high_event_handler_map.get("/");
		return e;
	}

	
	
	public void destroy(){
		static_src.destroy();
	}

	public static void main(String[] args){
		System.out.println(visit_path_level("//"));
	}


}
