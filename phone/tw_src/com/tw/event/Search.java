package com.tw.event;

import java.io.IOException;

import javax.servlet.ServletException;

import tw.search.domain.DSearch;
import tw.search.persistence.SearchKeyWordsDao;


import com.lizar.util.StringHelper;
import com.lizar.web.Web;
import com.lizar.web.controller.Event;
import com.lizar.web.controller.EventLoader;
import com.mongodb.Entity;
import com.mongodb.EntityList;
import com.tw.persistence.movie.MovieDao;

public class Search extends Event {

	private DSearch search;
	private SearchKeyWordsDao keywords_dao;
	private MovieDao movie_dao;
	
	@Override
	public void init_property() throws Exception {
		// TODO Auto-generated method stub
		search=Web.get(DSearch.class);
		keywords_dao=Web.get(SearchKeyWordsDao.class);
		
	}

	@Override
	public String default_handling_path() {
		// TODO Auto-generated method stub
		return "/search/*";
	}

	@Override
	public void handle(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(el.request_path(1).equals("")){
			search(el);
		}
	}
	
	private void search(EventLoader el) throws IOException, ServletException{
		String keys=el._str("keywords").toLowerCase();
		if(StringHelper.isNull(keys)){
			el.response_to_root();
			return;
		}
		
		//精确搜索
		//模糊搜素
		EntityList exact=movie_dao.search(keys);
		EntityList result_list=search.search(keys);
		el.set_attr("exact", exact);
		el.set_attr("result_list", result_list);
		int exact_num=0;
		int result_num=0;
		if(exact!=null)exact_num=exact.size();
		if(result_list!=null)result_num=result_list.size();
		keywords_dao.update(keys,exact_num,result_num);
		el.template("/WEB-INF/template/search_result.vm");
	}

	@Override
	public void handle_jsonp(EventLoader el) throws ServletException,
			IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void handle_json(EventLoader el) throws ServletException,
			IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void handle_xml(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void before(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void after(EventLoader el) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

}
