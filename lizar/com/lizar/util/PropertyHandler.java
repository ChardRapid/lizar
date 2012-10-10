package com.lizar.util;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class PropertyHandler {
	
		/**
		 *
		 * read all the properties from the given path
		 * @throws IOException 
		 **/
	    public static Properties readProperties(String filePath) throws IOException {
	    	java.util.Properties props = new java.util.Properties();
	         InputStream in = new BufferedInputStream (new FileInputStream(filePath));
	         props.load(in);
	         in.close();
	       return  props;
	    }

	    /**
		 *
		 * read all the properties from the given path
		 * @throws IOException 
		 **/
	    public static Properties readProperties(File f) throws IOException {
	    	java.util.Properties props = new java.util.Properties();
	         InputStream in = new BufferedInputStream (new FileInputStream(f));
	         props.load(in);
	         in.close();
	       return  props;
	    }
	    
	    /**
	     * 
	     * update the properties file through properties and filePath
	     * @throws IOException 
	     * 
	     * */
	    public static void writeProperties(String filePath,Properties prop,String comments) throws IOException {
	            OutputStream fos = new FileOutputStream(filePath);
	            prop.store(fos, comments);
	            fos.close();
	        
	    }

	    public static void writeProperties(File f,Properties prop,String comments) throws IOException {
            OutputStream fos = new FileOutputStream(f);
            prop.store(fos, comments);
            fos.close();
        
    }
	    
	    public static void main(String[] args) throws IOException {
	    	 Properties props=readProperties("D:\\03workspace\\java\\FinalProject\\config\\task\\task.properties");
	         System.out.println(props.getProperty("com.task.LuceneWorker"));
	         props.put("this", "yes");
	         writeProperties("D:\\03workspace\\java\\FinalProject\\config\\task\\task.properties",props,"");
	    }
	
}
