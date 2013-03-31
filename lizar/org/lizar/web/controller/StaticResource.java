package org.lizar.web.controller;

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

import org.lizar.exception.LifecycleException;
import org.lizar.log.Log;
import org.lizar.log.Logger;
import org.lizar.util.FileTool;
import org.lizar.util.MyMath;
import org.lizar.util.Pair;
import org.lizar.util.StringHelper;
import org.lizar.web.Controller;
import org.lizar.web.Lifecycle;


public class StaticResource implements Lifecycle{
	private static Log log=Logger.newInstance(StaticResource.class);
	
	public static  Map<String,Resource> file_map;
	
	public  String listen_path;
	
	public  long delay_load;
	
	public  long file_max_size;
	
	public  int file_cache_min_uses;
	
	private boolean started=false;
	
	private static StaticResource instance;
	
	private Thread checker;
	
	private StaticResource(){
		
	
	}
	
	private StaticResource(String _listen_path,long delay_load,long file_max_size,int file_cache_min_uses){
		this.listen_path = _listen_path;
		this.delay_load = delay_load;
		this.file_max_size = file_max_size;
		this.file_cache_min_uses = file_cache_min_uses;
	}
	
	public static StaticResource instance(String _listen_path,long delay_load,long file_max_size,int file_cache_min_uses){
		if(instance==null){
			instance=new StaticResource(_listen_path, delay_load, file_max_size, file_cache_min_uses);
		}
		return instance;
	}
	
	@Override
	public void stop() throws LifecycleException{
		log.info(this.getClass().getSimpleName()+" stopping");
		if(!started) throw new LifecycleException(this.getClass().getSimpleName()+" has stopped.");
		checker.interrupt();
		started=false;
	}
	
	@Override
	public  void start()throws LifecycleException{
		log.info(this.getClass().getSimpleName()+" starting");
		if(started) throw new LifecycleException(this.getClass().getSimpleName()+" has started.");
		file_map=new HashMap<String,Resource>();
		checker=new Thread(){
			@Override
			public void run() {
				while(started){
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
		started=true;
	}
	
	
	public static void handle(EventLoader el,String path) {
		handle(el,path,true);
	}
	
	public static void handle(EventLoader el,String path,boolean need_cache) {
		path=el.real_path(path);
		handle_abs_file(el,path,need_cache);
	}
	
	public static void handle_abs_file(EventLoader el,String path,boolean need_cache) {
		HttpServletResponse response=el.response();
		response.setContentType(ContentType.is(el.postfix()));
		response.setCharacterEncoding(Controller.encode_type);
		if(need_cache){
			Resource file=file_map.get(MyMath.encryptionWithMD5(path));
			if(file!=null){
				file.use++;
				write(file.list,el);
			}else{
				read_file(el,path,need_cache);
			}
		}else{
			read_file(el,path,need_cache);
		}
		
	}
	public static void handle_abs_file(EventLoader el,String path,boolean need_cache,int response_code) {
		HttpServletResponse response=el.response();
		response.setStatus(response_code);
		handle_abs_file(el,path,need_cache);
	}
	
	private static void response_not_found(EventLoader el,String path){
		el.response().setStatus(HttpServletResponse.SC_NOT_FOUND);
		String _404_file=BasicLoader.context.getRealPath("/WEB-INF/lizar/404.")+el.postfix();
		Resource file=file_map.get(MyMath.encryptionWithMD5(_404_file));
		if(file==null){
			if(StringHelper.isNull(el.postfix()))el.postfix("html");
			File f=new File(BasicLoader.context.getRealPath("/WEB-INF/lizar/404.")+el.postfix());
			if(!f.exists()){
				try {
					FileTool.write_to_file("404 page for default postfix",f);
				} catch (IOException e) {
					el.log.error(path+" read exception.",e);
				}
			}
			read_file_in_not_found(el,f);
		}else write(file.list,el);
	}
	
	private static void read_file_in_not_found(EventLoader el,File f) {
		Pair<Long,List<String>> b=null;
		try {
			b = read(f);
		} catch (FileNotFoundException e) {
			el.log.error("request:"+el.request_path()+" read failed for read file "+f.getPath(),e);
			try {
				el.text("404 page for default postfix");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		}  catch (IOException e) {
			el.log.error("request:"+el.request_path()+" read failed for read file "+f.getPath(),e);
			try {
				el.text("404 page for default postfix");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		}
		if(b==null||b.getValue().isEmpty()){
			try {
				el.text("404 page for default postfix");
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
			write(b.getValue(),el);
		}
	}
	
	private static void read_file(EventLoader el,String path,boolean need_cache) {
		Pair<Long,List<String>> b=null;
		try {
			b = read(path);
		} catch (FileNotFoundException e) {
			response_not_found(el,path);
			return;
		} catch (IOException e) {
			el.log.error("request:"+el.request_path()+" read failed for read file "+path,e);
			response_not_found(el,path);
			return;
		}
		if(b==null||b.getValue().isEmpty()){
			response_not_found(el,path);
			return;
		}else{
			if(need_cache&&b.getKey()<instance.file_max_size){
				Resource r=new Resource(b.getValue());
				r.file=new File(path);
				r.last_modify_time=last_modify_time(path);
				file_map.put(MyMath.encryptionWithMD5(path), r);
			}
			write(b.getValue(),el);
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
	
	
	private static void write(List<String> list,EventLoader el) {
		PrintWriter out  = null;
		try {
			out = el.response().getWriter();
			Iterator<String> itr=list.iterator();
			for(;itr.hasNext();){
				out.write(itr.next());
			}
			out.flush();
		}catch(Exception e){
			el.log.error("Response output stream exception in "+el.request_path(), e);
		}finally{
			if(out != null)out.close();
		}
		
		
		
	}

	

}
