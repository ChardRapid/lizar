// DBObject.java

/**
 *      Copyright (C) 2008 10gen Inc.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.mongodb;

import org.bson.BSONObject;
import org.bson.types.ObjectId;

/**
 * A key-value map that can be saved to the database.
 */
public interface Entity extends BSONObject {
	 /** Returns the value of a field as a boolean.
     * @param key the field to look up
     * @return the value of the field, or false if field does not exist
     */
    public boolean _bool( String key );

    public boolean _bool( String key , boolean _default );
    
    /**
     * Returns the object id or null if not set.
     * @param field The field to return
     * @return The field object value or null if not found (or if null :-^).
     */
    public ObjectId _obj_id( final String field ) ;
	public double _double(String name);

	public double _double(String name, double default1);

	public float _float(String name);

	public float _float(String name, float default1) ;

	public int _int(String name);

	public int _int(String name, int default1);

	public EntityList _list(String name);

	public MEntity _entity(String name);
	
	public long _long(String name) ;

	public long _long(String name, long default1) ;

	public String _string(String name) ;
	
	public String _string(String name,String _default) ;
    
    /**
     * if this object was retrieved with only some fields (using a field filter)
     * this method will be called to mark it as such.
     */
    public void markAsPartialObject();

    /**
     * whether markAsPartialObject was ever called
     * only matters if you are going to upsert and do not want to risk losing fields
     */
    public boolean isPartialObject();
    
    /**
     * 
     * transfer this Object to json String
     * 
     * */
    String toString();
    
    public <T> T get(String name,T t);
    

}
