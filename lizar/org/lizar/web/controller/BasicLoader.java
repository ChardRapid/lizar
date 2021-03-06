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


public class BasicLoader {
	public Log log=Logger.newInstance(this.getClass());
	public BasicLoader(String request_path,HttpServletRequest request,HttpServletResponse response){
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
		String[] ps=request_path.split("/");
		this.path=new ArrayList<String>(ps.length);
		for(String s:ps){
			if(StringHelper.isNotNull(s)){
				if(s.lastIndexOf(".")!=-1){
					path.add(s.substring(0, s.lastIndexOf(".")));
				}else path.add(s);
			}
		}
	}
	
	public static ServletContext context;
	
	private Map<String,Cookie> cookies;
	
	protected HttpServletRequest request;
	
	protected HttpServletResponse response;
	
	public boolean need_after=true;
	
	private  String request_path;
	
	private List<String> path;
	
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
	
	
	/**
	 * 
	 * get customer's client ip
	 * 
	 * */
	public String get_client_ip() {
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
	
	/**
	 * 
	 * request the path with split by /
	 * 
	 * like if request path is /user/setting/change
	 * 
	 * request_path(0) will be user
	 * 
	 * request_path(1) will be setting
	 * 
	 * request_path(2) will be change
	 * 
	 * request_path(3) will be ""
	 * 
	 * default will be "" string not null
	 * 
	 * */
	public String request_path(int i){
		if(i>-1&&i<path.size())return path.get(i);
		return "";
	}
	
	/**
	 * 
	 * get the web physical path
	 * 
	 * */
	public String real_path(){
		return context.getRealPath("");
	}
	
	/**
	 * 
	 * get the web physical path with a sub_path
	 * 
	 * */
	public String real_path(String sub_path){
		return context.getRealPath(sub_path);
	}
	
	/**
	 * 
	 * get the web context name
	 * 
	 * */
	public String context_name(){
		return request.getContextPath();
	}
	
	
	public static final String JPEG="JPEG";
	
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
	
	public <T> T get_session_attr(String key,Class<T>  t){
		Object v=request.getSession().getAttribute(key);
		if(v==null)return null;
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
		response.setContentType("text/html;charset="+encode_type);
	}
	
	public String encode_type(){
		return request.getCharacterEncoding();
	}
	
	/**
	 * 
	 * request.getParameter("parameter_name")
	 * 
	 * if result is null , will return default value 0
	 * 
	 * */
	public int _int(String key){
		Object v=request.getParameter(key);
		if(v==null)return 0;
		else {
			try{
				return Integer.parseInt(v.toString());
			}catch(Exception e){
				return 0;
			}
		}
	}
	
	/**
	 * 
	 * request.getParameter("parameter_name")
	 * 
	 * if result is null , will return default value:defv
	 * 
	 * */
	public int _int(String key,int defv){
		
		Object v=request.getParameter(key);
		if(v==null)return defv;
		else {
			try{
				return Integer.parseInt(v.toString());
			}catch(Exception e){
				return defv;
			}
		}
	}
	
	/**
	 * 
	 * request.getParameter("parameter_name")
	 * 
	 * if result is null , will return default value ""
	 * 
	 * */
	public String _str(String key){
		Object v=request.getParameter(key);
		if(v==null)return "";
		else {
			return v.toString();
		}
	}
	
	/**
	 * 
	 * request.getParameter("parameter_name")
	 * 
	 * if result is null , will return default value:defv
	 * 
	 * */
	public String _str(String key,String defv){
		Object v=request.getParameter(key);
		if(v==null)return defv;
		else {
			return v.toString();
		}
	}
	
	/**
	 * 
	 * request.getParameter("parameter_name")
	 * 
	 * if result is null , will return default value 0
	 * 
	 * */
	public long _long(String key){
		Object v=request.getParameter(key);
		if(v==null)return 0;
		else {
			try{
				return Long.parseLong(v.toString());
			}catch(Exception e){
				return 0;
			}
		}
	}
	
	/**
	 * 
	 * request.getParameter("parameter_name")
	 * 
	 * if result is null , will return default value:defv
	 * 
	 * */
	public long _long(String key,long _default){
		Object v=request.getParameter(key);
		if(v==null)return _default;
		else {
			try{
				return Long.parseLong(v.toString());
			}catch(Exception e){
				return _default;
			}
		}
	}
	
	/**
	 * 
	 * request.getParameter("parameter_name")
	 * 
	 * if result is null , will return default value 0
	 * 
	 * */
	public double _double(String key){
		Object v=request.getParameter(key);
		if(v==null)return 0;
		else {
			try{
				return Double.parseDouble(v.toString());
			}catch(Exception e){
				return 0;
			}
		}
	}
	
	/**
	 * 
	 * request.getParameter("parameter_name")
	 * 
	 * if result is null , will return default value:defv
	 * 
	 * */
	public double _double(String key,double _default){
		Object v=request.getParameter(key);
		if(v==null)return _default;
		else {
			try{
				return Double.parseDouble(v.toString());
			}catch(Exception e){
				return _default;
			}
		}
	}
	
	/**
	 * 
	 * request.getParameter("parameter_name")
	 * 
	 * if result is null , will return default value 0
	 * 
	 * */
	public float _float(String key){
		Object v=request.getParameter(key);
		if(v==null)return 0;
		else {
			try{
				return Float.parseFloat(v.toString());
			}catch(Exception e){
				return 0;
			}
		}
	}
	
	/**
	 * 
	 * request.getParameter("parameter_name")
	 * 
	 * if result is null , will return default value:defv
	 * 
	 * */
	public float _float(String key,float _default){
		Object v=request.getParameter(key);
		if(v==null)return _default;
		else {
			try{
				return Float.parseFloat(v.toString());
			}catch(Exception e){
				return _default;
			}
		}
	}
	
	/**
	 * 
	 * request.getParameterValues(key);
	 * return muti-value with a key
	 * 
	 * */
	public String[] params(String key){
		return request.getParameterValues(key);
	}
	
	/**
	 * 
	 * classic
	 * request.getParameter(key);
	 * 
	 * 
	 * */
	public String param(String key){
		return request.getParameter(key);
	}
	
	/**
	 * 
	 * classic
	 * 
	 * request.getParameter(key);
	 * 
	 * if it is null then will return _default value
	 * 
	 * */
	private <T> T param(String key,T _default){
		Object v=request.getParameter(key);
		if(v==null)return _default;
		else return (T)v;
	}
	
	
	public String session_id(){
		return request.getSession().getId();
	}
	
	public HttpSession session(){
		return request.getSession();
	}
	
	public void setHeader(String key,String value){
		response.setHeader(key, value);
	}
	
	public void setDateHeader(String key,long value){
		response.setDateHeader(key, value);
	}
	
	public String get_user_agent(){
		return request.getHeader("user-agent");
	}
	
	public String get_referer(){
		return request.getHeader("Referer");
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
	 *    	    handle(EventLoader el){
	 *    			
	 *    			el.encode_params("ISO-8859-1");
	 *    			String value=el.param("name","_default_value","gb2312",true);
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
