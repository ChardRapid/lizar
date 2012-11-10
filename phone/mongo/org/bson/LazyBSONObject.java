/**
 *      Copyright (C) 2008-2011 10gen Inc.
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
package org.bson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bson.io.BSONByteBuffer;
import org.bson.types.BSONTimestamp;
import org.bson.types.Code;
import org.bson.types.CodeWScope;
import org.bson.types.MaxKey;
import org.bson.types.MinKey;
import org.bson.types.ObjectId;
import org.bson.types.Symbol;

import com.mongodb.MEntity;
import com.mongodb.EntityList;

/**
 * @author antoine
 * @author brendan
 * @author scotthernandez
 * @author Kilroy Wuz Here
 */
public class LazyBSONObject implements BSONObject {

    public LazyBSONObject( byte[] data, LazyBSONCallback callback ){
        this( BSONByteBuffer.wrap( data ), callback );
    }

    public LazyBSONObject( byte[] data, int offset, LazyBSONCallback callback ){
        this( BSONByteBuffer.wrap( data, offset, data.length - offset ), offset, callback );
    }

    public LazyBSONObject( BSONByteBuffer buffer, LazyBSONCallback callback ){
        this( buffer, 0, callback );
    }

    public LazyBSONObject( BSONByteBuffer buffer, int offset, LazyBSONCallback callback ){
        _callback = callback;
        _input = buffer;
        _doc_start_offset = offset;
    }


    class ElementRecord {
        ElementRecord( final String name, final int offset ){
            this.name = name;
            this.offset = offset;
            this.type = getElementType( offset - 1 );
            this.fieldNameSize = sizeCString( offset ) + 1;
            this.valueOffset = offset + fieldNameSize;
        }

        final String name;
        /**
         * The offset the record begins at.
         */
        final byte type;
        final int fieldNameSize;
        final int valueOffset;
        final int offset;
    }

    public class LazyBSONIterator implements Iterator<String> {

        public boolean hasNext(){
            return !isElementEmpty( offset );
        }

        public String next(){
            int fieldSize = sizeCString( offset );
            int elementSize = getElementBSONSize( offset++ );
            String key = _input.getCString( offset );
            offset += ( fieldSize + elementSize );
            return key;
        }

        public void remove(){
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        int offset = _doc_start_offset + FIRST_ELMT_OFFSET;
    }

    public class LazyBSONKeySet implements Set<String> {

        public int size(){
            int size = 0;
            for ( String key : this ){
                ++size;
            }
            return size;
        }

        public boolean isEmpty(){
            return LazyBSONObject.this.isEmpty();
        }

        public boolean contains( Object o ){
            for ( String key : this ){
                if ( key.equals( o ) ){
                    return true;
                }
            }
            return false;
        }

        public Iterator<String> iterator(){
            return new LazyBSONIterator();
        }

        public Object[] toArray(){
            String[] array = new String[size()];
            return toArray( array );
        }

        @SuppressWarnings( "unchecked" )
        public <T> T[] toArray( T[] ts ){
            int i = 0;
            for ( String key : this ){
                ts[++i] = (T) key;
            }
            return ts;
        }

        public boolean add( String e ){
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public boolean remove( Object o ){
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public boolean containsAll( Collection<?> clctn ){
            for ( Object item : clctn ){
                if ( !contains( item ) ){
                    return false;
                }
            }
            return true;
        }

        public boolean addAll( Collection<? extends String> clctn ){
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public boolean retainAll( Collection<?> clctn ){
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public boolean removeAll( Collection<?> clctn ){
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public void clear(){
            throw new UnsupportedOperationException( "Not supported yet." );
        }
    }

    public Object put( String key, Object v ){
        throw new UnsupportedOperationException( "Object is read only" );
    }

    public void putAll( BSONObject o ){
        throw new UnsupportedOperationException( "Object is read only" );
    }

    public void putAll( Map m ){
        throw new UnsupportedOperationException( "Object is read only" );
    }

    public Object get( String key ){
        //get element up to the key
        ArrayList<ElementRecord> elements = getElementsToKey( key );
        
        //no found if null/empty
        if (elements == null || elements.size() == 0)
            return null;
        
        //get last to see if it is what we want; if it isn't then what we are looking for isn't there
        ElementRecord lastRec = elements.get( elements.size() - 1 );
        return ( lastRec.name.equals( key ) ) ? getElementValue( lastRec ) : null;
        
    }
    
    /**
     * returns all the ElementRecords to the point of the key; if no key is specified or found then a full list is returned.
     * @param key the field/key to stop at
     * @return all the ElementRecords to the point of the key (must be the last element returned)
     */
    ArrayList<ElementRecord> getElementsToKey(String key){
        int offset = _doc_start_offset + FIRST_ELMT_OFFSET;
        ArrayList<ElementRecord> elements = new ArrayList<LazyBSONObject.ElementRecord>();
 
        while ( !isElementEmpty( offset ) ){
            int fieldSize = sizeCString( offset );
            int elementSize = getElementBSONSize( offset++ );
            String name = _input.getCString( offset );
            final ElementRecord rec = new ElementRecord( name, offset );
            elements.add( rec );
            if ( name.equals( key ) ){
                //found it, break.
                break;
            }
            offset += ( fieldSize + elementSize );
        }

        return elements;
    }

    public Map toMap(){
        throw new UnsupportedOperationException( "Not Supported" );
    }

    public Object removeField( String key ){
        throw new UnsupportedOperationException( "Object is read only" );
    }

    @Deprecated
    public boolean containsKey( String s ){
        return containsField( s );
    }

    public boolean containsField( String s ){
        return keySet().contains( s );
    }

    public Set<String> keySet(){
        return new LazyBSONKeySet();
    }

    protected boolean isElementEmpty( int offset ){
        return getElementType( offset ) == BSON.EOO;
    }

    public boolean isEmpty(){
        return isElementEmpty( _doc_start_offset + FIRST_ELMT_OFFSET );
    }

    private int getBSONSize( final int offset ){
        return _input.getInt( offset );
    }

    public int getBSONSize(){
        return _input.getInt( _doc_start_offset );
    }

    private String getElementFieldName( final int offset ){
        return _input.getCString( offset );
    }

    protected byte getElementType( final int offset ){
        return _input.get( offset );
    }

    protected int getElementBSONSize( int offset ){
        int x = 0;
        byte type = getElementType( offset++ );
        int n = sizeCString( offset++ );
        int valueOffset = offset + n;
        switch ( type ){
            case BSON.EOO:
            case BSON.UNDEFINED:
            case BSON.NULL:
            case BSON.MAXKEY:
            case BSON.MINKEY:
                break;
            case BSON.BOOLEAN:
                x = 1;
                break;
            case BSON.NUMBER_INT:
                x = 4;
                break;
            case BSON.TIMESTAMP:
            case BSON.DATE:
            case BSON.NUMBER_LONG:
            case BSON.NUMBER:
                x = 8;
                break;
            case BSON.OID:
                x = 12;
                break;
            case BSON.SYMBOL:
            case BSON.CODE:
            case BSON.STRING:
                x = _input.getInt( valueOffset ) + 4;
                break;
            case BSON.CODE_W_SCOPE:
                x = _input.getInt( valueOffset );
                break;
            case BSON.REF:
                x = _input.getInt( valueOffset ) + 4 + 12;
                break;
            case BSON.OBJECT:
            case BSON.ARRAY:
                x = _input.getInt( valueOffset );
                break;
            case BSON.BINARY:
                x = _input.getInt( valueOffset ) + 4 + 1/*subtype*/;
                break;
            case BSON.REGEX:
                // 2 cstrs
                int part1 = sizeCString( valueOffset ) + 1;
                int part2 = sizeCString( valueOffset + part1 ) + 1;
                x = part1 + part2 + 4;
                break;
            default:
                throw new BSONException( "Invalid type " + type + " for field " + getElementFieldName( offset ) );
        }
        return x;
    }


    protected int sizeCString( int offset ){
        offset += 1;
        int end = offset;
        while ( true ){
            byte b = _input.get( end );
            if ( b == 0 )
                break;
            else
                end++;
        }
        return end - offset + 1;
    }

    protected Object getElementValue( ElementRecord record ){
        switch ( record.type ){
            case BSON.EOO:
            case BSON.UNDEFINED:
            case BSON.NULL:
                return null;
            case BSON.MAXKEY:
                return new MaxKey();
            case BSON.MINKEY:
                return new MinKey();
            case BSON.BOOLEAN:
                return ( _input.get( record.valueOffset ) != 0 );
            case BSON.NUMBER_INT:
                return _input.getInt( record.valueOffset );
            case BSON.TIMESTAMP:
                int inc = _input.getInt( record.valueOffset );
                int time = _input.getInt( record.valueOffset + 4 );
                return new BSONTimestamp( time, inc );
            case BSON.DATE:
                return new Date( _input.getLong( record.valueOffset ) );
            case BSON.NUMBER_LONG:
                return _input.getLong( record.valueOffset );
            case BSON.NUMBER:
                return Double.longBitsToDouble( _input.getLong( record.valueOffset ) );
            case BSON.OID:
                return new ObjectId( _input.getIntBE( record.valueOffset ),
                                     _input.getIntBE( record.valueOffset + 4 ),
                                     _input.getIntBE( record.valueOffset + 8 ) );
            case BSON.SYMBOL:
                return new Symbol( _input.getUTF8String( record.valueOffset ) );
            case BSON.CODE:
                return new Code( _input.getUTF8String( record.valueOffset ) );
            case BSON.STRING:
                return _input.getUTF8String( record.valueOffset );
            case BSON.CODE_W_SCOPE:
                int strsize = _input.getInt( record.valueOffset + 4 );
                String code = _input.getUTF8String( record.valueOffset + 4 );
                BSONObject scope =
                        (BSONObject) _callback.createObject( _input.array(), record.valueOffset + 4 + 4 + strsize );
                return new CodeWScope( code, scope );
            case BSON.REF:
                int csize = _input.getInt( record.valueOffset );
                String ns = _input.getCString( record.valueOffset + 4 );
                int oidOffset = record.valueOffset + csize + 4;
                ObjectId oid = new ObjectId( _input.getIntBE( oidOffset ),
                                             _input.getIntBE( oidOffset + 4 ),
                                             _input.getIntBE( oidOffset + 8 ) );
                return _callback.createDBRef( ns, oid );
            case BSON.OBJECT:
                return _callback.createObject( _input.array(), record.valueOffset );
            case BSON.ARRAY:
                return _callback.createArray( _input.array(), record.valueOffset );
            case BSON.BINARY:
                return readBinary( record.valueOffset );
            case BSON.REGEX:
                int n = sizeCString( record.valueOffset );
                String pattern = _input.getCString( record.valueOffset );
                String flags = _input.getCString( record.valueOffset + n );
                return Pattern.compile( pattern, BSON.regexFlags( flags ) );
            default:
                throw new BSONException(
                        "Invalid type " + record.type + " for field " + getElementFieldName( record.offset ) );
        }
    }

    private Object readBinary( int valueOffset ){
        final int totalLen = _input.getInt( valueOffset );
        valueOffset += 4;
        final byte bType = _input.get( valueOffset );
        valueOffset += 1;

        byte[] bin;
        switch ( bType ){
            case BSON.B_GENERAL:{
                bin = new byte[totalLen];
                for ( int n = 0; n < totalLen; n++ ){
                    bin[n] = _input.get( valueOffset + n );
                }
                return bin;
            }
            case BSON.B_BINARY:
                final int len = _input.getInt( valueOffset );
                if ( len + 4 != totalLen )
                    throw new IllegalArgumentException(
                            "Bad Data Size; Binary Subtype 2.  { actual len: " + len + " expected totalLen: " + totalLen
                            + "}" );
                valueOffset += 4;
                bin = new byte[len];
                for ( int n = 0; n < len; n++ ){
                    bin[n] = _input.get( valueOffset + n );
                }
                return bin;
            case BSON.B_UUID:
                if ( totalLen != 16 )
                    throw new IllegalArgumentException(
                            "Bad Data Size; Binary Subtype 3 (UUID). { total length: " + totalLen + " != 16" );

                long part1 = _input.getLong( valueOffset );
                valueOffset += 8;
                long part2 = _input.getLong( valueOffset );
                return new UUID( part1, part2 );
        }

        bin = new byte[totalLen];
        for ( int n = 0; n < totalLen; n++ ){
            bin[n] = _input.get( valueOffset + n );
        }
        return bin;
    }

    /**
     * Returns a JSON serialization of this object
     *
     * @return JSON serialization
     */
    public String toString(){
        return com.mongodb.util.JSON.serialize( this );
    }

    /**
     * In a "normal" (aka not embedded) doc, this will be the offset of the first element.
     *
     * In an embedded doc because we use ByteBuffers to avoid unecessary copying the offset must be manually set in
     * _doc_start_offset
     */
    final static int FIRST_ELMT_OFFSET = 4;

    protected final int _doc_start_offset;

    protected final BSONByteBuffer _input; // TODO - Guard this with synchronicity?
    // callback is kept to create sub-objects on the fly
    protected final LazyBSONCallback _callback;
    private static final Logger log = Logger.getLogger( "org.bson.LazyBSONObject" );
    
    
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

    /**
     * Returns the object id or null if not set.
     * @param field The field to return
     * @return The field object value or null if not found (or if null :-^).
     */
    public ObjectId _obj_id( final String field ) {
        Object obj = get( field );
        return (obj != null) ? (ObjectId)obj : null;
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

	public EntityList _list(String name) {
		Object o=get(name);
		if(o!=null) return(EntityList) o;
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

	public MEntity _entity(String name) {
		Object o=get(name);
		if(o!=null)return (MEntity)o;
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
}
