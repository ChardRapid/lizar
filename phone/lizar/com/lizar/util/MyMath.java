package com.lizar.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Stack;

public class MyMath {
	public static void main(String[] args) {
		
	}
	
	public static final char[] _60_array={'q','w','e','r','t','y','u','i','o','p','a','s','d','f','g','h','j','k','l','z','x','c','v','b','n','m','1','2','3','4','5','6','7','8','9','Q','W','E','R','T','Y','U','I','P','A','S','D','F','G','H','J','K','L','Z','X','C','V','B','N','M'};
	
	
	public static String _10_to_60(long number){
			Long rest=number;
			Stack<Character> stack=new Stack<Character>();
			StringBuilder result=new StringBuilder(0);
			while(rest!=0){
				stack.add(_60_array[new Long((rest-(rest/60)*60)).intValue()]);
				rest=rest/62;
			}
			for(;!stack.isEmpty();){
				result.append(stack.pop());
			}
			return result.toString();
			
	}
	
	public static long _60_to_10(String sixty_str){
		int multiple=1;
		long result=0;
		Character c;
		for(int i=0;i<sixty_str.length();i++){
			c=sixty_str.charAt(sixty_str.length()-i-1);
			result+=_60_value(c)*multiple;
			multiple=multiple*62;
		}
		return result;
	}
	
	private static int _60_value(Character c){
		for(int i=0;i<_60_array.length;i++){
			if(c==_60_array[i]){
				return i;
			}
		}
		return -1;
	}
	
public static String encryptionWithSHA(String words) {
		
		MessageDigest sha;
		try {
			sha = MessageDigest.getInstance("SHA");
			sha.update(words.getBytes());
			words=new String(sha.digest(),"ISO-8859-1");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			//log.error("Encryt the words:"+words+" failed. SHA algorithm is not supported",e);
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return words;
	}

    public static String encryptionWithSHA(long words) {
		return encryptionWithSHA(words+"");
	}
    
    
   public static String encryptionWithMD5(String words) {
			StringBuilder buf=new StringBuilder("");
	   try {
		   MessageDigest md = MessageDigest.getInstance("MD5");
		   md.update(words.getBytes());
		   byte b[] = md.digest();

		   int i;
		   for (int offset = 0; offset < b.length; offset++) {
		    i = b[offset];
		    if (i < 0)
		     i += 256;
		    if (i < 16)
		     buf.append("0");
		    buf.append(Integer.toHexString(i));
		   }
		  } catch (NoSuchAlgorithmException e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();

		  }
		return buf.toString();
	}
    
   public static String encryptionWithMD5(long words) {
		return encryptionWithMD5(words+"");
	}
   
	
}
