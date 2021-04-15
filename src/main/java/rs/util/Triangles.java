package rs.util;

public final class Triangles {

	public static boolean contains(int x, int y, int y0, int y1, int y2, int x0, int x1, int x2) {
		if (y < y0 && y < y1 && y < y2) {
			return false;
		}
		if (y > y0 && y > y1 && y > y2) {
			return false;
		}
		if (x < x0 && x < x1 && x < x2) {
			return false;
		}
		return !(x > x0 && x > x1 && x > x2);
	}

	private Triangles() {

	}
}
