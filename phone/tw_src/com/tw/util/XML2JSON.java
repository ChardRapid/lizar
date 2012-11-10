package com.tw.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.io.IOUtils;

import com.lizar.json.JList;
import com.lizar.util.StringHelper;
import com.lizar.util.http.Http;
import com.sun.xml.internal.txw2.Document;

/**
 *
 *
 */
public class XML2JSON {
	
	public static String xml2json(String xmlString) throws IOException{
		XMLSerializer xmlSerializer = new XMLSerializer();
		JSON json = xmlSerializer.read(xmlString);
		return json.toString(1);
	}
	
	
	
	/**
	 * ��xmlDocumentת��ΪJSON����
	 * @param xmlDocument	XML Document
	 * @return	JSON����
	 * @throws IOException 
	 */
	public static String xml2json(Document xmlDocument) throws IOException{
		return xml2json(xmlDocument.toString());
	}
	

    public String xmlFile2json(String xmlFile) throws FileNotFoundException {
        InputStream is = new FileInputStream(new File(xmlFile));
        String xml;
        String json = null;
        try {
            xml = IOUtils.toString(is);
            json = xml2json(xml);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }
	
	
	
}
