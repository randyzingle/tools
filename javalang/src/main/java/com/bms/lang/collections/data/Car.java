package com.bms.lang.collections.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Car {
	private String color;
	private String make;
	private int weight;

	public Car(String color, String make, int weight) {
		super();
		this.color = color;
		this.make = make;
		this.weight = weight;
	}

	public static List<Car> getCars(int number) {
		List<Car> clist = new ArrayList<>(number);
		Random rnd = new Random(System.currentTimeMillis());
		for (int i = 0; i < number; i++) {
			int makeIndex = rnd.nextInt(3);
			String make = null;
			switch (makeIndex) {
			case 0:
				make = "buttersmobile";
				break;
			case 1:
				make = "baldurcoup";
				break;
			case 2:
				make = "mymirmobile";
				break;
			}
			int colorIndex = rnd.nextInt(3);
			String color = null;
			switch (colorIndex) {
			case 0:
				color = "black";
				break;
			case 1:
				color = "green";
				break;
			case 2:
				color = "gold";
				break;
			}
			int weight = rnd.nextInt(1000) + 5000;
			Car car = new Car(color, make, weight);
			clist.add(car);
		}
		return clist;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getMake() {
		return make;
	}

	public void setMake(String make) {
		this.make = make;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Car [color=").append(color).append(", make=").append(make).append(", weight=").append(weight)
				.append("]");
		return builder.toString();
	}
}
