package org.lizar.web.loader;

import java.util.Date;

import javax.servlet.ServletContext;

import org.lizar.web.Lifecycle;

/**
 * 用来扩展系统模块，单例
 *
 */
public abstract class Plugin implements Cell, Runnable, Lifecycle{
	
	public Date start_time;
	
	public String interval_time;
	
	public String delay_time;
	
	private static ServletContext context;
	
	public void setContext(ServletContext _context){
		context=_context;
	}

	public ServletContext getContext(){
		return context;
	}
	  
	public abstract Date set_start_time();
	
	public abstract String set_interval_time();
	
	public abstract String set_delay_time();
}
