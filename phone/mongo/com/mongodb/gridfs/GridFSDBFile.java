// GridFSDBFile.java

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

package com.mongodb.gridfs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bson.types.ObjectId;

import com.mongodb.MEntity;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.Entity;
import com.mongodb.EntityList;
import com.mongodb.MongoException;

/**
 * This class enables to retrieve a GridFS file metadata and content.
 * Operations include:
 * - writing data to a file on disk or an OutputStream
 * - getting each chunk as a byte array
 * - getting an InputStream to stream the data into
 * @author antoine
 */
public class GridFSDBFile extends GridFSFile {
    
    
    /**
     * Returns an InputStream from which data can be read
     * @return
     */
    public InputStream getInputStream(){
        return new MyInputStream();
    }

    /**
     * Writes the file's data to a file on disk
     * @param filename the file name on disk
     * @return
     * @throws IOException
     */
    public long writeTo( String filename ) throws IOException {
        return writeTo( new File( filename ) );
    }
    /**
     * Writes the file's data to a file on disk
     * @param f the File object
     * @return
     * @throws IOException
     */
    public long writeTo( File f ) throws IOException {
        return writeTo( new FileOutputStream( f ) );
    }

    /**
     * Writes the file's data to an OutputStream
     * @param out the OutputStream
     * @return
     * @throws IOException
     */
    public long writeTo( OutputStream out )
        throws IOException {
        final int nc = numChunks();
        for ( int i=0; i<nc; i++ ){
            out.write( getChunk( i ) );
        }
        return _length;
    }
    
    byte[] getChunk( int i ){
        if ( _fs == null )
            throw new RuntimeException( "no gridfs!" );
        
        Entity chunk = _fs._chunkCollection.findOne( BasicDBObjectBuilder.start( "files_id" , _id )
                                                       .add( "n" , i ).get() );
        if ( chunk == null )
            throw new MongoException( "can't find a chunk!  file id: " + _id + " chunk: " + i );

        return (byte[])chunk.get( "data" );
    }

    class MyInputStream extends InputStream {

        MyInputStream(){
            _numChunks = numChunks();
        }
        
        public int available(){
            if ( _data == null )
                return 0;
            return _data.length - _offset;
        }
        
        public void close(){
        }

        public void mark(int readlimit){
            throw new RuntimeException( "mark not supported" );
        }
        public void reset(){
            throw new RuntimeException( "mark not supported" );
        }
        public boolean markSupported(){
            return false;
        }

        public int read(){
            byte b[] = new byte[1];
            int res = read( b );
            if ( res < 0 )
                return -1;
            return b[0] & 0xFF;
        }
        
        public int read(byte[] b){
            return read( b , 0 , b.length );
        }
        public int read(byte[] b, int off, int len){
            
            if ( _data == null || _offset >= _data.length ){
                
                if ( _nextChunk >= _numChunks )
                    return -1;
                
                _data = getChunk( _nextChunk );
                _offset = 0;
                _nextChunk++;
            }

            int r = Math.min( len , _data.length - _offset );
            System.arraycopy( _data , _offset , b , off , r );
            _offset += r;
            return r;
        }

        final int _numChunks;

        int _nextChunk = 0;
        int _offset;
        byte[] _data = null;
    }
    
    void remove(){
        _fs._filesCollection.remove( new MEntity( "_id" , _id ) );
        _fs._chunkCollection.remove( new MEntity( "files_id" , _id ) );
    }

	
    /** Returns the value of a field as a boolean.
     * @param key the field to look up
     * @return the value of the field, or false if field does not exist
     */
    public boolean _bool( String key ){
    	throw new RuntimeException("db file will not support this method");
    }

    public boolean _bool( String key , boolean _default ){
    	throw new RuntimeException("db file list will not support this method");
    }

    /**
     * Returns the object id or null if not set.
     * @param field The field to return
     * @return The field object value or null if not found (or if null :-^).
     */
    public ObjectId _obj_id( final String field ) {
    	throw new RuntimeException("db file list will not support this method");
    }

 	public double _double(String name) {
 		throw new RuntimeException("db file list will not support this method");
 	}

 	public double _double(String name, double default1) {
 		throw new RuntimeException("db file list will not support this method");
 	}

 	public float _float(String name) {
 		throw new RuntimeException("db file list will not support this method");
 	}

 	public float _float(String name, float default1) {
 		throw new RuntimeException("db file list will not support this method");
 	}

 	public int _int(String name) {
 		throw new RuntimeException("db file list will not support this method");
 	}

 	public int _int(String name, int default1) {
 		throw new RuntimeException("db file list will not support this method");
 	}

 	public EntityList _list(String name) {
 		throw new RuntimeException("db file list will not support this method");
 	}


 	public long _long(String name) {
 		throw new RuntimeException("db file list will not support this method");
 	}

 	public long _long(String name, long default1) {
 		throw new RuntimeException("db file list will not support this method");
 	}

 	public MEntity _entity(String name) {
 		throw new RuntimeException("db file list will not support this method");
 	}


 	public String _string(String name) {
 		throw new RuntimeException("db file list will not support this method");
 	}
 	
 	public String _string(String name,String _default) {
 		throw new RuntimeException("db file list will not support this method");
 	}
 	
 	public <T> T get(String name,T t){
 		throw new RuntimeException("db file list will not support this method");
 	}

	
}
