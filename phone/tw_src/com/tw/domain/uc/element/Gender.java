package com.tw.domain.uc.element;

import com.lizar.web.Web;
import com.lizar.web.config.I18Msg;

public class Gender {
	public static final int UN_KNOWN=0;
	
	public static final int FEMALE=1;
	
	public static final int MALE=2;
	
	public String validate_gender(String lan,String gender) {
		if (!gender.equals(UN_KNOWN+"") && !gender.equals(FEMALE+"") && !gender.equals(MALE+"")) {
			return Web.i18.get(lan,"register.gender_not_validate");
		}
		return null;
	}
	
}
