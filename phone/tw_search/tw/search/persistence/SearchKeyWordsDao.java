package tw.search.persistence;

import com.mongodb.Entity;
import com.mongodb.MEntity;
import com.tw.persistence.MongoDao;

public class SearchKeyWordsDao extends MongoDao{

	@Override
	public void init_property() throws Exception {
		this.init("search_keywords");
	}
	
	public void update(String keys,int exact_num,int result_num){
		Entity query=new MEntity();
		query.put("keys",keys.toLowerCase());
		Entity update=new MEntity();
		update.put("exact_num",exact_num);
		update.put("result_num",result_num);
		update.put("$inc",new MEntity("hit",1));
		Entity e=this.collection.findAndModify(query, new MEntity("_id",1), null, false,  update, false, false);
		if(e==null){
			query.put("exact_num",exact_num);
			query.put("result_num",result_num);
			query.put("hit",1);
			this.collection.insert(query);
		}
	}
}
