package rs.util;

public final class Hashtable {

	private final int size;
	private final Node[] buckets;

	public Hashtable(int size) {
		this.size = size;
		buckets = new Node[size];

		for (int i = 0; i < size; i++) {
			Node node = buckets[i] = new Node();
			node.prev = node;
			node.next = node;
		}
	}

	public Node get(long key) {
		Node start = buckets[(int) (key & (long) (size - 1))];
		for (Node node = start.prev; node != start; node = node.prev) {
			if (node.id == key) {
				return node;
			}
		}
		return null;
	}

	public void put(long key, Node value) {
		if (value.next != null) {
			value.unlink();
		}
		Node node = buckets[(int) (key & (long) (size - 1))];
		value.next = node.next;
		value.prev = node;
		value.next.prev = value;
		value.prev.next = value;
		value.id = key;
	}
}
