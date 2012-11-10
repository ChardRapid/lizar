package com.tw.domain.movie;

import java.io.IOException;
import java.util.Calendar;

import com.lizar.json.JList;
import com.lizar.util.StringHelper;
import com.lizar.util.http.Http;
import com.tw.util.XML2JSON;


public class DMovie  {
	
	private static void pick_up_image(com.lizar.json.JSON json){
		JList list=json._list("link");
		if(list!=null){
			for(Object o:list){
				com.lizar.json.JSON j=(com.lizar.json.JSON)o;
				if(StringHelper.equals(j._string("@rel"), "image")){
					json.put("image", j._string("@href").replace("spic", "lpic"));
					json.removeField("link");
					return;
				}
			}
		}
	}
	
	private static void pick_up_attribute(com.lizar.json.JSON json){
		JList list=json._list("attribute");
		if(list!=null){
			for(Object o:list){
				com.lizar.json.JSON j=(com.lizar.json.JSON)o;
				if(StringHelper.equals(j._string("@name"), "year")){
					json.put("year", Integer.parseInt(j._string("#text")));
				}else if(StringHelper.equals(j._string("@name"), "movie_duration")){
					json.put("movie_duration", j._string("#text"));
				}else if(StringHelper.equals(j._string("@name"), "country")){
					json.put("country", j._string("#text"));
				}else if(StringHelper.equals(j._string("@name"), "language")){
					json.put("language", j._string("#text"));
				}else if(StringHelper.equals(j._string("@name"), "pubdate")){
					json.put("pubdate", j._string("#text"));
				}else if(StringHelper.equals(j._string("@name"), "website")){
					json.put("website", j._string("#text"));
				}else if(StringHelper.equals(j._string("@name"), "imdb")){
					json.put("imdb", j._string("#text"));
				}else if(StringHelper.equals(j._string("@name"), "episodes")){
					json.put("episodes", Integer.parseInt(j._string("#text")));
				}else if(StringHelper.equals(j._string("@name"), "writer")){
					JList writer=json._list("writer");
						if(writer==null){
						writer=new JList();
					}
					writer.add(j._string("#text"));
					json.put("writer", writer);
				}else if(StringHelper.equals(j._string("@name"), "movie_type")){
					JList writer=json._list("movie_type");
					if(writer==null){
						writer=new JList();
					}
					writer.add(j._string("#text"));
					json.put("movie_type", writer);
				}else if(StringHelper.equals(j._string("@name"), "director")){
					JList writer=json._list("director");
					if(writer==null){
						writer=new JList();
					}
					writer.add(j._string("#text"));
					json.put("director", writer);
				}else if(StringHelper.equals(j._string("@name"), "aka")){
					if(StringHelper.equals(j._string("@lang"), "zh_CN")){
						json.put("title_cn", j._string("#text"));
						continue;
					}
					JList writer=json._list("aka");
					if(writer==null){
						writer=new JList();
					}
					writer.add(j._string("#text"));
					json.put("aka", writer);
				}else if(StringHelper.equals(j._string("@name"), "cast")){
					JList writer=json._list("cast");
					if(writer==null){
						writer=new JList();
					}
					writer.add(j._string("#text"));
					json.put("cast", writer);
				}
			}
			json.removeField("attribute");
		}
		
	}
	
	
	
	public static void main(String[] args) throws IOException {
		System.out.println(Calendar.getInstance().get(Calendar.YEAR));
		String result=Http.get("http://api.douban.com/movie/subject/10458899", 10000);
		XML2JSON test=new XML2JSON();
		System.out.println(result);
		System.out.println(".....................................................");
		System.out.println();
		result=result.replaceAll("db:attribute", "attribute").replaceAll("db:tag", "tag").replaceAll("gd:rating", "rating");
		com.lizar.json.JSON json=(com.lizar.json.JSON)com.lizar.json.util.JSONParser.parse(test.xml2json(result));
		json.removeField("@xmlns:opensearch");
		json.removeField("@xmlns:openSearch");
		json.removeField("@xmlns");
		json.removeField("@xmlns:db");
		json.removeField("category");
		json.removeField("@xmlns:gd");
		json.removeField("author");
		json.put("id", 6828823);
		pick_up_image(json);
		pick_up_attribute(json);
		transfer_rating(json);
		transfer_tag(json);
		System.out.println(json.to_beautifier_string());
	}

	private static void transfer_rating(com.lizar.json.JSON json){
		com.lizar.json.JSON rating=json._entity("rating");
		if(rating!=null){
			rating.removeField("@min");
			rating.removeField("@max");
			rating.put("average", rating.removeField("@average"));
			rating.put("numRaters", rating.removeField("@numRaters"));
		}
	}
	
	private static void transfer_tag(com.lizar.json.JSON json){
		if(json.get("tag")==null)return;
		JList list=null;
		if(json.get("tag") instanceof com.lizar.json.JObject){
			list=new JList();
			list.add(json._entity("tag")._string("@name"));
		}else{
			com.lizar.json.JList tags=json._list("tag");
			if(tags!=null){
				com.lizar.json.JSON tag;
				list=new JList();
				for(Object o:tags){
					tag=(com.lizar.json.JSON)o;
					list.add(tag.get("@name"));
				}
				json.put("tag",list);
			}
		}
		
		
		
		
	}
	
}
