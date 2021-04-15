package rs.media;

import rs.io.Archive;
import rs.io.Buffer;

public final class IndexedSprite extends Draw2D {

	public byte[] pixels;
	public int[] palette;
	public int width;
	public int height;
	public int clipX;
	public int clipY;
	public int clipWidth;
	public int clipHeight;

	public IndexedSprite(Archive archive, String name, int index) {
		Buffer dat = new Buffer(archive.get(name + ".dat", null));
		Buffer idx = new Buffer(archive.get("index.dat", null));

		idx.position = dat.get2U();

		clipWidth = idx.get2U();
		clipHeight = idx.get2U();

		palette = new int[idx.get1U()];

		for (int n = 0; n < palette.length - 1; n++) {
			palette[n + 1] = idx.get3();
		}

		for (int n = 0; n < index; n++) {
			idx.position += 2;
			dat.position += (idx.get2U() * idx.get2U());
			idx.position++;
		}

		clipX = idx.get1U();
		clipY = idx.get1U();
		width = idx.get2U();
		height = idx.get2U();

		int type = idx.get1U();
		pixels = new byte[width * height];

		if (type == 0) {
			for (int n = 0; n < pixels.length; n++) {
				pixels[n] = dat.get1();
			}
		} else if (type == 1) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					pixels[x + y * width] = dat.get1();
				}
			}
		}
	}

	public void shrink() {
		clipWidth /= 2;
		clipHeight /= 2;

		byte[] newPixels = new byte[clipWidth * clipHeight];
		int off = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				newPixels[((x + clipX >> 1) + (y + clipY >> 1) * clipWidth)] = pixels[off++];
			}
		}
		pixels = newPixels;
		width = clipWidth;
		height = clipHeight;
		clipX = 0;
		clipY = 0;
	}

	public void crop() {
		if (width != clipWidth || height != clipHeight) {
			byte[] newPixels = new byte[clipWidth * clipHeight];
			int off = 0;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					newPixels[x + clipX + (y + clipY) * clipWidth] = pixels[off++];
				}
			}
			pixels = newPixels;
			width = clipWidth;
			height = clipHeight;
			clipX = 0;
			clipY = 0;
		}
	}

	public void flipHorizontally() {
		byte[] flipped = new byte[width * height];
		int off = 0;
		for (int y = 0; y < height; y++) {
			for (int x = width - 1; x >= 0; x--) {
				flipped[off++] = pixels[x + y * width];
			}
		}
		pixels = flipped;
		clipX = clipWidth - width - clipX;
	}

	public void flipVertically() {
		byte[] flipped = new byte[width * height];
		int off = 0;
		for (int y = height - 1; y >= 0; y--) {
			for (int x = 0; x < width; x++) {
				flipped[off++] = pixels[x + y * width];
			}
		}
		pixels = flipped;
		clipY = clipHeight - height - clipY;
	}

	public void draw(int x, int y) {
		x += clipX;
		y += clipY;
		int dstOff = x + y * dstWidth;
		int srcOff = 0;
		int h = height;
		int w = width;
		int dstStep = dstWidth - w;
		int srcStep = 0;
		if (y < top) {
			int trim = top - y;
			h -= trim;
			y = top;
			srcOff += trim * w;
			dstOff += trim * dstWidth;
		}
		if (y + h > bottom) {
			h -= y + h - bottom;
		}
		if (x < left) {
			int trim = left - x;
			w -= trim;
			x = left;
			srcOff += trim;
			dstOff += trim;
			srcStep += trim;
			dstStep += trim;
		}
		if (x + w > right) {
			int trim = x + w - right;
			w -= trim;
			srcStep += trim;
			dstStep += trim;
		}
		if (w > 0 && h > 0) {
			copyImage(h, pixels, palette, w, dstOff, srcOff, dst, dstStep, srcStep);
		}
	}

	private void copyImage(int h, byte[] src, int[] palette, int w, int dstOff, int srcOff, int[] dst, int dstStep, int srcstep) {
		int hw = -(w >> 2);
		w = -(w & 0x3);
		for (int y = -h; y < 0; y++) {
			for (int x = hw; x < 0; x++) {
				int p = src[srcOff++];
				if (p != 0) {
					dst[dstOff++] = palette[p & 0xff];
				} else {
					dstOff++;
				}
				p = src[srcOff++];
				if (p != 0) {
					dst[dstOff++] = palette[p & 0xff];
				} else {
					dstOff++;
				}
				p = src[srcOff++];
				if (p != 0) {
					dst[dstOff++] = palette[p & 0xff];
				} else {
					dstOff++;
				}
				p = src[srcOff++];
				if (p != 0) {
					dst[dstOff++] = palette[p & 0xff];
				} else {
					dstOff++;
				}
			}
			for (int x = w; x < 0; x++) {
				int p = src[srcOff++];
				if (p != 0) {
					dst[dstOff++] = palette[p & 0xff];
				} else {
					dstOff++;
				}
			}
			dstOff += dstStep;
			srcOff += srcstep;
		}
	}
}
