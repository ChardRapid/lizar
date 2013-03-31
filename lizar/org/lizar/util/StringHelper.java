package org.lizar.util;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public class StringHelper {

	/**
	 * 过滤换行符
	 * 
	 * */
	public static String filterBreakLine(String lines){
		Pattern p = Pattern.compile("\\r\\n|\\r");
		Matcher m = p.matcher(lines);
		return (m.replaceAll("")).replaceAll("\\s+"," ");
	}

	
	public static StringBuilder getParameter(HttpServletRequest request,String name,StringBuilder defaultValue){
		StringBuilder param=new StringBuilder(request.getParameter(name));
		if(param==null||param.toString().equals("")){
			return defaultValue;
		}
		return param;
	}
	
	public static boolean equals(Object obj1, Object obj2) {
		if (obj1 == obj2) {
			return true;
		} else {
			if (obj1 == null || obj2 == null) {
				return false;
			} else if (obj1.toString().equals(obj2)) {
				return true;
			}
			return false;
		}
	}

	public static boolean isNotNull(String str) {
		if (null != str && !"".equals(str)) {
			return true;
		} else {
			return false;
		}
	}
	


	public static boolean isNull(String str) {
		if (str == null || str.length() == 0) {
			return true;
		}
		return false;
	}

	public static boolean isNotNull(StringBuilder str) {
		if (null != str && !"".equals(str.toString())) {
			return true;
		} else {
			return false;
		}
	}

	public static int getSize(Collection<?> coll) {
		int size = coll == null ? 0 : coll.size();
		return size;
	}

	public static int getSize(Map<?, ?> map) {
		int size = map == null ? 0 : map.size();
		return size;
	}

	public static String toString(Object str) {
		return str == null ? "" : str.toString();
	}

	public static int getLength(Object[] obj) {
		int length = 0;
		length = obj == null ? 0 : obj.length;
		return length;
	}

	public static boolean isInteger(String str) {
		if (isNotNull(str)) {
			try{
				Integer.parseInt(str);
			}catch(Exception e){
				return false;
			}
		}else{
			return false;
		}
		return true;
	}
	
	public static boolean isDigit(String str){
		if (isNotNull(str)) {
			for(char c : str.toCharArray()){
				if((c<'0'||c>'9'))return false;
			}
		}else{
			return false;
		}
		return true;
	}
	
	public static boolean isLong(String str) {
		if (isNotNull(str)) {
			try{
				Long.parseLong(str);
			}catch(Exception e){
				return false;
			}
		}else{
			return false;
		}
		return true;
	}

	public static boolean is_normal_char(String str){
		if(StringHelper.isNull(str))return false;
		for(int i=0;i<str.length();i++){
			char c=str.charAt(i);
			if(!((c<='9'&&c>='0') || (c<='z'&& c>='a') || (c<='Z'&&c>='A') || c=='_')){
				return false;
			}
		}
		return true;
	}
	
	public static boolean is_chinese_char(String str){
		char c;
		if(StringHelper.isNull(str))return false;
		for(int i=0;i<str.length();i++){
			c=str.charAt(i);
			if(!((c<='9'&&c>='0') || (c<='z'&& c>='a') || (c<='Z'&&c>='A') || c=='_'||(c>='\u4E00'&&c<='\u9FA5'))){
				return false;
			}
		}
		return true;
	}
	
	public static boolean is_normal_char(char c){
			if(!((c<='9'&&c>='0') || (c<='z'&& c>='a') || (c<='Z'&&c>='A') || c=='_')){
				return false;
			}
		return true;
	}
	
	public static boolean isDouble(String str) {
		if (isNotNull(str)) {
			Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
			return pattern.matcher(str).matches();
		}
		return false;
	}

	 public static boolean isEmail(final String searchPhrase){
		 	if(StringHelper.isNull(searchPhrase))return false;
		  final String check = "^\\w+([-+.']\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
		  final Pattern regex = Pattern.compile(check);
		  final Matcher matcher = regex.matcher(searchPhrase);
		  return matcher.matches();
	}
	
	 public static int get_start_integer(String str){
		 if(isNull(str))return 0;
		 if(str.charAt(0)<='9'&&str.charAt(0)>='0'){
			 StringBuilder result=new StringBuilder("");
			 for(int i=0;i<str.length();i++){
				 char c=str.charAt(i);
				 if(c<='9'&&c>='0'){
					 result.append(c);
				 }else break;
			 }
			 int n=Integer.parseInt(result.toString());
			 if(n<=Integer.MAX_VALUE)return n;
			 throw new RuntimeException("get start integer is out of max integer bound");
		 }else{
			 return 0;
		 }
		 
	 }
	 public static long wp_strlen(String content) {
			double result=0;
			int temp=0;
			for(int i=0;i<content.length();i++){
				try {
					temp=content.substring(i,i+1).getBytes("UTF-8").length;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if(temp==1){
					result=result+0.5f;
				}else{
					result=result+temp/3.0;
				}
			}
			return Math.round(Math.ceil(result));
		}

		
	
	 
	 public static long get_start_long(String str){
		 if(isNull(str))return 0;
		 if(str.charAt(0)<='9'&&str.charAt(0)>='0'){
			 StringBuilder result=new StringBuilder("");
			 for(int i=0;i<str.length();i++){
				 char c=str.charAt(i);
				 if(c<='9'&&c>='0'){
					 result.append(c);
				 }else break;
			 }
			 long n=Long.parseLong(result.toString());
			 if(n<=Long.MAX_VALUE)return n;
			 throw new RuntimeException("get start long is out of max long bound");
		 }else{
			 return 0;
		 }
		 
	 }
	 
	 
	public static void main(String[] args) {
		System.out.println(is_chinese_char("棉鞋"));
	}
}
