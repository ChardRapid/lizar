package com.lizar.web;

import java.util.List;
import java.util.Map;

import com.lizar.exception.NotDefined;
import com.lizar.log.Log;
import com.lizar.log.Logger;
import com.lizar.util.CollectionHelper;
import com.lizar.web.loader.Module;
import com.lizar.web.loader.Plugin;

public class ModuleManager {
	
	private Log log=Logger.newInstance(this.getClass());
	
	public static Map<String, Module> container ;
	
	private static ModuleManager module_manager;
	
	private ModuleManager(){
		if(container==null)container = Web.modules;
		log.info("Totally "+container.size()+" Module  is going to load.");
		init_modules();
	}
	
	public static ModuleManager instance(){
		if(module_manager==null)module_manager=new ModuleManager();
		return module_manager;
	}
	

	public static  <T> T getModule(Class<T> t){
		T e=null;
		if(container==null){
			e= Web.get(t);
			if(!(e instanceof Plugin)||e==null)throw new NotDefined("Module "+t.getName()+" not found in the container pls make sure it is exsits");
			return e;
		}
		e=(T)container.get(t.getName());
		if(e==null)throw new NotDefined("Module "+t.getName()+" not found in the container pls make sure it is exsits");
		return e;
	}
	
	private void init_modules(){
		Module module;
		boolean need_to_end=false;
		Exception ex=null;
		String module_name=null;
		List<String> start_keys=CollectionHelper.set_to_list(container.keySet());
		if(container.size()>0){
			log.info("..............................................................................");
			log.info("\t\t\tReady to init Module components.");
			log.info("..............................................................................");
		}
		while(!need_to_end){
			int s=start_keys.size();
			for(int i=0;i<start_keys.size();){
				module=container.get(start_keys.get(i));
				try {
					module_name=module.getClass().getName();
					module.init_component();
				} catch (Exception e1) {
					ex=e1;
					i++;
					continue;
				}
				start_keys.remove(i);
			}
			if(start_keys.size()==0||start_keys.size()==s)need_to_end=true;
			if(start_keys.size()>0&&need_to_end){
				log.error("Lizar component Module "+module_name+" init failed, caused by:",ex);
			}
		}
	}
	
	
	
}
