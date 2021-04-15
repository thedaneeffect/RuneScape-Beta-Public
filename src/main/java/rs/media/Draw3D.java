package rs.media;

import rs.io.Archive;
import rs.util.Colors;

public class Draw3D extends Draw2D {

	public static boolean lowmemory = true;
	public static boolean testX;
	public static boolean opaque;
	public static int alpha;
	public static int centerX;
	public static int centerY;
	public static int[] oneOverFixed1715 = new int[512];
	public static int[] oneOverFixed1616 = new int[2048];
	public static int[] sin = new int[2048];
	public static int[] cos = new int[2048];
	public static int[] offsets;
	public static int loadedTextureCount;
	public static IndexedSprite[] textures;
	public static boolean[] textureHasTransparency;
	public static int[] textureColors;
	public static int texelPoolPosition;
	public static int[][] texelBuffer1, texelBuffer2;
	public static int[] textureCycles;
	public static int cycle;
	public static int[] palette;
	public static int[][] texturePalettes;
	public static int viewportWidth = 512;
	public static int viewportHeight = 334;

	public static void unload() {
		oneOverFixed1715 = null;
		oneOverFixed1616 = null;
		sin = null;
		cos = null;
		offsets = null;
		textures = null;
		textureHasTransparency = null;
		textureColors = null;
		texelBuffer2 = null;
		texelBuffer1 = null;
		textureCycles = null;
		palette = null;
		texturePalettes = null;
	}

	public static int[] prepareOffsets() {
		offsets = new int[dstHeight];
		for (int y = 0; y < dstHeight; y++) {
			offsets[y] = dstWidth * y;
		}
		centerX = dstWidth / 2;
		centerY = dstHeight / 2;
		return offsets;
	}

	public static int[] prepareOffsets(int w, int h) {
		offsets = new int[h];
		for (int y = 0; y < h; y++) {
			offsets[y] = w * y;
		}
		centerX = w / 2;
		centerY = h / 2;
		return offsets;
	}

	public static void clearPools() {
		texelBuffer2 = null;

		for (int i = 0; i < 50; i++) {
			texelBuffer1[i] = null;
		}
	}

	public static void setupPools(int size) {
		if (texelBuffer2 == null) {
			texelPoolPosition = size;

			if (lowmemory) {
				texelBuffer2 = new int[texelPoolPosition][128 * 128];
			} else {
				texelBuffer2 = new int[texelPoolPosition][256 * 256];
			}

			for (int n = 0; n < 50; n++) {
				texelBuffer1[n] = null;
			}
		}
	}

	public static void unpackTextures(Archive archive) {
		loadedTextureCount = 0;

		for (int n = 0; n < 50; n++) {
			try {
				textures[n] = new IndexedSprite(archive, String.valueOf(n), 0);

				if (lowmemory && textures[n].clipWidth == 128) {
					textures[n].shrink();
				} else {
					textures[n].crop();
				}

				loadedTextureCount++;
			} catch (Exception e) {
				/* empty */
			}
		}
	}

	public static int getAverageTextureRGB(int textureIndex) {
		if (textureColors[textureIndex] != 0) {
			return textureColors[textureIndex];
		}

		int r = 0;
		int g = 0;
		int b = 0;
		int length = texturePalettes[textureIndex].length;

		// sum all the color channels
		for (int n = 0; n < length; n++) {
			r += texturePalettes[textureIndex][n] >> 16 & 0xff;
			g += texturePalettes[textureIndex][n] >> 8 & 0xff;
			b += texturePalettes[textureIndex][n] & 0xff;
		}

		// average each channel and bitpack
		int rgb = Colors.setBrightness((r / length << 16) + (g / length << 8) + b / length, 1.4);

		// we use 0 to identify as unretrieved
		if (rgb == 0) {
			rgb = 1;
		}

		// store the value to avoid having to average again
		textureColors[textureIndex] = rgb;
		return rgb;
	}

	public static void updateTexture(int textureIndex) {
		if (texelBuffer1[textureIndex] != null) {
			texelBuffer2[texelPoolPosition++] = texelBuffer1[textureIndex];
			texelBuffer1[textureIndex] = null;
		}
	}

	public static int[] getTexels(int textureIndex) {
		textureCycles[textureIndex] = cycle++;

		if (texelBuffer1[textureIndex] != null) {
			return texelBuffer1[textureIndex];
		}

		int[] buffer;

		// If we've updated a texture, use that one as our buffer.
		if (texelPoolPosition > 0) {
			buffer = texelBuffer2[--texelPoolPosition];
			texelBuffer2[texelPoolPosition] = null;
		} else {
			// select the oldest pushed buffer

			int oldestCycle = 0;
			int index = -1;

			// iterate through each texture
			for (int n = 0; n < loadedTextureCount; n++) {
				// if the buffer for this texture exists, and it hasn't updated in awhile or the current selected index is -1
				if (texelBuffer1[n] != null && (textureCycles[n] < oldestCycle || index == -1)) {
					oldestCycle = textureCycles[n];
					index = n;
				}
			}

			buffer = texelBuffer1[index];
			texelBuffer1[index] = null;
		}

		texelBuffer1[textureIndex] = buffer;

		IndexedSprite texture = textures[textureIndex];
		int[] texturePalette = texturePalettes[textureIndex];

		// low memory uses 64x64 textures instead of 128x128.
		if (lowmemory) {
			textureHasTransparency[textureIndex] = false;

			// iterate through each pixel
			for (int n = 0; n < (64 * 64); n++) {
				buffer[n] = texturePalette[texture.pixels[n]];

				// allow space to divide channels (loses some red and green)
				buffer[n] &= 0xF8F8FF;

				int rgb = buffer[n];

				if (rgb == 0) {
					textureHasTransparency[textureIndex] = true;
				}

				// darker
				buffer[n + (64 * 64)] = (rgb - (rgb >>> 3)) & 0xF8F8FF;

				// darker!
				buffer[n + (64 * 128)] = (rgb - (rgb >>> 2)) & 0xF8F8FF;

				// and darker!
				buffer[n + (64 * 192)] = (rgb - (rgb >>> 2) - (rgb >>> 3)) & 0xF8F8FF;
			}
		} else {
			if (texture.width == 64) {
				// implying src is 128x128: rescale from 128x128 to 64x64
				for (int y = 0; y < 128; y++) {
					for (int x = 0; x < 128; x++) {
						buffer[x + (y << 7)] = texturePalette[(texture.pixels[(x >> 1) + ((y >> 1) << 6)])];
					}
				}
			} else {
				for (int n = 0; n < (128 * 128); n++) {
					buffer[n] = texturePalette[texture.pixels[n]];
				}
			}

			textureHasTransparency[textureIndex] = false;

			for (int n = 0; n < (128 * 128); n++) {
				// allow space to divide channels (loses some red and green)
				buffer[n] &= 0xF8F8FF;

				int rgb = buffer[n];

				if (rgb == 0) {
					textureHasTransparency[textureIndex] = true;
				}

				// darker
				buffer[n + (128 * 128)] = (rgb - (rgb >>> 3)) & 0xF8F8FF;

				//darker!
				buffer[n + (256 * 128)] = (rgb - (rgb >>> 2)) & 0xF8F8FF;

				//and darker!
				buffer[n + (384 * 128)] = (rgb - (rgb >>> 2) - (rgb >>> 3)) & 0xF8F8FF;
			}

		}
		return buffer;
	}

	public static void fillShadedTriangle(int xA, int yA, int cA, int xB, int yB, int cB, int xC, int yC, int cC) {
		if (yC < yA) {
			int tmp = yA;
			yA = yC;
			yC = tmp;

			tmp = xA;
			xA = xC;
			xC = tmp;

			tmp = cA;
			cA = cC;
			cC = tmp;
		}

		if (yB < yA) {
			int tmp = yA;
			yA = yB;
			yB = tmp;

			tmp = xA;
			xA = xB;
			xB = tmp;

			tmp = cA;
			cA = cB;
			cB = tmp;
		}

		if (yC < yB) {
			int tmp = yB;
			yB = yC;
			yC = tmp;

			tmp = xB;
			xB = xC;
			xC = tmp;

			tmp = cB;
			cB = cC;
			cC = tmp;
		}

		if (yA >= bottom)
			return;

		if (yB > bottom) yB = bottom;
		if (yC > bottom) yC = bottom;

		int mAB = 0;
		int mBC = 0;
		int mCA = 0;

		int mCAB = 0;
		int mCBC = 0;
		int mCCA = 0;

		if (yB != yA) {
			mAB = ((xB - xA) << 16) / (yB - yA);
			mCAB = ((cB - cA) << 15) / (yB - yA);
		}

		if (yC != yB) {
			mBC = ((xC - xB) << 16) / (yC - yB);
			mCBC = ((cC - cB) << 15) / (yC - yB);
		}

		if (yC != yA) {
			mCA = ((xA - xC) << 16) / (yA - yC);
			mCCA = ((cA - cC) << 15) / (yA - yC);
		}

		int x0 = xA << 16;
		int x2 = xB << 16;
		int x1 = x0;

		int c0 = cA << 15;
		int c2 = cB << 15;
		int c1 = c0;

		if (yA < 0) {
			x0 -= mCA * yA;
			x1 -= mAB * yA;

			c0 -= mCCA * yA;
			c1 -= mCAB * yA;

			yA = 0;
		}

		if (yB < 0) {
			x2 -= mBC * yB;
			c2 -= mCBC * yB;
			yB = 0;
		}

		int heightA = yB - yA;
		int heightB = yC - yB;
		int offset = offsets[yA];

		while (--heightA >= 0) {
			drawGradientScanline(dst, offset, x0 >> 16, x1 >> 16, c0 >> 7, c1 >> 7);

			x0 += mCA;
			x1 += mAB;

			c0 += mCCA;
			c1 += mCAB;

			offset += dstWidth;
		}

		while (--heightB >= 0) {
			drawGradientScanline(dst, offset, x0 >> 16, x2 >> 16, c0 >> 7, c2 >> 7);

			x0 += mCA;
			x2 += mBC;

			c0 += mCCA;
			c2 += mCBC;

			offset += dstWidth;
		}
	}

	public static void drawGradientScanline(int[] dst, int off, int xA, int xB, int cA, int cB) {
		if (xA == xB)
			return;

		if (xA > xB) {
			int tmp = xB;
			xB = xA;
			xA = tmp;

			tmp = cB;
			cB = cA;
			cA = tmp;
		}

		int lightnessSlope = (cB - cA) / (xB - xA);

		if (testX) {
			if (xB > rightX) {
				xB = rightX;
			}

			if (xA < 0) {
				cA -= xA * lightnessSlope;
				xA = 0;
			}
		}

		off += xA;
		int length = xB - xA;

		if (alpha == 0) {
			while (length-- > 0) {
				dst[off++] = palette[(cA >> 8)];
				cA += lightnessSlope;
			}
		} else {
			int opacity = 256 - alpha;

			while (length-- > 0) {
				int rgb = palette[cA >> 8];
				cA += lightnessSlope;
				rgb = (((rgb & 0xFF00FF) * opacity >> 8 & 0xFF00FF) + ((rgb & 0xFF00) * opacity >> 8 & 0xFF00));
				dst[off++] = (rgb + ((dst[off] & 0xFF00FF) * alpha >> 8 & 0xFF00FF) + ((dst[off] & 0xFF00) * alpha >> 8 & 0xFF00));
			}
		}
	}

	public static void fillTriangle(int xA, int yA, int xB, int yB, int xC, int yC, int rgb) {
		if (yC < yA) {
			int a = yA;
			yA = yC;
			yC = a;

			a = xA;
			xA = xC;
			xC = a;
		}

		if (yB < yA) {
			int a = yA;
			yA = yB;
			yB = a;

			a = xA;
			xA = xB;
			xB = a;
		}

		if (yC < yB) {
			int a = yB;
			yB = yC;
			yC = a;

			a = xB;
			xB = xC;
			xC = a;
		}

		// the top point is off screen
		if (yA >= bottom) return;

		// clamp points on screen
		if (yB > bottom) yB = bottom;
		if (yC > bottom) yC = bottom;

		int mAB = 0;
		int mBC = 0;
		int mCA = 0;

		// calculate inverted slopes (this is because Y+ goes down)
		if (yB != yA) mAB = ((xB - xA) << 16) / (yB - yA);
		if (yC != yB) mBC = ((xC - xB) << 16) / (yC - yB);
		if (yC != yA) mCA = ((xA - xC) << 16) / (yA - yC);

		// our X positions along our edges
		int x0 = xA << 16;
		int x1 = x0;
		int x2 = xB << 16;

		// A is above the screen
		if (yA < 0) {
			x0 -= mCA * yA;
			x1 -= mAB * yA;
			yA = 0;
		}

		// B is above the screen
		if (yB < 0) {
			x2 -= mBC * yB;
			yB = 0;
		}

		int heightA = yB - yA;
		int heightB = yC - yB;
		int offset = offsets[yA];

		while (--heightA >= 0) {
			drawScanline(offset, x0 >> 16, x1 >> 16, rgb);

			x0 += mCA;
			x1 += mAB;

			offset += dstWidth;
		}

		while (--heightB >= 0) {
			drawScanline(offset, x0 >> 16, x2 >> 16, rgb);

			x0 += mCA;
			x2 += mBC;

			offset += dstWidth;
		}
	}

	private static void drawScanline(int offset, int xA, int xB, int rgb) {
		if (xA == xB) return;

		if (xA >= xB) {
			int a = xA;
			xA = xB;
			xB = a;
		}

		if (testX) {
			if (xB > rightX)
				xB = rightX;
			if (xA < 0)
				xA = 0;
		}

		int length = xB - xA;
		offset += xA;

		while (--length >= 0) {
			dst[offset++] = rgb;
		}
	}

	public static void fillTexturedTriangle(int aY, int bY, int cY, int aX, int bX, int cX, int aL, int bL, int cL, int originX, int horizontalX, int verticalX, int originY, int horizontalY, int verticalY, int originZ, int horizontalZ, int verticalZ, int textureIndex) {
		int[] texels = getTexels(textureIndex);

		opaque = !textureHasTransparency[textureIndex];

		horizontalX = originX - horizontalX;
		horizontalY = originY - horizontalY;
		horizontalZ = originZ - horizontalZ;
		verticalX -= originX;
		verticalY -= originY;
		verticalZ -= originZ;

		int originA = (verticalZ * originX - verticalX * originZ) << 5;
		int originC = (horizontalX * verticalZ - horizontalZ * verticalX) << 5;
		int originB = (horizontalZ * originX - horizontalX * originZ) << 5;

		int horizontalA = (verticalY * originZ - verticalZ * originY) << 8;
		int horizontalB = (horizontalY * originZ - horizontalZ * originY) << 8;
		int horizontalC = (horizontalZ * verticalY - horizontalY * verticalZ) << 8;

		int verticalA = (verticalX * originY - verticalY * originX) << 14;
		int verticalB = (horizontalX * originY - horizontalY * originX) << 14;
		int verticalC = (horizontalY * verticalX - horizontalX * verticalY) << 14;

		int slopeAB = 0;
		int lightSlopeAB = 0;

		if (bY != aY) {
			slopeAB = (bX - aX << 16) / (bY - aY);
			lightSlopeAB = (bL - aL << 16) / (bY - aY);
		}

		int slopeBC = 0;
		int lightSlopeBC = 0;

		if (cY != bY) {
			slopeBC = (cX - bX << 16) / (cY - bY);
			lightSlopeBC = (cL - bL << 16) / (cY - bY);
		}

		int slopeCA = 0;
		int lightSlopeCA = 0;

		if (cY != aY) {
			slopeCA = (aX - cX << 16) / (aY - cY);
			lightSlopeCA = (aL - cL << 16) / (aY - cY);
		}

		if (aY <= bY && aY <= cY) {
			if (aY < bottom) {
				if (bY > bottom) {
					bY = bottom;
				}

				if (cY > bottom) {
					cY = bottom;
				}

				if (bY < cY) {
					cX = aX <<= 16;
					cL = aL <<= 16;

					if (aY < 0) {
						cX -= slopeCA * aY;
						aX -= slopeAB * aY;

						cL -= lightSlopeCA * aY;
						aL -= lightSlopeAB * aY;

						aY = 0;
					}

					bX <<= 16;
					bL <<= 16;

					if (bY < 0) {
						bX -= slopeBC * bY;
						bL -= lightSlopeBC * bY;
						bY = 0;
					}

					int offsetY = aY - centerY;
					verticalA += originA * offsetY;
					verticalB += originB * offsetY;
					verticalC += originC * offsetY;

					if (aY != bY && slopeCA < slopeAB || aY == bY && slopeCA > slopeBC) {
						cY -= bY;
						bY -= aY;
						aY = offsets[aY];

						while (--bY >= 0) {
							drawTexturedScanline(dst, texels, 0, 0, aY, cX >> 16, aX >> 16, cL >> 8, aL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
							cX += slopeCA;
							aX += slopeAB;

							cL += lightSlopeCA;
							aL += lightSlopeAB;

							aY += dstWidth;

							verticalA += originA;
							verticalB += originB;
							verticalC += originC;
						}

						while (--cY >= 0) {
							drawTexturedScanline(dst, texels, 0, 0, aY, cX >> 16, bX >> 16, cL >> 8, bL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
							cX += slopeCA;
							bX += slopeBC;

							cL += lightSlopeCA;
							bL += lightSlopeBC;

							aY += dstWidth;

							verticalA += originA;
							verticalB += originB;
							verticalC += originC;
						}
					} else {
						cY -= bY;
						bY -= aY;
						aY = offsets[aY];

						while (--bY >= 0) {
							drawTexturedScanline(dst, texels, 0, 0, aY, aX >> 16, cX >> 16, aL >> 8, cL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
							cX += slopeCA;
							aX += slopeAB;

							cL += lightSlopeCA;
							aL += lightSlopeAB;

							aY += dstWidth;

							verticalA += originA;
							verticalB += originB;
							verticalC += originC;
						}

						while (--cY >= 0) {
							drawTexturedScanline(dst, texels, 0, 0, aY, bX >> 16, cX >> 16, bL >> 8, cL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
							cX += slopeCA;
							bX += slopeBC;

							cL += lightSlopeCA;
							bL += lightSlopeBC;

							aY += dstWidth;

							verticalA += originA;
							verticalB += originB;
							verticalC += originC;
						}
					}
				} else {
					bX = aX <<= 16;
					bL = aL <<= 16;

					if (aY < 0) {
						bX -= slopeCA * aY;
						aX -= slopeAB * aY;

						bL -= lightSlopeCA * aY;
						aL -= lightSlopeAB * aY;

						aY = 0;
					}

					cX <<= 16;
					cL <<= 16;

					if (cY < 0) {
						cX -= slopeBC * cY;
						cL -= lightSlopeBC * cY;
						cY = 0;
					}

					int offsetY = aY - centerY;
					verticalA += originA * offsetY;
					verticalB += originB * offsetY;
					verticalC += originC * offsetY;

					if (aY != cY && slopeCA < slopeAB || aY == cY && slopeBC > slopeAB) {
						bY -= cY;
						cY -= aY;
						aY = offsets[aY];

						while (--cY >= 0) {
							drawTexturedScanline(dst, texels, 0, 0, aY, bX >> 16, aX >> 16, bL >> 8, aL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
							bX += slopeCA;
							aX += slopeAB;

							bL += lightSlopeCA;
							aL += lightSlopeAB;

							aY += dstWidth;

							verticalA += originA;
							verticalB += originB;
							verticalC += originC;
						}

						while (--bY >= 0) {
							drawTexturedScanline(dst, texels, 0, 0, aY, cX >> 16, aX >> 16, cL >> 8, aL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
							cX += slopeBC;
							aX += slopeAB;

							cL += lightSlopeBC;
							aL += lightSlopeAB;

							aY += dstWidth;

							verticalA += originA;
							verticalB += originB;
							verticalC += originC;
						}
					} else {
						bY -= cY;
						cY -= aY;
						aY = offsets[aY];

						while (--cY >= 0) {
							drawTexturedScanline(dst, texels, 0, 0, aY, aX >> 16, bX >> 16, aL >> 8, bL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
							bX += slopeCA;
							aX += slopeAB;

							bL += lightSlopeCA;
							aL += lightSlopeAB;

							aY += dstWidth;

							verticalA += originA;
							verticalB += originB;
							verticalC += originC;
						}

						while (--bY >= 0) {
							drawTexturedScanline(dst, texels, 0, 0, aY, aX >> 16, cX >> 16, aL >> 8, cL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
							cX += slopeBC;
							aX += slopeAB;

							cL += lightSlopeBC;
							aL += lightSlopeAB;

							aY += dstWidth;

							verticalA += originA;
							verticalB += originB;
							verticalC += originC;
						}
					}
				}
			}
		} else if (bY <= cY) {
			if (bY < bottom) {
				if (cY > bottom) {
					cY = bottom;
				}

				if (aY > bottom) {
					aY = bottom;
				}

				if (cY < aY) {
					aX = bX <<= 16;
					aL = bL <<= 16;

					if (bY < 0) {
						aX -= slopeAB * bY;
						bX -= slopeBC * bY;

						aL -= lightSlopeAB * bY;
						bL -= lightSlopeBC * bY;

						bY = 0;
					}

					cX <<= 16;
					cL <<= 16;

					if (cY < 0) {
						cX -= slopeCA * cY;
						cL -= lightSlopeCA * cY;
						cY = 0;
					}

					int offsetY = bY - centerY;
					verticalA += originA * offsetY;
					verticalB += originB * offsetY;
					verticalC += originC * offsetY;

					if (bY != cY && slopeAB < slopeBC || bY == cY && slopeAB > slopeCA) {
						aY -= cY;
						cY -= bY;
						bY = offsets[bY];

						while (--cY >= 0) {
							drawTexturedScanline(dst, texels, 0, 0, bY, aX >> 16, bX >> 16, aL >> 8, bL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
							aX += slopeAB;
							bX += slopeBC;
							aL += lightSlopeAB;
							bL += lightSlopeBC;
							bY += dstWidth;
							verticalA += originA;
							verticalB += originB;
							verticalC += originC;
						}

						while (--aY >= 0) {
							drawTexturedScanline(dst, texels, 0, 0, bY, aX >> 16, cX >> 16, aL >> 8, cL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
							aX += slopeAB;
							cX += slopeCA;
							aL += lightSlopeAB;
							cL += lightSlopeCA;
							bY += dstWidth;
							verticalA += originA;
							verticalB += originB;
							verticalC += originC;
						}
					} else {
						aY -= cY;
						cY -= bY;
						bY = offsets[bY];

						while (--cY >= 0) {
							drawTexturedScanline(dst, texels, 0, 0, bY, bX >> 16, aX >> 16, bL >> 8, aL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
							aX += slopeAB;
							bX += slopeBC;

							aL += lightSlopeAB;
							bL += lightSlopeBC;

							bY += dstWidth;

							verticalA += originA;
							verticalB += originB;
							verticalC += originC;
						}

						while (--aY >= 0) {
							drawTexturedScanline(dst, texels, 0, 0, bY, cX >> 16, aX >> 16, cL >> 8, aL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
							aX += slopeAB;
							cX += slopeCA;

							aL += lightSlopeAB;
							cL += lightSlopeCA;

							bY += dstWidth;

							verticalA += originA;
							verticalB += originB;
							verticalC += originC;
						}
					}
				} else {
					cX = bX <<= 16;
					cL = bL <<= 16;

					if (bY < 0) {
						cX -= slopeAB * bY;
						bX -= slopeBC * bY;

						cL -= lightSlopeAB * bY;
						bL -= lightSlopeBC * bY;

						bY = 0;
					}

					aX <<= 16;
					aL <<= 16;

					if (aY < 0) {
						aX -= slopeCA * aY;
						aL -= lightSlopeCA * aY;
						aY = 0;
					}

					int offsetY = bY - centerY;
					verticalA += originA * offsetY;
					verticalB += originB * offsetY;
					verticalC += originC * offsetY;

					if (slopeAB < slopeBC) {
						cY -= aY;
						aY -= bY;
						bY = offsets[bY];

						while (--aY >= 0) {
							drawTexturedScanline(dst, texels, 0, 0, bY, cX >> 16, bX >> 16, cL >> 8, bL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
							cX += slopeAB;
							bX += slopeBC;

							cL += lightSlopeAB;
							bL += lightSlopeBC;

							bY += dstWidth;

							verticalA += originA;
							verticalB += originB;
							verticalC += originC;
						}

						while (--cY >= 0) {
							drawTexturedScanline(dst, texels, 0, 0, bY, aX >> 16, bX >> 16, aL >> 8, bL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
							aX += slopeCA;
							bX += slopeBC;

							aL += lightSlopeCA;
							bL += lightSlopeBC;

							bY += dstWidth;

							verticalA += originA;
							verticalB += originB;
							verticalC += originC;
						}
					} else {
						cY -= aY;
						aY -= bY;
						bY = offsets[bY];

						while (--aY >= 0) {
							drawTexturedScanline(dst, texels, 0, 0, bY, bX >> 16, cX >> 16, bL >> 8, cL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
							cX += slopeAB;
							bX += slopeBC;

							cL += lightSlopeAB;
							bL += lightSlopeBC;

							bY += dstWidth;

							verticalA += originA;
							verticalB += originB;
							verticalC += originC;
						}

						while (--cY >= 0) {
							drawTexturedScanline(dst, texels, 0, 0, bY, bX >> 16, aX >> 16, bL >> 8, aL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
							aX += slopeCA;
							bX += slopeBC;

							aL += lightSlopeCA;
							bL += lightSlopeBC;

							bY += dstWidth;

							verticalA += originA;
							verticalB += originB;
							verticalC += originC;
						}
					}
				}
			}
		} else if (cY < bottom) {
			if (aY > bottom) {
				aY = bottom;
			}

			if (bY > bottom) {
				bY = bottom;
			}

			if (aY < bY) {
				bX = cX <<= 16;
				bL = cL <<= 16;

				if (cY < 0) {
					bX -= slopeBC * cY;
					cX -= slopeCA * cY;

					bL -= lightSlopeBC * cY;
					cL -= lightSlopeCA * cY;

					cY = 0;
				}

				aX <<= 16;
				aL <<= 16;

				if (aY < 0) {
					aX -= slopeAB * aY;
					aL -= lightSlopeAB * aY;
					aY = 0;
				}

				int offsetY = cY - centerY;
				verticalA += originA * offsetY;
				verticalB += originB * offsetY;
				verticalC += originC * offsetY;

				if (slopeBC < slopeCA) {
					bY -= aY;
					aY -= cY;
					cY = offsets[cY];

					while (--aY >= 0) {
						drawTexturedScanline(dst, texels, 0, 0, cY, bX >> 16, cX >> 16, bL >> 8, cL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
						bX += slopeBC;
						cX += slopeCA;

						bL += lightSlopeBC;
						cL += lightSlopeCA;

						cY += dstWidth;

						verticalA += originA;
						verticalB += originB;
						verticalC += originC;
					}

					while (--bY >= 0) {
						drawTexturedScanline(dst, texels, 0, 0, cY, bX >> 16, aX >> 16, bL >> 8, aL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
						bX += slopeBC;
						aX += slopeAB;

						bL += lightSlopeBC;
						aL += lightSlopeAB;

						cY += dstWidth;

						verticalA += originA;
						verticalB += originB;
						verticalC += originC;
					}
				} else {
					bY -= aY;
					aY -= cY;
					cY = offsets[cY];

					while (--aY >= 0) {
						drawTexturedScanline(dst, texels, 0, 0, cY, cX >> 16, bX >> 16, cL >> 8, bL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
						bX += slopeBC;
						cX += slopeCA;

						bL += lightSlopeBC;
						cL += lightSlopeCA;

						cY += dstWidth;

						verticalA += originA;
						verticalB += originB;
						verticalC += originC;
					}

					while (--bY >= 0) {
						drawTexturedScanline(dst, texels, 0, 0, cY, aX >> 16, bX >> 16, aL >> 8, bL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
						bX += slopeBC;
						aX += slopeAB;

						bL += lightSlopeBC;
						aL += lightSlopeAB;

						cY += dstWidth;

						verticalA += originA;
						verticalB += originB;
						verticalC += originC;
					}
				}
			} else {
				aX = cX <<= 16;
				aL = cL <<= 16;

				if (cY < 0) {
					aX -= slopeBC * cY;
					cX -= slopeCA * cY;

					aL -= lightSlopeBC * cY;
					cL -= lightSlopeCA * cY;

					cY = 0;
				}

				bX <<= 16;
				bL <<= 16;

				if (bY < 0) {
					bX -= slopeAB * bY;
					bL -= lightSlopeAB * bY;
					bY = 0;
				}

				int offsetY = cY - centerY;
				verticalA += originA * offsetY;
				verticalB += originB * offsetY;
				verticalC += originC * offsetY;

				if (slopeBC < slopeCA) {
					aY -= bY;
					bY -= cY;
					cY = offsets[cY];

					while (--bY >= 0) {
						drawTexturedScanline(dst, texels, 0, 0, cY, aX >> 16, cX >> 16, aL >> 8, cL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
						aX += slopeBC;
						cX += slopeCA;

						aL += lightSlopeBC;
						cL += lightSlopeCA;

						cY += dstWidth;

						verticalA += originA;
						verticalB += originB;
						verticalC += originC;
					}

					while (--aY >= 0) {
						drawTexturedScanline(dst, texels, 0, 0, cY, bX >> 16, cX >> 16, bL >> 8, cL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
						bX += slopeAB;
						cX += slopeCA;

						bL += lightSlopeAB;
						cL += lightSlopeCA;

						cY += dstWidth;

						verticalA += originA;
						verticalB += originB;
						verticalC += originC;
					}
				} else {
					aY -= bY;
					bY -= cY;
					cY = offsets[cY];

					while (--bY >= 0) {
						drawTexturedScanline(dst, texels, 0, 0, cY, cX >> 16, aX >> 16, cL >> 8, aL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
						aX += slopeBC;
						cX += slopeCA;

						aL += lightSlopeBC;
						cL += lightSlopeCA;

						cY += dstWidth;

						verticalA += originA;
						verticalB += originB;
						verticalC += originC;
					}

					while (--aY >= 0) {
						drawTexturedScanline(dst, texels, 0, 0, cY, cX >> 16, bX >> 16, cL >> 8, bL >> 8, verticalA, verticalB, verticalC, horizontalA, horizontalB, horizontalC);
						bX += slopeAB;
						cX += slopeCA;

						bL += lightSlopeAB;
						cL += lightSlopeCA;

						cY += dstWidth;

						verticalA += originA;
						verticalB += originB;
						verticalC += originC;
					}
				}
			}
		}
	}

	public static void drawTexturedScanline(int[] dst, int[] texels, int uA, int vA, int off, int xA, int xB, int lightnessA, int lightnessB, int verticalA, int verticalB, int verticalC, int horizontalA, int horizontalB, int horizontalC) {
		if (xA >= xB) {
			return;
		}

		int length;
		int lightnessSlope;

		if (testX) {
			lightnessSlope = (lightnessB - lightnessA) / (xB - xA);

			if (xB > rightX) {
				xB = rightX;
			}

			if (xA < 0) {
				lightnessA -= xA * lightnessSlope;
				xA = 0;
			}

			if (xA >= xB) {
				return;
			}

			length = xB - xA >> 3;
			lightnessSlope <<= 12;
			lightnessA <<= 9;
		} else {
			if (xB - xA > 7) {
				length = xB - xA >> 3;
				lightnessSlope = (lightnessB - lightnessA) * oneOverFixed1715[length] >> 6;
			} else {
				length = 0;
				lightnessSlope = 0;
			}

			lightnessA <<= 9;
		}

		off += xA;

		if (lowmemory) {
			int uB = 0;
			int vB = 0;
			int delta = xA - centerX;

			verticalA += (horizontalA >> 3) * delta;
			verticalB += (horizontalB >> 3) * delta;
			verticalC += (horizontalC >> 3) * delta;

			int c = verticalC >> 12;

			if (c != 0) {
				uA = verticalA / c;
				vA = verticalB / c;

				if (uA < 0) {
					uA = 0;
				} else if (uA > (63 << 6)) {
					uA = (63 << 6);
				}
			}

			verticalA += horizontalA;
			verticalB += horizontalB;
			verticalC += horizontalC;
			c = verticalC >> 12;

			if (c != 0) {
				uB = verticalA / c;
				vB = verticalB / c;

				if (uB < 7) {
					uB = 7;
				} else if (uB > (63 << 6)) {
					uB = (63 << 6);
				}
			}

			int uStep = uB - uA >> 3;
			int vStep = vB - vA >> 3;

			uA += (lightnessA & (3 << 21)) >> 3;
			int lightness = lightnessA >> 23;

			if (opaque) {
				while (length-- > 0) {
					dst[off++] = texels[(vA & (63 << 6)) + (uA >> 6)] >>> lightness;
					uA += uStep;
					vA += vStep;

					dst[off++] = texels[(vA & (63 << 6)) + (uA >> 6)] >>> lightness;
					uA += uStep;
					vA += vStep;

					dst[off++] = texels[(vA & (63 << 6)) + (uA >> 6)] >>> lightness;
					uA += uStep;
					vA += vStep;

					dst[off++] = texels[(vA & (63 << 6)) + (uA >> 6)] >>> lightness;
					uA += uStep;
					vA += vStep;

					dst[off++] = texels[(vA & (63 << 6)) + (uA >> 6)] >>> lightness;
					uA += uStep;
					vA += vStep;

					dst[off++] = texels[(vA & (63 << 6)) + (uA >> 6)] >>> lightness;
					uA += uStep;
					vA += vStep;

					dst[off++] = texels[(vA & (63 << 6)) + (uA >> 6)] >>> lightness;
					uA += uStep;
					vA += vStep;

					dst[off++] = texels[(vA & (63 << 6)) + (uA >> 6)] >>> lightness;
					uA = uB;
					vA = vB;

					verticalA += horizontalA;
					verticalB += horizontalB;
					verticalC += horizontalC;
					c = verticalC >> 12;

					if (c != 0) {
						uB = verticalA / c;
						vB = verticalB / c;

						if (uB < 7) {
							uB = 7;
						} else if (uB > (63 << 6)) {
							uB = (63 << 6);
						}
					}

					uStep = uB - uA >> 3;
					vStep = vB - vA >> 3;
					lightnessA += lightnessSlope;
					uA += (lightnessA & (3 << 21)) >> 3;
					lightness = lightnessA >> 23;
				}

				length = xB - xA & 0x7;

				while (length-- > 0) {
					dst[off++] = texels[(vA & (63 << 6)) + (uA >> 6)] >>> lightness;
					uA += uStep;
					vA += vStep;
				}
			} else {
				while (length-- > 0) {
					int rgb;
					if ((rgb = texels[(vA & (63 << 6)) + (uA >> 6)] >>> lightness) != 0) {
						dst[off] = rgb;
					}
					off++;
					uA += uStep;
					vA += vStep;

					if ((rgb = texels[(vA & (63 << 6)) + (uA >> 6)] >>> lightness) != 0) {
						dst[off] = rgb;
					}
					off++;
					uA += uStep;
					vA += vStep;

					if ((rgb = texels[(vA & (63 << 6)) + (uA >> 6)] >>> lightness) != 0) {
						dst[off] = rgb;
					}
					off++;
					uA += uStep;
					vA += vStep;

					if ((rgb = texels[(vA & (63 << 6)) + (uA >> 6)] >>> lightness) != 0) {
						dst[off] = rgb;
					}
					off++;
					uA += uStep;
					vA += vStep;

					if ((rgb = texels[(vA & (63 << 6)) + (uA >> 6)] >>> lightness) != 0) {
						dst[off] = rgb;
					}
					off++;
					uA += uStep;
					vA += vStep;

					if ((rgb = texels[(vA & (63 << 6)) + (uA >> 6)] >>> lightness) != 0) {
						dst[off] = rgb;
					}
					off++;
					uA += uStep;
					vA += vStep;

					if ((rgb = texels[(vA & (63 << 6)) + (uA >> 6)] >>> lightness) != 0) {
						dst[off] = rgb;
					}
					off++;
					uA += uStep;
					vA += vStep;

					if ((rgb = texels[(vA & (63 << 6)) + (uA >> 6)] >>> lightness) != 0) {
						dst[off] = rgb;
					}
					off++;
					uA = uB;
					vA = vB;

					verticalA += horizontalA;
					verticalB += horizontalB;
					verticalC += horizontalC;
					c = verticalC >> 12;

					if (c != 0) {
						uB = verticalA / c;
						vB = verticalB / c;

						if (uB < 7) {
							uB = 7;
						} else if (uB > (63 << 6)) {
							uB = (63 << 6);
						}
					}

					uStep = uB - uA >> 3;
					vStep = vB - vA >> 3;
					lightnessA += lightnessSlope;
					uA += (lightnessA & (3 << 21)) >> 3;
					lightness = lightnessA >> 23;
				}

				length = xB - xA & 0x7;

				while (length-- > 0) {
					int rgb;

					if ((rgb = texels[(vA & (63 << 6)) + (uA >> 6)] >>> lightness) != 0) {
						dst[off] = rgb;
					}

					off++;
					uA += uStep;
					vA += vStep;
				}
			}
		} else {
			int u2 = 0;
			int v2 = 0;
			int delta = xA - centerX;

			verticalA += (horizontalA >> 3) * delta;
			verticalB += (horizontalB >> 3) * delta;
			verticalC += (horizontalC >> 3) * delta;
			int realC = verticalC >> 14;

			if (realC != 0) {
				uA = verticalA / realC;
				vA = verticalB / realC;

				if (uA < 0) {
					uA = 0;
				} else if (uA > (127 << 7)) {
					uA = (127 << 7);
				}
			}

			verticalA += horizontalA;
			verticalB += horizontalB;
			verticalC += horizontalC;
			realC = verticalC >> 14;

			if (realC != 0) {
				u2 = verticalA / realC;
				v2 = verticalB / realC;

				if (u2 < 7) {
					u2 = 7;
				} else if (u2 > (127 << 7)) {
					u2 = (127 << 7);
				}
			}

			int deltaU = u2 - uA >> 3;
			int deltaV = v2 - vA >> 3;
			uA += lightnessA & (3 << 21);
			int lightness = lightnessA >> 23;

			if (opaque) {
				while (length-- > 0) {
					dst[off++] = texels[(vA & (127 << 7)) + (uA >> 7)] >>> lightness;
					uA += deltaU;
					vA += deltaV;

					dst[off++] = texels[(vA & (127 << 7)) + (uA >> 7)] >>> lightness;
					uA += deltaU;
					vA += deltaV;

					dst[off++] = texels[(vA & (127 << 7)) + (uA >> 7)] >>> lightness;
					uA += deltaU;
					vA += deltaV;

					dst[off++] = texels[(vA & (127 << 7)) + (uA >> 7)] >>> lightness;
					uA += deltaU;
					vA += deltaV;

					dst[off++] = texels[(vA & (127 << 7)) + (uA >> 7)] >>> lightness;
					uA += deltaU;
					vA += deltaV;

					dst[off++] = texels[(vA & (127 << 7)) + (uA >> 7)] >>> lightness;
					uA += deltaU;
					vA += deltaV;

					dst[off++] = texels[(vA & (127 << 7)) + (uA >> 7)] >>> lightness;
					uA += deltaU;
					vA += deltaV;

					dst[off++] = texels[(vA & (127 << 7)) + (uA >> 7)] >>> lightness;
					uA = u2;
					vA = v2;

					verticalA += horizontalA;
					verticalB += horizontalB;
					verticalC += horizontalC;
					realC = verticalC >> 14;

					if (realC != 0) {
						u2 = verticalA / realC;
						v2 = verticalB / realC;

						if (u2 < 7) {
							u2 = 7;
						} else if (u2 > (127 << 7)) {
							u2 = (127 << 7);
						}
					}

					deltaU = u2 - uA >> 3;
					deltaV = v2 - vA >> 3;
					lightnessA += lightnessSlope;
					uA += lightnessA & (3 << 21);
					lightness = lightnessA >> 23;
				}

				length = xB - xA & 0x7;

				while (length-- > 0) {
					dst[off++] = texels[(vA & (127 << 7)) + (uA >> 7)] >>> lightness;
					uA += deltaU;
					vA += deltaV;
				}
			} else {
				while (length-- > 0) {
					int rgb;
					if ((rgb = (texels[(vA & (127 << 7)) + (uA >> 7)] >>> lightness)) != 0) {
						dst[off] = rgb;
					}
					off++;
					uA += deltaU;
					vA += deltaV;

					if ((rgb = (texels[(vA & (127 << 7)) + (uA >> 7)] >>> lightness)) != 0) {
						dst[off] = rgb;
					}
					off++;
					uA += deltaU;
					vA += deltaV;

					if ((rgb = (texels[(vA & (127 << 7)) + (uA >> 7)] >>> lightness)) != 0) {
						dst[off] = rgb;
					}
					off++;
					uA += deltaU;
					vA += deltaV;

					if ((rgb = (texels[(vA & (127 << 7)) + (uA >> 7)] >>> lightness)) != 0) {
						dst[off] = rgb;
					}
					off++;
					uA += deltaU;
					vA += deltaV;

					if ((rgb = (texels[(vA & (127 << 7)) + (uA >> 7)] >>> lightness)) != 0) {
						dst[off] = rgb;
					}
					off++;
					uA += deltaU;
					vA += deltaV;

					if ((rgb = (texels[(vA & (127 << 7)) + (uA >> 7)] >>> lightness)) != 0) {
						dst[off] = rgb;
					}
					off++;
					uA += deltaU;
					vA += deltaV;

					if ((rgb = (texels[(vA & (127 << 7)) + (uA >> 7)] >>> lightness)) != 0) {
						dst[off] = rgb;
					}
					off++;
					uA += deltaU;
					vA += deltaV;

					if ((rgb = (texels[(vA & (127 << 7)) + (uA >> 7)] >>> lightness)) != 0) {
						dst[off] = rgb;
					}
					off++;
					uA = u2;
					vA = v2;

					verticalA += horizontalA;
					verticalB += horizontalB;
					verticalC += horizontalC;
					realC = (verticalC >> 14);

					if (realC != 0) {
						u2 = verticalA / realC;
						v2 = verticalB / realC;

						if (u2 < 7) {
							u2 = 7;
						} else if (u2 > (127 << 7)) {
							u2 = (127 << 7);
						}
					}
					deltaU = u2 - uA >> 3;
					deltaV = v2 - vA >> 3;
					lightnessA += lightnessSlope;
					uA += lightnessA & (3 << 21);
					lightness = lightnessA >> 23;
				}

				length = xB - xA & 0x7;

				while (length-- > 0) {
					int rgb;
					if ((rgb = (texels[(vA & (127 << 7)) + (uA >> 7)] >>> lightness)) != 0) {
						dst[off] = rgb;
					}
					off++;
					uA += deltaU;
					vA += deltaV;
				}
			}
		}
	}

	static {
		for (int i = 1; i < 512; i++) {
			oneOverFixed1715[i] = (1 << 15) / i;
		}

		for (int i = 1; i < 2048; i++) {
			oneOverFixed1616[i] = (1 << 16) / i;
		}

		for (int i = 0; i < 2048; i++) {
			sin[i] = (int) (65536.0 * Math.sin((double) i * 0.0030679615));
			cos[i] = (int) (65536.0 * Math.cos((double) i * 0.0030679615));
		}

		textures = new IndexedSprite[50];
		textureHasTransparency = new boolean[50];
		textureColors = new int[50];
		texelBuffer1 = new int[50][];
		textureCycles = new int[50];
		palette = new int[65536];
		texturePalettes = new int[50][];
	}
}
