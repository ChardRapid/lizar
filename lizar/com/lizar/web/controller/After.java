package com.lizar.web.controller;

import java.io.IOException;

import javax.servlet.ServletException;

public interface After {
	abstract void after(EventLoader el)throws ServletException, IOException ;
	
}
