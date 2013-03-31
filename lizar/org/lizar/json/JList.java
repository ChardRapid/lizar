// BasicDBList.java

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.lizar.json.util.JSONParser;
import org.lizar.json.util.StringRangeSet;



/**
 * a basic implementation of bson list that is mongo specific 
 * @author antoine
 */
public class JList extends LinkedList<Object>  implements JSON {

    private static final long serialVersionUID = -4415279469780082174L;

   

    

    public Object copy() {
        // copy field values into new object
        JList newobj = new JList();
        // need to clone the sub obj
        for (int i = 0; i < size(); ++i) {
            Object val = get(i);
            if (val instanceof JObject) {
                val = ((JObject)val).copy();
            } else if (val instanceof JList) {
                val = ((JList)val).copy();
            }
            newobj.add(val);
        }
        return newobj;
    }
    

	/** 
     * Puts a value at an index.
     * For interface compatibility.  Must be passed a String that is parsable to an int.
     * @param key the index at which to insert the value
     * @param v the value to insert
     * @return the value
     * @throws IllegalArgumentException if <code>key</code> cannot be parsed into an <code>int</code>
     */ 
    public Object put( String key , Object v ){
        return put(_getInt( key ), v);
    }

    /** 
     * Puts a value at an index.
     * This will fill any unset indexes less than <code>index</code> with <code>null</code>.
     * @param key the index at which to insert the value
     * @param v the value to insert
     * @return the value
     */ 
    public Object put( int key, Object v ) {
        while ( key >= size() )
            add( null );
        set( key , v );
        return v;
    }

    @SuppressWarnings("unchecked")
    public void putAll( Map m ){
    	for ( Map.Entry entry : (Set<Map.Entry>)m.entrySet() ){
            put( entry.getKey().toString() , entry.getValue() );
        }
    } 
    
    
    /** 
     * Gets a value at an index.
     * For interface compatibility.  Must be passed a String that is parsable to an int.
     * @param key the index
     * @return the value, if found, or null
     * @throws IllegalArgumentException if <code>key</code> cannot be parsed into an <code>int</code>
     */ 
    public Object get( String key ){
        int i = _getInt( key );
        if ( i < 0 )
            return null;
        if ( i >= size() )
            return null;
        return get( i );
    }

    public Object removeField( String key ){
        int i = _getInt( key );
        if ( i < 0 )
            return null;
        if ( i >= size() )
            return null;
        return remove( i );        
    }

    public boolean containsField( String key ){
        int i = _getInt( key , false );
        if ( i < 0 )
            return false;
        return i >= 0 && i < size();
    }

    public Set<String> keySet(){
      return new StringRangeSet(size());
    }

    @SuppressWarnings("unchecked")
    public Map toMap() {
        Map m = new HashMap();
        Iterator i = this.keySet().iterator();
        while (i.hasNext()) {
            Object s = i.next();
            m.put(s, this.get(String.valueOf(s)));
        }
        return m;
    }
    
    int _getInt( String s ){
        return _getInt( s , true );
    }

    int _getInt( String s , boolean err ){
        try {
            return Integer.parseInt( s );
        }
        catch ( Exception e ){
            if ( err )
                throw new IllegalArgumentException( "BasicBSONList can only work with numeric keys, not: [" + s + "]" );
            return -1;
        }
    }
    

    /** Returns the value of a field as a boolean.
     * @param key the field to look up
     * @return the value of the field, or false if field does not exist
     */
    public boolean _bool( String key ){
    	throw new RuntimeException("list will not support this method");
    }

    public boolean _bool( String key , boolean _default ){
    	throw new RuntimeException("list will not support this method");
    }


 	public double _double(String name) {
 		throw new RuntimeException("list will not support this method");
 	}

 	public double _double(String name, double default1) {
 		throw new RuntimeException("list will not support this method");
 	}

 	public float _float(String name) {
 		throw new RuntimeException("list will not support this method");
 	}

 	public float _float(String name, float default1) {
 		throw new RuntimeException("list will not support this method");
 	}

 	public int _int(String name) {
 		throw new RuntimeException("list will not support this method");
 	}

 	public int _int(String name, int default1) {
 		throw new RuntimeException("list will not support this method");
 	}

 	public JList _list(String name) {
 		throw new RuntimeException("list will not support this method");
 	}


 	public long _long(String name) {
 		throw new RuntimeException("list will not support this method");
 	}

 	public long _long(String name, long default1) {
 		throw new RuntimeException("list will not support this method");
 	}

 	public JObject _entity(String name) {
 		throw new RuntimeException("list will not support this method");
 	}


 	public String _string(String name) {
 		throw new RuntimeException("list will not support this method");
 	}
 	
 	public String _string(String name,String _default) {
 		throw new RuntimeException("list will not support this method");
 	}
 	
 	public <T> T get(String name,T t) {
 		throw new RuntimeException("list will not support this method");
 	}

 	 /**
     * Returns a JSON serialization of this object
     * @return JSON serialization
     */    
    
    public String toString(){
        return JSONParser.serialize( this );
    }
    
    @Override
	public String to_beautifier_string() {
		
		  return JSONParser.beautifier_serialize(this);
	}
}
