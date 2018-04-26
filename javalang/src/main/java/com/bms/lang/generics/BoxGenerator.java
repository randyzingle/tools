package com.bms.lang.generics;

import java.util.ArrayList;
import java.util.List;

public class BoxGenerator {

	public List<Box<? extends Object>> getBoxStringList(int numberBoxes) {
		List<Box<? extends Object>> boxList = new ArrayList<>();
		for (int i=0; i<numberBoxes; i++) {
			Box<String> box = new Box<>();
			box.setBoxContents("box-"+i);
			boxList.add(box);
		}
		return boxList;
	}
}
