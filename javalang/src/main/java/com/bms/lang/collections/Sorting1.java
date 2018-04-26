package com.bms.lang.collections;

import java.util.List;

import com.bms.lang.collections.data.Apple;

public class Sorting1 {
	public static void main(String[] args) {
//		sortApples(10);
		appleStats(1000);
	}
	
	private static void appleStats(int number) {
		List<Apple> alist = Apple.getApples(number);
		System.out.println("Got the apples...");
		int sumWeight = alist.stream()
				.mapToInt(w -> w.getWeight())
				.sum();
//		printList(alist);
		System.out.printf("Total weight: %d%n", sumWeight);
		int sumRed = alist.stream()
				.filter(w -> w.getColor().equals("red"))
				.mapToInt(w -> w.getWeight())
				.sum();
		System.out.printf("Weight of red apples: %d%n", sumRed);
		System.out.printf("Ratio: %.4f", sumRed/(sumWeight*1.0));
	}
	
	private static void sortApples(int number) {
		List<Apple> alist = Apple.getApples(number);
		printList(alist);
	}
	
	private static void printList(List<Apple> plist) {
		for (Apple apple: plist) {
			System.out.println(apple);
		}
	}
}
