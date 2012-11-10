package lizar.anlaysis.persistence;

import com.mongodb.DBCursor;
import com.mongodb.Entity;
import com.mongodb.EntityList;
import com.mongodb.MEntity;
import com.tw.persistence.MongoDao;

public class RefererDao extends MongoDao{
	public void init_property() throws Exception {
		this.init("traffic_referer");
		Entity columns=new MEntity();
		columns.put("uri", 1);
		columns.put("referer", 1);
		this.create_index(columns, "uri_ip", true);
	}
	
	public EntityList get(String uri){
		Entity fields=new MEntity();
		fields.put("referer", 1);
		fields.put("num", 1);
		fields.put("_id", 0);
		DBCursor cur=this.collection.find(new MEntity("uri",uri), fields);
		if(cur.hasNext()){
			System.out.println("referer list is not null.");
			EntityList list=new EntityList();
			for(;cur.hasNext();){
				list.add(cur.next());
			}
			System.out.println("referer list:"+list);
			return list;
		}
		return null;
	}
	
	public void update(String uri,String referer){
		Entity query=new MEntity();
		query.put("uri", uri);
		query.put("referer", referer);
		Entity update=new MEntity();
		update.put("$inc",new MEntity("num",1));
		Entity e=this.collection.findAndModify(query, new MEntity("_id",1), null, false, update, false, false);
		if(e==null){
			query.put("num", 1);
			this.collection.insert(query);
		}
	}
	
	
}
