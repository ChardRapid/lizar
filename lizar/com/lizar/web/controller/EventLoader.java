package com.lizar.web.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lizar.log.Log;
import com.lizar.log.Logger;
import com.lizar.util.StringHelper;
import com.lizar.util.http.Http;
import com.lizar.web.Controller;
import com.lizar.web.Web;
import com.lizar.web.config.I18Msg;
import com.lizar.web.loader.I18Resource;

/**
 * exclusive resource and function owned by each request.
 * main task is set redirect type and set resource needed.
 * @see com.lizar.web.controller.Event
 *
 */
public class EventLoader {
	public Log log=Logger.newInstance(this.getClass());
	public EventLoader(String request_path,HttpServletRequest request,HttpServletResponse response){
		this.request=request;
		this.response=response;
		this.request_path=request_path;
		Cookie[] co=request.getCookies();
		if(co!=null){
			cookies=new HashMap<String,Cookie>();
			for(Cookie c:co){
				cookies.put(c.getName(), c);
			}
		}
	}
	
	public static ServletContext context;
	
	private Map<String,Cookie> cookies;
	
	protected HttpServletRequest request;
	
	protected HttpServletResponse response;
	
	private  String request_path;
	
	/**
	 * 
	 * get  language
	 * 
	 * */
	public String lan;
	
	
	public I18Msg I18Msg(){
		return Web.i18;
	}
	
	public  String msg(String key){
		
		return Web.i18.get(lan, key);
	}
	
	public  String msg(String key,List<String> args){
		return msg(key,lan,args);
	}
	
	public   String msg(String key,String lan){
		return Web.i18.get(lan, key);
	}
	
	public void set_locale(String lan){
		I18Resource res=Web.i18.get(lan);
		if(res==null){
			if(Web.i18._default!=null){
				log.warn("Locale "+lan+" is not found, system will use the default locale "+Web.i18._default.file.getPath());
				res=Web.i18._default;
				request.setAttribute(Web.i18.var, res.map);
			}else log.warn("Locale "+lan+" is not found, and  default locale is also not found.");
		}else request.setAttribute(Web.i18.var, res.map);
	}
	
	public  String msg(String key,String lan,List<String> args){
		StringBuilder tpl=new StringBuilder(Web.i18.get(lan, key));
		int i=0;
		int start=0;
		int end=0;
		if(args!=null){
			for(;i<args.size();i++){
				start=tpl.indexOf("${");
				if(start==-1)break;
				end=tpl.indexOf("}");
				tpl.replace(start, end+1, args.get(i).toString());
			}
		}
		return tpl.toString();
	}
	
	
	private  String postfix;
	
	public String postfix(){
		return postfix;
	}
	
	public void postfix(String v){
		this.postfix=v;
	}
	
	/**
	 * just for image redirect
	 */
	public BufferedImage image;

	/**
	 * just for static redirect and image redirect
	 */
	public boolean file_cache=false;

	public static final int EVENT_RESPONSE=0;
	
	public static final int EVENT_REQUEST=1;
	
	public static final int EVENT_JSON=2;
	
	public static final int EVENT_XML=6;
	
	public static final int EVENT_JSONP=3;
	
	public static final int EVENT_WRITE_IMG=4;
	
	public static final int EVENT_FILE=5;
	
	public HttpServletRequest request(){
		return request;
	}
	
	public HttpServletResponse response(){
		return response;
	}
	
	public void request(String file) throws ServletException, IOException{
		request.getRequestDispatcher(file).forward(request, response);
	}
	
	public void response(String url) throws IOException{
		response.sendRedirect(url);
	}
	
	public void response(String url,List<Cookie> cl) throws IOException{
		if(cl!=null){
			for(Cookie c:cl){
				response.addCookie(c);
			}
		}
		response.sendRedirect(url);
	}
	
	public String client_ip() {
	       String ip = request.getHeader("x-forwarded-for");
	       if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	           ip = request.getHeader("Proxy-Client-IP");
	       }
	       if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	           ip = request.getHeader("WL-Proxy-Client-IP");
	       }
	       if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	           ip = request.getRemoteAddr();
	       }
	       return ip; 
	}
	
	/**
	 * 
	 * request path
	 * 
	 * */
	public String request_path(){
		return request_path;
	}
	
	public String real_path(){
		return context.getRealPath("");
	}
	
	public String real_path(String sub_path){
		return context.getRealPath(sub_path);
	}
	
	public String context_name(){
		return request.getContextPath();
	}
	
	
	public void xml(String file_content) throws IOException{
		print(file_content,"application/xml");
	}
	
	public void text(String content) throws IOException{
		print(content,"text/plain");
	}
	
	public void json(String output_data) throws IOException{
		print(output_data,"text/json");
	}
	
	public void not_found(String output_data) throws IOException{
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		print(output_data,"text/plain");
	}
	
	public void jsonp(String fn,String json) throws IOException{
		StringBuilder res=new StringBuilder(fn);
		res.append("(").append(json).append(");");
		print(res.toString(),"text/json");
	}
	
	public void html(String output_data)throws IOException{
		print(output_data,"text/html;charset="+Controller.encode_type);
	}
	
	public void file(String path) throws IOException{
		file(path,true);
	}
	
	public void file(String path,boolean need_cache) throws IOException{
		StaticResource.handle(this, path, need_cache);
	}
	
	public void template(String path) throws IOException, ServletException{
		Controller.template.handle(path,this);
	}
	
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
	public void write_image(BufferedImage img) throws IOException{
		ImageIO.write(img, "image/jpeg",response.getOutputStream());
		response.getOutputStream().close();
		
	}
	
	
	
	public Cookie get_cookie(String name){
		if(cookies==null)return null;
		return cookies.get(name);
	}
	
	public void set_cookie(Cookie cookie){
		response.addCookie(cookie);
	}
	
	public void set_attr(String key,Object v){
		request.setAttribute(key, v);
	}
	
	public Object get_attr(String key){
		return request.getAttribute(key);
	}
	
	public <T> T get_attr(String key,T _default){
		Object v=request.getAttribute(key);
		if(v==null)return _default;
		else return (T)v;
	}
	
	public void remove_attr(String key){
		request.removeAttribute(key);
	}
	
	public void set_session_attr(String key,Object v){
		request.getSession().setAttribute(key, v);
	}
	
	public Object get_session_attr(String key){
		return request.getSession().getAttribute(key);
	}
	
	public void remove_session_attr(String key){
		request.removeAttribute(key);
	}
	
	public <T> T get_session_attr(String key,T _default){
		Object v=request.getSession().getAttribute(key);
		if(v==null)return _default;
		else return (T)v;
	}
	
	public void set_app_attr(String key,Object v){
		context.setAttribute(key, v);
	}
	
	public Object get_app_attr(String key){
		return context.getAttribute(key);
	}
	
	public void remove_app_attr(String key){
		context.removeAttribute(key);
	}
	
	public <T> T get_app_attr(String key,T _default){
		Object v=context.getAttribute(key);
		if(v==null)return _default;
		else return (T)v;
	}

	
	/**
	 * 
	 * this should invoke before any _param() or _int() method.
	 * 
	 * or it will not work.
	 * 
	 * **/
	public void encode_params(String encode_type) throws UnsupportedEncodingException{
		request.setCharacterEncoding(encode_type);
		response.setCharacterEncoding(encode_type);
	}
	
	public String encode_type(){
		return request.getCharacterEncoding();
	}
	
	public int _int(String key){
		return param(key, 0);
	}
	
	public int _int(String key,int defv){
		return param( key, defv);
	}
	
	public String _str(String key){
		return param( key, "");
	}
	
	public String _str(String key,String defv){
		return param(key, defv);
	}
	
	public long _long(String key){
		return param(key,0l);
	}
	
	public long _long(String key,long _default){
		return param(key,_default);
	}
	
	public double _double(String key){
		return param(key,0.0);
	}
	
	public double _double(String key,double _default){
		return param(key, _default);
	}
	
	public float _float(String key){
		return param(key,0.0f);
	}
	
	public float _float(String key,float _default){
		return param(key,_default);
	}
	
	public String[] _params(String key){
		return request.getParameterValues(key);
	}
	
	public Object param(String key){
		return request.getParameter(key);
	}
	
	public <T> T param(String key,T _default){
		Object v=request.getParameter(key);
		if(v==null)return _default;
		else return (T)v;
	}
	
	
	/**
	 * 
	 * if front gives you the encoded string like gb2312
	 * and you wish to convert it to utf-8
	 * 
	 * then there are two step can make you void bad code.
	 * 
	 * 1. encode_params("ISO-8859-1"); before any params get from EventLoader or HTTPServletRequest
	 * 
	 * 2. invoke this method and you will get avoid of bad code
	 * 
	 *    example:
	 *    
	 *    Event{
	 *    
	 *    	    handle(EventLoader event_loader){
	 *    			
	 *    			event_loader.encode_params("ISO-8859-1");
	 *    			String value=event_loader.param("name","_default_value","gb2312",true);
	 *    			.
	 *    			.
	 *    			.
	 *    		}
	 *    		.
	 *    		.
	 *    		.
	 *    		.
	 *    
	 *    }
	 * 
	 * 
	 * */
	public String param(String key, String _default,String encode_type,boolean need_trans_code){
		String value="";
		if(need_trans_code){
			try{
			value= Http.escape(new String(request.getParameter(key).getBytes("ISO-8859-1"),encode_type));
			}catch(Exception e){
				return _default;
			}
		}else{
			value=Http.escape(request.getParameter(key));
		}
		if(StringHelper.isNull(value))value=_default;
		return value;
	}
	
}
