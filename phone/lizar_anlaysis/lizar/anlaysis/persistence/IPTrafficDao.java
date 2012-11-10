package lizar.anlaysis.persistence;

import com.lizar.web.config.Keys;
import com.mongodb.Entity;
import com.mongodb.MEntity;
import com.mongodb.WriteResult;
import com.tw.persistence.MongoDao;

public class IPTrafficDao extends MongoDao  {

	public void init_property() throws Exception {
		this.init("traffic");
		Entity columns=new MEntity();
		columns.put("uri", 1);
		columns.put("query", 1);
		this.create_index(columns, "uri_ip", true);
	}
	
	public void update(String uri,String ip){
		Entity query=new MEntity();
		query.put("uri", uri);
		query.put("ip", ip);
		this.collection.update(query, query, true, false);
	}
	
	public long get_traffic(String uri){
		return this.collection.count(new MEntity("uri",uri));
	}
	
	
	
}
