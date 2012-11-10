package com.tw.domain.mc;

import org.bson.types.ObjectId;

import com.lizar.util.MyMath;
import com.lizar.web.Web;
import com.lizar.web.config.Group;
import com.lizar.web.loader.Cell;
import com.mongodb.Entity;
import com.mongodb.MEntity;
import com.tw.domain.uc.element.Gender;
import com.tw.domain.uc.element.Status;
import com.tw.persistence.AdminDao;
import com.tw.persistence.UserDetailDao;

public class AdminAccountCreate implements Cell{

	private AdminDao admin_dao;
	private MCConfig mc_config;
	private UserDetailDao user_detail_dao;
	
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		admin_dao=Web.get(AdminDao.class);
		user_detail_dao=Web.get(UserDetailDao.class);
		mc_config=Web.get(MCConfig.class);
		if(!user_detail_dao.email_exists(Group._str("mc", "email"))){
			Entity user=new MEntity();
			user.put("desc", "");
			user.put("gender", Gender.UN_KNOWN);
			user.put("head", "");
			user.put("new_msg_no", 0);
			user.put("status", Status.NORMAL);
			user.put("_id",new ObjectId());
			user.put("email", Group._str("mc", "email"));
			user.put("name", Group._str("mc", "name"));
			user.put("password", MyMath.encryptionWithMD5(Group._str("mc", "password")));
			user_detail_dao.insert(user);
			admin_dao.insert(user._obj_id("_id"),Admin.SUPER);
		}
	}
	
}
