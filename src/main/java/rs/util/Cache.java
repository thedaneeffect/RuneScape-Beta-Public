package rs.util;

public final class Cache {

	private final int capacity;
	private int available;
	private final Hashtable table = new Hashtable(1024);
	private final Stack history = new Stack();

	public Cache(int length) {
		capacity = length;
		available = length;
	}

	public CacheableNode get(long key) {
		CacheableNode node = (CacheableNode) table.get(key);
		if (node != null) {
			history.push(node);
		}
		return node;
	}

	public void put(long key, CacheableNode value) {
		if (available == 0) {
			CacheableNode l1 = history.pop();
			l1.unlink();
			l1.uncache();
		} else {
			available--;
		}
		table.put(key, value);
		history.push(value);
	}

	public void clear() {
		for (;;) {
			CacheableNode node = history.pop();
			if (node == null) {
				break;
			}
			node.unlink();
			node.uncache();
		}
		available = capacity;
	}
}
