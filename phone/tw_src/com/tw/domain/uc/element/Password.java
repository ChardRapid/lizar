package com.tw.domain.uc.element;

import com.lizar.util.StringHelper;
import com.lizar.web.Web;

public class Password {
	
	public static final int MIN_LENTH=6;
	
	public static final int MAX_LENGTH=20;
	
	public static String validate(String lan,String password){
		if(password.length()<6){
			return Web.i18.get(lan, "register.password_too_short");
		}else if(password.length()>20) return Web.i18.get(lan, "register.password_too_long");
		return null;
	}
	
	public static String login_validate(String lan,String password){
		if(StringHelper.isNull(password))return Web.i18.get(lan, "login.password_is_null");
		if(password.length()<6){
			return Web.i18.get(lan, "login.password_invalid");
		}else if(password.length()>20) return Web.i18.get(lan, "login.password_invalid");
		return null;
	}
}
