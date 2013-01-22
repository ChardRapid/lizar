package com.lizar.log;


public interface Log {
	void error(String message);
	void error(String message,Throwable t);
	void error(Throwable t);
	void info(String message);
	void info(Object obj);
	void info(Throwable t);
	void info(String message,Throwable t);
	void warn(String message);
	void warn(Throwable t);
	void warn(String message,Throwable t);
	void debug(String message);
	void debug(String message,Throwable t);
	void debug(Throwable t);
}
