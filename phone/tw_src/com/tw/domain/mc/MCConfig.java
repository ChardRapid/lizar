package com.tw.domain.mc;

import com.lizar.web.config.Group;
import com.lizar.web.loader.Cell;

public class MCConfig implements Cell {

	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		Group.set_up("mc", "email", "admin@tw.com");
		Group.set_up("mc", "password", "admin@tw.com");
		Group.set_up("mc", "name", "我爱甜味");
	}

}
