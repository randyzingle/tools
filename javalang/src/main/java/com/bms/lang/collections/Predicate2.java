package com.bms.lang.collections;

import static java.util.stream.Collectors.groupingBy;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bms.lang.collections.data.Apple;

public class Predicate2 {

	public static void main(String[] args) {
		List<Apple> alist = Apple.getApples(40);
		alist.removeIf((Apple apple) -> !"red".equals(apple.getColor()));
		System.out.printf("Found %d red apples%n", alist.size());
		System.out.println(alist);
		
		alist = Apple.getApples(100);
		Map<String, List<Apple>> heavyAppleMap = alist.stream()
				.filter((Apple apple) -> apple.getWeight() > 17)
				.collect(groupingBy(Apple::getType));
		printAppleMap(heavyAppleMap);
	}
	
	private static void printAppleMap(Map<String, List<Apple>> heavyAppleMap) {
		Set<String> keySet = heavyAppleMap.keySet();
		for (String key: keySet) {
			List<Apple> appleList = heavyAppleMap.get(key);
			System.out.printf("Found %d heavy %s apples%n", appleList.size(), key);
			System.out.println("     " + appleList.toString());
		}
	}

}
