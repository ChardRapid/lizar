// DB.java

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.mongodb.DBApiLayer.Result;
import com.mongodb.util.Util;

/**
 * an abstract class that represents a logical database on a server
 * @dochub databases
 */
public abstract class DB {

    /**
     * @param mongo the mongo instance
     * @param name the database name
     */
    public DB( Mongo mongo , String name ){
        _mongo = mongo;
    	_name = name;
        _options = new Bytes.OptionHolder( _mongo._netOptions );
    }

    /**
     * starts a new "consistent request".
     * Following this call and until requestDone() is called, all db operations should use the same underlying connection.
     * This is useful to ensure that operations happen in a certain order with predictable results.
     */
    public abstract void requestStart();

    /**
     * ends the current "consistent request"
     */
    public abstract void requestDone();

    /**
     * ensure that a connection is assigned to the current "consistent request" (from primary pool, if connected to a replica set)
     */
    public abstract void requestEnsureConnection();

    /**
     * Returns the collection represented by the string &lt;dbName&gt;.&lt;collectionName&gt;.
     * @param name the name of the collection
     * @return the collection
     */
    protected abstract DBCollection doGetCollection( String name );

    /**
     * Gets a collection with a given name.
     * If the collection does not exist, a new collection is created.
     * @param name the name of the collection to return
     * @return the collection
     */
    public final DBCollection getCollection( String name ){
        DBCollection c = doGetCollection( name );
        return c;
    }

    /**
     * Creates a collection with a given name and options.
     * If the collection does not exist, a new collection is created.
     * Note that if the options parameter is null, the creation will be deferred to when the collection is written to.
     * Possible options:
     * <dl>
     * <dt>capped</dt><dd><i>boolean</i>: if the collection is capped</dd>
     * <dt>size</dt><dd><i>int</i>: collection size (in bytes)</dd>
     * <dt>max</dt><dd><i>int</i>: max number of documents</dd>
     * </dl>
     * @param name the name of the collection to return
     * @param options options
     * @return the collection
     */
    public final DBCollection createCollection( String name, Entity options ){
        if ( options != null ){
            Entity createCmd = new MEntity("create", name);
            createCmd.putAll(options);
            CommandResult result = command(createCmd);
            result.throwOnError();
        }
        return getCollection(name);
    }


    /**
     * Returns a collection matching a given string.
     * @param s the name of the collection
     * @return the collection
     */
    public DBCollection getCollectionFromString( String s ){
        DBCollection foo = null;

        int idx = s.indexOf( "." );
        while ( idx >= 0 ){
            String b = s.substring( 0 , idx );
            s = s.substring( idx + 1 );
            if ( foo == null )
                foo = getCollection( b );
            else
                foo = foo.getCollection( b );
            idx = s.indexOf( "." );
        }

        if ( foo != null )
            return foo.getCollection( s );
        return getCollection( s );
    }

    /**
     * Executes a database command.
     * This method calls {@link DB#command(com.mongodb.Entity, int) } with 0 as query option.
     * @see <a href="http://mongodb.onconfluence.com/display/DOCS/List+of+Database+Commands">List of Commands</a>
     * @param cmd dbobject representing the command to execute
     * @return result of command from the database
     * @throws MongoException
     * @dochub commands
     */
    public CommandResult command( Entity cmd ) throws MongoException{
        return command( cmd, 0 );
    }

    /**
     * Executes a database command.
     * @see <a href="http://mongodb.onconfluence.com/display/DOCS/List+of+Database+Commands">List of Commands</a>
     * @param cmd dbobject representing the command to execute
     * @param options query options to use
     * @param readPrefs ReadPreferences for this command (nodes selection is the biggest part of this)
     * @return result of command from the database
     * @dochub commands
     * @throws MongoException
     */
    public CommandResult command( Entity cmd , int options, ReadPreference readPrefs )
        throws MongoException {

        Iterator<Entity> i = getCollection("$cmd").__find(cmd, new MEntity(), 0, -1, 0, options, readPrefs , DefaultDBDecoder.FACTORY.create());
        if ( i == null || ! i.hasNext() )
            return null;

        Entity res = i.next();
        ServerAddress sa = (i instanceof Result) ? ((Result) i).getServerAddress() : null;
        CommandResult cr = new CommandResult(sa);
        cr.putAll( res );
        cr._cmd = cmd;
        return cr;
    }

    /**
     * Executes a database command.
     * @see <a href="http://mongodb.onconfluence.com/display/DOCS/List+of+Database+Commands">List of Commands</a>
     * @param cmd dbobject representing the command to execute
     * @param options query options to use
     * @return result of command from the database
     * @dochub commands
     * @throws MongoException
     */
    public CommandResult command( Entity cmd , int options )
        throws MongoException {
	return command(cmd, options, null);
    }
    /**
     * Executes a database command.
     * This method constructs a simple dbobject and calls {@link DB#command(com.mongodb.Entity) }
     * @see <a href="http://mongodb.onconfluence.com/display/DOCS/List+of+Database+Commands">List of Commands</a>
     * @param cmd command to execute
     * @return result of command from the database
     * @throws MongoException
     */
    public CommandResult command( String cmd )
        throws MongoException {
        return command( new MEntity( cmd , Boolean.TRUE ) );
    }

    /**
     * Executes a database command.
     * This method constructs a simple dbobject and calls {@link DB#command(com.mongodb.Entity, int)  }
     * @see <a href="http://mongodb.onconfluence.com/display/DOCS/List+of+Database+Commands">List of Commands</a>
     * @param cmd command to execute
     * @param options query options to use
     * @return result of command from the database
     * @throws MongoException
     */
    public CommandResult command( String cmd, int options  )
        throws MongoException {
        return command( new MEntity( cmd , Boolean.TRUE ), options );
    }

    /**
     * evaluates a function on the database.
     * This is useful if you need to touch a lot of data lightly, in which case network transfer could be a bottleneck.
     * @param code the function in javascript code
     * @param args arguments to be passed to the function
     * @return The command result
     * @throws MongoException
     */
    public CommandResult doEval( String code , Object ... args )
        throws MongoException {

        return command( BasicDBObjectBuilder.start()
                        .add( "$eval" , code )
                        .add( "args" , args )
                        .get() );
    }

    /**
     * calls {@link DB#doEval(java.lang.String, java.lang.Object[]) }.
     * If the command is successful, the "retval" field is extracted and returned.
     * Otherwise an exception is thrown.
     * @param code the function in javascript code
     * @param args arguments to be passed to the function
     * @return The object
     * @throws MongoException
     */
    public Object eval( String code , Object ... args )
        throws MongoException {

        CommandResult res = doEval( code , args );
        res.throwOnError();
        return res.get( "retval" );
    }

    /**
     * Returns the result of "dbstats" command
     * @return
     */
    public CommandResult getStats() {
        return command("dbstats");
    }

    /**
     * Returns the name of this database.
     * @return the name
     */
    public String getName(){
	return _name;
    }

    /**
     * Makes this database read-only.
     * Important note: this is a convenience setting that is only known on the client side and not persisted.
     * @param b if the database should be read-only
     */
    public void setReadOnly( Boolean b ){
        _readOnly = b;
    }

    /**
     * Returns a set containing the names of all collections in this database.
     * @return the names of collections in this database
     * @throws MongoException
     */
    public Set<String> getCollectionNames()
        throws MongoException {

        DBCollection namespaces = getCollection("system.namespaces");
        if (namespaces == null)
            throw new RuntimeException("this is impossible");

        // TODO - Is ReadPreference OK for collection Names?
        Iterator<Entity> i = namespaces.__find(new MEntity(), null, 0, 0, 0, getOptions(), null, null);
        if (i == null)
            return new HashSet<String>();

        List<String> tables = new ArrayList<String>();

        for (; i.hasNext();) {
            Entity o = i.next();
            if ( o.get( "name" ) == null ){
                throw new MongoException( "how is name null : " + o );
            }
            String n = o.get("name").toString();
            int idx = n.indexOf(".");

            String root = n.substring(0, idx);
            if (!root.equals(_name))
                continue;

            if (n.indexOf("$") >= 0)
                continue;

            String table = n.substring(idx + 1);

            tables.add(table);
        }

        Collections.sort(tables);

        return new LinkedHashSet<String>(tables);
    }

    /**
     * Checks to see if a collection by name %lt;name&gt; exists.
     * @param collectionName The collection to test for existence
     * @return false if no collection by that name exists, true if a match to an existing collection was found
     */
    public boolean collectionExists(String collectionName)
    {
        if (collectionName == null || "".equals(collectionName))
            return false;

        Set<String> collections = getCollectionNames();
        if (collections.isEmpty())
            return false;

        for (String collection : collections)
        {
            if (collectionName.equalsIgnoreCase(collection))
                return true;
        }

        return false;
    }


    /**
     * Returns the name of this database.
     * @return the name
     */
    
    public String toString(){
        return _name;
    }

    /**
     * Gets the the error (if there is one) from the previous operation on this connection.
     * The result of this command will look like
     *
     * <pre>
     * { "err" :  errorMessage  , "ok" : 1.0 }
     * </pre>
     *
     * The value for errorMessage will be null if no error occurred, or a description otherwise.
     *
     * Important note: when calling this method directly, it is undefined which connection "getLastError" is called on.
     * You may need to explicitly use a "consistent Request", see {@link DB#requestStart()}
     * For most purposes it is better not to call this method directly but instead use {@link WriteConcern}
     *
     * @return DBObject with error and status information
     * @throws MongoException
     */
    public CommandResult getLastError()
        throws MongoException {
        return command(new MEntity("getlasterror", 1));
    }

    /**
     * @see {@link DB#getLastError() }
     * @param concern the concern associated with "getLastError" call
     * @return
     * @throws MongoException
     */
    public CommandResult getLastError( com.mongodb.WriteConcern concern )
        throws MongoException {
        return command( concern.getCommand() );
    }

    /**
     * @see {@link DB#getLastError(com.mongodb.WriteConcern) }
     * @param w
     * @param wtimeout
     * @param fsync
     * @return The command result
     * @throws MongoException
     */
    public CommandResult getLastError( int w , int wtimeout , boolean fsync )
        throws MongoException {
        return command( (new com.mongodb.WriteConcern( w, wtimeout , fsync )).getCommand() );
    }


    /**
     * Sets the write concern for this database. It Will be used for
     * writes to any collection in this database. See the
     * documentation for {@link WriteConcern} for more information.
     * @param concern write concern to use
     */
    public void setWriteConcern( com.mongodb.WriteConcern concern ){
        if (concern == null) throw new IllegalArgumentException();
        _concern = concern;
    }

    /**
     * Gets the write concern for this database.
     * @return
     */
    public com.mongodb.WriteConcern getWriteConcern(){
        if ( _concern != null )
            return _concern;
        return _mongo.getWriteConcern();
    }

    /**
     * Sets the read preference for this database. Will be used as default for
     * reads from any collection in this database. See the
     * documentation for {@link ReadPreference} for more information.
     *
     * @param preference Read Preference to use
     */
    public void setReadPreference( ReadPreference preference ){
        _readPref = preference;
    }

    /**
     * Gets the default read preference
     * @return
     */
    public ReadPreference getReadPreference(){
        if ( _readPref != null )
            return _readPref;
        return _mongo.getReadPreference();
    }

    /**
     * Drops this database. Removes all data on disk. Use with caution.
     * @throws MongoException
     */
    public void dropDatabase()
        throws MongoException {

        CommandResult res = command(new MEntity("dropDatabase", 1));
        res.throwOnError();
        _mongo._dbs.remove(this.getName());
    }

    /**
     * Returns true if a user has been authenticated
     *
     * @return true if authenticated, false otherwise
     * @dochub authenticate
     */
    public boolean isAuthenticated() {
	return ( _username != null );
    }

    /**
     *  Authenticates to db with the given name and password
     *
     * @param username name of user for this database
     * @param passwd password of user for this database
     * @return true if authenticated, false otherwise
     * @throws MongoException
     * @dochub authenticate
     */
    public boolean authenticate(String username, char[] passwd )
        throws MongoException {

        if ( username == null || passwd == null )
            throw new NullPointerException( "username can't be null" );

        if ( _username != null )
	    throw new IllegalStateException( "can't call authenticate twice on the same DBObject" );

        String hash = _hash( username , passwd );
        CommandResult res = _doauth( username , hash.getBytes() );
        if ( !res.ok())
            return false;
        _username = username;
        _authhash = hash.getBytes();
        return true;
    }

    /**
     *  Authenticates to db with the given name and password
     *
     * @param username name of user for this database
     * @param passwd password of user for this database
     * @return the CommandResult from authenticate command
     * @throws MongoException if authentication failed due to invalid user/pass, or other exceptions like I/O
     * @dochub authenticate
     */
    public CommandResult authenticateCommand(String username, char[] passwd )
        throws MongoException {

        if ( username == null || passwd == null )
            throw new NullPointerException( "username can't be null" );

        if ( _username != null )
	    throw new IllegalStateException( "can't call authenticate twice on the same DBObject" );

        String hash = _hash( username , passwd );
        CommandResult res = _doauth( username , hash.getBytes() );
        res.throwOnError();
        _username = username;
        _authhash = hash.getBytes();
        return res;
    }

    /*
    boolean reauth(){
        if ( _username == null || _authhash == null )
            throw new IllegalStateException( "no auth info!" );
        return _doauth( _username , _authhash );
    }
    */

    Entity _authCommand( String nonce ){
        if ( _username == null || _authhash == null )
            throw new IllegalStateException( "no auth info!" );

        return _authCommand( nonce , _username , _authhash );
    }

    static Entity _authCommand( String nonce , String username , byte[] hash ){
        String key = nonce + username + new String( hash );

        MEntity cmd = new MEntity();

        cmd.put("authenticate", 1);
        cmd.put("user", username);
        cmd.put("nonce", nonce);
        cmd.put("key", Util.hexMD5(key.getBytes()));

        return cmd;
    }

    private CommandResult _doauth( String username , byte[] hash ){
        CommandResult res = command(new MEntity("getnonce", 1));
        res.throwOnError();

        Entity cmd = _authCommand( res._string( "nonce" ) , username , hash );
        return command(cmd);
    }

    /**
     * Adds a new user for this db
     * @param username
     * @param passwd
     */
    public WriteResult addUser( String username , char[] passwd ){
        return addUser(username, passwd, false);
    }

    /**
     * Adds a new user for this db
     * @param username
     * @param passwd
     * @param readOnly if true, user will only be able to read
     */
    public WriteResult addUser( String username , char[] passwd, boolean readOnly ){
        DBCollection c = getCollection( "system.users" );
        Entity o = c.findOne( new MEntity( "user" , username ) );
        if ( o == null )
            o = new MEntity( "user" , username );
        o.put( "pwd" , _hash( username , passwd ) );
        o.put( "readOnly" , readOnly );
        return c.save( o );
    }

    /**
     * Removes a user for this db
     * @param username
     */
    public WriteResult removeUser( String username ){
        DBCollection c = getCollection( "system.users" );
        return c.remove(new MEntity( "user" , username ));
    }

    String _hash( String username , char[] passwd ){
        ByteArrayOutputStream bout = new ByteArrayOutputStream( username.length() + 20 + passwd.length );
        try {
            bout.write( username.getBytes() );
            bout.write( ":mongo:".getBytes() );
            for ( int i=0; i<passwd.length; i++ ){
                if ( passwd[i] >= 128 )
                    throw new IllegalArgumentException( "can't handle non-ascii passwords yet" );
                bout.write( (byte)passwd[i] );
            }
        }
        catch ( IOException ioe ){
            throw new RuntimeException( "impossible" , ioe );
        }
        return Util.hexMD5( bout.toByteArray() );
    }

    /**
     *  Returns the last error that occurred since start of database or a call to <code>resetError()</code>
     *
     *  The return object will look like
     *
     *  <pre>
     * { err : errorMessage, nPrev : countOpsBack, ok : 1 }
     *  </pre>
     *
     * The value for errorMessage will be null of no error has occurred, otherwise the error message.
     * The value of countOpsBack will be the number of operations since the error occurred.
     *
     * Care must be taken to ensure that calls to getPreviousError go to the same connection as that
     * of the previous operation.
     * See {@link DB#requestStart()} for more information.
     *
     * @return DBObject with error and status information
     * @throws MongoException
     */
    public CommandResult getPreviousError()
        throws MongoException {
        return command(new MEntity("getpreverror", 1));
    }

    /**
     * Resets the error memory for this database.
     * Used to clear all errors such that {@link DB#getPreviousError()} will return no error.
     * @throws MongoException
     */
    public void resetError()
        throws MongoException {
        command(new MEntity("reseterror", 1));
    }

    /**
     * For testing purposes only - this method forces an error to help test error handling
     * @throws MongoException
     */
    public void forceError()
        throws MongoException {
        command(new MEntity("forceerror", 1));
    }

    /**
     * Gets the Mongo instance
     * @return
     */
    public Mongo getMongo(){
        return _mongo;
    }

    /**
     * Gets another database on same server
     * @param name name of the database
     * @return
     */
    public DB getSisterDB( String name ){
        return _mongo.getDB( name );
    }

    /**
     * Makes it possible to execute "read" queries on a slave node
     *
     * @deprecated Replaced with ReadPreference.SECONDARY
     * @see com.mongodb.ReadPreference.SECONDARY
     */
    @Deprecated
    public void slaveOk(){
        addOption( Bytes.QUERYOPTION_SLAVEOK );
    }

    /**
     * Adds the give option
     * @param option
     */
    public void addOption( int option ){
        _options.add( option );
    }

    /**
     * Sets the query options
     * @param options
     */
    public void setOptions( int options ){
        _options.set( options );
    }

    /**
     * Resets the query options
     */
    public void resetOptions(){
        _options.reset();
    }

    /**
     * Gets the query options
     * @return
     */
    public int getOptions(){
        return _options.get();
    }

    public abstract void cleanCursors( boolean force ) throws MongoException;


    final Mongo _mongo;
    final String _name;

    protected boolean _readOnly = false;
    private com.mongodb.WriteConcern _concern;
    private com.mongodb.ReadPreference _readPref;
    final Bytes.OptionHolder _options;

    String _username;
    byte[] _authhash = null;

}
