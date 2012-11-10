package com.lizar.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ObjectHelper {
	
	public static List<String> fech_sub_classes_from_lib(String class_path,Class father_class,String lib_patten){
		if(StringHelper.isNotNull(lib_patten)){
			List<String> sub_class=new ArrayList<String>();
			
			return sub_class;
		}
		return null;
	}
	
	public static List<String> fech_sub_classes(String class_path,Class father_class,String[] package_list){
		if(package_list!=null){
			List<String> sub_class=new ArrayList<String>();
			for(String _package:package_list){
				String directory_path=class_path+_package.trim().replace(".","/");
				if(directory_path.endsWith("/*")){
					directory_path=directory_path.substring(0, directory_path.length()-1);
					_package=_package.substring(0, _package.length()-1);
					depth_fetch_sub_classes(directory_path,_package,father_class,sub_class);
				}else{
					if(!directory_path.endsWith("/")){
						directory_path+="/";
						_package+=".";
					}
					lower_fetch_sub_classes(directory_path,_package,father_class,sub_class);
				}
					
			}
			return sub_class;
		}
		return null;
	}
	
	private static void lower_fetch_sub_classes(String directory_path,String _package,Class father_class,List<String> sub_class){
		File file=new File(directory_path);
		if(file.isDirectory()&&file.exists()){
			String class_name=null;
			for(File sub_file:file.listFiles()){
				if(sub_file.isFile()&&sub_file.getName().endsWith(".class")){
					class_name=_package+sub_file.getName().substring(0, sub_file.getName().length()-".class".length());
					boolean is_sub_class=true;
					try {
						Class.forName(class_name).asSubclass(father_class);
					} catch (Exception e){
						is_sub_class=false;
					}
					if(is_sub_class){
						
						sub_class.add(class_name);
					}
				}
			}
		}
	}
	
	private static void lower_fetch_sub_classes(String directory_path,String _package,String father_class,List<String> sub_class){
		File file=new File(directory_path);
		if(file.isDirectory()&&file.exists()){
			String class_name=null;
			for(File sub_file:file.listFiles()){
				if(sub_file.isFile()&&sub_file.getName().endsWith(".class")){
					class_name=_package+sub_file.getName().substring(0, sub_file.getName().length()-".class".length());
					boolean is_sub_class=true;
					try {
						Class.forName(class_name).asSubclass(Class.forName(father_class));
					} catch (Exception e){
						is_sub_class=false;
					}
					if(is_sub_class){
						sub_class.add(class_name);
					}
				}
			}
		}
	}
	
	private static void depth_fetch_sub_classes(String directory_path,String _package,Class father_class,List<String> sub_class){
		File file=new File(directory_path);
		if(file.isDirectory()&&file.exists()){
			String class_name=null;
			for(File sub_file:file.listFiles()){
				if(sub_file.isFile()&&sub_file.getName().endsWith(".class")){
					class_name=_package+sub_file.getName().substring(0, sub_file.getName().length()-".class".length());
					boolean is_sub_class=true;
					try {
						if(class_name.indexOf("$")!=-1){
							is_sub_class=false;
						}else{
							Class t=Class.forName(class_name);
							t.asSubclass(father_class);
						}
					} catch (Throwable e){
						is_sub_class=false;
					}
					
					if(is_sub_class){
						sub_class.add(class_name);
					}
				}else if(sub_file.isDirectory()){
					depth_fetch_sub_classes(
							directory_path+sub_file.getName()+"/",
							_package+sub_file.getName()+".",
							father_class,
							sub_class);
				}
			}
		}
	}
	
	private static void depth_fetch_sub_classes(String directory_path,String _package,String father_class,List<String> sub_class){
		File file=new File(directory_path);
		if(file.isDirectory()&&file.exists()){
			String class_name=null;
			for(File sub_file:file.listFiles()){
				if(sub_file.isFile()&&sub_file.getName().endsWith(".class")){
					class_name=_package+sub_file.getName().substring(0, sub_file.getName().length()-".class".length());
					boolean is_sub_class=true;
					try {
						Class.forName(class_name).asSubclass(Class.forName(father_class));
					} catch (Exception e){
						is_sub_class=false;
					}
					if(is_sub_class){
						sub_class.add(class_name);
					}
				}else if(sub_file.isDirectory()){
					depth_fetch_sub_classes(
							directory_path+sub_file.getName()+"/",
							_package+sub_file.getName()+".",
							father_class,
							sub_class);
				}
			}
		}
	}
	
	public static List<String> fech_sub_classes(String class_path,Class father_class,List<String> package_list){
		if(package_list!=null){
			List<String> sub_class=new ArrayList<String>();
			for(String _package:package_list){
				String class_name=null;
				String directory_path=class_path+_package.trim().replace(".","/");
				if(directory_path.endsWith("/*")){
					directory_path=directory_path.substring(0, directory_path.length()-1);
					_package=_package.substring(0, _package.length()-1);
					depth_fetch_sub_classes(directory_path,_package,father_class,sub_class);
				}else{
					if(!directory_path.endsWith("/")){
						directory_path+="/";
						_package+=".";
					}
					lower_fetch_sub_classes(directory_path,_package,father_class,sub_class);
				}
					
			}
			return sub_class;
		}
		return null;
	}
	
	public static List<String> fech_sub_classes(String class_path,String father_class,List<String> package_list){
		if(package_list!=null){
			List<String> sub_class=new ArrayList<String>();
			for(String _package:package_list){
				String class_name=null;
				String directory_path=class_path+_package.trim().replace(".","/");
				if(directory_path.endsWith("/*")){
					directory_path=directory_path.substring(0, directory_path.length()-1);
					_package=_package.substring(0, _package.length()-1);
					depth_fetch_sub_classes(directory_path,_package,father_class,sub_class);
				}else{
					if(!directory_path.endsWith("/")){
						directory_path+="/";
						_package+=".";
					}
					lower_fetch_sub_classes(directory_path,_package,father_class,sub_class);
				}
					
			}
			return sub_class;
		}
		return null;
	}
	
	
	
	public static String getGetMethodName(String propertyName){
		StringBuilder methodName=new StringBuilder("get");
		return methodName.append(Character.toUpperCase(propertyName.charAt(0))).append(propertyName.substring(1)).toString();
	}
	
	public static String getSetMethodName(String propertyName){
		StringBuilder methodName=new StringBuilder("set");
		return methodName.append(Character.toUpperCase(propertyName.charAt(0))).append(propertyName.substring(1)).toString();
	}
	
	
	public static void main(String[] args){
		System.out.println(getSetMethodName("asdf"));
	}
}
