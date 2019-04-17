package com.mbronshteyn.pingo20.data;

public class LinkedNode<T> {

	private LinkedNode<T> next = null;
	private LinkedNode<T> previous = null;
	private T content;

	protected LinkedNode(T content) {
		this.content = content;
	}

	protected LinkedNode<T> getNext() {
		return next;
	}

	protected void setNext(LinkedNode<T> next) {
		this.next = next;
	}

	protected LinkedNode<T> getPrevious() {
		return previous;
	}

	protected void setPrevious(LinkedNode<T> previous) {
		this.previous = previous;
	}

	protected T getContent() {
		return content;
	}

	protected void setContent(T content) {
		this.content = content;
	}

}
