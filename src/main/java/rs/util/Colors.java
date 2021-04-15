package rs.util;

public final class Colors {

	public static final int[] SPOKEN = {0xFFFF00, 0xFF0000, 0xFF00, 0xFFFF, 0xFF00FF, 0xFFFFFF};

	public static final int SCROLLBAR_TRACK = 0x23201B;
	public static final int SCROLLBAR_GRIP_LOWLIGHT = 0x332D25;
	public static final int SCROLLBAR_GRIP_HIGHLIGHT = 0x766654;
	public static final int SCROLLBAR_GRIP_FOREGROUND = 0x4D4233;

	public static final int RED = 0xFF0000;
	public static final int GREEN = 0xFF00, DARK_GREEN = 0xB000, LIME_GREEN = 0x80FF80;
	public static final int BLUE = 0xFF;
	public static final int YELLOW = 0xFFFF00;
	public static final int CYAN = 0xFFFF;
	public static final int PINK = 0xFF00FF;
	public static final int WHITE = 0xFFFFFF;

	public static int getSpokenColor(int index, int value) {
		if (index < 6)
			return SPOKEN[index];

		switch (index) {
			case 6:
				return (value % 20) < 10 ? RED : YELLOW;
			case 7:
				return (value % 20) < 10 ? BLUE : CYAN;
			case 8:
				return (value % 20) < 10 ? DARK_GREEN : LIME_GREEN;
			case 9: {
				value = 150 - value;

				if (value < 50)
					return RED + (value * 0x500);
				else if (value < 100)
					return YELLOW - ((value - 50) * 0x50000);
				else if (value < 150)
					return GREEN + ((value - 100) * 0x5);
			}
			case 10: {
				value = 150 - value;

				if (value < 50)
					return RED + (value * 0x5);
				else if (value < 100)
					return PINK - ((value - 50) * 0x50000);
				else if (value < 150)
					return ((BLUE + ((value - 100) * 0x50000)) - ((value - 100) * 0x5));
			}
			case 11: {
				value = 150 - value;

				if (value < 50) {
					return WHITE - (value * 0x50005);
				} else if (value < 100) {
					return GREEN + ((value - 50) * 0x50005);
				} else if (value < 150) {
					return WHITE - ((value - 100) * 0x50000);
				}
			}
			case 12: {
				return (int) (Math.random() * 0xFFFFFF);
			}
		}

		// flash red for invalid
		return ((int) (Math.random() * 0xFF)) << 16;
	}

	public static int compositeSrcIn(int src, int dst, int alpha) {
		int opacity = 256 - alpha;
		return (((((dst & 0xff00ff) * opacity) + ((src & 0xff00ff) * alpha)) & 0xff00ff00) + ((((dst & 0xff00) * opacity) + ((src & 0xff00) * alpha)) & 0xff0000)) >> 8;
	}

	public static int setBrightness(int rgb, double brightness) {
		double r = (double) (rgb >> 16) / 256.0;
		double g = (double) (rgb >> 8 & 0xff) / 256.0;
		double b = (double) (rgb & 0xff) / 256.0;
		r = Math.pow(r, brightness);
		g = Math.pow(g, brightness);
		b = Math.pow(b, brightness);
		return ((int) (r * 256.0) << 16) + ((int) (g * 256.0) << 8) + (int) (b * 256.0);
	}

	private Colors() {

	}
}
