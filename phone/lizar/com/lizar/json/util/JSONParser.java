// JSON.java

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

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;

import com.lizar.json.JList;
import com.lizar.json.JObject;
import com.lizar.json.JSON;
import com.lizar.util.StringHelper;

/**
 *   Helper methods for JSON serialization and de-serialization
 */
public class JSONParser {

    /**
     *  Serializes an object into it's JSON form
     *
     * @param o object to serialize
     * @return  String containing JSON form of the object
     */
    public static String serialize( Object o ){
        StringBuilder buf = new StringBuilder();
        serialize( o , buf );
        return buf.toString();
    }

    public static String beautifier_serialize( Object o ){
        StringBuilder buf = new StringBuilder();
        beautifier_serialize( o , buf ,-1);
        return buf.toString();
    }
    
    static void string( StringBuilder a , String s ){
        a.append("\"");
        for(int i = 0; i < s.length(); ++i){
            char c = s.charAt(i);
            if (c == '\\')
                a.append("\\\\");
            else if(c == '"')
                a.append("\\\"");
            else if(c == '\n')
                a.append("\\n");
            else if(c == '\r')
                a.append("\\r");
            else if(c == '\t')
                a.append("\\t");
            else if(c == '\b')
                a.append("\\b");
            else if ( c < 32 )
                continue;
            else
                a.append(c);
        }
        a.append("\"");
    }

    @SuppressWarnings("unchecked")
    public static void serialize( Object o , StringBuilder buf ){
        
        if ( o == null ){
            buf.append( " null " );
            return;
        }
        
        if ( o instanceof Number ){
            buf.append( o );
            return;
        }
        
        if ( o instanceof String ){
            string( buf , o.toString() );
            return;
        }

        if ( o instanceof Iterable){

            boolean first = true;
            buf.append( "[" );
            
            for ( Object n : (Iterable)o ){
                if ( first ) first = false;
                else buf.append( " , " );
                
                serialize( n , buf );
            }
            
            buf.append( "]" );
            return;
        }
        if ( o instanceof JSON){
 
            boolean first = true;
            buf.append( "{ " );
            
            JSON dbo = (JSON)o;
            
            for ( String name : dbo.keySet() ){
                if ( first ) first = false;
                else buf.append( " , " );
                
                string( buf , name );
                buf.append( " : " );
                serialize( dbo.get( name ) , buf );
            }
            
            buf.append( "}" );
            return;
        }

        if ( o instanceof Map ){
 
            boolean first = true;
            buf.append( "{ " );
            
            Map m = (Map)o;

            for ( Map.Entry entry : (Set<Map.Entry>)m.entrySet() ){
                if ( first ) first = false;
                else buf.append( " , " );
                
                string( buf , entry.getKey().toString() );
                buf.append( " : " );
                serialize( entry.getValue() , buf );
            }
            
            buf.append( "}" );
            return;
        }


        if (o instanceof Date) {
            Date d = (Date) o;
            SimpleDateFormat format = 
		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            format.setCalendar(new GregorianCalendar(new SimpleTimeZone(0, "GMT")));
	    serialize(new JObject("$date", format.format(d)), buf);
            return;
        }


        if (o instanceof Boolean) {
            buf.append(o);
            return;
        }


        if ( o.getClass().isArray() ){
            buf.append( "[" );
            
            for ( int i=0; i<Array.getLength( o ); i++) {
                if ( i > 0 ) buf.append( " , " );
                serialize( Array.get( o , i ) , buf );
            }
            
            buf.append( "]" );
            return;
        }

       
        
        throw new RuntimeException( "json can't serialize obj : "+o.getClass()+":" + o.toString() );
    }

    
    private static String append_t(int level){
    	if(level<=0)return"\n\t";
    	StringBuilder result=new StringBuilder("\n\t");
    	for(int i=0;i<level;i++){
    		result.append("\t");
    	}
    	return result.toString();
    }

 public static void beautifier_serialize( Object o , StringBuilder buf ,int level){
        int start_level;
        if ( o == null ){
            buf.append( "null" );
            return;
        }
        
        if ( o instanceof Number ){
            buf.append( o );
            return;
        }
        
        if ( o instanceof String ){
            string( buf , o.toString() );
            return;
        }

        if ( o instanceof Iterable){
        	int i=0;
        	start_level=level;
            level++;
            buf.append( "[" ).append(append_t(level));
            for ( Object n : (Iterable)o ){
                beautifier_serialize( n , buf,level );
                i++;
                buf.append(",").append(append_t(level));
            }
            if(i>=1)buf.replace(buf.length()-append_t(level).length()-1, buf.length(), "");
            level--;
            buf.append(append_t(start_level)).append( "]" );
            return;
        }
        if ( o instanceof JSON){
           int i=0;
           boolean need_step=true;
           start_level=level;
           level++;
            buf.append( "{ " ).append(append_t(level));
            JSON dbo = (JSON)o;
            for ( String name : dbo.keySet() ){
                string( buf , name );
                buf.append( " : " );
                Object value=dbo.get( name ) ;
                if(value!=null&&value instanceof JSON)buf.append(append_t(level));
                beautifier_serialize(value , buf,level );
                i++;
                if(i!=dbo.toMap().size()) buf.append( "," );
                else{
                	 if(value!=null&&value instanceof JSON)need_step=false;
                }
                buf.append(append_t(level));
            }
            level--;
            if(need_step) buf.append(append_t(start_level));
            else buf.append("");
            buf.append( "}" );
            return;
        }

        if ( o instanceof Map ){
        	int i=0;
        	 boolean need_step=true;
        	 start_level=level;
            level++;
            buf.append( "{ " ).append(append_t(level));
            Map m = (Map)o;
            for ( Map.Entry entry : (Set<Map.Entry>)m.entrySet() ){
                string( buf , entry.getKey().toString() );
                buf.append( " : " );
                beautifier_serialize(entry.getValue() , buf,level );
                i++;
                if(i!=m.size()) buf.append( "," );
                else{
                	 if(entry.getValue() !=null&&entry.getValue()  instanceof JSON)need_step=false;
                }
            }
            level--;
            if(need_step) buf.append(append_t(start_level));
            else buf.append("");
            buf.append( "}" );
            return;
        }


        if (o instanceof Date) {
            Date d = (Date) o;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            format.setCalendar(new GregorianCalendar(new SimpleTimeZone(0, "GMT")));
            beautifier_serialize(new JObject("$date", format.format(d)), buf,level);
            return;
        }


        if (o instanceof Boolean) {
            buf.append(o);
            return;
	}

        if ( o.getClass().isArray() ){
        	int j=0;
        	 start_level=level;
        	 level++;
            buf.append( "[" ).append(append_t(level));
            for ( int i=0; i<Array.getLength( o ); i++) {
                beautifier_serialize( Array.get( o , i ) , buf ,level);
                i++;
                if(i!=Array.getLength( o )) buf.append( " , " );
            }
            level--;
            buf.append(append_t(start_level)).append( "]" );
            return;
        }

        throw new RuntimeException( "json can't serialize obj : "+o.getClass()+":" + o.toString() );
    }

    /**
     *  Parses a JSON string representing a JSON value
     *
     * @param s the string to parse
     * @return the object
     */
    public static Object parse( String s ){
	return parse( s, null );
    }
    
    public static void main(String[] args){
    	System.out.println(JSONParser.parse("{ 		}"));
    }

    /**
     * Parses a JSON string representing a JSON value
     *
     * @param s the string to parse
     * @return the object
     */
    public static Object parse( String s, JSONCallback c ){
        if (s == null || (s=s.trim()).equals("")) {
            return (JSON)null;
        }
        if(StringHelper.equals(s.replace("\n", "").replace("\t", ""), "{}"))return new JObject();
        if(StringHelper.equals(s.replace("\n", "").replace("\t", ""), "[]"))return new JList();
        JSONCoreParser p = new JSONCoreParser(s, c);
        return p.parse();
    }

}


/**
 * Parser for JSON objects.
 *
 * Supports all types described at www.json.org, except for
 * numbers with "e" or "E" in them.
 */
class JSONCoreParser {

    String s;
    int pos = 0;
    JSONCallback _callback;

    /**
     * Create a new parser.
     */
    public JSONCoreParser(String s) {
	this(s, null);
    }

    /**
     * Create a new parser.
     */
    public JSONCoreParser(String s, JSONCallback callback) {
        this.s = s;
	_callback = (callback == null) ? new JSONCallback() : callback;
    }


    /**
     * Parse an unknown type.
     *
     * @return Object the next item
     * @throws JSONParseException if invalid JSON is found
     */
    public Object parse() {
	return parse(null);
    }

    /**
     * Parse an unknown type.
     *
     * @return Object the next item
     * @throws JSONParseException if invalid JSON is found
     */
    protected Object parse(String name) {
        Object value = null;
        char current = get();

        switch(current) {
        // null
        case 'n':
            read('n'); read('u'); read('l'); read('l');
	    value = null;
            break;
        // NaN
        case 'N':
            read('N'); read('a'); read('N');
	    value = Double.NaN;
            break;
        // true
        case 't':
            read('t'); read('r'); read('u'); read('e');
            value = true;
            break;
        // false
        case 'f':
            read('f'); read('a'); read('l'); read('s'); read('e');
            value = false;
            break;
        // string
        case '\'':
        case '\"':
            value = parseString(true);
            break;
        // number
        case '0': case '1': case '2': case '3': case '4': case '5':
        case '6': case '7': case '8': case '9': case '+': case '-':
            value = parseNumber();
            break;
        // array
        case '[':
            value = parseArray(name);
            break;
        // object
        case '{':
            value = parseObject(name);
            break;
        default:
            throw new JSONParseException(s, pos);
        }
        return value;
    }

    /**
     * Parses an object for the form <i>{}</i> and <i>{ members }</i>.
     *
     * @return DBObject the next object
     * @throws JSONParseException if invalid JSON is found
     */
    public Object parseObject() {
	return parseObject(null);
    }

    /**
     * Parses an object for the form <i>{}</i> and <i>{ members }</i>.
     *
     * @return DBObject the next object
     * @throws JSONParseException if invalid JSON is found
     */
    protected Object parseObject(String name){
	if (name != null) {
	    _callback.objectStart(name);
	} else {
	    _callback.objectStart();
	}

        read('{');
        char current = get();
        while(get() != '}') {
            String key = parseString(false);
            read(':');
            Object value = parse(key);
	    doCallback(key, value);

            if((current = get()) == ',') {
                read(',');
            }
            else {
                break;
            }
        }
        read('}');

        return _callback.objectDone();
    }
    
    protected void doCallback(String name, Object value) {
	if (value == null) {
	    _callback.gotNull(name);
	} else if (value instanceof String) {
	    _callback.gotString(name, (String)value);
	} else if (value instanceof Boolean) {
	    _callback.gotBoolean(name, (Boolean)value);
	} else if (value instanceof Integer) {
	    _callback.gotInt(name, (Integer)value);
	} else if (value instanceof Long) {
	    _callback.gotLong(name, (Long)value);
	} else if (value instanceof Double) {
	    _callback.gotDouble(name, (Double)value);
	} 
    }

    /**
     * Read the current character, making sure that it is the expected character.
     * Advances the pointer to the next character.
     *
     * @param ch the character expected
     *
     * @throws JSONParseException if the current character does not match the given character
     */
    public void read(char ch) {
        if(!check(ch)) {
            throw new JSONParseException(s, pos);
        }
        pos++;
    }

    public char read(){
        if ( pos >= s.length() )
            throw new IllegalStateException( "string done" );
        return s.charAt( pos++ );
    }

    /** 
     * Read the current character, making sure that it is a hexidecimal character.
     *
     * @throws JSONParseException if the current character is not a hexidecimal character
     */
    public void readHex() {
        if (pos < s.length() && 
            ((s.charAt(pos) >= '0' && s.charAt(pos) <= '9') ||
             (s.charAt(pos) >= 'A' && s.charAt(pos) <= 'F') ||
             (s.charAt(pos) >= 'a' && s.charAt(pos) <= 'f'))) {
            pos++;
        }
        else {
            throw new JSONParseException(s, pos);
        }
    }

    /**
     * Checks the current character, making sure that it is the expected character.
     *
     * @param ch the character expected
     *
     * @throws JSONParseException if the current character does not match the given character
     */
    public boolean check(char ch) {
        return get() == ch;
    }

    /**
     * Advances the position in the string past any whitespace.
     */
    public void skipWS() {
        while(pos < s.length() && Character.isWhitespace(s.charAt(pos))) {
            pos++;
        }
    }

    /**
     * Returns the current character.
     * Returns -1 if there are no more characters.
     *
     * @return the next character
     */
    public char get() {
        skipWS();
        if(pos < s.length())
            return s.charAt(pos);
        return (char)-1;
    }

    /**
     * Parses a string.
     *
     * @return the next string.
     * @throws JSONParseException if invalid JSON is found
     */
    public String parseString(boolean needQuote) {
        char quot = 0;
        if(check('\''))
            quot = '\'';
        else if(check('\"'))
            quot = '\"';
        else if (needQuote)
            throw new JSONParseException(s, pos);

        char current;

        if (quot > 0)
            read(quot);
        StringBuilder buf = new StringBuilder();
        int start = pos;
        while(pos < s.length()) {
            current = s.charAt(pos);
            if (quot > 0) {
                if (current == quot)
                    break;
            } else {
                if (current == ':' || current == ' ')
                    break;
            }

            if(current == '\\') {
                pos++;
                
                char x = get();
                
                char special = 0;

                switch ( x ){

                case 'u':
                    { // decode unicode
                        buf.append(s.substring(start, pos-1));
                        pos++;
                        int tempPos = pos;
                        
                        readHex();
                        readHex();
                        readHex();
                        readHex();
                        
                        int codePoint = Integer.parseInt(s.substring(tempPos, tempPos+4), 16);
                        buf.append((char)codePoint);
                        
                        start = pos;
                        continue;
                    }
                case 'n': special = '\n'; break;
                case 'r': special = '\r'; break;
                case 't': special = '\t'; break;
                case 'b': special = '\b'; break;
                case '"': special = '\"'; break;
                case '\\': special = '\\'; break;
                }

                buf.append(s.substring(start, pos-1));
                if ( special != 0 ){
                    pos++;
                    buf.append( special );
                }
                start = pos;
                continue;
            }
            pos++;
        }
        buf.append(s.substring(start, pos));
        if (quot > 0)
            read(quot);            
        return buf.toString();
    }

    /**
     * Parses a number.
     *
     * @return the next number (int or double).
     * @throws JSONParseException if invalid JSON is found
     */
    public Number parseNumber() {

        char current = get();
        int start = this.pos;
        boolean isDouble = false;

        if(check('-') || check('+')) {
            pos++;
        }

        outer:
        while(pos < s.length()) {
            switch(s.charAt(pos)) {
            case '0': case '1': case '2': case '3': case '4': 
            case '5': case '6': case '7': case '8': case '9':
                pos++;
                break;
            case '.':
                isDouble = true;
                parseFraction();
                break;
            case 'e': case 'E':
                isDouble = true;
                parseExponent();
                break;
            default:
                break outer;
            }
        }

        if (isDouble)
          return Double.valueOf(s.substring(start, pos));
        
        Long val = Long.valueOf(s.substring(start, pos));
        if (val <= Integer.MAX_VALUE)
            return val.intValue();
        return val;
    }

    /** 
     * Advances the pointed through <i>.digits</i>.
     */
    public void parseFraction() {
        // get past .
        pos++;

        outer:
        while(pos < s.length()) {
            switch(s.charAt(pos)) {
            case '0': case '1': case '2': case '3': case '4': 
            case '5': case '6': case '7': case '8': case '9':
                pos++;
                break;
            case 'e': case 'E':
                parseExponent();
                break;
            default:
                break outer;
            }
        }
    }

    /** 
     * Advances the pointer through the exponent.
     */
    public void parseExponent() {
        // get past E
        pos++;

        if(check('-') || check('+')) {
            pos++;
        }

        outer:
        while(pos < s.length()) {
            switch(s.charAt(pos)) {
            case '0': case '1': case '2': case '3': case '4': 
            case '5': case '6': case '7': case '8': case '9':
                pos++;
                break;
            default:
                break outer;
            }
        }
    }

    /**
     * Parses the next array.
     *
     * @return the array
     * @throws JSONParseException if invalid JSON is found
     */
    public Object parseArray() {
	return parseArray(null);
    }

    /**
     * Parses the next array.
     *
     * @return the array
     * @throws JSONParseException if invalid JSON is found
     */
    protected Object parseArray(String name) {
	if (name != null) {
	    _callback.arrayStart(name);
	} else {
	    _callback.arrayStart();
	}

        read('[');

	int i = 0;
        char current = get();
        while( current != ']' ) {
	    String elemName = String.valueOf(i++);
            Object elem = parse(elemName);
	    doCallback(elemName, elem);

            if((current = get()) == ',') {
                read(',');
            }
            else if(current == ']') {
                break;
            }
            else {
                throw new JSONParseException(s, pos);
            }
        }

        read(']');

        return _callback.arrayDone();
    }

}
