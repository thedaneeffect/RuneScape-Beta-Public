package rs.data;

import rs.io.Archive;
import rs.io.Buffer;

public class FloType {

	public static int count;
	public static FloType[] instances;
	public int rgb;
	public int textureIndex = -1;
	public boolean occlude = true;
	public String name;
	public int hue;
	public int saturation;
	public int lightness;
	public int blendHue;
	public int hsl16;
	public int blendHueMultiplier;
	public int index;

	public static void unpack(Archive archive) {
		Buffer b = new Buffer(archive.get("flo.dat", null));

		count = b.get2U();

		if (instances == null) {
			instances = new FloType[count];
		}

		for (int n = 0; n < count; n++) {
			if (instances[n] == null) {
				instances[n] = new FloType();
			}
			instances[n].index = n;
			instances[n].read(b);
		}
	}

	private void setColor(int rgb) {
		double r = (double) (rgb >> 16 & 0xff) / 256.0;
		double g = (double) (rgb >> 8 & 0xff) / 256.0;
		double b = (double) (rgb & 0xff) / 256.0;

		double min = Math.min(Math.min(r, g), b);
		double max = Math.max(Math.max(r, g), b);

		double h = 0.0;
		double s = 0.0;
		double l = (min + max) / 2.0;

		if (min != max) {
			if (l < 0.5) {
				s = (max - min) / (max + min);
			}
			if (l >= 0.5) {
				s = (max - min) / (2.0 - max - min);
			}

			if (r == max) {
				h = (g - b) / (max - min);
			} else if (g == max) {
				h = 2.0 + (b - r) / (max - min);
			} else if (b == max) {
				h = 4.0 + (r - g) / (max - min);
			}
		}

		h /= 6.0;

		this.hue = (int) (h * 256.0);
		this.saturation = (int) (s * 256.0);
		this.lightness = (int) (l * 256.0);

		if (this.saturation < 0) {
			this.saturation = 0;
		} else if (this.saturation > 255) {
			this.saturation = 255;
		}

		if (this.lightness < 0) {
			this.lightness = 0;
		} else if (this.lightness > 255) {
			this.lightness = 255;
		}

		if (l > 0.5) {
			this.blendHueMultiplier = (int) ((1.0 - l) * s * 512.0);
		} else {
			this.blendHueMultiplier = (int) (l * s * 512.0);
		}

		if (this.blendHueMultiplier < 1) {
			this.blendHueMultiplier = 1;
		}

		this.blendHue = (int) (h * (double) this.blendHueMultiplier);

		// XXX: from 317
		setHSL16();
	}

	private void setHSL16() {
		int h0 = (hue + (int) (Math.random() * 16D)) - 8;

		if (h0 < 0) {
			h0 = 0;
		} else if (h0 > 255) {
			h0 = 255;
		}

		int s0 = (saturation + (int) (Math.random() * 48D)) - 24;

		if (s0 < 0) {
			s0 = 0;
		} else if (s0 > 255) {
			s0 = 255;
		}

		int l0 = (lightness + (int) (Math.random() * 48D)) - 24;

		if (l0 < 0) {
			l0 = 0;
		} else if (l0 > 255) {
			l0 = 255;
		}

		hsl16 = hsl24to16(h0, s0, l0);
	}

	public int hsl24to16(int h, int s, int l) {
		if (l > 179) {
			s /= 2;
		}
		if (l > 192) {
			s /= 2;
		}
		if (l > 217) {
			s /= 2;
		}
		if (l > 243) {
			s /= 2;
		}
		return (h / 4 << 10) + (s / 32 << 7) + l / 2;
	}

	private void read(Buffer buffer) {
		for (; ; ) {
			int code = buffer.get1U();

			if (code == 0) {
				break;
			}

			if (code == 1) {
				this.setColor(rgb = buffer.get3());
			} else if (code == 2) {
				this.textureIndex = buffer.get1U();
			} else if (code == 3) {
				// dummy
			} else if (code == 5) {
				this.occlude = false;
			} else if (code == 6) {
				this.name = buffer.getString();
			} else {
				System.out.println("Error unrecognized config code: " + code);
			}
		}
	}

}
