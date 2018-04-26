package com.bms.lang.collections.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Apple {
	private static int MAX_WEIGHT = 20;
	private String color;
	private int weight;
	private String type;

	public static List<Apple> getApples(int number) {
		List<Apple> alist = new ArrayList<>(number);
		Random rnd = new Random(System.currentTimeMillis());
		for (int i = 0; i < number; i++) {
			int weight = rnd.nextInt(MAX_WEIGHT);
			int colorIndex = rnd.nextInt(3);
			String color = null;
			switch (colorIndex) {
			case 0:
				color = "red";
				break;
			case 1:
				color = "green";
				break;
			case 2:
				color = "blue";
				break;
			}
			int typeIndex = rnd.nextInt(3);
			String type = null;
			switch (typeIndex) {
			case 0:
				type = "baldurtosh";
				break;
			case 1:
				type = "fujimyms";
				break;
			case 2:
				type = "goldendebutters";
				break;
			}
			Apple apple = new Apple(color, weight, type);
			alist.add(apple);
		}
		return alist;
	}

	public Apple(String color, int weight, String type) {
		this.color = color;
		this.weight = weight;
		this.type = type;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Apple [color=").append(color).append(", weight=").append(weight).append(", type=").append(type)
				.append("]");
		return builder.toString();
	}

}
