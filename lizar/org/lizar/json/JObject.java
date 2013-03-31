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

package org.lizar.json;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.lizar.json.util.JSONParser;


/**
 * a basic implementation of bson object that is mongo specific.
 * A <code>DBObject</code> can be created as follows, using this class:
 * <blockquote><pre>
 * DBObject obj = new BasicDBObject();
 * obj.put( "foo", "bar" );
 * </pre></blockquote>
 */
public class JObject  extends LinkedHashMap<String,Object> implements JSON {

    private static final long serialVersionUID = -4415279469780082174L;
    
    /**
     *  Creates an empty object.
     */
    public JObject(){
    }
    
    /**
     * creates an empty object
     * @param size an estimate of number of fields that will be inserted
     */
    public JObject(int size){
    	super(size);
    }

    /**
     * creates an object with the given key/value
     * @param key  key under which to store
     * @param value value to stor
     */
    public JObject(String key, Object value){
    	put(key, value);
    }

    /**
     * Creates an object from a map.
     * @param m map to convert
     */
    public JObject(Map m) {
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
        return JSONParser.serialize( this );
    }

    
    public String to_beautifier_string(){
        return JSONParser.beautifier_serialize( this );
    }
    
    public JObject append( String key , Object val ){
        put( key , val );
        return this;
    }

    public Object copy() {
        // copy field values into new object
        JObject newobj = new JObject(this.toMap());
        // need to clone the sub obj
        for (String field : keySet()) {
            Object val = get(field);
            if (val instanceof JObject) {
                newobj.put(field, ((JObject)val).copy());
            } else if (val instanceof JList) {
                newobj.put(field, ((JList)val).copy());
            }
        }
        return newobj;
    }
    
    private boolean _isPartialObject = false;

    /**
     * Converts a DBObject to a map.
     * @return the DBObject
     */
    public Map toMap() {
        return new LinkedHashMap<String,Object>(this);
    }

    /** Deletes a field from this object.
     * @param key the field name to remove
     * @return the object removed
     */
    public Object removeField( String key ){
        return remove( key );
    }

    /** Checks if this object contains a given field
     * @param field field name
     * @return if the field exists
     */
    public boolean containsField( String field ){
        return super.containsKey(field);
    }


    
    /** Gets a value from this object
     * @param key field name
     * @return the value
     */
    public Object get( String key ){
        return super.get(key);
    }
    /** Returns the value of a field as a boolean.
     * @param key the field to look up
     * @return the value of the field, or false if field does not exist
     */
    public boolean _bool( String key ){
        return _bool(key, false);
    }

    public boolean _bool( String key , boolean _default ){
        Object foo = get( key );
        if ( foo == null )
        return _default;
        try{
        	return ((Boolean)foo).booleanValue();
        }catch(Throwable e){
        	return  _default;
        }
    }

  

	public double _double(String name) {
		return _double(name, 0);
	}

	public double _double(String name, double default1) {
		Object o=get(name);
		try{
			return ((Number)o).doubleValue();
		}catch(Throwable e){
			return default1;
		}
	}

	public float _float(String name) {
		return _float(name,0);
	}

	public float _float(String name, float default1) {
		Object o=get(name);
		try{
			return ((Number)o).floatValue();
		}catch(Throwable e){
			return default1;
		}
	}

	public int _int(String name) {
		return _int(name,0);
	}

	public int _int(String name, int default1) {
		Object o=get(name);
		try{
			return ((Number)o).intValue();
		}catch(Throwable e){
			return default1;
		}
	}

	public JList _list(String name) {
		Object o=get(name);
		if(o!=null) return(JList) o;
		return null;
	}


	public long _long(String name) {
		return _long(name,0);
	}

	public long _long(String name, long default1) {
		Object o=get(name);
		try{
			return ((Number)o).longValue();
		}catch(Throwable e){
			return default1;
		}
	}

	public JObject _entity(String name) {
		Object o=get(name);
		if(o!=null)return (JObject)o;
		return null;
	}


	public String _string(String name) {
		Object o=get(name);
		if(o!=null)return (String)o;
		return "";
	}

	public String _string(String name,String _default) {
		Object o=get(name);
		if(o!=null)return (String)o;
		return _default;
	}
	
	public <T> T get(String name,T t){
		Object o=get(name);
		if(o!=null)return (T)o;
		return t;
	}
	
    /** Add a key/value pair to this object
     * @param key the field name
     * @param val the field value
     * @return the <code>val</code> parameter
     */
    public Object put( String key , Object val ){
        return super.put( key , val );
    }

    @SuppressWarnings("unchecked")
    public void putAll( Map m ){
        for ( Map.Entry entry : (Set<Map.Entry>)m.entrySet() ){
            put( entry.getKey().toString() , entry.getValue() );
        }
    }




    public boolean equals( Object o ){
        if ( ! ( o instanceof JObject ) )
            return false;

        JObject other = (JObject)o;
        if ( ! keySet().equals( other.keySet() ) )
            return false;

        for ( String key : keySet() ){
            Object a = get( key );
            Object b = other.get( key );

            if ( a == null ){
                if ( b != null )
                    return false;
            }
            if ( b == null ){
                if ( a != null )
                    return false;
            }
            else if ( a instanceof Number && b instanceof Number ){
                if ( ((Number)a).doubleValue() !=
                     ((Number)b).doubleValue() )
                    return false;
            }
            else if ( a instanceof Pattern && b instanceof Pattern ){
                Pattern p1 = (Pattern) a;
                Pattern p2 = (Pattern) b;
                if (!p1.pattern().equals(p2.pattern()) || p1.flags() != p2.flags())
                    return false;
            }
            else {
                if ( ! a.equals( b ) )
                    return false;
            }
        }
        return true;
    }

	
}
