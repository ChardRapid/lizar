// BasicDBObject.java

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

import java.util.List;
import java.util.Map;

import org.bson.BasicBSONObject;

import com.mongodb.util.JSON;

/**
 * a basic implementation of bson object that is mongo specific.
 * A <code>DBObject</code> can be created as follows, using this class:
 * <blockquote><pre>
 * DBObject obj = new BasicDBObject();
 * obj.put( "foo", "bar" );
 * </pre></blockquote>
 */
public class MEntity extends BasicBSONObject implements Entity {

    private static final long serialVersionUID = -4415279469780082174L;
    
    /**
     *  Creates an empty object.
     */
    public MEntity(){
    }
    
    /**
     * creates an empty object
     * @param size an estimate of number of fields that will be inserted
     */
    public MEntity(int size){
    	super(size);
    }

    /**
     * creates an object with the given key/value
     * @param key  key under which to store
     * @param value value to stor
     */
    public MEntity(String key, Object value){
        super(key, value);
    }

    /**
     * Creates an object from a map.
     * @param m map to convert
     */
    public MEntity(Map m) {
        super(m);
    }

    public boolean isPartialObject(){
        return _isPartialObject;
    }

    public void markAsPartialObject(){
        _isPartialObject = true;
    }

    /**
     * Returns a JSON serialization of this object
     * @return JSON serialization
     */    
    
    public String toString(){
        return JSON.serialize( this );
    }

    
    public MEntity append( String key , Object val ){
        put( key , val );
        return this;
    }

    public Object copy() {
        // copy field values into new object
        MEntity newobj = new MEntity(this.toMap());
        // need to clone the sub obj
        for (String field : keySet()) {
            Object val = get(field);
            if (val instanceof MEntity) {
                newobj.put(field, ((MEntity)val).copy());
            } else if (val instanceof EntityList) {
                newobj.put(field, ((EntityList)val).copy());
            }
        }
        return newobj;
    }
    
    public boolean _isPartialObject = false;



	
}
