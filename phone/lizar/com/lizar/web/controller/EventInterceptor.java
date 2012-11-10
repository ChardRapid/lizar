package com.lizar.web.controller;

import java.io.IOException;

import javax.servlet.ServletException;

/**
 * Executing each user request.
 * sequence EventInterceptor.before event.handle EventInterceptor.after.
 *
 */
public interface EventInterceptor {
	
	void before(EventLoader event_loader) throws ServletException, IOException;
	
	void after(EventLoader event_loader) throws ServletException, IOException;
}
