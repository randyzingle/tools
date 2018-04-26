package com.bms.lang.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.bms.lang.collections.data.Apple;
import com.bms.lang.collections.data.Car;

public class Predicate1 {
	
	public static void main(String[] args) {
		List<Apple> apples = Apple.getApples(10);
		List<Apple> greenApples = filterApples(apples, Predicate1::isGreenApple);
		List<Apple> heavyApples = filterApples(apples, Predicate1::isHeavyApple);
		System.out.println("Green Apples: " + greenApples);
//		System.out.println(heavyApples);
		List<Apple> redApples = filterApples(apples, (Apple apple) -> "red".equals(apple.getColor()));
		System.out.println("Red Apples: " + redApples);
		List<Apple> redBaldurtoshes = filterApples(apples, (Apple apple) -> 
			"red".equals(apple.getColor()) && "baldurtosh".equals(apple.getType()));
		System.out.println("Baldurtoshes: " + redBaldurtoshes);
		
		List<Car> cars = Car.getCars(5);
		List<Car> goldCars = filterCars(cars, Predicate1::isGoldCar);
		List<Car> heavyCars = filterBuiltIn(cars, Predicate1::isHeavyCar);
//		System.out.println(goldCars);
//		System.out.println(heavyCars);
	}
	
	public static boolean isGoldCar(Car car) {
		return "gold".equals(car.getColor());
	}
	
	public static boolean isHeavyCar(Car car) {
		return car.getWeight() > 5800;
	}
	
	public static boolean isGreenApple(Apple apple) {
		return "green".equals(apple.getColor());
	}
	
	public static boolean isHeavyApple(Apple apple) {
		return apple.getWeight() > 15;
	}
	
	public interface BaldurAI<T> {
		boolean test(T t);
	}
	
	private static List<Car> filterBuiltIn(List<Car> list, Predicate<Car> p) {
		List<Car> result = new ArrayList<>();
		for (Car car : list) {
			if (p.test(car)) result.add(car);
		}
		return result;
	}
	
	private static List<Car> filterCars(List<Car> list, BaldurAI<Car> bai) {
		List<Car> result = new ArrayList<>();
		for (Car car: list) {
			if(bai.test(car)) result.add(car);
		}
		return result;
	}
	
	private static List<Apple> filterApples(List<Apple> inventory, BaldurAI<Apple> bai) {
		List<Apple> result = new ArrayList<>();
		for (Apple apple: inventory) {
			if(bai.test(apple)) result.add(apple);
		}
		return result;
	}

}
