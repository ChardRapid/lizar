package com.tw.domain.movie;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.lizar.json.JList;
import com.lizar.util.http.Http;
import com.tw.util.XML2JSON;

public class DMovieReviews {
	public static void main(String[] args) throws IOException {
		String result=Http.get("http://api.douban.com/movie/subject/6040982/reviews?start-index=10", 36000);
		XML2JSON test=new XML2JSON();
		System.out.println(result);
		System.out.println(".....................................................");
		System.out.println();
		result=result.replaceAll("db:comments", "comments");
		result=result.replaceAll("db:useless", "useless");
		result=result.replaceAll("db:votes", "votes");
		result=result.replaceAll("gd:rating", "rating");
		result=result.replaceAll("opensearch:startIndex", "startIndex");
		result=result.replaceAll("opensearch:totalResults", "totalResults");
		result=result.replaceAll("opensearch:itemsPerPage", "itemsPerPage");
		com.lizar.json.JSON json=(com.lizar.json.JSON)com.lizar.json.util.JSONParser.parse(test.xml2json(result));
		json.removeField("@xmlns:openSearch");
		json.removeField("@xmlns:opensearch");
		json.removeField("@xmlns");
		json.removeField("@xmlns:db");
		json.removeField("@xmlns:gd");
		json.removeField("link");
		update_entry(json);
		System.out.println(json.to_beautifier_string());
		
	}
	
	private static void update_entry(com.lizar.json.JSON json){
		JList list=json._list("entry");
		List<com.lizar.json.JSON> remove_list=new LinkedList<com.lizar.json.JSON>();
		if(list!=null){
			System.out.println("rest size:"+list.size());
			for(Object o:list){
				com.lizar.json.JSON e=(com.lizar.json.JSON)o;
				e.removeField("id");
				e.removeField("link");
				e.removeField("author");
				//e.put("votes", Integer.parseInt(e._string("votes")));
				if(e._entity("votes")!=null)e.put("votes", Integer.parseInt(e._entity("votes")._string("@value")));
				if(e._entity("useless")!=null)e.put("useless", Integer.parseInt(e._entity("useless")._string("@value")));
				if(e._entity("comments")!=null)e.put("comments", Integer.parseInt(e._entity("comments")._string("@value")));
				if(e._entity("rating")!=null)e.put("rating", Integer.parseInt(e._entity("rating")._string("@value")));
				if(e._int("useless")==0||e._int("votes")==0||e._int("comments")==0)remove_list.add(e);
			}
			for(com.lizar.json.JSON e:remove_list){
				list.remove(e);
			}
			System.out.println("rest size:"+list.size());
		}
	}
}
