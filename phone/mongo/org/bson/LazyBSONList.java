package org.bson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.bson.io.BSONByteBuffer;
import org.bson.types.ObjectId;

import com.mongodb.MEntity;
import com.mongodb.EntityList;

@SuppressWarnings( "rawtypes" )
public class LazyBSONList extends LazyBSONObject implements List {

    public LazyBSONList(byte[] data , LazyBSONCallback callback) { super( data , callback ); }
    public LazyBSONList(byte[] data , int offset , LazyBSONCallback callback) { super( data , offset , callback ); }
    public LazyBSONList(BSONByteBuffer buffer , LazyBSONCallback callback) { super( buffer , callback ); }
    public LazyBSONList(BSONByteBuffer buffer , int offset , LazyBSONCallback callback) { super( buffer , offset , callback ); }

    
    public boolean contains( Object arg0 ){
        return indexOf(arg0) > -1;
    }

    
    public boolean containsAll( Collection arg0 ){
        for ( Object obj : arg0 ) {
            if ( !contains( obj ) )
                return false;
        }
        return true;
    }

    
    public Object get( int pos ){
        return get("" + pos);
    }
    
    
    public Iterator iterator(){
        return new LazyBSONListIterator();
    }

    
    public int indexOf( Object arg0 ){
        int pos = 0;
        Iterator it = iterator();
        while ( it.hasNext() ) {
            Object curr = it.next();
            if ( arg0.equals( curr ) )
                return pos;

            pos++;
        }
        return -1;
    }
    
    
    public int lastIndexOf( Object arg0 ){
        int pos = 0;
        int lastFound = -1;
        
        Iterator it = iterator();
        while(it.hasNext()) {
            Object curr = it.next();
            if(arg0.equals( curr ))
                lastFound = pos;
            
            pos++;
        }
        
        return lastFound;
    }

    
    public int size(){
        //TODO check the last one and get the key/field name to see the ordinal position incase the array is stored with missing elements.
        return getElementsToKey( null ).size();
    }

    public class LazyBSONListIterator implements Iterator {
        ArrayList<ElementRecord> elements;
        int pos=0;
        
        public LazyBSONListIterator() {
            elements = getElementsToKey( null );
        }
        
        
        public boolean hasNext(){
            return pos < elements.size();
        }

        
        public Object next(){
            return getElementValue(elements.get(pos++));
        }

        
        public void remove(){
            throw new UnsupportedOperationException( "Read Only" );
        }
        
    }

    
    public ListIterator listIterator( int arg0 ){
        throw new UnsupportedOperationException( "Not Supported" );
    }

    
    public ListIterator listIterator(){
        throw new UnsupportedOperationException( "Not Supported" );
    }


    
    public boolean add( Object arg0 ){
        throw new UnsupportedOperationException( "Read Only" );
    }

    
    public void add( int arg0 , Object arg1 ){
        throw new UnsupportedOperationException( "Read Only" );
    }

    
    public boolean addAll( Collection arg0 ){
        throw new UnsupportedOperationException( "Read Only" );
    }

    
    public boolean addAll( int arg0 , Collection arg1 ){
        throw new UnsupportedOperationException( "Read Only" );
    }

    
    public void clear(){
        throw new UnsupportedOperationException( "Read Only" );
    }

    
    public boolean remove( Object arg0 ){
        throw new UnsupportedOperationException( "Read Only" );
    }

    
    public Object remove( int arg0 ){
        throw new UnsupportedOperationException( "Read Only" );
    }

    
    public boolean removeAll( Collection arg0 ){
        throw new UnsupportedOperationException( "Read Only" );
    }

    
    public boolean retainAll( Collection arg0 ){
        throw new UnsupportedOperationException( "Read Only" );
    }

    
    public Object set( int arg0 , Object arg1 ){
        throw new UnsupportedOperationException( "Read Only" );
    }

    
    public List subList( int arg0 , int arg1 ){
        throw new UnsupportedOperationException( "Not Supported" );
    }

    
    public Object[] toArray(){
        throw new UnsupportedOperationException( "Not Supported" );
    }

    
    public Object[] toArray( Object[] arg0 ){
        throw new UnsupportedOperationException( "Not Supported" );
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

    /**
     * Returns the object id or null if not set.
     * @param field The field to return
     * @return The field object value or null if not found (or if null :-^).
     */
    public ObjectId _obj_id( final String field ) {
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

 	public EntityList _list(String name) {
 		throw new RuntimeException("list will not support this method");
 	}


 	public long _long(String name) {
 		throw new RuntimeException("list will not support this method");
 	}

 	public long _long(String name, long default1) {
 		throw new RuntimeException("list will not support this method");
 	}

 	public MEntity _entity(String name) {
 		throw new RuntimeException("list will not support this method");
 	}


 	public String _string(String name) {
 		throw new RuntimeException("list will not support this method");
 	}
 	
 	public String _string(String name,String _default) {
 		throw new RuntimeException("list will not support this method");
 	}

 	
 	public <T> T get(String name,T t){
 		throw new RuntimeException("list will not support this method");
 	}
}
