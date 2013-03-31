package org.lizar.log;

import org.apache.log4j.Logger;
import org.lizar.web.config.Config;



public class Log4j implements Log{
	public Logger log;
	
	public Log4j(Class t){
		log=Logger.getLogger(t.getClass());
	}
	
	@Override
	public void debug(String message) {
		if(Config.xpath("server_info.debug",false)){
			log.debug(message);
		}
	}

	@Override
	public void debug(String message, Throwable t) {
		if(Config.xpath("server_info.debug",false)){
		log.debug(message,t);
		}
	}

	@Override
	public void error(String message) {
		// TODO Auto-generated method stub
		log.error(message);
	}

	@Override
	public void error(String message, Throwable t) {
		// TODO Auto-generated method stub
		log.error(message,t);
	}

	@Override
	public void info(String message) {
		// TODO Auto-generated method stub
		log.info(message);
	}

	@Override
	public void info(String message, Throwable t) {
		// TODO Auto-generated method stub
		log.info(message,t);
	}

	@Override
	public void warn(String message) {
		// TODO Auto-generated method stub
		log.warn(message);
	}

	@Override
	public void warn(String message, Throwable t) {
		// TODO Auto-generated method stub
		log.warn(message,t);
	}

	@Override
	public void debug(Throwable t) {
		// TODO Auto-generated method stub
		log.debug("", t);
	}

	@Override
	public void error(Throwable t) {
		// TODO Auto-generated method stub
		log.error("", t);
	}

	@Override
	public void info(Object obj) {
		// TODO Auto-generated method stub
		log.info(obj);
	}

	@Override
	public void info(Throwable t) {
		// TODO Auto-generated method stub
		log.info("", t);
	}

	@Override
	public void warn(Throwable t) {
		// TODO Auto-generated method stub
		log.warn("", t);
	}


}
