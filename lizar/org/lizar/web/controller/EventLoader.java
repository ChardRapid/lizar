package org.lizar.web.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.lizar.log.Log;
import org.lizar.log.Logger;
import org.lizar.util.StringHelper;
import org.lizar.util.http.Http;
import org.lizar.web.Controller;
import org.lizar.web.Web;
import org.lizar.web.config.Config;
import org.lizar.web.config.I18Msg;
import org.lizar.web.loader.I18Resource;


/**
 * exclusive resource and function owned by each request.
 * main task is set redirect type and set resource needed.
 * @see org.lizar.web.controller.Event
 *
 */
public class EventLoader extends BasicLoader{
	public Log log=Logger.newInstance(this.getClass());
	public EventLoader(String request_path,HttpServletRequest request,HttpServletResponse response){
		super(request_path,request,response);
	}
	
	
	public HttpServletRequest request(){
		return request;
	}
	
	public HttpServletResponse response(){
		return response;
	}
	
	/**
	 * 
	 * request a file content
	 * 
	 * */
	public void request(String file) throws ServletException, IOException{
		request.getRequestDispatcher(file).forward(request, response);
		//chain.doFilter(request, response);
	}
	
	/**
	 * 
	 * response to the root(server_info.root)
	 * 
	 * */
	public void response_to_root() throws IOException{
		response.sendRedirect(Config.xpath_str("server_info.root"));
	}
	
	/**
	 * 
	 * response redirect
	 * 
	 * */
	public void response(String url) throws IOException{
		response.sendRedirect(url);
	}
	
	/**
	 * 
	 * response redirect with cookie list
	 * 
	 * */
	public void response(String url,List<Cookie> cl) throws IOException{
		if(cl!=null){
			for(Cookie c:cl){
				response.addCookie(c);
			}
		}
		response.sendRedirect(url);
	}
	
	/**
	 * 
	 * response redirect with cookie setting
	 * 
	 * */
	public void response(String url,Cookie cookie) throws IOException{
		response.addCookie(cookie);
		response.sendRedirect(url);
	}
	
	/**
	 * 
	 * return the file_content as a xml type
	 * 
	 * if you want to return a xml file path a giving path, pls try EventLoader.file(path,need_cache) method
	 * 
	 * */
	public void xml(String file_content) throws IOException{
		print(file_content,"application/xml");
	}
	
	
	/**
	 * 
	 * return a text content as text/plain
	 * 
	 * if you want to return a txt file path a giving path, pls try EventLoader.file(path,need_cache) method
	 * 
	 * */
	public void text(String content) throws IOException{
		print(content,"text/plain");
	}
	
	/**
	 * 
	 * return a output_data  as text/json
	 * 
	 * */
	public void json(String output_data) throws IOException{
		print(output_data,"text/json");
	}
	
	/**
	 * 
	 * response a 404 with output_data 
	 * 
	 * */
	public void not_found(String output_data) throws IOException{
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		print(output_data,"text/plain");
	}
	
	/**
	 * 
	 * return jsonp with function and json filled
	 * 
	 * */
	public void jsonp(String fn,String json) throws IOException{
		StringBuilder res=new StringBuilder(fn);
		res.append("(").append(json).append(");");
		print(res.toString(),"text/json");
	}
	
	/**
	 * 
	 * return a html output_data with server_info.encode_type encode type
	 * 
	 * if you want to return a html file path a giving path, pls try EventLoader.file(path,need_cache) method
	 * */
	public void html(String output_data)throws IOException{
		print(output_data,"text/html;charset="+Controller.encode_type);
	}
	

	
	/**
	 * 
	 * return a static file with path
	 * 
	 * it can be txt/xml/html/js/css/jpg/flv or any static file
	 * 
	 * */
	public void file(String path) throws IOException{
		file(path,true);
	}
	
	/**
	 * 
	 * return a static file with path check if need cache
	 * 
	 * it can be txt/xml/html/js/css/jpg/flv or any static file
	 * 
	 * */
	public void file(String path,boolean need_cache) throws IOException{
		StaticResource.handle(this, path, need_cache);
	}
	
	/**
	 * 
	 * use template support to parse the template
	 * 
	 * */
	public void template(String path) throws IOException, ServletException{
		Controller.template.handle(path,this);
	}
	
	/**
	 * 
	 * write the template file and  parse it with EventLoader setting data to a file
	 * 
	 * */
	public void write_template_to_file(String path,String filepath) throws Exception{
		Controller.template.write_to_file(path, this, filepath);
	}
	
	/**
	 * 
	 * print data with content type
	 * 
	 * */
	public void print(String data,String type) throws IOException{
		response.setContentType(type);
		PrintWriter out = response.getWriter();
		out.println(data);
		out.flush();
		out.close();
	}
	
	/**
	 * generate images
	 * @throws IOException 
	 */
	public void write_image(BufferedImage img,String image_type) throws IOException{
		ImageIO.write(img, "JPEG",response.getOutputStream());
		response.getOutputStream().close();
		
	}
}
