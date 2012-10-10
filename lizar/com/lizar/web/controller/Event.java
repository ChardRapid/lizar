package com.lizar.web.controller;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.lizar.web.loader.Cell;

/**
 * @caution1 handle user request,just one instance in system.
 * @caution2 contains <strong>common</strong> resource and function of a kind event
 * @caution3 Resource own by an event and a <code>EventLoader</code> are all resource needed by a user request
 * @caution4 redirect and output ajax data are executed in {@link com.lizar.web.Controller controller}.
 * 
 * @see com.lizar.web.controller.EventLoader
 * @see com.lizar.web.Controller
 * @see com.chard.LoaderStater
 */
abstract public class Event implements Cell{
	private String handling_path;

	public  boolean enable=true;
	
	private int priority;
	
	public static String encode_type;
	
	public int priority(){
		return priority;
	}
	
	public void priority(int i){
		priority=i;
	}
	
	public void handling_path(String path){
		handling_path=path;
	}
	
	public String handling_path(){
		return handling_path;
	}
	
	private static ServletContext context;
	
	public void set_servlet_context(ServletContext _context){
		context=_context;
	}

	public ServletContext context(){return context;}
	
	/**
	 * for init visit_uri
	 */
	public abstract  String default_handling_path();
	
	/**
	 * @task invoked by controller,handle user request
	 * @param event_loader resource and function owned by each request
	 */
	public abstract void handle(EventLoader event_loader)throws ServletException, IOException ;
	

	/**
	 * @task invoked by controller,handle user request
	 * @param event_loader resource and function owned by each request
	 */
	public abstract void handle_jsonp(EventLoader event_loader)throws ServletException, IOException ;

	/**
	 * @task invoked by controller,handle user request
	 * @param event_loader resource and function owned by each request
	 */
	public abstract  void handle_json(EventLoader event_loader)throws ServletException, IOException ;
	

	/**
	 * @task invoked by controller,handle user request
	 * @param event_loader resource and function owned by each request
	 */
	public abstract void handle_xml(EventLoader event_loader)throws ServletException, IOException ;
	
	/**
	 * @task invoked by controller,handle user request
	 * @param event_loader resource and function owned by each request
	 */
	public abstract  void before(EventLoader event_loader)throws ServletException, IOException ;
	
	public abstract  void after(EventLoader event_loader)throws ServletException, IOException ;
	
	
}
