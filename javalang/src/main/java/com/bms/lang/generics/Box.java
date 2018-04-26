package com.bms.lang.generics;

public class Box<T> {
	
	private T boxContents;

	public T getBoxContents() {
		return boxContents;
	}

	public void setBoxContents(T boxContent) {
		this.boxContents = boxContent;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Box [boxContent=").append(boxContents).append("]");
		return builder.toString();
	}
}
