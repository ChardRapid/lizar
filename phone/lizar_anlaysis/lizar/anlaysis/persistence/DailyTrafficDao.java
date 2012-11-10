package lizar.anlaysis.persistence;

import com.mongodb.DBCursor;
import com.mongodb.Entity;
import com.mongodb.EntityList;
import com.mongodb.MEntity;
import com.mongodb.WriteResult;
import com.tw.persistence.MongoDao;

public class DailyTrafficDao extends MongoDao{
	public void init_property() throws Exception {
		this.init("daily_traffic");
		Entity columns=new MEntity();
		columns.put("uri", 1);
		columns.put("time", 1);
		this.create_index(columns, "uri_time", true);
	}
	
	public EntityList get(int time){
		DBCursor cur=this.collection.find(new MEntity("time",time));
		if(cur.hasNext()){
			EntityList list=new EntityList();
			while(cur.hasNext()){
				list.add(cur.next());
			}
			return list;
		}
		return null;
	}
	
	public void update_ip(String uri,int time,long ip,EntityList refer){
		Entity query=new MEntity();
		query.put("uri", uri);
		query.put("time", time);
		Entity update=new MEntity();
		update.put("$inc", new MEntity("ip",ip));
		if(refer!=null&&!refer.isEmpty())update.put("$addToSet", new MEntity("referer",refer));
		this.collection.update(query, update);
	}
	
	public void inc_pv(String uri,int time){
		Entity query=new MEntity();
		query.put("uri", uri);
		query.put("time", time);
		Entity inc=new MEntity();
		inc.put("pv", 1);
		Entity e=this.collection.findAndModify(query, new MEntity("$inc",inc));
		if(e==null){
			inc.put("uri", uri);
			inc.put("time", time);
			inc.put("ip", 0);
			this.collection.insert(inc);
		}
	}
}
