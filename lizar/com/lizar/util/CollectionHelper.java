package com.lizar.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CollectionHelper {
	
	
	public static void printMap(Map map){
		Iterator itr=map.entrySet().iterator();
		for(;itr.hasNext();){
			Entry e=(Entry)itr.next();
			System.out.println("key:"+e.getKey().toString()+"  value:"+e.getValue().toString());
		}
	}
	
	public static void printList(List list,String propertyName){
		Iterator itr=list.iterator();
		Method[] methods=list.get(0).getClass().getMethods();
		String methodName=null;
		for(Method m:methods){
			if(StringHelper.equals(m.getName(),ObjectHelper.getGetMethodName(propertyName))){
				methodName=m.getName();
			}
		}
		for(;itr.hasNext();){
			Object e=itr.next();
			try {
				System.out.println(e.getClass().getMethod(methodName).invoke(e));
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	
}
