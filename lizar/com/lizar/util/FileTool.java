package com.lizar.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import com.lizar.HotLoader;
import com.lizar.json.JList;
import com.lizar.json.JObject;
import com.lizar.json.JSON;
import com.lizar.json.util.JSONParser;

public class FileTool {
	
	/**
	 * 
	 * check the dir if it is exists and return the dir File
	 * 
	 * if not, then mkdirs
	 * 
	 * **/
	public static void check_dir(String dir){
		File f=new File(dir);
		if(!f.exists()){
			f.mkdirs();
		}
		if(f.isFile()){
			delete_sub_files(f);
			f.delete();
			f.mkdirs();
		}
	}
	
	public static void check_dir(File f){
		if(!f.exists()){
			f.mkdirs();
		}
		if(f.isFile()){
			delete_sub_files(f);
			f.delete();
			f.mkdirs();
		}
	}
	
	public static void delete_sub_files(File container){
		File[] file=container.listFiles();
		if(file!=null){
			for(File f:file){
				if(f.isFile()){
					f.delete();
				}else{
					delete_sub_files(f);
				}
			}
		}
	}
	
	public static void write_to_file(JSON entity,String file_name) throws IOException{
	        File f=new File(file_name);
	        OutputStream out =new FileOutputStream(f);
	        byte[] b=entity.to_beautifier_string().getBytes();
	        try{
		        for (int i = 0; i < b.length; i++) {
		            out.write(b[i]);
		        }
	        }finally{
	        	if(out!=null)out.close();
	        }
	       
	       
	}
	
	public static void write_to_file(JSON entity,File file) throws IOException{
	        OutputStream out =new FileOutputStream(file);
	        byte[] b=entity.to_beautifier_string().getBytes();
	        try{
	        	for (int i = 0; i < b.length; i++) {
		            out.write(b[i]);
		        }
	        }finally{
	        	if(out!=null)out.close();
	        }
	}
	
	public static void write_to_file(JList list,File file) throws IOException{
	        OutputStream out =new FileOutputStream(file);
	        byte[] b=list.to_beautifier_string().getBytes();
	        try{
		        for (int i = 0; i < b.length; i++) {
		            out.write(b[i]);
		        }
			}finally{
		    	if(out!=null)out.close();
		    }
	}
	
	public static void write_to_file(String data,File file) throws IOException{
	        OutputStream out =new FileOutputStream(file);
	        byte[] b=data.getBytes();
	        try{
	        	 for (int i = 0; i < b.length; i++) {
	 	            out.write(b[i]);
	 	        }
	        }finally{
	        	if(out!=null)out.close();
	        }
	}
	
	
	public static JSON read_json(File f){
		String result=read_it(f);
		return (JSON)JSONParser.parse(result);
	}
	
	private static String read_it(File file){
		try {
			FileReader fr = new FileReader(file); 
			BufferedReader br = new BufferedReader(fr); 
			StringBuilder result=new StringBuilder("");
			String line = br.readLine(); 
			while(line!=null){
				result.append(line);
				line=br.readLine();
			}
			return result.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}   
		System.err.println("config file read failed. pls check with "+file.toString());
        return "";
	}

	/** 
     * 
     * @param oldPath String
     * @param newPath String 
     * @return boolean 
     */ 
   public static void copyFile(String oldPath, String newPath) { 
       try { 
           int bytesum = 0; 
           int byteread = 0; 
           File oldfile = new File(oldPath); 
           if (oldfile.exists()) { 
               InputStream inStream = new FileInputStream(oldPath); 
               FileOutputStream fs = new FileOutputStream(newPath); 
               byte[] buffer = new byte[1024]; 
               int length; 
               while ( (byteread = inStream.read(buffer)) != -1) { 
                   bytesum += byteread; 
                   System.out.println(bytesum); 
                   fs.write(buffer, 0, byteread); 
               } 
               inStream.close(); 
           } 
       } 
       catch (Exception e) { 
           e.printStackTrace(); 

       } 

   } 
   
	public static long file_size(String size){
		size=size.toLowerCase();
		if(StringHelper.isLong(size)){
			return Long.parseLong(size);
		}
		if(size.endsWith("kb")){
			if(size.length()==2)return 1024;
			size=size.substring(0, size.length()-2);
			long s=Long.parseLong(size);
			return s*1024;
		}else if(size.endsWith("mb")){
			if(size.length()==2)return 1024*1024;
			size=size.substring(0, size.length()-2);
			long s=Long.parseLong(size);
			return s*1024*1024;
		}else if(size.endsWith("gb")){
			if(size.length()==2)return 1024*1024*1024;
			size=size.substring(0, size.length()-2);
			long s=Long.parseLong(size);
			return s*1024*1024*1024;
		}else if(size.endsWith("tb")){
			if(size.length()==2)return 1024*1024*1024*1024;
			size=size.substring(0, size.length()-2);
			long s=Long.parseLong(size);
			return s*1024*1024*1024*1024;
		}
		return 0;
	}
   
   private static FileFilter class_filter = new FileFilter() {
       public boolean accept(File pathname) {
           return pathname.getName().indexOf("$")==-1&&(pathname.isDirectory() || pathname.getName().endsWith(".class")||pathname.getName().endsWith(".jar"));
       };
   };
   
   /**
    * load and initialize cell
    */
   public static void scan_class_file(File dir,List<Class> list,Class father,HotLoader loader) throws IOException {
	   if(dir.isFile()){
		   scan_file(dir,list,father,loader);
		   return;
	   }
	   File[] files = dir.listFiles(class_filter);
       if (files.length>0) {
           for (File file : files) {
        	   if(file.isDirectory()){
        		   scan_class_file(file,list,father,loader);
        	   }else if(file.isFile()){
        		   scan_file(file,list,father,loader);
        	   }
           }
       }
       
   }
   
   /**
    * load cell from class file and jar file
    */
   private static void scan_file(File file,List<Class> list,Class father,HotLoader loader) throws IOException {
	   if(file.getName().endsWith(".jar")){
		   scan_jar_file(file,list,father,loader);
	   }else if(file.getName().endsWith(".class")){
		  String class_name=fetch_class_name(file);
		  if(class_name==null)return;
		   Class c;
		   try {
			   c = Class.forName(class_name.replace("/", ".").replace("\\", "."));
		   } catch (ClassNotFoundException e1) {
			   return;
		   }
		   if(!c.isInterface() ){
			   try{
				   c.asSubclass(father);
			   }catch(Exception e){
				   return;
			   }
			   list.add(c);
		   }
	   }
   }
   
   /**
    * Parse class file and return class name.
    * @param file class file
    */
   public static String fetch_class_name(File file) {
	   DataInputStream in=null ;
	   String name=null;
	   try {
		 in = new DataInputStream(new FileInputStream(file));
		 if(in.readInt() != 0xCAFEBABE) {
			   return name;
		 }
		 in.readUnsignedShort();// 次版本号
		 in.readUnsignedShort();// 主版本号
		 in.readUnsignedShort();// 长度
		 in.readByte();// CLASS=7
		 in.readUnsignedShort();// 忽略这个地方
		 in.readByte();// UTF8=1
		 name=in.readUTF();//类的名字!!!
	   } catch (IOException e) {
		   e.printStackTrace();
	   }finally{
		   if(in!=null){
			   try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		   }
	   }
	  return name;
   }
   
   /**
    * load cess from jar file
    */
   public static void scan_jar_file(File jar_file,List<Class> list,Class father,HotLoader loader) throws IOException {
	   URL url=new URL("file:"+jar_file.getPath());
		URLClassLoader myClassLoader=new URLClassLoader(new URL[]{url},Thread.currentThread().getContextClassLoader());
		JarFile jarFile =new JarFile(jar_file.getPath());
		Enumeration<JarEntry> entries=jarFile.entries();
		for(;entries.hasMoreElements();){
			JarEntry e=entries.nextElement();
			if(!e.isDirectory()&&e.getName().endsWith(".class")){
				Class c;
				try {
					c = myClassLoader.loadClass(e.getName().replace("/", ".").replace("\\",".").substring(0, e.getName().length()-6));
					if(!c.isInterface()){
						try {
							c.asSubclass(father);
						} catch (Exception e1) {
							continue;
						} 
						list.add(c);
					}
				} catch (Throwable e2) {
					continue;
				}
			}
		}
   }
   
   
   /** 
     * 
     * @param oldPath String 
     * @param newPath String 
     * @return boolean 
     */ 
   public static  void copyFolder(String oldPath, String newPath) { 

       try { 
           (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹 
           File a=new File(oldPath); 
           String[] file=a.list(); 
           File temp=null; 
           for (int i = 0; i < file.length; i++) { 
               if(oldPath.endsWith(File.separator)){ 
                   temp=new File(oldPath+file[i]); 
               } 
               else{ 
                   temp=new File(oldPath+File.separator+file[i]); 
               } 

               if(temp.isFile()){ 
                   FileInputStream input = new FileInputStream(temp); 
                   FileOutputStream output = new FileOutputStream(newPath + "/" + 
                           (temp.getName()).toString()); 
                   byte[] b = new byte[1024 * 5]; 
                   int len; 
                   while ( (len = input.read(b)) != -1) { 
                       output.write(b, 0, len); 
                   } 
                   output.flush(); 
                   output.close(); 
                   input.close(); 
               } 
               if(temp.isDirectory()){//如果是子文件夹 
                   copyFolder(oldPath+"/"+file[i],newPath+"/"+file[i]); 
               } 
           } 
       } 
       catch (Exception e) { 
           e.printStackTrace(); 

       } 

   }
   
	
   public static void main(String[] args) throws IOException{
	  File f=new File("Q:/eclipse/notice.html");
	  JObject l=new JObject();
	  l.put("ddd","assdf");
	  l.put("we", true);
	  l.put("werer", 20.3);
	  JObject m=new JObject();
	  m.put("lkl","wew");
	  m.put("werwe", "oop");
	  l.put("json", m);
	  JList list=new JList();
	  list.add("werw");
	  list.add("rwer");
	  JObject gg=new JObject();
	  gg.put("qweqw", "nk");
	  gg.put("ttt", "222");
	  list.add(gg);
	  l.put("list",list);
	  
	  
	  write_to_file(l.to_beautifier_string(), f);
	  
   }
   
}
