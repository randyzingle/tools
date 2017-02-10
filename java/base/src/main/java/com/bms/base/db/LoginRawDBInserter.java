package com.bms.base.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Random;

public class LoginRawDBInserter {
	
	private static Random rnd = new Random();

	public static void main(String[] args) throws Exception {
		String url = "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=bakaphel";
		Connection conn = DriverManager.getConnection(url);
		conn.setAutoCommit(true);
		
		String sql = "insert into login_table (username, login_time) values (?,?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		Timestamp ts;
		
		for (int i=0; i<20; i++) {
			String s = getUser();
			ps.setString(1, s);
			ts = new Timestamp(System.currentTimeMillis());
			ps.setTimestamp(2, ts);
			ps.execute();
			System.out.println(ts + ": " + s);
			Thread.sleep(20 * rnd.nextInt(10));
		}
		
		ps.close();
		conn.close();

	}
	
	public static String getUser() {
		String s;
		String[] ulist = {"baldur", "finnr", "mymir", "butters", "razing", "aldir"}; 
		
		s = ulist[rnd.nextInt(ulist.length)];
		return s;
	}

}
