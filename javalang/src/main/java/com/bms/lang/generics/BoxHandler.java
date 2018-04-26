package com.bms.lang.generics;

import java.util.List;

public class BoxHandler<T> {
	
	private Box<T> box;

	public static void main(String[] args) {
		BoxHandler<Integer> bh = new BoxHandler<Integer>();
		bh.box = new Box<>();
		bh.run(bh.box);
	}
	
	@SuppressWarnings("unchecked")
	private void run(Box<T> box) {
		box.setBoxContents((T)"Self Made Box");
		System.out.println(box);
		
		BoxGenerator bg = new BoxGenerator();
		List<Box<? extends Object>> blist = bg.getBoxStringList(5);
		for (Box<?> b: blist) {
			System.out.println(b);
		}
	}

}
