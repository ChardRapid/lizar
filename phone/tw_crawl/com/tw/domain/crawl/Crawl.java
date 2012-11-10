package com.tw.domain.crawl;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import com.lizar.util.StringHelper;
import com.lizar.util.http.Http;
import com.lizar.web.Web;
import com.lizar.web.config.Group;
import com.tw.persistence.crawl.page.CrawledPageDao;
import com.tw.persistence.crawl.page.DoubanCelebrityDao;
import com.tw.persistence.crawl.page.DoubanMovieDao;
import com.tw.persistence.crawl.page.FailedPageDao;
import com.tw.persistence.crawl.page.NewPageDao;

public class Crawl extends Thread{
	
	private FailedPageDao failed_page_dao=Web.get(FailedPageDao.class);
	private CrawledPageDao crawled_page_dao=Web.get(CrawledPageDao.class);
	private NewPageDao new_page_dao=Web.get(NewPageDao.class);
	private DoubanMovieDao douban_movie_dao=Web.get(DoubanMovieDao.class);
	private DoubanCelebrityDao douban_celebrity_dao=Web.get(DoubanCelebrityDao.class);
	public   Queue<String> queue = new LinkedList<String>();
	@Override
	public void run() {
		while(true){
			if(CrawlLoader.run&&queue.size()>0){
				fetch(queue.poll());
			}
			try {
				sleep(Group._int("crawl.thread_sleep_interval"));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void push_task(String url){
		queue.add(url);
	}
	
	public  void fetch(String url){
		if(failed_page_dao.exists(url)||crawled_page_dao.exists(url))return;
		String result =null;
		try {
			result=Http.get(url,12300);
		} catch (IOException e) {
			e.printStackTrace();
			failed_page_dao.upsert(url,e.toString());
			return;
		}
		if(StringHelper.isNotNull(result)){
			fetch_urls(url,result);
		}
		
		
	}
	
	private  void fetch_urls(String url,String result) {
	int point=0;
	int find=0;
	int each_end=0;
	String u=null;
	Set<String> result_set=new HashSet<String>();
	while(true){
		find=result.indexOf("\"http://movie.douban.com/", point);
		if(find!=-1){
			each_end=result.indexOf("\"", find+1);
			u=result.substring(find+1,each_end);
			point=find+u.length()+2;
			if(allowed(u)){
				result_set.add(u);
			}
		}else break;
	}
	int new_page=0;
	int movie_num=0;
	int celebrity_num=0;
	long query_start_time=0;
	for(String r:result_set){
		query_start_time=System.currentTimeMillis();
		if(failed_page_dao.exists(r)||crawled_page_dao.exists(r)||new_page_dao.exists(r))continue;
		new_page_dao.insert(r,System.currentTimeMillis()-query_start_time);
		long id=get_movie_id(r);
		if(id!=0){
			System.out.println(r);
			douban_movie_dao.insert(id);
			movie_num++;
		}
		if(id==0l){
			id=get_celebrity_id(r);
			if(id!=0){
				System.out.println(r);
				douban_celebrity_dao.insert(id);
				celebrity_num++;
			}
		}
		new_page++;
	}
	crawled_page_dao.insert(url,new_page,movie_num,celebrity_num);
	
	}
	
	private static boolean allowed(String url){
		if(url.length()>24){
			if(url.endsWith("/mupload"))return false;
			url=url.substring(23);
			int end=url.indexOf("/",1);
			if(end==-1){
				return Group.get("crawl_disallow_map."+url)==null;
			}else{
				return Group.get("crawl_disallow_map."+ url.substring(0, end))==null;
			}
		}
		return true;
	}
	
	
	private static long get_movie_id(String url){
		int end=url.indexOf("?");
		if(end!=-1)url=url.substring(0,url.indexOf("?"));
		if(url.endsWith("/"))url=url.substring(0, url.length()-1);
		if(url.startsWith("http://movie.douban.com/subject/")){
			try{
				return Long.parseLong(url.substring(32));
			}catch(Exception e){
				return 0;
			}
		}
		return 0;
	}
	
	private static long get_celebrity_id(String url){
		int end=url.indexOf("?");
		if(end!=-1)url=url.substring(0,url.indexOf("?"));
		if(url.endsWith("/"))url=url.substring(0, url.length()-1);
		if(url.startsWith("http://movie.douban.com/celebrity/")){
			try{
				return Long.parseLong(url.substring(34));
			}catch(Exception e){
				return 0;
			}
		}
		return 0;
	}
	
	public static void main(String[] args) throws IOException{
		
		System.out.println(get_celebrity_id("http://movie.douban.com/celebrity/1018271/"));
//		String result=Http.get("http://movie.douban.com",12300);
//		int point=0;
//		int find=0;
//		int each_end=0;
//		String u=null;
//		Set<String> result_set=new HashSet<String>();
//		while(true){
//			find=result.indexOf("\"http://movie.douban.com/", point);
//			if(find!=-1){
//				each_end=result.indexOf("\"", find+1);
//				u=result.substring(find+1,each_end);
//				point=find+u.length()+2;
//				System.out.println(u);
//			}else break;
//		}
	}
	
}
