package com.lizar.web.controller;

import java.io.IOException;

import javax.servlet.ServletException;

public interface Before {
	abstract void before(BasicLoader bl)throws ServletException, IOException ;
}
