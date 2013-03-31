package org.lizar.web.controller;

public interface ExceptionRecorder {
	void handle(EventLoader el,Exception e);
}
