package com.lizar.util;

import com.lizar.exception.UnknownTime;
import com.lizar.web.loader.Cell;

public class Time {

	/**
	 * 
	 * time is a string like 100,100ms,100h,100d,100s.
	 * input without postfix will be regard as mini seconds
	 * return is minisecond
	 * 
	 * */
	public static long translate_time(String time) throws UnknownTime{
		if(StringHelper.isNull(time))return 0L;
		if(StringHelper.isLong(time)){
			return Long.parseLong(time);
		}else{
			time=time.toLowerCase();
			if(time.endsWith("ms")){
				return Long.parseLong(time.substring(0, time.length()-2));
			}else if(time.endsWith("m")){
				return Long.parseLong(time.substring(0, time.length()-1))*60*1000;
			}else if(time.endsWith("h")){
				return Long.parseLong(time.substring(0, time.length()-1))*60*60*1000;
			}else if(time.endsWith("d")){
				return Long.parseLong(time.substring(0, time.length()-1))*24*60*60*1000;
			}else if(time.endsWith("s")){
				return Long.parseLong(time.substring(0, time.length()-1))*1000;
			}else{
				throw new UnknownTime(time);
			}
		}
	}
	
	
}
