package com.lizar.web;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lizar.log.Log;
import com.lizar.log.Logger;
import com.lizar.web.config.Config;
import com.lizar.web.config.Group;
import com.lizar.web.config.I18Msg;
import com.lizar.web.config.Keys;
import com.lizar.web.controller.Event;
import com.lizar.web.loader.Module;
import com.lizar.web.loader.Plugin;


/**
 * 
 * @caution1 config the config.json file location in web.xml, or system will use default location /WEB-INF/config.json
 * @caution2 dao,plugin,event and all kinds of modules have only one instance in system.
 * 
 * @task1 startup system components:<br/>
 * 			two level components:module, {cell,plugin,event}<br/>
 * 			cell: lizar object<br/>
 * 			plugin: scheduled task<br/>
 * 			event: user request task<br/>
 * 			sequence: 1 model{container,controller,pluginManager},2 {cell,plugin,event}
 * @task2 <strong>receives</strong> all users' requests, controller <strong>dispatches</strong> the requests to proper event
 *  	  and then event <strong>handles</strong> the request.
 * 
 * */
public class Web extends HttpServlet{
    
	private Log log=Logger.newInstance(this.getClass());
	
	private static final long serialVersionUID = 6456484779700079405L;

    public static Map<String,Plugin> plugins;
    
    public static Map<String,Event> events;
    
    public static Map<String,Module> modules;
    
    public static Controller controller;
    
    public static PluginManager plugin_manager;
    
    public static Group group;

    public static Keys keys;
    
    public static I18Msg i18;
    
    public static com.lizar.web.config.Config cfg;
    
    public static ServletContext context;
    
    public static Container container;
    
    public static boolean debug=false;
    
    public static ModuleManager module_manager;
 
    
    public static <T> T get(Class<T> t){
    	return container.get( t);
    }
    
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		controller.handle_event(request, response);
	}
	
    
    @Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		context=this.getServletContext();
		String debug_mode=this.getInitParameter("debug");
		if(debug_mode!=null&&debug_mode.trim().toLowerCase().equals("true"))debug=true;
		log.info("Start to Run...");
		log.info("Base Dir:"+this.getServletContext().getRealPath("/"));
		try {
			_init_property();
		} catch (Exception e) {
			log.info("Framework init property failed, pls check....",e);
			return;
		}
		
	}
    
	/**
	 * 
	 * @throws Exception 
	 * @task1 init all the properties
	 * @task2 init the config set in lizar.json
	 * */
	private void _init_property() throws Exception {
		log.info("Init Property.......");
		plugins=new HashMap<String,Plugin>();
		events=new HashMap<String,Event>();
		modules=new HashMap<String,Module>();
		cfg=Config.instance();						//load global system configuration
		keys=Keys.instance();						//load cell configuration
		group=Group.instance();						//load cell configuration
		i18=I18Msg.instance();
		
		
		keys.persistence();							//read msg from file
		group.persistence();						//read group from file
		
		container=Container.instance();				//search and create all the cells and init property
		
		keys.persistence();							//store msg to file
		group.persistence();						//store group to file
		i18.persistence();							//load msg from files
		
		module_manager=ModuleManager.instance();
		plugin_manager=PluginManager.instance();	//create all plugins and call pre_run()
		controller=Controller.instance();
		cfg.ready_to_check_changes();				//ready to check changes
	}
    


	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		log.info("..............................................................................");
		log.info("\t\t\tlizar is going to clean up threads.");
		log.info("");
		plugin_manager.destroy();
		cfg.destroy();
		controller.destroy();
	}


	
	

	
	

	


	
	
	
	
	

	
	
	
	
	
	
}
