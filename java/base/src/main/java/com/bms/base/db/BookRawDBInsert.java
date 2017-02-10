package com.bms.base.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;

public class BookRawDBInsert {

	public static void main(String[] args) throws Exception {
		String url = "jdbc:postgresql://10.48.19.149/postgres?user=postgres&password=bakaphel";
		Connection conn = DriverManager.getConnection(url);

	}
	
	private class Book {
		ArrayList<String> lines;
		String author;
		String title;
	}

}
