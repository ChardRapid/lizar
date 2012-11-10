package com.lizar.web;


import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


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
public class JspSupport implements Filter{
    
	
	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain chain) throws IOException, ServletException {
		// TODO Auto-generated method stub
		HttpServletRequest request=(HttpServletRequest)arg0;
		HttpServletResponse response=(HttpServletResponse)arg1;
		String path=request.getRequestURI();
		if(path.endsWith(".jsp")){
			request.getRequestDispatcher(path.substring(request.getContextPath().length()+1,path.length())).forward(request, response);
		}
		chain.doFilter(arg0, arg1);
	}

	
    
    @Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
		
	}



	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}
    
	

	
	

	


	
	
	
	
	

	
	
	
	
	
	
}
