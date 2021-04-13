package rs.media;

import rs.io.Archive;
import rs.io.Buffer;

public final class IndexedFont extends Draw2D {

	public static int[] CHAR_LOOKUP = new int[256];

	public byte[][] pixels = new byte[94][];
	public int[] charWidth = new int[94];
	public int[] charHeight = new int[94];
	public int[] charOffsetX = new int[94];
	public int[] charOffsetY = new int[94];
	public int[] charSpace = new int[95];
	public int[] drawWidth = new int[256];
	public int height;

	public IndexedFont(String name, Archive archive) {
		Buffer dat = new Buffer(archive.get(name + ".dat", null));
		Buffer idx = new Buffer(archive.get("index.dat", null));
		idx.position = dat.get2U() + 4;

		int off = idx.get1U();

		if (off > 0) {
			idx.position += (off - 1) * 3;
		}

		for (int n = 0; n < 94; n++) {
			charOffsetX[n] = idx.get1U();
			charOffsetY[n] = idx.get1U();

			int w = charWidth[n] = idx.get2U();
			int h = charHeight[n] = idx.get2U();

			int type = idx.get1U();
			int len = w * h;
			pixels[n] = new byte[len];

			if (type == 0) {
				for (int i = 0; i < len; i++) {
					pixels[n][i] = dat.get1();
				}
			} else if (type == 1) {
				for (int x = 0; x < w; x++) {
					for (int y = 0; y < h; y++) {
						pixels[n][x + y * w] = dat.get1();
					}
				}
			}

			if (h > height) {
				height = h;
			}

			charOffsetX[n] = 1;
			charSpace[n] = w + 2;

			int i = 0;

			for (int y = h / 7; y < h; y++) {
				i += pixels[n][y * w];
			}

			if (i <= h / 7) {
				charSpace[n]--;
				charOffsetX[n] = 0;
			}

			i = 0;

			for (int y = h / 7; y < h; y++) {
				i += pixels[n][w - 1 + y * w];
			}

			if (i <= h / 7) {
				charSpace[n]--;
			}
		}

		charSpace[94] = charSpace[8];

		for (int c = 0; c < 256; c++) {
			drawWidth[c] = charSpace[CHAR_LOOKUP[c]];
		}
	}

	public void drawRightAligned(String s, int x, int y, int rgb) {
		draw(s, x - stringWidth(s), y, rgb);
	}

	public void drawCentered(String s, int x, int y, int rgb) {
		draw(s, x - stringWidth(s) / 2, y, rgb);
	}

	public void drawTaggableCentered(String s, int x, int y, int rgb, boolean shadow) {
		drawTaggable(s, x - stringWidth(s) / 2, y, rgb, shadow);
	}

	public int stringWidth(String s) {
		if (s == null) {
			return 0;
		}
		int w = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '@' && i + 4 < s.length() && s.charAt(i + 4) == '@') {
				i += 4;
			} else {
				w += drawWidth[s.charAt(i)];
			}
		}
		return w;
	}

	public void draw(String string, int x, int y, int rgb) {
		y -= height;
		for (int i = 0; i < string.length(); i++) {
			int c = CHAR_LOOKUP[string.charAt(i)];
			if (c != 94) {
				drawChar(pixels[c], x + charOffsetX[c], y + charOffsetY[c], charWidth[c], charHeight[c], rgb);
			}
			x += charSpace[c];
		}
	}

	public void drawCenteredWave(String s, int x, int y, int rgb, int phase) {
		x -= stringWidth(s) / 2;
		y -= height;
		for (int i = 0; i < s.length(); i++) {
			int c = CHAR_LOOKUP[s.charAt(i)];
			if (c != 94) {
				drawChar(pixels[c], x + charOffsetX[c], (y + charOffsetY[c] + (int) (Math.sin((i / 2.0) + (phase / 5.0)) * 5.0)), charWidth[c], charHeight[c], rgb);
			}
			x += charSpace[c];
		}
	}

	public void drawTaggable(String s, int x, int y, int rgb, boolean shadow) {
		if (s == null) {
			return;
		}

		y -= height;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '@' && i + 4 < s.length() && s.charAt(i + 4) == '@') {
				String tag = s.substring(i + 1, i + 4);
				if (tag.equals("red")) {
					rgb = 16711680;
				}
				if (tag.equals("gre")) {
					rgb = 65280;
				}
				if (tag.equals("blu")) {
					rgb = 255;
				}
				if (tag.equals("yel")) {
					rgb = 16776960;
				}
				if (tag.equals("cya")) {
					rgb = 65535;
				}
				if (tag.equals("mag")) {
					rgb = 16711935;
				}
				if (tag.equals("whi")) {
					rgb = 16777215;
				}
				if (tag.equals("bla")) {
					rgb = 0;
				}
				if (tag.equals("lre")) {
					rgb = 16748608;
				}
				if (tag.equals("dre")) {
					rgb = 8388608;
				}
				if (tag.equals("dbl")) {
					rgb = 128;
				}
				if (tag.equals("or1")) {
					rgb = 16756736;
				}
				if (tag.equals("or2")) {
					rgb = 16740352;
				}
				if (tag.equals("or3")) {
					rgb = 16723968;
				}
				if (tag.equals("gr1")) {
					rgb = 12648192;
				}
				if (tag.equals("gr2")) {
					rgb = 8453888;
				}
				if (tag.equals("gr3")) {
					rgb = 4259584;
				}
				i += 4;
			} else {
				int c = CHAR_LOOKUP[s.charAt(i)];
				if (c != 94) {
					if (shadow) {
						drawChar(pixels[c], x + charOffsetX[c] + 1, y + charOffsetY[c] + 1, charWidth[c], charHeight[c], 1);
					}
					drawChar(pixels[c], x + charOffsetX[c], y + charOffsetY[c], charWidth[c], charHeight[c], rgb);
				}
				x += charSpace[c];
			}
		}
	}

	private void drawChar(byte[] data, int x, int y, int w, int h, int rgb) {
		int dstOff = x + y * dstWidth;
		int dstStep = dstWidth - w;
		int srcStep = 0;
		int srcOff = 0;

		if (y < top) {
			int cutoff = top - y;
			h -= cutoff;
			y = top;
			srcOff += cutoff * w;
			dstOff += cutoff * dstWidth;
		}

		if (y + h >= bottom) {
			h -= y + h - bottom + 1;
		}

		if (x < left) {
			int cutoff = left - x;
			w -= cutoff;
			x = left;
			srcOff += cutoff;
			dstOff += cutoff;
			srcStep += cutoff;
			dstStep += cutoff;
		}

		if (x + w >= right) {
			int cutoff = x + w - right + 1;
			w -= cutoff;
			srcStep += cutoff;
			dstStep += cutoff;
		}

		if (w > 0 && h > 0) {
			drawMask(Draw2D.dst, data, rgb, srcOff, dstOff, w, h, dstStep, srcStep);
		}
	}

	private void drawMask(int[] dst, byte[] src, int rgb, int srcOff, int dstOff, int w, int h, int dstStep, int srcStep) {
		int hw = -(w >> 2);
		w = -(w & 0x3);

		for (int y = -h; y < 0; y++) {
			for (int x = hw; x < 0; x++) {
				if (src[srcOff++] != 0) {
					dst[dstOff++] = rgb;
				} else {
					dstOff++;
				}

				if (src[srcOff++] != 0) {
					dst[dstOff++] = rgb;
				} else {
					dstOff++;
				}

				if (src[srcOff++] != 0) {
					dst[dstOff++] = rgb;
				} else {
					dstOff++;
				}

				if (src[srcOff++] != 0) {
					dst[dstOff++] = rgb;
				} else {
					dstOff++;
				}
			}

			for (int x = w; x < 0; x++) {
				if (src[srcOff++] != 0) {
					dst[dstOff++] = rgb;
				} else {
					dstOff++;
				}
			}

			dstOff += dstStep;
			srcOff += srcStep;
		}
	}

	static {
		String s = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"\u00a3$%^&*()-_=+[{]};:'@#~,<.>/?\\| ";

		for (int i = 0; i < 256; i++) {
			int c = s.indexOf(i);
			if (c == -1) {
				c = 74;
			}
			CHAR_LOOKUP[i] = c;
		}
	}
}
