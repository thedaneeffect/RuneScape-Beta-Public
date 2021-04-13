package rs.scene;

import rs.data.FloType;
import rs.data.LocType;
import rs.data.SeqType;
import rs.io.Buffer;
import rs.media.Draw3D;
import rs.util.LinkedList;

public final class SceneBuilder {

	public static final int VIEW_DIAMETER = 50;
	public static final int VIEW_RADIUS = VIEW_DIAMETER / 2;
	public static final int FAR_Z = 128 * VIEW_RADIUS;
	public static final int NEAR_Z = 50;

	private static final int[] WALL_ROTATION_TYPE1 = {0x1, 0x2, 0x4, 0x8};
	private static final int[] WALL_ROTATION_TYPE2 = {0x10, 0x20, 0x40, 0x80};
	private static final int[] WALL_DECO_ROT_SIZE_X_DIR = {1, 0, -1, 0};
	private static final int[] WALL_DECO_ROT_SIZE_Y_DIR = {0, -1, 0, 1};

	/**
	 * Forces all locations to be right clickable whether their names are null or they have no actions.
	 */
	public static boolean forceInteraction = false;

	public static boolean lowmemory = true;
	public static int levelBuilt;

	/**
	 * I accidentally broke the occluders somehow. Luckily that was the only thing that is really broken. Having this
	 * disabled does not affect the visual part of the game, but it does affect performance. Occluders are invisible
	 * walls or boxes which determine whether things behind it should be rendered or not.
	 */
	public static boolean occlusionEnabled = true;
	public static boolean checkClick;

	public static int clickX;
	public static int clickY;

	public static int mouseX;
	public static int mouseY;

	public static int clickedTileX = -1;
	public static int clickedTileZ = -1;

	public int tileCountX;
	public int tileCountZ;
	public int[][][] heightmap;
	public byte[][][] renderFlags;
	public byte[][][] planeUnderlayFloorIndices;
	public byte[][][] planeOverlayFloorIndices;
	public byte[][][] planeOverlayTypes;
	public byte[][][] planeOverlayRotations;
	public byte[][][] shadowmap;
	public int[][] lightmap;
	public int[] blendedHue;
	public int[] blendedSaturation;
	public int[] blendedLightness;
	public int[] blendedHueMultiplier;
	public int[] blendDirectionTracker;
	public int[][][] occludeFlags;

	public SceneBuilder(int sizeX, int sizeY, byte[][][] renderFlags, int[][][] heightmap) {
		this.tileCountX = sizeX;
		this.tileCountZ = sizeY;
		this.heightmap = heightmap;
		this.renderFlags = renderFlags;
		this.planeUnderlayFloorIndices = new byte[4][tileCountX][tileCountZ];
		this.planeOverlayFloorIndices = new byte[4][tileCountX][tileCountZ];
		this.planeOverlayTypes = new byte[4][tileCountX][tileCountZ];
		this.planeOverlayRotations = new byte[4][tileCountX][tileCountZ];
		this.occludeFlags = new int[4][tileCountX + 1][tileCountZ + 1];
		this.shadowmap = new byte[4][tileCountX + 1][tileCountZ + 1];
		this.lightmap = new int[tileCountX + 1][tileCountZ + 1];
		this.blendedHue = new int[tileCountZ];
		this.blendedSaturation = new int[tileCountZ];
		this.blendedLightness = new int[tileCountZ];
		this.blendedHueMultiplier = new int[tileCountZ];
		this.blendDirectionTracker = new int[tileCountZ];
	}

	public final void clearLandscape(int tileX, int tileY, int width, int height) {
		byte defaultFloor = 0;
		for (int n = 0; n < FloType.count; n++) {
			if (FloType.instances[n].name.equalsIgnoreCase("water")) {
				defaultFloor = (byte) (n + 1);
				break;
			}
		}

		for (int y = tileY; y < tileY + height; y++) {
			for (int x = tileX; x < tileX + width; x++) {
				if (x >= 0 && x < tileCountX && y >= 0 && y < tileCountZ) {
					planeOverlayFloorIndices[0][x][y] = defaultFloor;

					for (int plane = 0; plane < 4; plane++) {
						heightmap[plane][x][y] = 0;
						renderFlags[plane][x][y] = (byte) 0;
					}
				}
			}
		}
	}

	public final void readLandscape(byte[] src, int baseTileX, int baseTileY, int baseChunkTileX, int baseChunkTileY) {
		Buffer b = new Buffer(src);
		for (int plane = 0; plane < 4; plane++) {
			for (int tileX = 0; tileX < 64; tileX++) {
				for (int tileY = 0; tileY < 64; tileY++) {
					int x = tileX + baseTileX;
					int y = tileY + baseTileY;

					if (x >= 0 && x < 104 && y >= 0 && y < 104) {
						renderFlags[plane][x][y] = (byte) 0;
						for (; ; ) {
							int type = b.get1U();

							if (type == 0) {
								if (plane == 0) {
									heightmap[0][x][y] = -getPerlinNoise((x + 932731 + baseChunkTileX), (y + 556238 + baseChunkTileY)) * 8;
								} else {
									heightmap[plane][x][y] = heightmap[plane - 1][x][y] - 240;
								}
								break;
							}

							if (type == 1) {
								int i = b.get1U();

								if (i == 1) {
									i = 0;
								}

								if (plane == 0) {
									heightmap[0][x][y] = -i * 8;
								} else {
									heightmap[plane][x][y] = heightmap[plane - 1][x][y] - (i * 8);
								}
								break;
							}

							if (type <= 49) {
								planeOverlayFloorIndices[plane][x][y] = b.get1();
								planeOverlayTypes[plane][x][y] = (byte) ((type - 2) / 4);
								planeOverlayRotations[plane][x][y] = (byte) (type - 2 & 0x3);
							} else if (type <= 81) {
								renderFlags[plane][x][y] = (byte) (type - 49);
							} else {
								planeUnderlayFloorIndices[plane][x][y] = (byte) (type - 81);
							}
						}
					} else {
						for (; ; ) {
							int i = b.get1U();
							if (i == 0) {
								break;
							}
							if (i == 1) {
								b.get1U();
								break;
							}
							if (i <= 49) {
								b.get1U();
							}
						}
					}
				}
			}
		}
	}

	public final void readLocs(byte[] src, int mapBaseX, int mapBaseY, Scene graph, CollisionMap[] planeCollisions, LinkedList sequencedLocs) {
		Buffer b = new Buffer(src);
		int locIndex = -1;

		for (; ; ) {
			int msb = b.getSmartU();

			if (msb == 0) {
				break;
			}

			locIndex += msb;
			int position = 0;

			for (; ; ) {
				int lsb = b.getSmartU();

				if (lsb == 0) {
					break;
				}

				position += lsb - 1;

				int x = position & 0x3f;
				int y = position >> 6 & 0x3f;
				int plane = position >> 12;

				int flags = b.get1U();
				int rotation = flags & 0x3;
				int type = flags >> 2;

				int tileX = y + mapBaseX;
				int tileY = x + mapBaseY;

				if (tileX > 0 && tileY > 0 && tileX < 103 && tileY < 103) {
					addLoc(locIndex, type, graph, planeCollisions[plane], sequencedLocs, tileX, tileY, plane, rotation);
				}
			}
		}
	}

	public int getRenderLevel(int level, int x, int z) {
		if ((renderFlags[level][x][z] & 0x8) != 0) {
			return 0;
		}
		if (level > 0 && (renderFlags[1][x][z] & 0x2) != 0) {
			return level - 1;
		}
		return level;
	}

	public final void addLoc(int locIndex, int type, Scene graph, CollisionMap collision, LinkedList animatedLocations, int tileX, int tileY, int level, int rotation) {
		if (lowmemory) {
			if (getRenderLevel(level, tileX, tileY) != levelBuilt || (renderFlags[level][tileX][tileY] & 0x10) != 0) {
				return;
			}
		}

		int southwestY = heightmap[level][tileX][tileY];
		int southeastY = heightmap[level][tileX + 1][tileY];
		int northeastY = heightmap[level][tileX + 1][tileY + 1];
		int northwestY = heightmap[level][tileX][tileY + 1];
		int averageY = (southwestY + southeastY + northeastY + northwestY) >> 2;

		LocType l = LocType.get(locIndex);

		if (l == null) {
			return;
		}

		int bitset = tileX + (tileY << 7) + (locIndex << 14) + (LocType.TYPE_NORMAL << 29);

		if (l.animationIndex >= SeqType.count) {
			l.animationIndex = -1;
		}

		if (!forceInteraction && !l.interactable) {
			bitset += Integer.MIN_VALUE;
		}

		byte info = (byte) ((rotation << 6) + type);

		if (type == 22) {
			if (!lowmemory || l.interactable) {
				Model m = l.getModel(22, rotation, southwestY, southeastY, northeastY, northwestY, -1);
				graph.addGroundDecoration(m, level, tileX, tileY, averageY, info, bitset);

				if (l.hasCollision && l.interactable) {
					collision.setBlocked(tileX, tileY);
				}
			}
		} else if (type == 10 || type == 11) {
			Model m = l.getModel(10, rotation, southwestY, southeastY, northeastY, northwestY, -1);

			if (m != null) {
				int yaw = 0;

				if (type == 11) {
					yaw += 256;
				}

				int sizeX;
				int sizeY;

				if (rotation == 1 || rotation == 3) {
					sizeX = l.sizeZ;
					sizeY = l.sizeX;
				} else {
					sizeX = l.sizeX;
					sizeY = l.sizeZ;
				}

				if (graph.addLocation(m, null, tileX, tileY, sizeX, sizeY, averageY, level, yaw, bitset, info) && l.hasShadow) {
					for (int x = 0; x <= sizeX; x++) {
						for (int y = 0; y <= sizeY; y++) {
							int darkness = m.lengthXZ / 4;

							if (darkness > 30) {
								darkness = 30;
							}

							if (darkness > shadowmap[level][tileX + x][tileY + y]) {
								shadowmap[level][tileX + x][tileY + y] = (byte) darkness;
							}
						}
					}
				}
			}

			if (l.hasCollision) {
				collision.setLoc(tileX, tileY, l.sizeX, l.sizeZ, rotation, l.isSolid);
			}

			if (l.animationIndex != -1) {
				animatedLocations.push(new AnimatedLoc(SeqType.instances[l.animationIndex], locIndex, 2, tileX, tileY, level));
			}
		} else if (type >= 12) {
			Model m = l.getModel(type, rotation, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addLocation(m, null, tileX, tileY, 1, 1, averageY, level, 0, bitset, info);

			if (type >= 12 && type <= 17 && type != 13 && level > 0) {
				occludeFlags[level][tileX][tileY] |= 0x800 | 0x100 | 0x20 | 0x4;
			}

			if (l.hasCollision) {
				collision.setLoc(tileX, tileY, l.sizeX, l.sizeZ, rotation, l.isSolid);
			}

			if (l.animationIndex != -1) {
				animatedLocations.push(new AnimatedLoc(SeqType.instances[l.animationIndex], locIndex, 2, tileX, tileY, level));
			}
		} else if (type == 0) {
			Model m = l.getModel(0, rotation, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addWall(m, null, level, tileX, tileY, averageY, bitset, info, WALL_ROTATION_TYPE1[rotation], 0);

			if (rotation == 0) {
				if (l.hasShadow) {
					shadowmap[level][tileX][tileY] = (byte) 50;
					shadowmap[level][tileX][tileY + 1] = (byte) 50;
				}

				if (l.culls) {
					occludeFlags[level][tileX][tileY] |= 0x200 | 0x40 | 0x8 | 0x1;
				}
			} else if (rotation == 1) {
				if (l.hasShadow) {
					shadowmap[level][tileX][tileY + 1] = (byte) 50;
					shadowmap[level][tileX + 1][tileY + 1] = (byte) 50;
				}

				if (l.culls) {
					occludeFlags[level][tileX][tileY + 1] |= 0x400 | 0x80 | 0x10 | 0x2;
				}
			} else if (rotation == 2) {
				if (l.hasShadow) {
					shadowmap[level][tileX + 1][tileY] = (byte) 50;
					shadowmap[level][tileX + 1][tileY + 1] = (byte) 50;
				}

				if (l.culls) {
					occludeFlags[level][tileX + 1][tileY] |= 0x200 | 0x40 | 0x8 | 0x1;
				}
			} else if (rotation == 3) {
				if (l.hasShadow) {
					shadowmap[level][tileX][tileY] = (byte) 50;
					shadowmap[level][tileX + 1][tileY] = (byte) 50;
				}

				if (l.culls) {
					occludeFlags[level][tileX][tileY] |= 0x400 | 0x80 | 0x10 | 0x2;
				}
			}

			if (l.hasCollision) {
				collision.setWall(tileX, tileY, type, rotation, l.isSolid);
			}
		} else if (type == 1) {
			Model m = l.getModel(1, rotation, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addWall(m, null, level, tileX, tileY, averageY, bitset, info, WALL_ROTATION_TYPE2[rotation], 0);

			if (l.hasShadow) {
				if (rotation == 0) {
					shadowmap[level][tileX][tileY + 1] = (byte) 50;
				} else if (rotation == 1) {
					shadowmap[level][tileX + 1][tileY + 1] = (byte) 50;
				} else if (rotation == 2) {
					shadowmap[level][tileX + 1][tileY] = (byte) 50;
				} else if (rotation == 3) {
					shadowmap[level][tileX][tileY] = (byte) 50;
				}
			}

			if (l.hasCollision) {
				collision.setWall(tileX, tileY, type, rotation, l.isSolid);
			}
		} else if (type == 2) {
			int nextRotation = rotation + 1 & 0x3;
			Model model1 = l.getModel(2, rotation + 4, southwestY, southeastY, northeastY, northwestY, -1);
			Model model2 = l.getModel(2, nextRotation, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addWall(model1, model2, level, tileX, tileY, averageY, bitset, info, WALL_ROTATION_TYPE1[rotation], WALL_ROTATION_TYPE1[nextRotation]);

			if (l.culls) {
				if (rotation == 0) {
					occludeFlags[level][tileX][tileY] |= 0x200 | 0x40 | 0x8 | 0x1;
					occludeFlags[level][tileX][tileY + 1] |= 0x400 | 0x80 | 0x10 | 0x2;
				} else if (rotation == 1) {
					occludeFlags[level][tileX][tileY + 1] |= 0x400 | 0x80 | 0x10 | 0x2;
					occludeFlags[level][tileX + 1][tileY] |= 0x200 | 0x40 | 0x8 | 0x1;
				} else if (rotation == 2) {
					occludeFlags[level][tileX + 1][tileY] |= 0x200 | 0x40 | 0x8 | 0x1;
					occludeFlags[level][tileX][tileY] |= 0x400 | 0x80 | 0x10 | 0x2;
				} else if (rotation == 3) {
					occludeFlags[level][tileX][tileY] |= 0x400 | 0x80 | 0x10 | 0x2;
					occludeFlags[level][tileX][tileY] |= 0x200 | 0x40 | 0x8 | 0x1;
				}
			}

			if (l.hasCollision) {
				collision.setWall(tileX, tileY, type, rotation, l.isSolid);
			}
		} else if (type == 3) {
			Model m = l.getModel(3, rotation, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addWall(m, null, level, tileX, tileY, averageY, bitset, info, WALL_ROTATION_TYPE2[rotation], 0);

			if (l.hasShadow) {
				if (rotation == 0) {
					shadowmap[level][tileX][tileY + 1] = (byte) 50;
				} else if (rotation == 1) {
					shadowmap[level][tileX + 1][tileY + 1] = (byte) 50;
				} else if (rotation == 2) {
					shadowmap[level][tileX + 1][tileY] = (byte) 50;
				} else if (rotation == 3) {
					shadowmap[level][tileX][tileY] = (byte) 50;
				}
			}
			if (l.hasCollision) {
				collision.setWall(tileX, tileY, type, rotation, l.isSolid);
			}
		} else if (type == 9) {
			Model m = l.getModel(type, rotation, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addLocation(m, null, tileX, tileY, 1, 1, averageY, level, 0, bitset, info);

			if (l.hasCollision) {
				collision.setLoc(tileX, tileY, l.sizeX, l.sizeZ, rotation, l.isSolid);
			}
		} else if (type == 4) {
			Model m = l.getModel(4, 0, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addWallDecoration(m, tileX, tileY, averageY, 0, 0, level, bitset, info, WALL_ROTATION_TYPE1[rotation], rotation * 512);

			if (l.animationIndex != -1) {
				animatedLocations.push(new AnimatedLoc(SeqType.instances[l.animationIndex], locIndex, 1, tileX, tileY, level));
			}
		} else if (type == 5) {
			int thickness = 16;
			int wallBitset = graph.getWallBitset(tileX, tileY, level);

			if (wallBitset > 0) {
				thickness = LocType.get(wallBitset >> 14 & 0x7fff).thickness;
			}

			Model m = l.getModel(4, 0, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addWallDecoration(m, tileX, tileY, averageY, WALL_DECO_ROT_SIZE_X_DIR[rotation] * thickness, WALL_DECO_ROT_SIZE_Y_DIR[rotation] * thickness, level, bitset, info, WALL_ROTATION_TYPE1[rotation], rotation * 512);

			if (l.animationIndex != -1) {
				animatedLocations.push(new AnimatedLoc(SeqType.instances[l.animationIndex], locIndex, 1, tileX, tileY, level));
			}
		} else if (type == 6) {
			Model m = l.getModel(4, 0, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addWallDecoration(m, tileX, tileY, averageY, 0, 0, level, bitset, info, 256, rotation);

			if (l.animationIndex != -1) {
				animatedLocations.push(new AnimatedLoc(SeqType.instances[l.animationIndex], locIndex, 1, tileX, tileY, level));
			}
		} else if (type == 7) {
			Model m = l.getModel(4, 0, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addWallDecoration(m, tileX, tileY, averageY, 0, 0, level, bitset, info, 512, rotation);

			if (l.animationIndex != -1) {
				animatedLocations.push(new AnimatedLoc(SeqType.instances[l.animationIndex], locIndex, 1, tileX, tileY, level));
			}
		} else if (type == 8) {
			Model m = l.getModel(4, 0, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addWallDecoration(m, tileX, tileY, averageY, 0, 0, level, bitset, info, 768, rotation);

			if (l.animationIndex != -1) {
				animatedLocations.push(new AnimatedLoc(SeqType.instances[l.animationIndex], locIndex, 1, tileX, tileY, level));
			}
		}
	}

	public final void buildLandscape(CollisionMap[] planeCollisions, Scene graph) {
		CollisionMap lastCollisionMap = null;

		for (int plane = 0; plane < 4; plane++) {
			CollisionMap collisionMap = planeCollisions[plane];

			for (int x = 0; x < 104; x++) {
				for (int y = 0; y < 104; y++) {
					if ((renderFlags[plane][x][y] & 0x1) == 1) {
						collisionMap.setBlocked(x, y);
					}

					// it's a bridge!
					if (plane > 0 && (renderFlags[1][x][y] & 0x2) == 2) {
						lastCollisionMap.flags[x][y] = collisionMap.flags[x][y];
					}
				}
			}
			lastCollisionMap = collisionMap;
		}

		for (int level = 0; level < 4; level++) {
			byte[][] sm = shadowmap[level];
			int minIntensity = 96;
			int lightSpecularFactor = 768;
			int lightX = -50;
			int lightY = -10;
			int lightZ = -50;
			int lightLength = (int) Math.sqrt((double) (lightX * lightX + lightY * lightY + lightZ * lightZ));
			int specularDistribution = (lightSpecularFactor * lightLength) >> 8;

			for (int tileY = 1; tileY < tileCountZ - 1; tileY++) {
				for (int tileX = 1; tileX < tileCountX - 1; tileX++) {
					int x = heightmap[level][tileX + 1][tileY] - heightmap[level][tileX - 1][tileY];
					int y = heightmap[level][tileX][tileY + 1] - heightmap[level][tileX][tileY - 1];
					int length = (int) Math.sqrt((double) ((x * x) + (256 * 256) + (y * y)));

					if (length == 0) {
						length = 256;
					}

					int normalX = (x << 8) / length;
					int normalY = (256 << 8) / length;
					int normalZ = (y << 8) / length;

					int intensity = minIntensity + (lightX * normalX + lightY * normalY + lightZ * normalZ) / specularDistribution;
					int subtraction = (sm[tileX - 1][tileY] >> 2) + (sm[tileX + 1][tileY] >> 3) + (sm[tileX][tileY - 1] >> 2) + (sm[tileX][tileY + 1] >> 3) + (sm[tileX][tileY] >> 1);
					lightmap[tileX][tileY] = intensity - subtraction;
				}
			}

			for (int y = 0; y < tileCountZ; y++) {
				blendedHue[y] = 0;
				blendedSaturation[y] = 0;
				blendedLightness[y] = 0;
				blendedHueMultiplier[y] = 0;
				blendDirectionTracker[y] = 0;
			}

			for (int x = -5; x < tileCountX + 5; x++) {
				for (int y = 0; y < tileCountZ; y++) {
					int dx = x + 5;

					if (dx >= 0 && dx < tileCountX) {
						int index = (planeUnderlayFloorIndices[level][dx][y] & 0xFF) - 1;

						if (index >= 0 && index < FloType.instances.length) {
							FloType f = FloType.instances[index];
							blendedHue[y] += f.blendHue;
							blendedSaturation[y] += f.saturation;
							blendedLightness[y] += f.lightness;
							blendedHueMultiplier[y] += f.blendHueMultiplier;
							blendDirectionTracker[y]++;
						}
					}

					dx = x - 5;

					if (dx >= 0 && dx < tileCountX) {
						int index = (planeUnderlayFloorIndices[level][dx][y] & 0xFF) - 1;

						if (index >= 0 && index < FloType.instances.length) {
							FloType f = FloType.instances[index];
							blendedHue[y] -= f.blendHue;
							blendedSaturation[y] -= f.saturation;
							blendedLightness[y] -= f.lightness;
							blendedHueMultiplier[y] -= f.blendHueMultiplier;
							blendDirectionTracker[y]--;
						}
					}
				}

				if (x >= 1 && x < tileCountX - 1) {
					int hue = 0;
					int saturation = 0;
					int lightness = 0;
					int hueDivisor = 0;
					int directionTracker = 0;

					for (int y = -5; y < tileCountZ + 5; y++) {
						int yD = y + 5;

						if (yD >= 0 && yD < tileCountZ) {
							hue += blendedHue[yD];
							saturation += blendedSaturation[yD];
							lightness += blendedLightness[yD];
							hueDivisor += blendedHueMultiplier[yD];
							directionTracker += blendDirectionTracker[yD];
						}

						yD = y - 5;

						if (yD >= 0 && yD < tileCountZ) {
							hue -= blendedHue[yD];
							saturation -= blendedSaturation[yD];
							lightness -= blendedLightness[yD];
							hueDivisor -= blendedHueMultiplier[yD];
							directionTracker -= blendDirectionTracker[yD];
						}

						if (y >= 1 && y < tileCountZ - 1) {
							if (lowmemory) {
								int p = level;

								// it's a bridge!
								if (level > 0 && (renderFlags[1][x][y] & 0x2) != 0) {
									p--;
								}

								if (((renderFlags[level][x][y]) & 0x8) != 0) {
									p = 0;
								}

								if (p != levelBuilt || ((renderFlags[level][x][y]) & 0x10) != 0) {
									continue;
								}
							}

							int underlayFloorIndex = planeUnderlayFloorIndices[level][x][y] & 0xFF;
							int overlayFloorIndex = planeOverlayFloorIndices[level][x][y] & 0xFF;

							if (underlayFloorIndex > 0 || overlayFloorIndex > 0) {
								int southwestY = heightmap[level][x][y];
								int southeastY = heightmap[level][x + 1][y];
								int northeastY = heightmap[level][x + 1][y + 1];
								int northwestY = heightmap[level][x][y + 1];

								int southwestLightness = lightmap[x][y];
								int southeastLightness = lightmap[x + 1][y];
								int northeastLightness = lightmap[x + 1][y + 1];
								int northwestLightness = lightmap[x][y + 1];

								int color = -1;

								if (underlayFloorIndex > 0) {
									if (hueDivisor != 0 && directionTracker != 0) {
										color = hsl24To16((hue * 256) / hueDivisor, saturation / directionTracker, lightness / directionTracker);
									}
								}

								if (level > 0 && !lowmemory) {
									boolean hideUnderlay = true;

									if (underlayFloorIndex == 0 && planeOverlayTypes[level][x][y] != 0) {
										hideUnderlay = false;
									}

									if (overlayFloorIndex > 0 && overlayFloorIndex - 1 < FloType.count && !(FloType.instances[overlayFloorIndex - 1].occlude)) {
										hideUnderlay = false;
									}

									if (hideUnderlay && southwestY == southeastY && southwestY == northeastY && southwestY == northwestY) {
										// Occlusion flags enabled:
										// FLAG		PLANE
										// C		0
										// A | B	1
										// B | C	2
										// A		3
										occludeFlags[level][x][y] |= 0b100_100_100_100;
									}
								}

								int minimapColor = 0;

								if (color != -1) {
									minimapColor = Draw3D.palette[adjustHSLLightness1(color, 96)];
								}

								if (overlayFloorIndex == 0) {
									graph.addTile(level, x, y, 0, 0, -1, southwestY, southeastY, northeastY, northwestY, adjustHSLLightness1(color, southwestLightness), adjustHSLLightness1(color, southeastLightness), adjustHSLLightness1(color, northeastLightness), adjustHSLLightness1(color, northwestLightness), 0, 0, 0, 0, minimapColor, 0);
								} else {
									int type = planeOverlayTypes[level][x][y] + 1;
									byte rotation = planeOverlayRotations[level][x][y];

									overlayFloorIndex--;

									if (overlayFloorIndex >= FloType.count) {
										overlayFloorIndex = 0;
									}

									FloType f = FloType.instances[overlayFloorIndex];
									int texture = f.textureIndex;
									int rgb;
									int hsl;

									if (texture >= 0) {
										rgb = Draw3D.getAverageTextureRGB(texture);
										hsl = -1;
									} else if (f.rgb == 0xFF00FF) {
										rgb = 0;
										hsl = -2;
										texture = -1;
									} else {
										hsl = hsl24To16(f.hue, f.saturation, f.lightness);
										rgb = Draw3D.palette[adjustHSLLightness0(f.hsl16, 96)];
									}

									graph.addTile(level, x, y, type, rotation, texture, southwestY, southeastY, northeastY, northwestY, adjustHSLLightness1(color, southwestLightness), adjustHSLLightness1(color, southeastLightness), adjustHSLLightness1(color, northeastLightness), adjustHSLLightness1(color, northwestLightness), adjustHSLLightness0(hsl, southwestLightness), adjustHSLLightness0(hsl, southeastLightness), adjustHSLLightness0(hsl, northeastLightness), adjustHSLLightness0(hsl, northwestLightness), minimapColor, rgb);
								}
							}
						}
					}
				}
			}

			for (int z = 1; z < tileCountZ - 1; z++) {
				for (int x = 1; x < tileCountX - 1; x++) {
					graph.setPhysicalLevel(level, x, z, getRenderLevel(level, x, z));
				}
			}
		}

		graph.applyLighting(-50, -10, -50, 64, 768);

		for (int x = 0; x < tileCountX; x++) {
			for (int z = 0; z < tileCountZ; z++) {
				if ((renderFlags[1][x][z] & 0x2) == 2) {
					graph.setBridge(x, z);
				}
			}
		}

		if (!SceneBuilder.occlusionEnabled) {
			return;
		}

		setupOccluders317();
	}

	public void setupOccluders317() {
		int flagA = 1;
		int flagB = 2;
		int flagC = 4;

		for (int levelTop = 0; levelTop < 4; levelTop++) {
			if (levelTop > 0) {
				flagA <<= 3;
				flagB <<= 3;
				flagC <<= 3;
			}

			for (int level = 0; level <= levelTop; level++) {
				for (int z = 0; z <= tileCountZ; z++) {
					for (int x = 0; x <= tileCountX; x++) {
						if ((occludeFlags[level][x][z] & flagA) != 0) {
							int minZ = z;
							int maxZ = z;
							int minLevel = level;
							int maxLevel = level;

							// findMinZ
							for (/**/; minZ > 0; minZ--) {
								if (((occludeFlags[level][x][minZ - 1]) & flagA) == 0)
									break;
							}

							// findMaxZ
							for (/**/; maxZ < tileCountZ; maxZ++) {
								if (((occludeFlags[level][x][maxZ + 1]) & flagA) == 0)
									break;
							}

							findBottomLevel:
							for (/**/; minLevel > 0; minLevel--) {
								for (int z0 = minZ; z0 <= maxZ; z0++) {
									if (((occludeFlags[minLevel - 1][x][z0]) & flagA) == 0) {
										break findBottomLevel;
									}
								}
							}

							findTopLevel:
							for (/**/; maxLevel < levelTop; maxLevel++) {
								for (int z0 = minZ; z0 <= maxZ; z0++) {
									if (((occludeFlags[maxLevel + 1][x][z0]) & flagA) == 0)
										break findTopLevel;
								}
							}

							int area = (maxLevel + 1 - minLevel) * (maxZ - minZ + 1);

							if (area >= 8) {
								int offsetY = 240;
								int minY = (heightmap[maxLevel][x][minZ] - offsetY);
								int maxY = heightmap[minLevel][x][minZ];

								Scene.addOcclude(1,
										x * 128, minY, minZ * 128,
										x * 128, maxY, maxZ * 128 + 128,
										levelTop
								);

								for (int l = minLevel; l <= maxLevel; l++) {
									for (int z0 = minZ; z0 <= maxZ; z0++) {
										occludeFlags[l][x][z0] &= flagA ^ 0xffffffff;
									}
								}
							}
						}

						if ((occludeFlags[level][x][z] & flagB) != 0) {
							int minX = x;
							int maxX = x;
							int minLevel = level;
							int maxLevel = level;

							// findMinX
							for (/**/; minX > 0; minX--) {
								if (((occludeFlags[level][minX - 1][z]) & flagB) == 0) {
									break;
								}
							}

							// findMaxX
							for (/**/; maxX < tileCountX; maxX++) {
								if (((occludeFlags[level][maxX + 1][z]) & flagB) == 0) {
									break;
								}
							}

							findBottomLevel:
							{
								for (/**/; minLevel > 0; minLevel--) {
									for (int x0 = minX; x0 <= maxX; x0++) {
										if (((occludeFlags[minLevel - 1][x0][z]) & flagB) == 0) {
											break findBottomLevel;
										}
									}
								}
							}

							findTopLevel:
							{
								for (/**/; maxLevel < levelTop; maxLevel++) {
									for (int x0 = minX; x0 <= maxX; x0++) {
										if (((occludeFlags[maxLevel + 1][x0][z]) & flagB) == 0) {
											break findTopLevel;
										}
									}
								}
							}

							int area = (maxLevel + 1 - minLevel) * (maxX - minX + 1);
							if (area >= 8) {
								int offsetY = 240;
								int minY = (heightmap[maxLevel][minX][z] - offsetY);
								int maxY = heightmap[minLevel][minX][z];

								Scene.addOcclude(2,
										minX * 128, minY, z * 128,
										maxX * 128 + 128, maxY, z * 128,
										levelTop
								);

								// remove flag b
								for (int l = minLevel; l <= maxLevel; l++) {
									for (int x0 = minX; x0 <= maxX; x0++) {
										occludeFlags[l][x0][z] &= flagB ^ 0xffffffff;
									}
								}
							}
						}
						if ((occludeFlags[level][x][z] & flagC) != 0) {
							int minX = x;
							int maxX = x;
							int minZ = z;
							int maxZ = z;

							// findMinZ
							for (/**/; minZ > 0; minZ--) {
								if (((occludeFlags[level][x][minZ - 1]) & flagC) == 0) {
									break;
								}
							}

							// findMaxZ
							for (/**/; maxZ < tileCountZ; maxZ++) {
								if (((occludeFlags[level][x][maxZ + 1]) & flagC) == 0) {
									break;
								}
							}

							findMinX:
							{
								for (/**/; minX > 0; minX--) {
									for (int z0 = minZ; z0 <= maxZ; z0++) {
										if (((occludeFlags[level][minX - 1][z0]) & flagC) == 0) {
											break findMinX;
										}
									}
								}
							}

							findMaxX:
							{
								for (/**/; maxX < tileCountX; maxX++) {
									for (int z0 = minZ; z0 <= maxZ; z0++) {
										if (((occludeFlags[level][maxX + 1][z0]) & flagC) == 0) {
											break findMaxX;
										}
									}
								}
							}

							if ((maxX - minX + 1) * (maxZ - minZ + 1) >= 4) {
								int minY = heightmap[level][minX][minZ];

								Scene.addOcclude(4,
										minX * 128, minY, minZ * 128,
										maxX * 128 + 128, minY, maxZ * 128 + 128,
										levelTop
								);

								for (int x0 = minX; x0 <= maxX; x0++) {
									for (int z0 = minZ; z0 <= maxZ; z0++) {
										occludeFlags[level][x0][z0] &= flagC ^ 0xffffffff;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public void setupOccluders186() {
		int flagA = 1;
		int flagB = 2;
		int flagC = 4;

		for (int topPlane = 0; topPlane < 4; topPlane++) {
			if (topPlane > 0) {
				flagA <<= 3;
				flagB <<= 3;
				flagC <<= 3;
			}
			for (int plane = 0; plane <= topPlane; plane++) {
				for (int tileZ = 0; tileZ <= tileCountZ; tileZ++) {
					for (int tileX = 0; tileX <= tileCountX; tileX++) {
						if ((occludeFlags[plane][tileX][tileZ] & flagA) != 0) {
							int minTileZ = tileZ;
							int maxTileZ = tileZ;
							int minPlane = plane;
							int maxPlane = plane;

							for (/**/; minTileZ > 0; minTileZ--) {
								if (((occludeFlags[plane][tileX][minTileZ - 1]) & flagA) == 0) {
									break;
								}
							}

							for (/**/; maxTileZ < tileCountZ; maxTileZ++) {
								if (((occludeFlags[plane][tileX][maxTileZ + 1])
										& flagA)
										== 0) {
									break;
								}
							}

							FIND_MIN_PLANE:
							for (/**/; minPlane > 0; minPlane--) {
								for (int z = minTileZ; z <= maxTileZ; z++) {
									if (((occludeFlags[minPlane - 1][tileX][z]) & flagA) == 0) {
										break FIND_MIN_PLANE;
									}
								}
							}

							FIND_MAX_PLANE:
							for (/**/; maxPlane < topPlane; maxPlane++) {
								for (int i_135_ = minTileZ; i_135_ <= maxTileZ; i_135_++) {
									if (((occludeFlags[maxPlane + 1][tileX][i_135_]) & flagA) == 0) {
										break FIND_MAX_PLANE;
									}
								}
							}

							int area = ((maxPlane + 1 - minPlane) * (maxTileZ - minTileZ + 1));

							if (area >= 8) {
								int offsetY = 240;
								int minY = ((heightmap[maxPlane][tileX][minTileZ]) - offsetY);
								int maxY = (heightmap[minPlane][tileX][minTileZ]);

								Scene.addOcclude(1,
										tileX * 128, minY, minTileZ * 128,
										tileX * 128, maxY, maxTileZ * 128 + 128,
										topPlane
								);

								// remove flag a
								for (int p = minPlane; p <= maxPlane; p++) {
									for (int z = minTileZ; z <= maxTileZ; z++) {
										occludeFlags[p][tileX][z] &= flagA ^ 0xffffffff;
									}
								}
							}
						}

						if ((occludeFlags[plane][tileX][tileZ] & flagB) != 0) {
							int minTileX = tileX;
							int maxTileX = tileX;
							int minPlane = plane;
							int maxPlane = plane;

							for (/**/; minTileX > 0; minTileX--) {
								if (((occludeFlags[plane][minTileX - 1][tileZ]) & flagB) == 0) {
									break;
								}
							}

							for (/**/; maxTileX < tileCountX; maxTileX++) {
								if (((occludeFlags[plane][maxTileX + 1][tileZ]) & flagB) == 0) {
									break;
								}
							}

							FIND_MIN_PLANE:
							for (/**/; minPlane > 0; minPlane--) {
								for (int i_146_ = minTileX; i_146_ <= maxTileX; i_146_++) {
									if (((occludeFlags[minPlane - 1][i_146_][tileZ]) & flagB) == 0) {
										break FIND_MIN_PLANE;
									}
								}
							}
							FIND_MAX_PLANE:
							for (/**/; maxPlane < topPlane; maxPlane++) {
								for (int i_147_ = minTileX; i_147_ <= maxTileX; i_147_++) {
									if (((occludeFlags[maxPlane + 1][i_147_][tileZ]) & flagB) == 0) {
										break FIND_MAX_PLANE;
									}
								}
							}

							int area = ((maxPlane + 1 - minPlane) * (maxTileX - minTileX + 1));
							if (area >= 8) {
								int offsetY = 240;
								int minY = ((heightmap[maxPlane][minTileX][tileZ]) - offsetY);
								int maxY = (heightmap[minPlane][minTileX][tileZ]);

								Scene.addOcclude(2,
										minTileX * 128, minY, tileZ * 128,
										maxTileX * 128 + 128, maxY, tileZ * 128,
										topPlane);

								for (int p = minPlane; p <= maxPlane; p++) {
									for (int x = minTileX; x <= maxTileX; x++) {
										occludeFlags[p][x][tileZ] &= flagB ^ 0xffffffff;
									}
								}
							}
						}

						if (!lowmemory && (occludeFlags[plane][tileX][tileZ] & flagC) != 0) {
							int minTileX = tileX;
							int maxTileX = tileX;
							int minTileZ = tileZ;
							int maxTileZ = tileZ;

							for (/**/; minTileZ > 0; minTileZ--) {
								if (((occludeFlags[plane][tileX][minTileZ - 1]) & flagC) == 0) {
									break;
								}
							}

							for (/**/; maxTileZ < tileCountZ; maxTileZ++) {
								if (((occludeFlags[plane][tileX][maxTileZ + 1]) & flagC) == 0) {
									break;
								}
							}

							FIND_MIN_X:
							for (/**/; minTileX > 0; minTileX--) {
								for (int i_158_ = minTileZ; i_158_ <= maxTileZ; i_158_++) {
									if (((occludeFlags[plane][minTileX - 1][i_158_]) & flagC) == 0) {
										break FIND_MIN_X;
									}
								}
							}

							FIND_MAX_X:
							for (/**/; maxTileX < tileCountX; maxTileX++) {
								for (int i_159_ = minTileZ; i_159_ <= maxTileZ; i_159_++) {
									if (((occludeFlags[plane][maxTileX + 1][i_159_]) & flagC) == 0) {
										break FIND_MAX_X;
									}
								}
							}

							if ((maxTileX - minTileX + 1) * (maxTileZ - minTileZ + 1) >= 4) {
								int minY = (heightmap[plane][minTileX][minTileZ]);

								Scene.addOcclude(4,
										minTileX * 128, minY, minTileZ * 128,
										maxTileX * 128 + 128, minY, maxTileZ * 128 + 128,
										topPlane);

								for (int x = minTileX; x <= maxTileX; x++) {
									for (int z = minTileZ; z <= maxTileZ; z++) {
										occludeFlags[plane][x][z] &= flagC ^ 0xffffffff;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public static final int getPerlinNoise(int x, int y) {
		int v = (getSmoothNoise(x + 45365, y + 91923, 4) - 128 + (getSmoothNoise(x + 10294, y + 37821, 2) - 128 >> 1) + (getSmoothNoise(x, y, 1) - 128 >> 2));
		v = (int) ((double) v * 0.3) + 35;

		if (v < 10) {
			v = 10;
		} else if (v > 60) {
			v = 60;
		}

		return v;
	}

	private static int getSmoothNoise(int x, int y, int fraction) {
		int x1 = x / fraction;
		int x2 = x & fraction - 1;
		int y1 = y / fraction;
		int y2 = y & fraction - 1;
		int a = getSmoothNoise2D(x1, y1);
		int b = getSmoothNoise2D(x1 + 1, y1);
		int c = getSmoothNoise2D(x1, y1 + 1);
		int d = getSmoothNoise2D(x1 + 1, y1 + 1);
		int e = getCosineLerp(a, b, x2, fraction);
		int f = getCosineLerp(c, d, x2, fraction);
		return getCosineLerp(e, f, y2, fraction);
	}

	private static int getCosineLerp(int a, int b, int ft, int frac) {
		int f = (65536 - (Draw3D.cos[ft * 1024 / frac]) >> 1);
		return (a * (65536 - f) >> 16) + (b * f >> 16);
	}

	private static int getSmoothNoise2D(int x, int y) {
		int corners = (getNoise(x - 1, y - 1) + getNoise(x + 1, y - 1) + getNoise(x - 1, y + 1) + getNoise(x + 1, y + 1));
		int sides = (getNoise(x - 1, y) + getNoise(x + 1, y) + getNoise(x, y - 1) + getNoise(x, y + 1));
		int center = getNoise(x, y);
		return corners / 16 + sides / 8 + center / 4;
	}

	private static int getNoise(int x, int y) {
		int z = x + y * 57;
		z = z << 13 ^ z;
		int v = (z * (z * z * 15731 + 789221) + 1376312589 & 0x7fffffff);
		return (v >> 19) & 0xFF;
	}

	private static int adjustHSLLightness1(int hsl, int lightness) {
		if (hsl == -1) {
			return 12345678;
		}

		lightness = lightness * (hsl & 0x7f) / 128;

		if (lightness < 2) {
			lightness = 2;
		} else if (lightness > 126) {
			lightness = 126;
		}

		return (hsl & 0xff80) + lightness;
	}

	private int adjustHSLLightness0(int hsl, int lightness) {
		if (hsl == -2) {
			return 12345678;
		}

		if (hsl == -1) {
			if (lightness < 0) {
				lightness = 0;
			} else if (lightness > 127) {
				lightness = 127;
			}
			lightness = 127 - lightness;
			return lightness;
		}

		lightness = lightness * (hsl & 0x7f) / 128;

		if (lightness < 2) {
			lightness = 2;
		} else if (lightness > 126) {
			lightness = 126;
		}
		return (hsl & 0xff80) + lightness;
	}

	private int hsl24To16(int hue, int saturation, int lightness) {
		if (lightness > 179) {
			saturation /= 2;
		}
		if (lightness > 192) {
			saturation /= 2;
		}
		if (lightness > 217) {
			saturation /= 2;
		}
		if (lightness > 243) {
			saturation /= 2;
		}
		return (hue / 4 << 10) + (saturation / 32 << 7) + lightness / 2;
	}

	public static final void addLoc(int type, int index, int tileX, int tileY, int plane, int groundPlane, int rotation, int[][][] planeHeightmaps, Scene graph, CollisionMap collision, LinkedList animatedLocations) {
		int southwestY = planeHeightmaps[groundPlane][tileX][tileY];
		int southeastY = planeHeightmaps[groundPlane][tileX + 1][tileY];
		int northeastY = planeHeightmaps[groundPlane][tileX + 1][tileY + 1];
		int northwestY = planeHeightmaps[groundPlane][tileX][tileY + 1];
		int averageY = southwestY + southeastY + northeastY + northwestY >> 2;

		LocType c = LocType.get(index);

		int bitset = tileX + (tileY << 7) + (index << 14) + 0x40000000;

		if (!c.interactable) {
			bitset |= 0x80000000;
		}

		byte info = (byte) ((rotation << 6) + type);

		if (type == 22) {
			Model m = c.getModel(22, rotation, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addGroundDecoration(m, plane, tileX, tileY, averageY, info, bitset);

			if (c.hasCollision && c.interactable) {
				collision.setBlocked(tileX, tileY);
			}
		} else if (type == 10 || type == 11) {
			Model m = c.getModel(10, rotation, southwestY, southeastY, northeastY, northwestY, -1);

			if (m != null) {
				int yaw = 0;

				if (type == 11) {
					yaw += 256;
				}

				int sizeY;
				int sizeX;

				if (rotation == 1 || rotation == 3) {
					sizeY = c.sizeZ;
					sizeX = c.sizeX;
				} else {
					sizeY = c.sizeX;
					sizeX = c.sizeZ;
				}

				graph.addLocation(m, null, tileX, tileY, sizeY, sizeX, averageY, plane, yaw, bitset, info);
			}

			if (c.hasCollision) {
				collision.setLoc(tileX, tileY, c.sizeX, c.sizeZ, rotation, c.isSolid);
			}

			if (c.animationIndex != -1) {
				animatedLocations.push(new AnimatedLoc(SeqType.instances[c.animationIndex], index, 2, tileX, tileY, plane));
			}
		} else if (type >= 12) {
			Model m = c.getModel(type, rotation, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addLocation(m, null, tileX, tileY, 1, 1, averageY, plane, 0, bitset, info);

			if (c.hasCollision) {
				collision.setLoc(tileX, tileY, c.sizeX, c.sizeZ, rotation, c.isSolid);
			}

			if (c.animationIndex != -1) {
				animatedLocations.push(new AnimatedLoc(SeqType.instances[c.animationIndex], index, 2, tileX, tileY, plane));
			}
		} else if (type == 0) {
			Model m = c.getModel(0, rotation, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addWall(m, null, plane, tileX, tileY, averageY, bitset, info, WALL_ROTATION_TYPE1[rotation], 0);

			if (c.hasCollision) {
				collision.setWall(tileX, tileY, type, rotation, c.isSolid);
			}
		} else if (type == 1) {
			Model m = c.getModel(1, rotation, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addWall(m, null, plane, tileX, tileY, averageY, bitset, info, WALL_ROTATION_TYPE2[rotation], 0);

			if (c.hasCollision) {
				collision.setWall(tileX, tileY, type, rotation, c.isSolid);
			}
		} else if (type == 2) {
			int nextRotation = rotation + 1 & 0x3;
			Model model1 = c.getModel(2, rotation + 4, southwestY, southeastY, northeastY, northwestY, -1);
			Model model2 = c.getModel(2, nextRotation, southwestY, southeastY, northeastY, northwestY, -1);

			graph.addWall(model1, model2, plane, tileX, tileY, averageY, bitset, info, WALL_ROTATION_TYPE1[rotation], WALL_ROTATION_TYPE1[nextRotation]);

			if (c.hasCollision) {
				collision.setWall(tileX, tileY, type, rotation, c.isSolid);
			}
		} else if (type == 3) {
			Model m = c.getModel(3, rotation, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addWall(m, null, plane, tileX, tileY, averageY, bitset, info, WALL_ROTATION_TYPE2[rotation], 0);

			if (c.hasCollision) {
				collision.setWall(tileX, tileY, type, rotation, c.isSolid);
			}
		} else if (type == 9) {
			Model m = c.getModel(type, rotation, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addLocation(m, null, tileX, tileY, 1, 1, averageY, plane, 0, bitset, info);

			if (c.hasCollision) {
				collision.setLoc(tileX, tileY, c.sizeX, c.sizeZ, rotation, c.isSolid);
			}
		} else if (type == 4) {
			Model m = c.getModel(4, 0, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addWallDecoration(m, tileX, tileY, averageY, 0, 0, plane, bitset, info, WALL_ROTATION_TYPE1[rotation], rotation * 512);

			if (c.animationIndex != -1) {
				animatedLocations.push(new AnimatedLoc(SeqType.instances[c.animationIndex], index, 1, tileX, tileY, plane));
			}
		} else if (type == 5) {
			int thickness = 16;
			int wallBitset = graph.getWallBitset(tileX, tileY, plane);

			if (wallBitset > 0) {
				thickness = LocType.get(wallBitset >> 14 & 0x7fff).thickness;
			}

			Model m = c.getModel(4, 0, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addWallDecoration(m, tileX, tileY, averageY, WALL_DECO_ROT_SIZE_X_DIR[rotation] * thickness, WALL_DECO_ROT_SIZE_Y_DIR[rotation] * thickness, plane, bitset, info, WALL_ROTATION_TYPE1[rotation], rotation * 512);

			if (c.animationIndex != -1) {
				animatedLocations.push(new AnimatedLoc(SeqType.instances[c.animationIndex], index, 1, tileX, tileY, plane));
			}
		} else if (type == 6) {
			Model m = c.getModel(4, 0, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addWallDecoration(m, tileX, tileY, averageY, 0, 0, plane, bitset, info, 0x100, rotation);

			if (c.animationIndex != -1) {
				animatedLocations.push(new AnimatedLoc(SeqType.instances[c.animationIndex], index, 1, tileX, tileY, plane));
			}
		} else if (type == 7) {
			Model m = c.getModel(4, 0, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addWallDecoration(m, tileX, tileY, averageY, 0, 0, plane, bitset, info, 0x200, rotation);

			if (c.animationIndex != -1) {
				animatedLocations.push(new AnimatedLoc(SeqType.instances[c.animationIndex], index, 1, tileX, tileY, plane));
			}
		} else if (type == 8) {
			Model m = c.getModel(4, 0, southwestY, southeastY, northeastY, northwestY, -1);
			graph.addWallDecoration(m, tileX, tileY, averageY, 0, 0, plane, bitset, info, 0x300, rotation);

			if (c.animationIndex != -1) {
				animatedLocations.push(new AnimatedLoc(SeqType.instances[c.animationIndex], index, 1, tileX, tileY, plane));
			}
		}
	}
}
