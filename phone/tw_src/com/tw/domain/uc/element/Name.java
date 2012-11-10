package com.tw.domain.uc.element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lizar.util.StringHelper;
import com.lizar.web.Web;

public class Name {
	
	public static final int LENGTH=25;
	
	public static boolean validate(String name){
		return false;
	}
	
	public static String validate(String lan,String nick_name) {
		 final String check = "^[\\u4E00-\\u9FA5\\uf900-\\ufa2d\\w]{1,18}$";
		 final Pattern regex = Pattern.compile(check);
		 final Matcher matcher = regex.matcher(nick_name);
		 if(!matcher.matches()||StringHelper.isDigit(nick_name)){
			 return Web.i18.get(lan, "register.name_not_valid");
		 }
		return null;
	}
	


	
}
