package com.sas.mkt.kafka.clients.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.log4j.Logger;

import com.sas.mkt.kafka.domain.TestEvent;

public class PostgreSQLWriter implements CIKafkaRecordProcessor {
	
	private Logger logger = Logger.getLogger(this.getClass());
	private Connection conn;
	private PreparedStatement ps;

	public static void main(String[] args) {
		try (PostgreSQLWriter pg = new PostgreSQLWriter()) {
			pg.setupPS();
			pg.writeIt("baldur", "thread2", 1, 1);
			pg.readIt();
			pg.tearDownPS();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public PostgreSQLWriter() {
		this.setupPS();
	}
	
	private void readIt() throws Exception {
		String url = "jdbc:postgresql://localhost/postgres?user=postgres&password=bakaphel";
		Connection conn = DriverManager.getConnection(url);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select tenant, thread, partition, sequence from testevents");
		while (rs.next()) {
			String s = String.format("%s %s %d %d%n", 
					rs.getString("tenant"), rs.getString("thread"), rs.getInt("partition"), rs.getInt("sequence"));
			System.out.println(s);
		}
		rs.close();
		stmt.close();
		conn.close();
	}
	
	private void setupPS() {
		try {
			conn = this.getConnection();
			String s = "insert into testevents (tenant, thread, partition, sequence) values (?,?,?,?)";
			ps = conn.prepareStatement(s);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void tearDownPS() {
		try {
			ps.close();
			conn.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void writeIt(String tenant, String thread, int partition, int sequence) {
		try {
			ps.setString(1, tenant);
			ps.setString(2, thread);
			ps.setInt(3, partition);
			ps.setInt(4, sequence);
			ps.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private Connection getConnection() throws Exception {
		String url = "jdbc:postgresql://localhost/postgres?user=postgres&password=bakaphel";
		Connection conn = DriverManager.getConnection(url);
		return conn;
	}

	@Override
	public void processRecord(ConsumerRecord<String, SpecificRecordBase> record) {
		TestEvent te = (TestEvent)record.value();
		String tenant = te.getTenant();
		String thread = Thread.currentThread().getName();
		int partition = record.partition();
		int sequence = te.getSequence();
		this.writeIt(tenant, thread, partition, sequence);
	}

	@Override
	public void close() throws Exception {
		logger.info("Shutting down message processor");
		this.tearDownPS();
	}

}
