package com.lizar.web;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;

import com.lizar.exception.NotDefined;
import com.lizar.log.Log;
import com.lizar.log.Logger;
import com.lizar.util.PropertyHandler;
import com.lizar.util.StringHelper;
import com.lizar.util.Time;
import com.lizar.web.config.Config;
import com.lizar.web.loader.Plugin;

/**
 * 
 * 
 * 
 * */
public class PluginManager  extends Timer {
	private Log log=Logger.newInstance(this.getClass());
	
	/**
	 * 
	 * Plugin 容器
	 * 
	 * */
	public static Map<String, Plugin> container ;
	
	private static File plugin_folder;

	private static PluginManager plugin_manager;
	
	public static final String DIR="/WEB-INF/lizar/plugin";
	
	
	private PluginManager(){
		if(container==null)container = Web.plugins;
		Map<String,Plugin> start_map =check_plugins();
		log.info("Totally "+start_map.size()+" Plugin has is going to load.");
		init_plugin_folder();
		pre_run(start_map);
	}
	
	public  void check(){
		for(Entry<String,Plugin> p:container.entrySet()){
			if(!p.getValue().file.exists()){
				try {
					write_timer_to_file(p.getValue());
				} catch (IOException e) {
					log.error("Lizar component Plugin "+p.getValue().getClass().getName()+" rewrite to file failed, caused by:",e);
				}
				continue;
			}
			if(p.getValue().file.lastModified()!=p.getValue().last_modify){
				check(p.getValue());
			}
		}
	}
	
	
	
	private void check(Plugin plugin){
		Properties property=null;
		try {
			property=PropertyHandler.readProperties(plugin.file);
		} catch (IOException e) {
			log.error("Lizar component Plugin "+plugin.getClass().getName()+" read property from file "+plugin.file.getPath()+" failed, caused by:",e);
			return;
		}
		String interval_time=property.getProperty("interval_time");
		String delay_time=property.getProperty("delay_time");
		long start_time=0;
		try {
			Date d=to_date(property.getProperty("start_time"));
			if(d!=null)start_time=d.getTime();
		} catch (ParseException e) {
			log.error(plugin.getClass().getName()+" renew property failed, with a invoid start_time format in "+plugin.file.getPath()+"\n a void start_time should be like YYYY-MM-DD ");
			return;
		}
		if(interval_time==null)interval_time="";
		else interval_time=interval_time.trim();
		if(delay_time==null)delay_time="";
		else delay_time=delay_time.trim();
		long plugin_time=0;
		if(plugin.start_time!=null)plugin_time=plugin.start_time.getTime();
		if(!StringHelper.equals(interval_time.trim(), plugin.interval_time)||
				!StringHelper.equals(delay_time.trim(), plugin.delay_time)||
				start_time!=plugin_time){
			String f=plugin.file.getPath();
			plugin.cancel();
			this.purge();
			try {
				plugin=plugin.getClass().newInstance();
				String key=plugin.getClass().getName();
				plugin.pre_run();
				plugin.interval_time=interval_time;
				plugin.delay_time=delay_time;
				if(start_time!=0)plugin.start_time=new Date(start_time);
				plugin.file=new File(f);
				plugin.last_modify=plugin.file.lastModified();
				container.put(key, plugin);
			} catch (Exception e) {
				e.printStackTrace();
			} 
			schedule(plugin);
		}
	}
	
	
	
	
	private Map<String,Plugin> check_plugins(){
		Map<String,Plugin> start_map =Web.container.get_sub_cells_of(Plugin.class);
		return start_map;
	}
	
	/**
	 * create plugin folder
	 */
	private void init_plugin_folder(){
		plugin_folder=new File(Config.path_filter(DIR));
		if(plugin_folder.isFile()){
			plugin_folder.delete();
			plugin_folder.mkdirs();
		}
		if(!plugin_folder.exists()){
			plugin_folder.mkdirs();
			return;
		}
	}
	
	public void pre_run(Map<String,Plugin> start_map){
		Plugin plugin;
		boolean need_to_end=false;
		Exception ex=null;
		String plugin_name=null;
		String cell_name=null;
		List<Plugin> start_queue=new LinkedList<Plugin>();
		List<String> start_keys=set_to_list(start_map.keySet());
		if(start_map.size()>0){
			log.info("..............................................................................");
			log.info("\t\t\tReady to arrange Plugin module pre_run.");
			log.info("..............................................................................");
		}
		while(!need_to_end){
			int s=start_keys.size();
			for(int i=0;i<start_keys.size();){
				plugin=start_map.get(start_keys.get(i));
				try {
					plugin_name=plugin.getClass().getName();
					cell_name=start_keys.get(i);
					load_plugin_file(cell_name,plugin);
					plugin.pre_run();
				} catch (Exception e1) {
					ex=e1;
					i++;
					continue;
				}
				start_queue.add(plugin);
				start_keys.remove(i);
				container.put(cell_name,plugin);
			}
			if(start_keys.size()==0||start_keys.size()==s)need_to_end=true;
			if(start_keys.size()>0&&need_to_end){
				log.error("Lizar component Plugin "+plugin_name+" init failed, caused by:",ex);
			}
		}
		Web.plugins=container;
		run(start_queue);
	}
	
	private static <T> List<T> set_to_list(Set<T> set){
		if(set==null)return null;
		List<T> list=new ArrayList<T>();
		for(T obj:set){
			list.add(obj);
		}
		return list;
	}



	private void load_plugin_file(String cell_name,Plugin plugin) throws IOException, ParseException{
		File timer=new File(plugin_folder.getPath()+"/"+cell_name+".property");
		plugin.file=timer;
		plugin.last_modify=timer.lastModified();
		if(timer.isFile()){
			read_timer_from_file(plugin);
		}else write_timer_to_file(plugin);
	}
	
	private void read_timer_from_file(Plugin plugin) throws IOException{
		Properties property=null;
		property=PropertyHandler.readProperties(plugin.file);
		if(property!=null){
			plugin.interval_time=property.getProperty("interval_time");
			plugin.delay_time=property.getProperty("delay_time");
			try {
				plugin.start_time=to_date(property.getProperty("start_time"));
			} catch (ParseException e) {
				log.error(plugin.getClass().getName()+" start time configured in "+plugin.file.getPath()+" has invalid start_time format "+property.getProperty("start_time")+" pls make sure it is right ");
				log.error("a void start time format should be like YYYY-MM-DD ");
			}
		}
	}
	
	private static String to_standard_time(Date d){
		if(d==null)return "";
		SimpleDateFormat format= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(d);
	}
	
	private static Date to_date(String string) throws ParseException{
		if(StringHelper.isNull(string))return null;
		SimpleDateFormat format= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.parse(string);
	}
	
	
	private void write_timer_to_file(Plugin plugin) throws IOException{
		Properties properties=new Properties();
		if(StringHelper.isNull(plugin.set_interval_time()))properties.put("interval_time", "");
		else properties.put("interval_time", plugin.set_interval_time());
		if(StringHelper.isNull(plugin.set_delay_time()))properties.put("delay_time", "");
		else properties.put("delay_time", plugin.set_delay_time());
		if(plugin.set_start_time()==null)properties.put("start_time", "");
		else properties.put("start_time", to_standard_time(plugin.set_start_time()));
		try{
			PropertyHandler.writeProperties(plugin.file, properties, "ready to write key value properties set\ninterval_time=10s\ndelay_time=2s\nstart_time=2012-01-02 12:58:30");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	private void run(List<Plugin> start_list){
		if(start_list.size()>0){
			log.info("..............................................................................");
			log.info("\t\t\tReady to arrange Plugin schedule.");
			log.info("..............................................................................");
		}
		for(Plugin plugin:start_list){
			try {
				schedule(plugin);
			} catch (Exception e) {
				log.error("Plugin "+plugin.getClass().getName()+" schedule to run failed.",e);
			}
		}
	}
	
	
	public static PluginManager instance(){
		if(plugin_manager==null)plugin_manager=new PluginManager();
		return plugin_manager;
	}
	



	/**
	 * 
	 * 获得所有的Plugin
	 * */
	public Map<String, Plugin> getAllPlugins() {
		return container;
	}

	/**
	 * 
	 * 设定定时器，定时更新模块
	 * @throws NotDefined 
	 * 
	 * */
	public  void schedule( Plugin plugin) {
		if(StringHelper.isNotNull(plugin.interval_time)){
			log.info("[Run schedule]:plugin class:"+ plugin.getClass().getName());
			log.info("-loader Interval_Time:" + plugin.interval_time);
			log.info("-Delay Time:" + plugin.delay_time);
			log.info("-Start Time:" + plugin.start_time);
			if(plugin.start_time!=null){
				this.scheduleAtFixedRate(plugin,plugin.start_time, Time.translate_time(plugin.interval_time));
			}else{
				this.schedule(plugin, Time.translate_time(plugin.delay_time), Time.translate_time(plugin.interval_time));
			}
		}else{
			log.info("[Normal Module]:plugin class:"+ plugin.getClass().getName());
		}
	}

	
	

	public static  <T> T getPlugin(Class<T> t){
		T e=null;
		if(container==null){
			e= Web.get(t);
			if(!(e instanceof Plugin)||e==null)throw new NotDefined("Plugin "+t.getName()+" not found in the container pls make sure it is exsits");
			return e;
		}
		e=(T)container.get(t.getName());
		if(e==null)throw new NotDefined("Plugin "+t.getName()+" not found in the container pls make sure it is exsits");
		return e;
	}
	
	
	

	public void destroy() {
		log.info("..............................................................................");
		log.info("\t\t\tPlugin Manager is begin to clean up threads.");
		
		Plugin plugin=null;
		for(Entry<String, Plugin> e:container.entrySet()){
			plugin=e.getValue();
			plugin.cancel();
			plugin.destroy();
			log.info(plugin.getClass().getName()+" has been cleaned up.");
		}
		container.clear();
		this.cancel();
		this.purge();
		log.info("..............................................................................");
	}


	
}
