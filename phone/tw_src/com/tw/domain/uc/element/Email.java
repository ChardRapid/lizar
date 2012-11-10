package com.tw.domain.uc.element;

import com.lizar.util.StringHelper;
import com.lizar.web.Web;
import com.lizar.web.loader.Cell;
import com.tw.persistence.RegisterDao;
import com.tw.persistence.UserDetailDao;

public class Email implements Cell{
	public static final int LENGTH=60;
	


	public static String validate_email_without_db_verify(String lan,String email) {
		if (StringHelper.isNull(email)) {
			return Web.i18.get(lan, "register.email_cannot_be_null");
		} else if (!StringHelper.isEmail(email)) {
			return  Web.i18.get(lan, "register.email_format_is_not_valid");
		} else if (email.length() > 60) {
			return Web.i18.get(lan, "register.email_too_long");
		}
		return null;
	}
	
	

	
	public static String validate(String lan,String email) {
		String msg = validate_email_without_db_verify(lan,email);
		if(StringHelper.isNull(msg)){
			if(user_detail_dao.email_exists(email)){
				return Web.i18.get(lan, "register.email_exists");
			}
		}
		return msg;
	}




	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		register_dao=Web.get(RegisterDao.class);
		user_detail_dao=Web.get(UserDetailDao.class);
	}

	private static RegisterDao register_dao;
	private static UserDetailDao user_detail_dao;
	
}
