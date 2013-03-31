package org.lizar.web;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.lizar.exception.LifecycleException;
import org.lizar.exception.NotDefined;
import org.lizar.json.JObject;
import org.lizar.json.JSON;
import org.lizar.log.Log;
import org.lizar.log.Logger;
import org.lizar.util.CollectionHelper;
import org.lizar.util.FileTool;
import org.lizar.util.StringHelper;
import org.lizar.util.Time;
import org.lizar.web.config.Config;
import org.lizar.web.loader.Plugin;


/**
 * 
 * 
 * 
 * */
public class PluginManager implements Lifecycle {
	private static Log log=Logger.newInstance(PluginManager.class);
	
	private static boolean started=false;
	
	
	private ScheduledExecutorService scheduExec ;
	
	/**
	 * 
	 * Plugin 容器
	 * 
	 * */
	public static Map<String, Plugin> container ;
	
	private static PluginManager plugin_manager;
	
	private static PluginConfig plugin_config=new PluginConfig();
	
	private PluginManager(){
		
	}
	
	public static PluginManager instance(){
		if(plugin_manager==null)plugin_manager=new PluginManager();
		return plugin_manager;
	}
	
//	public  void check(){
//		if(!plugin_folder.exists()){
//			log.warn("Lizar lizar/plugin folder is not exists, system will auto generate new folder in 1 minute");
//			try {
//				Thread.sleep(60*1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			plugin_folder.mkdirs();
//		}
//		for(Entry<String,Plugin> p:container.entrySet()){
//			if(!p.getValue().file.exists()){
//				try {
//					write_timer_to_file(p.getValue());
//				} catch (IOException e) {
//					log.error("Lizar component Plugin "+p.getValue().getClass().getName()+" rewrite to file failed, caused by:",e);
//				}
//				continue;
//			}
//			if(p.getValue().file.lastModified()!=p.getValue().last_modify){
//				check(p.getValue());
//			}
//		}
//	}
	
	
	
//	private void check(Plugin plugin){
//		Properties property=null;
//		try {
//			property=PropertyHandler.readProperties(plugin.file);
//		} catch (IOException e) {
//			log.error("Lizar component Plugin "+plugin.getClass().getName()+" read property from file "+plugin.file.getPath()+" failed, caused by:",e);
//			return;
//		}
//		String interval_time=property.getProperty("interval_time");
//		String delay_time=property.getProperty("delay_time");
//		long start_time=0;
//		try {
//			Date d=to_date(property.getProperty("start_time"));
//			if(d!=null)start_time=d.getTime();
//		} catch (ParseException e) {
//			log.error(plugin.getClass().getName()+" renew property failed, with a invoid start_time format in "+plugin.file.getPath()+"\n a void start_time should be like YYYY-MM-DD ");
//			return;
//		}
//		if(interval_time==null)interval_time="";
//		else interval_time=interval_time.trim();
//		if(delay_time==null)delay_time="";
//		else delay_time=delay_time.trim();
//		long plugin_time=0;
//		if(plugin.start_time!=null)plugin_time=plugin.start_time.getTime();
//		if(!StringHelper.equals(interval_time.trim(), plugin.interval_time)||
//				!StringHelper.equals(delay_time.trim(), plugin.delay_time)||
//				start_time!=plugin_time){
//			String f=plugin.file.getPath();
////			plugin.cancel();
////			this.purge();
//			try {
//				plugin=plugin.getClass().newInstance();
//				String key=plugin.getClass().getName();
//				plugin.init_property();
//				plugin.pre_run();
//				plugin.interval_time=interval_time;
//				plugin.delay_time=delay_time;
//				if(start_time!=0)plugin.start_time=new Date(start_time);
//				plugin.file=new File(f);
//				plugin.last_modify=plugin.file.lastModified();
//				container.put(key, plugin);
//			} catch (Exception e) {
//				e.printStackTrace();
//			} 
//			schedule(plugin);
//		}
//	}
	
	

	
	
	
	@Override
	public void start() throws LifecycleException, Exception{
		log.info(this.getClass().getSimpleName()+" starting");
		if(started) throw new LifecycleException(this.getClass().getSimpleName()+" has started.");
		
		
		if(container==null)container = Web.plugins;
		log.info("Totally "+container.size()+" Plugin  is going to load.");
		plugin_config.load_plugin_config();		//load plugin config
		
		
		Plugin plugin;
		boolean need_to_end=false;
		Exception ex=null;
		String plugin_name=null;
		String cell_name=null;
		List<Plugin> start_queue=new LinkedList<Plugin>();
		List<String> start_keys=CollectionHelper.set_to_list(container.keySet());
		if(container.size()>0){
			log.info("..............................................................................");
			log.info("\t\t\tReady to start plugins.");
			log.info("..............................................................................");
		}
		while(!need_to_end){
			int s=start_keys.size();
			for(int i=0;i<start_keys.size();){
				plugin=container.get(start_keys.get(i));
				try {
					plugin_name=plugin.getClass().getName();
					cell_name=start_keys.get(i);
					plugin_config.init_plugin(cell_name,plugin);
					plugin.start();
				} catch (Exception e1) {
					ex=e1;
					i++;
					continue;
				}
				start_queue.add(plugin);
				start_keys.remove(i);
			}
			if(start_keys.size()==0||start_keys.size()==s)need_to_end=true;
			if(start_keys.size()>0&&need_to_end){
				log.error("Lizar component Plugin "+plugin_name+" init failed, caused by:",ex);
			}
		}
		run(start_queue);
		plugin_config.write_plugin_config();		//clean up deprecated plugin config and persist.
		
		started=true;
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
	
	
	
	private void run(List<Plugin> start_list){
		if(start_list.size()>0){
			log.info("..............................................................................");
			log.info("\t\t\tReady to arrange plugins schedule.");
			log.info("..............................................................................");
		}
		int thread_num=0;
		for(Plugin plugin:start_list){
			if(StringHelper.isNotNull(plugin.interval_time)){
				thread_num++;
			}
		}
		if(this.scheduExec==null&&thread_num>0){
			scheduExec=Executors.newScheduledThreadPool(thread_num);
			for(Plugin plugin:start_list){
				try {
					schedule(plugin);
				} catch (Exception e) {
					log.error("Plugin "+plugin.getClass().getName()+" schedule to run failed.",e);
				}
			}
		}
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
			scheduExec.scheduleAtFixedRate
			(plugin, Time.translate_time(plugin.delay_time), Time.translate_time(plugin.interval_time), TimeUnit.MILLISECONDS);
			
		}else{
			log.info("[Normal Module]:plugin class:"+ plugin.getClass().getName());
		}
	}
	

	@SuppressWarnings("unchecked")
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
	
	@Override
	public void stop() throws LifecycleException{
		log.info("..............................................................................");
		log.info("\t\t\tPlugin Manager stop and begin to clean up threads.");
		if(!started) throw new LifecycleException("PluginManager has not started.");
		
		Plugin plugin=null;
		for(Entry<String, Plugin> e:container.entrySet()){
			plugin=e.getValue();
			try{
				plugin.stop();
			}catch(Throwable t){
				log.error("", t);
			}
			log.info(plugin.getClass().getName()+" has been cleaned up.");
		}
		container.clear();
		if(scheduExec!=null)scheduExec.shutdown();
		started=false;
		log.info("..............................................................................");
	}
	
	
	
	
	/**
	 * process plugin config issues.
	 * 
	 * @author wujianchao
	 *
	 */
	private static class PluginConfig{
		
		private File config_file;
		
		public static final String FILE="/WEB-INF/lizar/plugins.json";
		
		JSON config;
		
		/**
		 * load plugin config
		 * @throws IOException 
		 */
		public void load_plugin_config() throws IOException{
			config_file=new File(Config.path_filter(FILE));
			config_file.createNewFile();
			config=FileTool.read_json(config_file);
			if(config==null) config=new JObject();
		}
		
		/**
		 * clean up deprecated plugin config file then persist.
		 * sleeping 1ms to ensure last_mofify is different from every plugins' last_modify
		 * 
		 * @throws IOException 
		 * @throws InterruptedException 
		 */
		public void write_plugin_config() throws IOException, InterruptedException{
			//clean up deprecated plugins
			Set<String> key_set=container.keySet();
			for(Object key :config.toMap().keySet()){
				if(!key_set.contains(key)) config.removeField(key.toString());
			}
			Thread.sleep(1);		// sleep 1ms
			FileTool.write_to_file(config, config_file);
		}


		public void init_plugin(String cell_name,Plugin plugin) throws IOException, ParseException{
			JSON c = config._entity(cell_name);
			if(c==null){
				c=new JObject();
				config.put(cell_name, c);
				if(StringHelper.isNull(plugin.set_interval_time()))c.put("interval_time", "");
				else c.put("interval_time", plugin.set_interval_time());
				if(StringHelper.isNull(plugin.set_delay_time()))c.put("delay_time", "");
				else c.put("delay_time", plugin.set_delay_time());
				if(plugin.set_start_time()==null)c.put("start_time", "");
				else c.put("start_time", to_standard_time(plugin.set_start_time()));
//				c.put("last_modify", System.currentTimeMillis());
				
			}
			
			plugin.interval_time=c._string("interval_time");
			plugin.delay_time=c._string("delay_time");
			plugin.start_time=to_date(c._string("start_time"));
		}
		
		
	}
	
	

}
