package com.tw.domain.movie;

import com.lizar.util.StringHelper;
import com.mongodb.EntityList;

public class DDownload {
	
	public static final int KB=1;
	
	public static final int MB=2;
	public static final int GB=3;
	
	public static final int CAM=1;
	
	public static final int DVD=2;
	
	public static final int HD=3;
	
	/***
	 * 
	 * 视频品质：超清
	 * 
	 * */
	public static final int SD=4;
	
	public static String rest_time_translate(long time){
		long rest_time=System.currentTimeMillis()-time;
		if(rest_time<=0)return "刚刚发布";
		if(rest_time<1000l)return "1秒钟以前";
		if(rest_time<1000l*60)return rest_time/1000+"秒钟以前";
		if(rest_time<1000l*60*60)return rest_time/(1000*60)+"分钟以前";
		if(rest_time<1000l*60*60*24)return rest_time/(1000*60*60)+"小时以前";
		if(rest_time<1000l*60*60*24*30)return rest_time/(1000*60*60*24)+"天以前";
		if(rest_time<1000l*60*60*24*30*12)return rest_time/(1000l*60*60*24*30)+"个月前";
		return rest_time/(1000l*60*60*24*30*12)+"年前";
	}
	
	
	
	public static long calculate_kb_size(String size_type,double size){
		if(size_type.equals("MB")){
			return (long)size*1024;
		}else if(size_type.equals("KB")){
			return (long)size;
		}else if(size_type.equals("GB")){
			return (long)size*1024*1024;
		}
		throw new RuntimeException("unkown size type :"+size_type);
	}

	public static void main(String[] args){
		System.out.println(rest_time_translate(System.currentTimeMillis()-1000l*5*60*60*24*30*12));
	}
	
	public static boolean validate_size_type(int size_type){
		if(size_type>0&&size_type<4)return true;
		return false;
	}
	
	public static EntityList split_links(String links){
		if(StringHelper.isNull(links))return null;
		String[] lks=links.split(";");
		EntityList lks_list=new EntityList();
		for(String l:lks){
			if(StringHelper.isNotNull(l)){
				
				lks_list.add(l);
			}
		}
		return lks_list;
	}
	
	public static boolean validate_quality(int quality){
		if(quality<0||quality>4){
			return false;
		}
		return true;
	}

}
