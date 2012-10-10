package com.lizar.web;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lizar.log.Log;
import com.lizar.log.Logger;
import com.lizar.web.config.Config;
import com.lizar.web.config.Group;
import com.lizar.web.config.I18Msg;
import com.lizar.web.config.Keys;
import com.lizar.web.controller.Event;
import com.lizar.web.loader.Plugin;


/**
 * 
 * @caution1 config the config.json file location in web.xml, or system will use default location /WEB-INF/config.json
 * @caution2 dao,plugin,event and all kinds of modules have only one instance in system.
 * 
 * @task1 startup system components:<br/>
 * 			two level components:module, {dao,plugin,event}<br/>
 * 			dao: database access object<br/>
 * 			plugin: scheduled task<br/>
 * 			event: user request task<br/>
 * 			sequence: 1 model{container,controller,pluginManager},2 {cell,plugin,event}
 * @task2 <strong>receives</strong> all users' requests, controller <strong>dispatches</strong> the requests to proper event
 *  	  and then event <strong>handles</strong> the request.
 * 
 * */
public class Web implements Filter{
    
	private Log log=Logger.newInstance(this.getClass());
	
	private static final long serialVersionUID = 6456484779700079405L;

    public static Map<String,Plugin> plugins;
    
    public static Map<String,Event> events;
    
    public static Controller controller;
    
    public static PluginManager plugin_manager;
    
    public static Group group;

    public static Keys keys;
    
    public static I18Msg i18;
    
    public static com.lizar.web.config.Config cfg;
    
    public static ServletContext context;
    
    public static Container container;
    
    public static boolean debug=false;
    
 
    
    public static <T> T get(Class<T> t){
    	return container.get( t);
    }
    
	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain chain) throws IOException, ServletException {
		// TODO Auto-generated method stub
		HttpServletRequest request=(HttpServletRequest)arg0;
		HttpServletResponse response=(HttpServletResponse)arg1;
		String path=request.getRequestURI();
		if(path.endsWith(".jsp")){
			request.getRequestDispatcher(path.substring(request.getContextPath().length()+1,path.length())).forward(request, response);
		}else controller.handle_event(request, response);
	}

	
    
    @Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		context=filterConfig.getServletContext();
		String debug_mode=filterConfig.getInitParameter("debug");
		if(debug_mode!=null&&debug_mode.trim().toLowerCase().equals("true"))debug=true;
		log.info("Start to Run...");
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
		cfg=Config.instance();						//load system configuration
		keys=Keys.instance();
		group=Group.instance();	
		i18=I18Msg.instance();
		
		
		keys.persistence();							//read msg from file
		group.persistence();						//read group from file
		
		container=Container.instance();				//search and create all the cells and init property
		
		keys.persistence();							//store msg to file
		group.persistence();						//store group to file
		i18.persistence();							//load msg from files
		
		
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
