package com.tw.domain.uc.element;

import com.lizar.util.StringHelper;

public class Desc {
	public static final int LENGTH=140;
	


/**
 * verify description less than 140 characters
 * @return null if description less than 140 characters;
 */
public static String validate_description(String desc){
	if(StringHelper.wp_strlen(desc)>LENGTH){
		return "描述不应超过140个字符";
	}
	return null;
}
}



