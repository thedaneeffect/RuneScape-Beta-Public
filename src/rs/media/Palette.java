package rs.media;

import rs.util.Colors;

public final class Palette {

	public static int[] rgb = new int[128 * 512];

	public static void setBrightness(double brightness) {
		int off = 0;
		for (int y = 0; y < 512; y++) {
			double fGreen = ((double) (y / 8) / 64.0) + 0.0078125;
			double saturation = ((double) (y & 0x7) / 8.0) + 0.0625;

			for (int x = 0; x < 128; x++) {
				double intensity = (double) x / 128.0;
				double red = intensity;
				double green = intensity;
				double blue = intensity;

				if (saturation != 0.0) {
					double a;

					if (intensity < 0.5) {
						a = intensity * (1.0 + saturation);
					} else {
						a = (intensity + saturation) - (intensity * saturation);
					}

					double b = (2.0 * intensity) - a;

					double fRed = fGreen + (1.0 / 3.0);
					double fBlue = fGreen - (1.0 / 3.0);

					if (fRed > 1.0) fRed--;
					if (fBlue < 0.0) fBlue++;

					red = getValue(fRed, a, b);
					green = getValue(fGreen, a, b);
					blue = getValue(fBlue, a, b);
				}

				rgb[off++] = Colors.setBrightness(((int) (red * 256.0) << 16) | ((int) (green * 256.0) << 8) | (int) (blue * 256.0), brightness);
			}
		}

		// TODO: create TextureManager

		for (int n = 0; n < 50; n++) {
			if (Draw3D.textures[n] != null) {
				int[] texturePalette = Draw3D.textures[n].palette;
				Draw3D.texturePalettes[n] = new int[texturePalette.length];

				for (int i = 0; i < texturePalette.length; i++) {
					Draw3D.texturePalettes[n][i] = Colors.setBrightness(texturePalette[i], brightness);
				}
			}
			Draw3D.updateTexture(n);
		}
	}

	private static double getValue(double value, double a, double b) {
		if ((6.0 * value) < 1.0)
			return b + ((a - b) * 6.0 * value);
		if (2.0 * value < 1.0)
			return a;
		if (3.0 * value < 2.0)
			return b + ((a - b) * ((2.0 / 3.0) - value) * 6.0);
		return b;
	}

	private Palette() {

	}

}
