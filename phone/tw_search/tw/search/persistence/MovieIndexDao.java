package tw.search.persistence;

import com.mongodb.Entity;
import com.mongodb.MEntity;
import com.tw.persistence.MongoDao;

public class MovieIndexDao extends MongoDao {
	public void init_property() throws Exception {
		this.init("movie_index");
	}
	
	public long count(){
		return this.collection.count();
	}
	
	public void upsert(long id){
		this.collection.insert(new MEntity("_id",id));
	}
	
	public long next(){
		Entity e=this.collection.findOne();
		return e._long("_id");
	}
	
	public void remove(long id){
		this.collection.remove(new MEntity("_id",id));
	}
}
