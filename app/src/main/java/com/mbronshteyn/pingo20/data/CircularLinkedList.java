package com.mbronshteyn.pingo20.data;

public class CircularLinkedList<T> {

	private LinkedNode<T> tail = null;
	private LinkedNode<T> pointer = null;

	public CircularLinkedList(T content) {

		this.tail = new LinkedNode<T>(content);

		this.tail.setPrevious(this.tail);
		this.tail.setNext(this.tail);
		this.pointer = this.tail;

	}

	public void add(T content) {

		LinkedNode<T> node = new LinkedNode<T>(content);

		LinkedNode<T> headNode = tail.getPrevious();
		headNode.setNext(node);
		node.setNext(tail);
		node.setPrevious(headNode);
		tail.setPrevious(node);

	}

	public T getTail() {

		pointer = tail;
		return tail.getContent();
	}

	public T getNextNode() {

		pointer = pointer.getNext();

		return pointer.getContent();
	}

	public T getPreviousNode() {

		pointer = pointer.getPrevious();

		return pointer.getContent();
	}

	public T remove() {

		LinkedNode<T> node = pointer;

		pointer.getPrevious().setNext(pointer.getNext());
		pointer.getNext().setPrevious(pointer.getPrevious());

		if (tail.getContent().equals(pointer.getContent())) {
			tail = pointer.getNext();
		}

		pointer = pointer.getNext();

		return node.getContent();

	}

}
