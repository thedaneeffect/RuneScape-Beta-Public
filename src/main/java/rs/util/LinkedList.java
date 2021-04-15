package rs.util;

public final class LinkedList {

	private Node head = new Node();
	private Node selected;

	public LinkedList() {
		head.prev = head;
		head.next = head;
	}

	public void push(Node node) {
		if (node.next != null) {
			node.unlink();
		}
		node.next = head.next;
		node.prev = head;
		node.next.prev = node;
		node.prev.next = node;
	}

	public Node poll() {
		Node node = head.prev;
		if (node == head) {
			return null;
		}
		node.unlink();
		return node;
	}

	public Node peekLast() {
		Node node = head.prev;
		if (node == head) {
			selected = null;
			return null;
		}
		selected = node.prev;
		return node;
	}

	public Node peekFirst() {
		Node node = head.next;
		if (node == head) {
			selected = null;
			return null;
		}
		selected = node.next;
		return node;
	}

	public Node getPrevious() {
		Node node = selected;
		if (node == head) {
			selected = null;
			return null;
		}
		selected = node.prev;
		return node;
	}

	public Node getNext() {
		Node node = selected;
		if (node == head) {
			selected = null;
			return null;
		}
		selected = node.next;
		return node;
	}

	public int size() {
		int count = 0;
		for (Node node = head.prev; node != head; node = node.prev) {
			count++;
		}
		return count;
	}

	public void clear() {
		for (; ; ) {
			Node node = head.prev;
			if (node == head) {
				break;
			}
			node.unlink();
		}
	}
}
