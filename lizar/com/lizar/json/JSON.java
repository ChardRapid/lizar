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

package com.lizar.json;

import java.util.Map;
import java.util.Set;

/**
 * A key-value map that can be saved to the database.
 */
public interface JSON  {
	 /** Returns the value of a field as a boolean.
     * @param key the field to look up
     * @return the value of the field, or false if field does not exist
     */
    public boolean _bool( String key );

    public boolean _bool( String key , boolean _default );

 
	public double _double(String name);

	public double _double(String name, double default1);

	public float _float(String name);

	public float _float(String name, float default1) ;

	public int _int(String name);

	public int _int(String name, int default1);

	public JList _list(String name);

	public JObject _entity(String name);
	
	public long _long(String name) ;

	public long _long(String name, long default1) ;

	public String _string(String name) ;
	
	public String _string(String name,String _default) ;
    
    /**
     * 
     * transfer this Object to json String
     * 
     * */
    String toString();
    
    String to_beautifier_string();
    
    public <T> T get(String name,T t);
    
    /**
     * Sets a name/value pair in this object.
     * @param key Name to set
     * @param v Corresponding value
     * @return <tt>v</tt>
     */
    public Object put( String key , Object v );

    /**
     * Sets all key/value pairs from a map into this object
     * @param m the map
     */
    public void putAll( Map m );

    /**
     * Gets a field from this object by a given name.
     * @param key The name of the field fetch
     * @return The field, if found
     */
    public Object get( String key );

    /**
     * Returns a map representing this BSONObject.
     * @return the map
     */
    public Map toMap();

    /**
     * Removes a field with a given name from this object.
     * @param key The name of the field to remove
     * @return The value removed from this object
     */
    public Object removeField( String key );


    /**
     * Checks if this object contains a field with the given name.
     * @param s Field name for which to check
     * @return True if the field is present
     */
    public boolean containsField(String s);

    /**
     * Returns this object's fields' names
     * @return The names of the fields in this object
     */
    public Set<String> keySet();
    

}
