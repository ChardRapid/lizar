// JSONCallback.java

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

package com.lizar.json.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.SimpleTimeZone;

import com.lizar.json.JList;
import com.lizar.json.JObject;
import com.lizar.json.JSON;

public class JSONCallback  {
    
    
    public JObject create(){
        return new JObject();
    }
    
    
    protected JList createList() {
        return new JList();
    }
    
    private Object objectDone2(){
        JSON o =_stack.removeLast();
        if ( _nameStack.size() > 0 )
            _nameStack.removeLast();
        else if ( _stack.size() > 0 ) {
	    throw new IllegalStateException( "something is wrong" );
	}
        return (JSON)o;
    }

    public Object objectDone(){
        String name = curName();
        Object o = objectDone2();
        JSON b = (JSON)o;

        // override the object if it's a special type
	if ( ! _lastArray ) {
	    if ( b.containsField( "$date" ) ) {
		SimpleDateFormat format = 
		    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                GregorianCalendar calendar = new GregorianCalendar(new SimpleTimeZone(0, "GMT"));
                format.setCalendar(calendar);
                String txtdate = (String) b.get("$date");
                o = format.parse(txtdate, new ParsePosition(0));
                if (o == null) {
                    // try older format with no ms
                    format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    format.setCalendar(calendar);
                    o = format.parse(txtdate, new ParsePosition(0));
                }
		if (!isStackEmpty()) {
		    cur().put( name, o );
		} else {
		    setRoot(o);
		}
	    }  
	}
        return o;
    }

    private boolean _lastArray = false;
    

    public JSONCallback(){
        reset();
    }


    public JSON create( boolean array , List<String> path ){
        if ( array )
            return createList();
        return create();
    }

    public void arrayStart(){
    	objectStart( true );
     }

        public void arrayStart(String name){
            objectStart( true , name );
    }
    
    public void objectStart(){
        if ( _stack.size() > 0 ) {
	    throw new IllegalStateException( "something is wrong" );
	}
	objectStart(false);
    }

    public void objectStart(boolean array){
        _root = create(array, null);
        _stack.add( (JSON)_root );
    }
   
    
    public void objectStart(String name){
        objectStart( false , name );
    }
    
    
    public void objectStart(boolean array, String name){
        _lastArray = array;
        _nameStack.addLast( name );
        JSON o = create( array , _nameStack );
        _stack.getLast().put( name , o);
        _stack.addLast( o );
    }

    public Object arrayDone(){
        return objectDone();
    }

    public void gotNull( String name ){
        cur().put( name , null );
    }
        
    public void gotUndefined( String name ){
    }

    public void gotBoolean( String name , boolean v ){
        _put( name , v );
    }
    
    public void gotDouble( String name , double v ){
        _put( name , v );
    }
    
    public void gotInt( String name , int v ){
        _put( name , v );
    }
    
    public void gotLong( String name , long v ){
        _put( name , v );
    }

    public void gotDate( String name , long millis ){
        _put( name , new Date( millis ) );
    }
   
    
    public void gotString( String name , String v ){
        _put( name , v );
    }


    protected void _put( String name , Object o ){
        cur().put( name , o  );
    }
    
    protected JSON cur(){
        return _stack.getLast();
    }
    
    protected String curName(){
        if (_nameStack.isEmpty())
            return null;
        return _nameStack.getLast();
    }

    public Object get(){
	return _root;
    }

    protected void setRoot(Object o) {
	_root = o;
    }

    protected boolean isStackEmpty() {
	return _stack.size() < 1;
    }    
    
    public void reset(){
        _root = null;
        _stack.clear();
        _nameStack.clear();
    }

    private Object _root;
    private final LinkedList<JSON> _stack = new LinkedList<JSON>();
    private final LinkedList<String> _nameStack = new LinkedList<String>();
}
