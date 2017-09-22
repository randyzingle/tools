package com.sas.mkt.kafka.performance.domain;

public class MessagesInJMXStats {
	
	private String brokerId;
	private long count;
	private String eventType;
	private double fifteenMinuteRate;
	private double fiveMinuteRate;
	private double oneMinuteRate;
	private double meanRate;
	private String rateUnit;
	
	public MessagesInJMXStats(String brokerId, long count, String eventType, double fifteenMinuteRate, double fiveMinuteRate,
			double oneMinuteRate, double meanRate, String rateUnit) {
		this.brokerId = brokerId;
		this.count = count;
		this.eventType = eventType;
		this.fifteenMinuteRate = fifteenMinuteRate;
		this.fiveMinuteRate = fiveMinuteRate;
		this.oneMinuteRate = oneMinuteRate;
		this.meanRate = meanRate;
		this.rateUnit = rateUnit;
	}
	
	public String getBrokerId() {
		return brokerId;
	}
	public void setBrokerId(String brokerId) {
		this.brokerId = brokerId;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	public double getFifteenMinuteRate() {
		return fifteenMinuteRate;
	}
	public void setFifteenMinuteRate(double fifteenMinuteRate) {
		this.fifteenMinuteRate = fifteenMinuteRate;
	}
	public double getFiveMinuteRate() {
		return fiveMinuteRate;
	}
	public void setFiveMinuteRate(double fiveMinuteRate) {
		this.fiveMinuteRate = fiveMinuteRate;
	}
	public double getOneMinuteRate() {
		return oneMinuteRate;
	}
	public void setOneMinuteRate(double oneMinuteRate) {
		this.oneMinuteRate = oneMinuteRate;
	}
	public double getMeanRate() {
		return meanRate;
	}
	public void setMeanRate(double meanRate) {
		this.meanRate = meanRate;
	}
	public String getRateUnit() {
		return rateUnit;
	}
	public void setRateUnit(String rateUnit) {
		this.rateUnit = rateUnit;
	}
	@Override
	public String toString() {
		return "MessagesInJMXStats [brokerId=" + brokerId + ", count=" + count + ", eventType=" + eventType
				+ ", fifteenMinuteRate=" + fifteenMinuteRate + ", fiveMinuteRate=" + fiveMinuteRate + ", oneMinuteRate="
				+ oneMinuteRate + ", meanRate=" + meanRate + ", rateUnit=" + rateUnit + "]";
	}

}
