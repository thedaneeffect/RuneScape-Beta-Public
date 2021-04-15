package rs.util;

public class CacheableNode extends Node {

	public CacheableNode nextCacheable;
	public CacheableNode prevCacheable;

	public void uncache() {
		if (prevCacheable != null) {
			prevCacheable.nextCacheable = nextCacheable;
			nextCacheable.prevCacheable = prevCacheable;
			nextCacheable = null;
			prevCacheable = null;
		}
	}
}
