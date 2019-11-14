package cn.wulin.netty.thread.fast_local;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import org.junit.Test;

import io.netty.util.concurrent.FastThreadLocal;

public class TestFastThreadLocal {
	
	@Test
	public void fastLocalTest() {
		FastThreadLocal<String> local = new FastThreadLocal<String>(){
			@Override
			protected String initialValue() throws Exception {
				return "123";
			}
		};
		
		System.out.println(local.get());
		
		local.set("456");
		System.out.println(local.get());
		
		local.remove();
		System.out.println(local.get());
	}
	
	@Test
	public void setFromIdentityMap() {
		IdentityHashMap<String, Boolean> identityHashMap = new IdentityHashMap<String, Boolean>();
		
		String a = new String("a");
		String b = new String("a");
		identityHashMap.put(a, true);
		identityHashMap.put(b, false);
		identityHashMap.put("b", true);
		identityHashMap.put("c", true);
		
		
		Set<String> variablesToRemove = Collections.newSetFromMap(new IdentityHashMap<String, Boolean>());
		
		variablesToRemove.add(a);
		variablesToRemove.add(a);
		variablesToRemove.add("b");
		variablesToRemove.add("c");
		
		System.out.println();
		
	}
	
//	

}
