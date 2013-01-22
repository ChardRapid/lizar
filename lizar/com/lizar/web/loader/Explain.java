package com.lizar.web.loader;

import com.lizar.json.JSON;

public class Explain {
	public String group;
	
	public String code;
	
	public Object  value;
	
	public String desc;
	
	public String full_code(){
		return group+"."+code;
	}
	
	public static Explain to_explain(JSON e){
		if(e==null)return null;
		Explain expl=new Explain();
		expl.group=e._string("group");
		expl.code=e._string("code");
		expl.value=e._string("value");
		expl.desc=e._string("desc");
		return expl;
	}
	
	public String toString(){
		return value.toString();
	}
}
