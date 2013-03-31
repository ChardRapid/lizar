package org.lizar.web.controller;

import java.io.File;
import java.util.List;

public class Resource {
	public List<String> list;
	
	public File file;
	
	public long last_modify_time;
	
	public Resource(List<String> list){
		this.list=list;
	}
	
	public int use=1;
}
