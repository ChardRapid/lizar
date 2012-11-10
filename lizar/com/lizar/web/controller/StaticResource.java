package com.lizar.web.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.lizar.log.Log;
import com.lizar.log.Logger;
import com.lizar.util.FileTool;
import com.lizar.util.MyMath;
import com.lizar.util.Pair;
import com.lizar.util.StringHelper;
import com.lizar.web.Controller;

public class StaticResource {
	private static Log log=Logger.newInstance(StaticResource.class);
	public static  Map<String,Resource> file_map;
	
	public  String listen_path;
	
	public  long delay_load;
	
	public  long file_max_size;
	
	public  int file_cache_min_uses;
	
	private boolean end=false;
	
	private static StaticResource instance;
	
	private Thread checker;
	
	private StaticResource(){
		
	
	}
	
	public static StaticResource instance(String _listen_path,long delay_load,long file_max_size,int file_cache_min_uses){
		if(instance==null){
			instance=new StaticResource();
			instance.init(_listen_path, delay_load, file_max_size, file_cache_min_uses);
		}
		return instance;
	}
	
	public void destroy(){
		end=true;
		checker.interrupt();
	}
	
	public  void init(String _listen_path,long delay_load,long file_max_size,int file_cache_min_uses){
		file_map=new HashMap<String,Resource>();
		listen_path=_listen_path;
		instance.delay_load=delay_load;
		instance.file_max_size=file_max_size;
		instance.file_cache_min_uses=file_cache_min_uses;
		checker=new Thread(){
			@Override
			public void run() {
				while(!end){
					try {
						sleep(instance.delay_load);
					} catch (InterruptedException e) {
						log.debug("static resource checker sleep  has been interrupted as the thread is going to close.");
					}
					List<String> keys=new LinkedList<String>();
					for(Resource rs:file_map.values()){
						rs.use=1;
						if(!rs.file.exists()||rs.use<instance.file_cache_min_uses||rs.file.lastModified()!=rs.last_modify_time){
							keys.add(MyMath.encryptionWithMD5(rs.file.getPath()));
							continue;
						}
						
					}
					for(String key:keys){
						file_map.remove(key);
					}
					
				}
				log.info("..............................................................................");
				log.info("\t\t\tStatic Resource Checker has been successfully terminated.");
				log.info("..............................................................................");
			}
		};
		checker.start();
	}
	
	
	public static void handle(EventLoader event_loader,String path) {
		handle(event_loader,path,true);
	}
	
	public static void handle(EventLoader event_loader,String path,boolean need_cache) {
		path=event_loader.real_path(path);
		handle_abs_file(event_loader,path,need_cache);
	}
	
	public static void handle_abs_file(EventLoader event_loader,String path,boolean need_cache) {
		HttpServletResponse response=event_loader.response();
		response.setContentType(ContentType.is(event_loader.postfix()));
		response.setCharacterEncoding(Controller.encode_type);
		if(need_cache){
			Resource file=file_map.get(MyMath.encryptionWithMD5(path));
			if(file!=null){
				file.use++;
				write(file.list,event_loader);
			}else{
				read_file(event_loader,path,need_cache);
			}
		}else{
			read_file(event_loader,path,need_cache);
		}
		
	}
	public static void handle_abs_file(EventLoader event_loader,String path,boolean need_cache,int response_code) {
		HttpServletResponse response=event_loader.response();
		response.setStatus(response_code);
		handle_abs_file(event_loader,path,need_cache);
	}
	
	private static void response_not_found(EventLoader event_loader,String path){
		event_loader.response().setStatus(HttpServletResponse.SC_NOT_FOUND);
		String _404_file=event_loader.context.getRealPath("/WEB-INF/lizar/404.")+event_loader.postfix();
		Resource file=file_map.get(MyMath.encryptionWithMD5(_404_file));
		if(file==null){
			if(StringHelper.isNull(event_loader.postfix()))event_loader.postfix("html");
			File f=new File(event_loader.context.getRealPath("/WEB-INF/lizar/404.")+event_loader.postfix());
			if(!f.exists()){
				try {
					FileTool.write_to_file("404 page for default postfix",f);
				} catch (IOException e) {
					event_loader.log.error(path+" read exception.",e);
				}
			}
			read_file_in_not_found(event_loader,f);
		}else write(file.list,event_loader);
	}
	
	private static void read_file_in_not_found(EventLoader event_loader,File f) {
		Pair<Long,List<String>> b=null;
		try {
			b = read(f);
		} catch (FileNotFoundException e) {
			event_loader.log.error("request:"+event_loader.request_path()+" read failed for read file "+f.getPath(),e);
			try {
				event_loader.text("404 page for default postfix");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		}  catch (IOException e) {
			event_loader.log.error("request:"+event_loader.request_path()+" read failed for read file "+f.getPath(),e);
			try {
				event_loader.text("404 page for default postfix");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		}
		if(b==null||b.getValue().isEmpty()){
			try {
				event_loader.text("404 page for default postfix");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}else{
			if(b.getKey()<instance.file_max_size){
				Resource file=new Resource(b.getValue());
				file.file=f;
				file.last_modify_time=last_modify_time(f.getPath());
				file_map.put(MyMath.encryptionWithMD5(f.getPath()), file);
			}
			write(b.getValue(),event_loader);
		}
	}
	
	private static void read_file(EventLoader event_loader,String path,boolean need_cache) {
		Pair<Long,List<String>> b=null;
		try {
			b = read(path);
		} catch (FileNotFoundException e) {
			response_not_found(event_loader,path);
			return;
		} catch (IOException e) {
			event_loader.log.error("request:"+event_loader.request_path()+" read failed for read file "+path,e);
			response_not_found(event_loader,path);
			return;
		}
		if(b==null||b.getValue().isEmpty()){
			response_not_found(event_loader,path);
			return;
		}else{
			if(need_cache&&b.getKey()<instance.file_max_size){
				Resource r=new Resource(b.getValue());
				r.file=new File(path);
				r.last_modify_time=last_modify_time(path);
				file_map.put(MyMath.encryptionWithMD5(path), r);
			}
			write(b.getValue(),event_loader);
		}
	}
	

	
	private static long last_modify_time(String path){
		File f=new File(path);
		return f.lastModified();
	}
	
	private static Pair<Long,List<String>> read(String path) throws IOException {
		FileInputStream in = null;
		in= new FileInputStream(path);
		int size = in.available();
		in.close();
		List<String> list=new LinkedList<String>();
		FileReader filereader = new FileReader(path);
		BufferedReader bufferedReader=new BufferedReader(filereader);
		String str="";
		while((str=bufferedReader.readLine())!=null){
			list.add(str);
		}
		filereader.close();
		bufferedReader.close();
		return new Pair<Long,List<String>>(new Long(size),list);
	}
	
	private static Pair<Long,List<String>> read(File f) throws IOException {
		FileInputStream in = null;
		in= new FileInputStream(f);
		int size = in.available();
		in.close();
		List<String> list=new LinkedList<String>();
		FileReader filereader = new FileReader(f);
		BufferedReader bufferedReader=new BufferedReader(filereader);
		String str="";
		while((str=bufferedReader.readLine())!=null){
			list.add(str);
		}
		filereader.close();
		bufferedReader.close();
		return new Pair<Long,List<String>>(new Long(size),list);
	}
	
	
	private static void write(List<String> list,EventLoader event_loader) {
		PrintWriter out  = null;
		try {
			out = event_loader.response().getWriter();
			Iterator<String> itr=list.iterator();
			for(;itr.hasNext();){
				out.write(itr.next());
			}
			out.flush();
		}catch(Exception e){
			event_loader.log.error("Response output stream exception in "+event_loader.request_path(), e);
		}finally{
			if(out != null)out.close();
		}
		
		
		
	}

	

}
