package org.lizar.web.controller;

import java.io.IOException;

import javax.servlet.ServletException;

/**
 * Executing each user request.
 * sequence EventInterceptor.before event.handle EventInterceptor.after.
 *
 */
public interface EventInterceptor {
	
	void before(BasicLoader bl) throws ServletException, IOException;
	
	void after(BasicLoader bl) throws ServletException, IOException;
}
