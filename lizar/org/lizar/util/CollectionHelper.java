package org.lizar.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class CollectionHelper {
	
	
	public static <T> List<T> set_to_list(Set<T> set){
		if(set==null)return null;
		List<T> list=new ArrayList<T>();
		for(T obj:set){
			list.add(obj);
		}
		return list;
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
