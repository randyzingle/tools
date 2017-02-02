package com.bms.base.streams;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListFilter {

	public static void main(String[] args) {
		ListFilter lf = new ListFilter();
		List<String> slist = lf.getStrings();
		System.out.printf("Found %d words%n", slist.size());
		for (String s: slist) System.out.println(s);
	}
	
	private List<String> getStrings() {
		List<String> slist = new ArrayList<>();
		try (FileReader fr = new FileReader("src/main/resources/xanadu.txt");
				BufferedReader br = new BufferedReader(fr);) {
			String s = null;
			while ( (s = br.readLine()) != null) {
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
