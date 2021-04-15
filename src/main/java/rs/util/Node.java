package rs.util;

public class Node {

	public long id;
	public Node prev;
	public Node next;

	public void unlink() {
		if (next != null) {
			next.prev = prev;
			prev.next = next;
			prev = null;
			next = null;
		}
	}
}
