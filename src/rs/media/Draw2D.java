package rs.media;

import rs.util.CacheableNode;
import rs.scene.Model;

public class Draw2D extends CacheableNode {

	public static int[] dst;
	public static int dstWidth;
	public static int dstHeight;
	public static int top;
	public static int bottom;
	public static int left;
	public static int right;
	public static int rightX;
	public static int centerX;
	public static int centerY;

	// used for drawing circles
	private static final int[] tmpX = new int[64];
	private static final int[] tmpY = new int[64];

	public static void prepare(int[] data, int w, int h) {
		Draw2D.dst = data;
		dstWidth = w;
		dstHeight = h;
		setBounds(0, 0, w, h);
	}

	public static void resetBounds() {
		left = 0;
		top = 0;
		right = dstWidth;
		bottom = dstHeight;
		rightX = right - 1;
		centerX = right / 2;
	}

	public static void setBounds(int x0, int y0, int x1, int y1) {
		if (x0 < 0) {
			x0 = 0;
		}
		if (y0 < 0) {
			y0 = 0;
		}
		if (x1 > dstWidth) {
			x1 = dstWidth;
		}
		if (y1 > dstHeight) {
			y1 = dstHeight;
		}
		left = x0;
		top = y0;
		right = x1;
		bottom = y1;
		rightX = right - 1;
		centerX = right / 2;
		centerY = bottom / 2;
	}

	public static void clear() {
		int len = dstWidth * dstHeight;
		for (int i = 0; i < len; i++) {
			dst[i] = 0;
		}
	}

	public static void plot(int x, int y, int rgb) {
		if (x < 0 || x >= dstWidth || y < 0 || y >= dstHeight) {
			return;
		}
		dst[x + (y * dstWidth)] = rgb;
	}

	public static void fillOval(int x, int y, int w, int h, int rgb, int segments) {
		int cx = x + (w / 2);
		int cy = y + (h / 2);

		for (int i = 0; i < segments; i++) {
			int angle = (i << 11) / segments;

			tmpX[i] = x + ((w * Model.cos[angle]) >> 16);
			tmpY[i] = y + ((h * Model.sin[angle]) >> 16);
		}

		for (int i = 1; i < segments; i++) {
			x = tmpX[i - 1];
			y = tmpY[i - 1];
			int x1 = tmpX[i];
			int y1 = tmpY[i];

			Draw3D.fillTriangle(cx, cy, x, y, x1, y1, rgb);
		}
	}

	public static void drawLine(int x1, int y1, int x2, int y2, int rgb) {
		int d = 0;

		int dy = Math.abs(y2 - y1);
		int dx = Math.abs(x2 - x1);

		int dy2 = (dy << 1);
		int dx2 = (dx << 1);

		int ix = x1 < x2 ? 1 : -1;
		int iy = y1 < y2 ? 1 : -1;

		if (dy <= dx) {
			for (; ; ) {
				plot(x1, y1, rgb);

				if (x1 == x2) {
					break;
				}

				x1 += ix;
				d += dy2;

				if (d > dx) {
					y1 += iy;
					d -= dx2;
				}
			}
		} else {
			for (; ; ) {
				plot(x1, y1, rgb);

				if (y1 == y2) {
					break;
				}

				y1 += iy;
				d += dx2;

				if (d > dy) {
					x1 += ix;
					d -= dy2;
				}
			}
		}
	}

	public static void fillRect(int x, int y, int w, int h, int rgb) {
		if (x < left) {
			w -= left - x;
			x = left;
		}

		if (y < top) {
			h -= top - y;
			y = top;
		}

		if (x + w > right) {
			w = right - x;
		}

		if (y + h > bottom) {
			h = bottom - y;
		}

		int stride = dstWidth - w;
		int off = x + y * dstWidth;

		for (int i = -h; i < 0; i++) {
			for (int j = -w; j < 0; j++) {
				dst[off++] = rgb;
			}
			off += stride;
		}
	}

	public static void drawRect(int x, int y, int w, int h, int rgb) {
		drawHorizontalLine(x, y, w, rgb);
		drawHorizontalLine(x, y + h - 1, w, rgb);
		drawVerticalLine(x, y, h, rgb);
		drawVerticalLine(x + w - 1, y, h, rgb);
	}

	public static void drawHorizontalLine(int x, int y, int len, int rgb) {
		if (y >= top && y < bottom) {
			if (x < left) {
				len -= left - x;
				x = left;
			}
			if (x + len > right) {
				len = right - x;
			}
			int off = x + y * dstWidth;
			for (int i = 0; i < len; i++) {
				dst[off + i] = rgb;
			}
		}
	}

	public static void drawVerticalLine(int x, int y, int len, int rgb) {
		if (x >= left && x < right) {
			if (y < top) {
				len -= top - y;
				y = top;
			}
			if (y + len > bottom) {
				len = bottom - y;
			}
			int off = x + y * dstWidth;
			for (int i = 0; i < len; i++) {
				dst[off + i * dstWidth] = rgb;
			}
		}
	}
}
