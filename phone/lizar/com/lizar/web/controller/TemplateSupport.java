package com.lizar.web.controller;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.lizar.json.JList;
import com.lizar.json.JSON;

abstract public class TemplateSupport {
	private String listen;
	
	public void listen(JList listen){
		this.listen=default_listen();
		if(listen==null||listen.isEmpty())return;
		if(listen.size()==1){
			this.listen=listen.get(0).toString();
			return;
		}
		StringBuilder st=new StringBuilder();
		for(int i=0;i<listen.size();i++){
			st.append(listen.get(i).toString());
			if(i!=listen.size()-1){
				st.append(",");
			}
		}
		this.listen=st.toString();
	}
	
	public String listen(){
		return listen;
	}

	public abstract String default_listen();
	
	/**
	 * invoked by controller,做些初始化event的前置工作
	 */
	public abstract  void init(ServletContext context,JSON params)throws ServletException ;
	
	/**
	 * @task invoked by controller,handle user request
	 * @param event_loader resource and function owned by each request
	 */
	public abstract  void handle(EventLoader event_loader)throws ServletException, IOException ;
	
	
	/**
	 * @task invoked by controller,handle user request
	 * @param event_loader resource and function owned by each request
	 */
	public abstract  void handle(String path,EventLoader event_loader)throws ServletException, IOException ;
}
