package com.lizar.web.loader;

import java.io.File;
import java.util.Date;
import java.util.TimerTask;

import javax.servlet.ServletContext;

/**
 * 用来扩展系统模块，单例
 *
 */
public abstract class Plugin extends TimerTask implements Cell{
	
	public Date start_time;
	
	public String interval_time;
	
	public String delay_time;
	
	public File file;
	
	public long last_modify;
	
	private static ServletContext context;
	
	public void set_servlet_context(ServletContext _context){
		context=_context;
	}

	public ServletContext context(){return context;}

	public abstract void pre_run()throws Exception;
	  
	public abstract void run();
	
	public abstract void destroy();
	
	public abstract Date set_start_time();
	
	public abstract String set_interval_time();
	
	public abstract String set_delay_time();
}
