package com.bms.base.streams;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ListFilter {
	
	private final String fileName = "src/main/resources/xanadu.txt";

	public static void main(String[] args) {
		ListFilter lf = new ListFilter();
		List<String> slist = lf.getStrings();
		String[] alp = lf.upperAlphabet();
//		for (String s: alp) {
//			lf.printStartsWith(slist, s);
//		}
		lf.sortedPrintStartsWith();
		
	}
	
	private String[] upperAlphabet() {
		int startChar = 65; // A
		int endChar = 90; // Z
		String[] a = new String[26];
		for (int i=startChar, j=0; i<=endChar; i++, j++) {
			a[j] = String.valueOf((char)i);
		}
		return a;
	}
	
	private void sortedPrintStartsWith() {
		String[] alphabet = this.upperAlphabet();
		List<String> allWords = this.getStrings();
		TreeSet<WordsAndSize> treeSet = new TreeSet<>();
		for (String a: alphabet) {
			List<String> filtered = allWords.stream().filter(e -> e.toUpperCase().startsWith(a.toUpperCase())).collect(Collectors.toList());
			WordsAndSize was = new WordsAndSize(filtered, a);
			treeSet.add(was);
		}
		for (WordsAndSize was: treeSet) {
			double per = was.wordList.size()*100.0/allWords.size();
			System.out.printf("%s %d %.2f%% %s%n", was.prefix, was.wordList.size(), per, was.wordList.toString());
		}
	}
	
	private class WordsAndSize implements Comparable<WordsAndSize>{
		
		private List<String> wordList;
		private String prefix;
		
		private WordsAndSize(List<String> wordList, String prefix) {
			this.wordList = wordList;
			this.prefix = prefix;
		}

		@Override
		public int compareTo(WordsAndSize o) {
			if (this.wordList.size() > o.wordList.size()) return 1;
			if (this.wordList.size() < o.wordList.size()) return -1;
			return 0;
		}
		
	}
	
	private void printStartsWith(List<String> slist, String prefix) {
		List<String> filtered = slist.stream().filter(e -> e.toUpperCase().startsWith(prefix.toUpperCase())).collect(Collectors.toList());
		System.out.println("Prefix: " + prefix);
		System.out.println(filtered);	
		System.out.printf("%d words out of %d, or %.2f%%%n", filtered.size(), slist.size(), filtered.size()*100.0/slist.size());
	}

	private List<String> getStrings() {
		List<String> slist = new ArrayList<>();
		try (FileReader fr = new FileReader(fileName);
				BufferedReader br = new BufferedReader(fr);) {
			String s = null;
			while ( (s = br.readLine()) != null) {
				if (s.trim().isEmpty() || s.trim() == "") continue;
				s = s.replaceAll("[^a-zA-Z\\s+]", "");
				String[] sarray = s.trim().split("\\s+");
				slist.addAll(Arrays.asList(sarray));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return slist;
	}

}

