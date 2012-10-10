package com.lizar.log;

import com.lizar.web.loader.Cell;

public  class Logger {
	
	public static Log newInstance(Class t){
		try{
			Object o=Class.forName("org.apache.log4j.Logger");
			Log4j log=new Log4j(t);
			if(log.log.getAllAppenders().hasMoreElements())return log;
			else return  new Console(t);
		}catch(Exception e){
			return new Console(t);
		}
	}


	
}
