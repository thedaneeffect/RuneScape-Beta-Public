package rs.util;

public final class Stack {

	private CacheableNode head = new CacheableNode();

	public Stack() {
		head.nextCacheable = head;
		head.prevCacheable = head;
	}

	public void push(CacheableNode node) {
		if (node.prevCacheable != null) {
			node.uncache();
		}
		node.prevCacheable = head.prevCacheable;
		node.nextCacheable = head;
		node.prevCacheable.nextCacheable = node;
		node.nextCacheable.prevCacheable = node;
	}

	public CacheableNode pop() {
		CacheableNode node = head.nextCacheable;
		if (node == head) return null;
		node.uncache();
		return node;
	}
}
