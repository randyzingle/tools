package com.bms.base.lambda;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class IntStringSorter {
	
	private int n = 10;
	private int bound = 100;

	public static void main(String[] args) {
		IntStringSorter iss = new IntStringSorter();
		iss.sortOld();
		iss.sortWithLambda();
	}

	private void sortWithLambda() {
		System.out.println("lambda-based sort:");
		String[] sarray = genArray(n);
		print(sarray);
		Arrays.sort(sarray, (s1, s2) -> Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2)));
		print(sarray);
	}

	private void sortOld() {
		System.out.println("oldschool sort:");
		String[] sarray = genArray(n);
		print(sarray);
		// FunctionalInterface -> i.e annotated with @FunctionalInterface and has one method
		Comparator<String> numComp = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return Integer.compare(Integer.parseInt(o1), Integer.parseInt(o2));
			}	
		};
		Arrays.sort(sarray,numComp);
		print(sarray);
		System.out.println();
	}
	
	private String[] genArray(int n) {
		String[] array = new String[n];
		Random rnd = new Random();
		for (int i=0; i<n; i++) {
			array[i] = Integer.toString(rnd.nextInt(bound));
		}
		return array;
	}
	
	private void print(String[] a) {
		for(String s: a) {
			System.out.printf("%s,",s);
		}
		System.out.println();
	}

}
