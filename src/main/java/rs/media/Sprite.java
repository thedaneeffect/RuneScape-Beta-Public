package rs.media;

import rs.io.Archive;
import rs.io.Buffer;

import java.awt.*;
import java.awt.image.PixelGrabber;

public class Sprite extends Draw2D {

	public int[] pixels;
	public int width;
	public int height;
	public int clipX;
	public int clipY;
	public int clipWidth;
	public int clipHeight;

	public Sprite(int w, int h) {
		pixels = new int[w * h];
		width = clipWidth = w;
		height = clipHeight = h;
		clipX = clipY = 0;
	}

	public Sprite(byte[] src, Component c) {
		try {
			Image i = Toolkit.getDefaultToolkit().createImage(src);
			MediaTracker mt = new MediaTracker(c);
			mt.addImage(i, 0);
			mt.waitForAll();
			width = i.getWidth(c);
			height = i.getHeight(c);
			clipWidth = width;
			clipHeight = height;
			clipX = 0;
			clipY = 0;
			pixels = new int[width * height];
			PixelGrabber pg = new PixelGrabber(i, 0, 0, width, height, pixels, 0, width);
			pg.grabPixels();
		} catch (Exception e) {
			System.out.println("Error converting jpg");
		}
	}

	public Sprite(Archive archive, String name, int index) {
		Buffer dat = new Buffer(archive.get(name + ".dat", null));
		Buffer idx = new Buffer(archive.get("index.dat", null));
		idx.position = dat.get2U();

		clipWidth = idx.get2U();
		clipHeight = idx.get2U();

		int[] palette = new int[idx.get1U()];

		for (int i = 0; i < palette.length - 1; i++) {
			palette[i + 1] = idx.get3();
			if (palette[i + 1] == 0) {
				palette[i + 1] = 1;
			}
		}

		for (int i = 0; i < index; i++) {
			idx.position += 2;
			dat.position += (idx.get2U() * idx.get2U());
			idx.position++;
		}

		clipX = idx.get1U();
		clipY = idx.get1U();
		width = idx.get2U();
		height = idx.get2U();

		int type = idx.get1U();
		int len = width * height;
		pixels = new int[len];

		if (type == 0) {
			for (int i = 0; i < len; i++) {
				pixels[i] = palette[dat.get1U()];
			}
		} else if (type == 1) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					pixels[x + y * width] = palette[dat.get1U()];
				}
			}
		}
	}

	public void prepare() {
		Draw2D.prepare(pixels, width, height);
	}

	public void drawOpaque(int x, int y) {
		x += clipX;
		y += clipY;

		int dstOff = x + y * Draw2D.dstWidth;
		int srcOff = 0;
		int h = height;
		int w = width;
		int dstStep = Draw2D.dstWidth - w;
		int srcStep = 0;

		if (y < Draw2D.top) {
			int cutoff = Draw2D.top - y;
			h -= cutoff;
			y = Draw2D.top;
			srcOff += cutoff * w;
			dstOff += cutoff * Draw2D.dstWidth;
		}

		if (y + h > Draw2D.bottom) {
			h -= y + h - Draw2D.bottom;
		}

		if (x < Draw2D.left) {
			int cutoff = Draw2D.left - x;
			w -= cutoff;
			x = Draw2D.left;
			srcOff += cutoff;
			dstOff += cutoff;
			srcStep += cutoff;
			dstStep += cutoff;
		}
		if (x + w > Draw2D.right) {
			int i_22_ = x + w - Draw2D.right;
			w -= i_22_;
			srcStep += i_22_;
			dstStep += i_22_;
		}

		if (w > 0 && h > 0) {
			copyImage(w, h, pixels, srcOff, srcStep, Draw2D.dst, dstOff, dstStep);
		}
	}

	private void copyImage(int w, int h, int[] src, int srcOff, int srcStep, int[] dst, int dstOff, int dstStep) {
		int hw = -(w >> 2);
		w = -(w & 0x3);

		for (int y = -h; y < 0; y++) {
			for (int x = hw; x < 0; x++) {
				dst[dstOff++] = src[srcOff++];
				dst[dstOff++] = src[srcOff++];
				dst[dstOff++] = src[srcOff++];
				dst[dstOff++] = src[srcOff++];
			}

			for (int x = w; x < 0; x++) {
				dst[dstOff++] = src[srcOff++];
			}

			dstOff += dstStep;
			srcOff += srcStep;
		}
	}

	public void draw(int x, int y) {
		x += clipX;
		y += clipY;

		int dstOff = x + y * Draw2D.dstWidth;
		int srcOff = 0;
		int w = width;
		int h = height;
		int dstStep = Draw2D.dstWidth - w;
		int srcStep = 0;

		if (y < Draw2D.top) {
			int cutoff = Draw2D.top - y;
			h -= cutoff;
			y = Draw2D.top;
			srcOff += cutoff * w;
			dstOff += cutoff * Draw2D.dstWidth;
		}

		if (y + h > Draw2D.bottom) {
			h -= y + h - Draw2D.bottom;
		}

		if (x < Draw2D.left) {
			int cutoff = Draw2D.left - x;
			w -= cutoff;
			x = Draw2D.left;
			srcOff += cutoff;
			dstOff += cutoff;
			srcStep += cutoff;
			dstStep += cutoff;
		}

		if (x + w > Draw2D.right) {
			int cutoff = x + w - Draw2D.right;
			w -= cutoff;
			srcStep += cutoff;
			dstStep += cutoff;
		}

		if (w > 0 && h > 0) {
			copyImage(h, w, pixels, srcOff, srcStep, Draw2D.dst, dstOff, dstStep, 0);
		}
	}

	public void copyImage(int h, int w, int[] src, int srcOff, int srcStep, int[] dst, int dstOff, int dstStep, int rgb) {
		int hw = -(w >> 2);
		w = -(w & 0x3);
		for (int x = -h; x < 0; x++) {
			for (int y = hw; y < 0; y++) {
				rgb = src[srcOff++];

				if (rgb != 0) {
					dst[dstOff++] = rgb;
				} else {
					dstOff++;
				}

				rgb = src[srcOff++];

				if (rgb != 0) {
					dst[dstOff++] = rgb;
				} else {
					dstOff++;
				}

				rgb = src[srcOff++];

				if (rgb != 0) {
					dst[dstOff++] = rgb;
				} else {
					dstOff++;
				}

				rgb = src[srcOff++];

				if (rgb != 0) {
					dst[dstOff++] = rgb;
				} else {
					dstOff++;
				}
			}

			for (int y = w; y < 0; y++) {
				rgb = src[srcOff++];

				if (rgb != 0) {
					dst[dstOff++] = rgb;
				} else {
					dstOff++;
				}
			}

			dstOff += dstStep;
			srcOff += srcStep;
		}
	}

	public void draw(int x, int y, int alpha) {
		x += clipX;
		y += clipY;

		int dstOff = x + y * Draw2D.dstWidth;
		int srcOff = 0;
		int w = width;
		int h = height;
		int dstStep = Draw2D.dstWidth - w;
		int srcStep = 0;

		if (y < Draw2D.top) {
			int cutoff = Draw2D.top - y;
			h -= cutoff;
			y = Draw2D.top;
			srcOff += cutoff * w;
			dstOff += cutoff * Draw2D.dstWidth;
		}

		if (y + h > Draw2D.bottom) {
			h -= y + h - Draw2D.bottom;
		}

		if (x < Draw2D.left) {
			int cutoff = Draw2D.left - x;
			w -= cutoff;
			x = Draw2D.left;
			srcOff += cutoff;
			dstOff += cutoff;
			srcStep += cutoff;
			dstStep += cutoff;
		}

		if (x + w > Draw2D.right) {
			int cutoff = x + w - Draw2D.right;
			w -= cutoff;
			srcStep += cutoff;
			dstStep += cutoff;
		}

		if (w > 0 && h > 0) {
			copyImage(w, h, pixels, srcOff, srcStep, Draw2D.dst, dstOff, dstStep, alpha, 0);
		}
	}

	private void copyImage(int w, int h, int[] src, int srcOff, int srcStep, int[] dst, int dstOff, int dstStep, int alpha, int rgb) {
		int opacity = 256 - alpha;
		for (int y = -h; y < 0; y++) {
			for (int x = -w; x < 0; x++) {
				rgb = src[srcOff++];
				if (rgb != 0) {
					int dstRGB = dst[dstOff];
					dst[dstOff++] = ((((rgb & 0xff00ff) * alpha + (dstRGB & 0xff00ff) * opacity) & ~0xff00ff) + (((rgb & 0xff00) * alpha + (dstRGB & 0xff00) * opacity) & 0xff0000)) >> 8;
				} else {
					dstOff++;
				}
			}
			dstOff += dstStep;
			srcOff += srcStep;
		}
	}

	public void draw(int x, int y, int w, int h, int pivotX, int pivotY, int theta, int[] lineStart, int[] lineWidth) {
		try {
			int cx = w / 2;
			int cy = h / 2;

			int sin = (int) (Math.sin(theta / 326.11) * 65536.0);
			int cos = (int) (Math.cos(theta / 326.11) * 65536.0);

			int originX = (pivotX << 16) - ((cy * sin) + (cx * cos));
			int originY = (pivotY << 16) - ((cy * cos) - (cx * sin));

			int origin = x + (y * Draw2D.dstWidth);

			for (y = 0; y < h; y++) {
				int start = lineStart[y];
				int dstOff = origin + start;

				int srcX = originX + (cos * start);
				int srcY = originY - (sin * start);

				for (x = 0; x < lineWidth[y]; x++) {
					Draw2D.dst[dstOff++] = pixels[(srcX >> 16) + (srcY >> 16) * width];
					srcX += cos;
					srcY -= sin;
				}

				originX += sin;
				originY += cos;
				origin += Draw2D.dstWidth;
			}
		} catch (Exception ignored) {
		}
	}

	public void draw(int x, int y, int w, int h, int anchorx, int anchory, int theta) {
		try {
			int centerX = -w / 2;
			int centerY = -h / 2;

			int sin = (int) (Math.sin(theta / 326.11) * 65536.0);
			int cos = (int) (Math.cos(theta / 326.11) * 65536.0);

			int originX = (anchorx << 16) + ((centerY * sin) + (centerX * cos));
			int originY = (anchory << 16) + ((centerY * cos) - (centerX * sin));
			int origin = x + (y * Draw2D.dstWidth);

			for (y = 0; y < h; y++) {
				int dstOff = origin;
				int srcX = originX + cos;
				int srcY = originY - sin;

				for (x = 0; x < w; x++) {
					int rgb = pixels[(srcX >> 16) + (srcY >> 16) * width];

					if (rgb != 0) {
						Draw2D.dst[dstOff++] = rgb;
					} else {
						dstOff++;
					}
					srcX += cos;
					srcY -= sin;
				}
				originX += sin;
				originY += cos;
				origin += Draw2D.dstWidth;
			}
		} catch (Exception ignored) {
		}
	}

	public void draw(IndexedSprite mask, int x, int y) {
		x += clipX;
		y += clipY;

		int dstOff = x + y * Draw2D.dstWidth;
		int srcOff = 0;

		int h = height;
		int w = width;

		int dstStep = Draw2D.dstWidth - w;
		int srcStep = 0;

		if (y < Draw2D.top) {
			int i = Draw2D.top - y;
			h -= i;
			y = Draw2D.top;
			srcOff += i * w;
			dstOff += i * Draw2D.dstWidth;
		}

		if (y + h > Draw2D.bottom) {
			h -= y + h - Draw2D.bottom;
		}

		if (x < Draw2D.left) {
			int i = Draw2D.left - x;
			w -= i;
			x = Draw2D.left;
			srcOff += i;
			dstOff += i;
			srcStep += i;
			dstStep += i;
		}
		if (x + w > Draw2D.right) {
			int i = x + w - Draw2D.right;
			w -= i;
			srcStep += i;
			dstStep += i;
		}

		if (w > 0 && h > 0) {
			copyImage(Draw2D.dst, srcOff, 0, h, srcStep, dstOff, dstStep, pixels, mask.pixels, w);
		}
	}

	private void copyImage(int[] is, int i, int i_111_, int i_112_, int i_113_, int i_114_, int i_115_, int[] is_116_, byte[] is_117_, int i_118_) {
		int i_119_ = -(i_118_ >> 2);
		i_118_ = -(i_118_ & 0x3);
		for (int i_120_ = -i_112_; i_120_ < 0; i_120_++) {
			for (int i_121_ = i_119_; i_121_ < 0; i_121_++) {
				i_111_ = is_116_[i++];
				if (i_111_ != 0 && is_117_[i_114_] == 0) {
					is[i_114_++] = i_111_;
				} else {
					i_114_++;
				}
				i_111_ = is_116_[i++];
				if (i_111_ != 0 && is_117_[i_114_] == 0) {
					is[i_114_++] = i_111_;
				} else {
					i_114_++;
				}
				i_111_ = is_116_[i++];
				if (i_111_ != 0 && is_117_[i_114_] == 0) {
					is[i_114_++] = i_111_;
				} else {
					i_114_++;
				}
				i_111_ = is_116_[i++];
				if (i_111_ != 0 && is_117_[i_114_] == 0) {
					is[i_114_++] = i_111_;
				} else {
					i_114_++;
				}
			}
			for (int i_122_ = i_118_; i_122_ < 0; i_122_++) {
				i_111_ = is_116_[i++];
				if (i_111_ != 0 && is_117_[i_114_] == 0) {
					is[i_114_++] = i_111_;
				} else {
					i_114_++;
				}
			}
			i_114_ += i_115_;
			i += i_113_;
		}
	}


}
