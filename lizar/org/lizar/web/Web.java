package org.lizar.web;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lizar.log.Log;
import org.lizar.log.Logger;
import org.lizar.web.config.Config;
import org.lizar.web.config.Group;
import org.lizar.web.config.I18Msg;
import org.lizar.web.config.Keys;
import org.lizar.web.controller.Event;
import org.lizar.web.loader.Module;
import org.lizar.web.loader.Plugin;
import org.lizar.web.Container;
import org.lizar.web.Lifecycle;
import org.lizar.exception.LifecycleException;



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
    
    public static org.lizar.web.config.Config cfg;
    
    public static ServletContext context;
    
    public static Container container;
    
    public static boolean debug=false;
    
    public static ModuleManager module_manager;
 
    
    @SuppressWarnings("static-access")
	public static <T> T get(Class<T> t){
    	return container.get(t);
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
	 * starting all system components
	 * 
	 * if any exception happens, system start fails
	 * 
	 * */
	private void _init_property() throws Exception {
		log.info("..............................................................................");
		log.info("\t\t\tlizar is going to start");
		plugins=new HashMap<String,Plugin>();
		events=new HashMap<String,Event>();
		modules=new HashMap<String,Module>();
		
		cfg=Config.instance();						//load global system configuration
		if(cfg instanceof Lifecycle) ((Lifecycle)cfg).start();
		
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
		
		plugin_manager=PluginManager.instance();	//create all plugins
		if(plugin_manager instanceof Lifecycle) ((Lifecycle)plugin_manager).start();
		
		controller=Controller.instance();
		if(controller instanceof Lifecycle) ((Lifecycle)controller).start();
		
		cfg.ready_to_check_changes();				//ready to check changes
	}
    


	/**
	 * Destruction of the servlet. <br>
	 * 
	 * stop all active components.
	 */
	public void destroy() {
		log.info("..............................................................................");
		log.info("\t\t\tlizar is going to stop");
		try {
			controller.stop();
		} catch (LifecycleException e2) {
			// TODO Auto-generated catch block
			log.error("Component Config has been stopped",e2);
		}
		
		try {
			plugin_manager.stop();
		} catch (org.lizar.exception.LifecycleException e1) {
			// TODO Auto-generated catch block
			log.error("Component PluginManager has been stopped",e1);
		}
		
		try {
			cfg.stop();
		} catch (LifecycleException e3) {
			// TODO Auto-generated catch block
			log.error("Component Config has been stopped",e3);
		}
		log.info("..............................................................................");
	}


	
	

	
	

	


	
	
	
	
	

	
	
	
	
	
	
}
