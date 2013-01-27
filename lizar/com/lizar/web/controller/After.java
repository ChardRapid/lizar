package com.lizar.web.controller;

import java.io.IOException;

import javax.servlet.ServletException;

public interface After {
	abstract void after(BasicLoader bl)throws ServletException, IOException ;
	
}
