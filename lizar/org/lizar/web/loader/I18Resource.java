package org.lizar.web.loader;

import java.io.File;
import java.util.Map;

public class I18Resource {
	public long last_modify;
	
	public File file;
	
	public String lan;
	
	public Map<String,String> map;
	
	public void clean(){
		if(map!=null)map.clear();
		file=null;
		last_modify=0l;
	}
	
	
	
}
