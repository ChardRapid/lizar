package com.lizar.web.controller;

import java.io.IOException;

import javax.servlet.ServletException;

public interface Before {
	abstract void before(EventLoader el)throws ServletException, IOException ;
}
