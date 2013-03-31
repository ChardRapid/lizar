package org.lizar.exception;


public class GroupCodeIsNotExists extends RuntimeException {
	private static final long serialVersionUID = -7780149164665729686L;

	public GroupCodeIsNotExists(String group,String code) {
		super("Group:"+group+" of code:"+code+" is not exists, you must set the value before use it");
	}

	public GroupCodeIsNotExists(String fullcode) {
		super("GroupCode:"+fullcode+" is not exists, you must set the value before use it");
	}
	
}
