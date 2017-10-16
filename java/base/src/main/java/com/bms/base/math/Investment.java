package com.bms.base.math;

public class Investment {

	public static void main(String[] args) {
		double b = 0.0; // balance
		double p = 400000; // principal
		double ymax = 10.0; // number of years work
		double ytot = 35.0; // total number of years
		double c = 43900.0; // annual contribution (my 401K $18900, melissa $13000?, joint $12000)
		double r = 0.04; // growth rate % - inflation
		double w = 40000.0; // yearly withdraw (we'll get ~48,000 / year in SS) 
		
		for (int y=1; y<ymax; y++) {
			b = p * Math.pow((1+r), y) + c * ( (Math.pow((1+r), (y+1)) - (1+r)) / r );
			System.out.printf("%d %d %6.1f%n", y+2017, y+51, b);
		}
		
		double bstart = b;
		double xx = 0;
		for (int y=0; y<=ytot-ymax; y++) {
			xx = w*Math.pow((1+0.03), y);
			b = b*(1+r) - xx;
			System.out.printf("%d %d %6.1f%n", y+2017+(int)ymax, y+51+(int)ymax, b);
		}
		System.out.println("Final year's expenses: " + xx);

	}

}
