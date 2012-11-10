package com.lizar;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;



public class HotLoader extends ClassLoader {
	
	public HotLoader(ClassLoader parent) {
        super(parent);
    }
	
	private byte[] getBytes(String filename) throws IOException{
		File file=new File(filename);
		long len=file.length();
		byte[] raw=new byte[(int)len];
		
		FileInputStream fin=new FileInputStream(file);

		int r=fin.read(raw);
		if(r!=len) throw new IOException("Can't read all,"+r+"!="+len);

		fin.close();

		return raw;
	}

	private byte[] getBytes(File file) throws IOException{
		long len=file.length();
		byte[] raw=new byte[(int)len];
		
		FileInputStream fin=new FileInputStream(file);

		int r=fin.read(raw);
		if(r!=len) throw new IOException("Can't read all,"+r+"!="+len);

		fin.close();

		return raw;
	}
	
	@Override
	public Class findClass(String name){
		byte[] bytes=null;
		try {
			bytes = getBytes(name);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Class theClass = defineClass(null, bytes, 0, bytes.length);
        this.resolveClass(theClass);
        if(theClass == null) {
            throw new ClassFormatError();
         }
        return theClass;
		
	}
	
	public Class findClass(File f){
		byte[] bytes=null;
		try {
			bytes = getBytes(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Class theClass = defineClass(null, bytes, 0, bytes.length);
        this.resolveClass(theClass);
        if(theClass == null) {
            throw new ClassFormatError();
         }
        return theClass;
		
	}
	
}
