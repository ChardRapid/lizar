package tw.search.persistence;

import com.mongodb.DBCursor;
import com.mongodb.Entity;
import com.mongodb.EntityList;
import com.mongodb.MEntity;
import com.tw.persistence.MongoDao;

public class IndexInfoDao extends MongoDao {
	public void init_property() throws Exception {
		this.init("index_info");
	}
	
	public void end_event(long start_time,long end_num,String exception){
		Entity update =new MEntity();
		update.put("end_num", end_num);
		update.put("end_time", System.currentTimeMillis());
		update.put("exception", exception);
		this.collection.update(new MEntity("start_time",start_time), update);
	}
	
	public void insert(Entity e){
		this.collection.insert(e);
	}
	
	public EntityList recent(int num){
		DBCursor cur=this.collection.find().sort(new MEntity("start_time",-1)).limit(num);
		if(cur.hasNext()){
			EntityList list=new EntityList();
			while(cur.hasNext()){
				list.add(cur.next());
			}
			return list;
		}
		return null;
	}
}
