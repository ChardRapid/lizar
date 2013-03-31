package org.lizar.web;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lizar.HotLoader;
import org.lizar.exception.CellIsNull;
import org.lizar.exception.UnknownCellKey;
import org.lizar.json.JList;
import org.lizar.log.Log;
import org.lizar.log.Logger;
import org.lizar.util.FileTool;
import org.lizar.web.config.Config;
import org.lizar.web.controller.Event;
import org.lizar.web.loader.Cell;
import org.lizar.web.loader.Module;
import org.lizar.web.loader.Plugin;



/**
 * 
 * This is Lizar Engine * 
 * 
 * */
public class Container {
    
	private Log log=Logger.newInstance(this.getClass());
	
	private static Container engine;
	
	
	
    private static Map<String,Cell> lizars;
    
	private Container(){
		lizars=new HashMap<String,Cell>();
		check_cells();
	}
	
	public <T> Map<String,T> get_sub_cells_of(Class<T> t){
		Map<String,T> map=new LinkedHashMap<String,T>();
		for(Entry<String,Cell> entry:lizars.entrySet()){
			try{
				entry.getValue().getClass().asSubclass(t);
			}catch(Exception e){
				continue;
			}
			map.put(entry.getKey(), (T)entry.getValue());
		}
		return map;
	}
	
	/**
	 * check and initialize all cells by calling cell's init_property method.
	 */
	private void check_cells(){
		JList list=Config.xpath_list("container.dirs");
		List<Class> files=new LinkedList<Class>();
		HotLoader loader=new HotLoader(Thread.currentThread().getContextClassLoader());
		for(Object o:list){
			if(o==null)continue;
			File dir=new File(o.toString());
			if(dir.exists()){
				try {
					FileTool.scan_class_file(dir,files,Cell.class,loader);
				} catch (IOException e) {
					log.warn("File load failed:"+dir);
					log.debug("", e);
				}
			}
		}
		Cell b;
		List<Cell> start_list=new ArrayList<Cell>(files.size());
		for(Class c:files){
			try{
				b=(Cell)c.newInstance();
			}catch(Exception e){
				log.warn("Cell "+c.getName()+" can not be created");
				continue;
			}
			start_list.add(b);
			lizars.put(c.getName(), b);
			if(b instanceof Plugin)Web.plugins.put(c.getName(), (Plugin)b);
			if(b instanceof Event)Web.events.put(c.getName(), (Event)b);
			if(b instanceof Module)Web.modules.put(c.getName(), (Module)b);
		}
		init_cells(start_list);
	}
	
	private void init_cells(List<Cell> start_list){
		Cell cell;
		boolean need_to_end=false;
		Exception ex=null;
		String class_name=null;
		log.info("Totally "+start_list.size()+" cell has been found to init.");
		while(!need_to_end){
			int s=start_list.size();
			for(int i=0;i<start_list.size();){
				cell=start_list.get(i);
				try {
					class_name=cell.getClass().getName();
					cell.init_property();
				} catch (Exception e1) {
					ex=e1;
					i++;
					log.debug("trying to init :"+class_name+"  failed, \t\t\tskip it.");
					continue;
				}
				log.debug("trying to init :"+class_name+"  \t\t\tdone.");
				start_list.remove(i);
			}
			if(start_list.size()==0||start_list.size()==s)need_to_end=true;
			if(start_list.size()>0&&need_to_end){
				log.error("Container init cell "+class_name+" init failed pls make sure init_property is only for init property of class not to use then, caused by:",ex);
			}
		}
	}
	
    public static Container instance(){
    	if(engine==null){
    		engine=new Container();
    	}
    	return engine;
    }
    
    
    public static <T> T get(Class<T> t){
    	Cell h=lizars.get(t.getName());
    	if(h==null)throw new UnknownCellKey(t.getName());
    	return (T)h;
    }
    
    public static void set(Cell cell){
    	if(cell==null)throw new CellIsNull("");
    	lizars.put(cell.getClass().getName(), cell);
    }

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		lizars.clear();
    	
	}

	


	
	
	
	
	

	
	
	
	
	
	
}
