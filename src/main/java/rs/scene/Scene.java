package rs.scene;

import rs.data.LocType;
import rs.data.Model;
import rs.media.Draw2D;
import rs.media.Draw3D;
import rs.util.LinkedList;

public final class Scene {

	public static boolean lowmemory = true;

	public int maxLevel;
	public int tileCountX;
	public int tileCountZ;
	public int[][][] heightmap;
	public Tile[][][] levelTiles;
	public int minLevel;
	public int locCount;
	public Loc[] locs = new Loc[5000];
	public int[][][] levelTileCycles;
	public static int lastTileUpdateCount;
	public static int tileUpdateCount;
	public static int activeLevel;
	public static int cycle;
	public static int minTileX;
	public static int maxTileX;
	public static int minTileZ;
	public static int maxTileZ;
	public static int cameraTileX;
	public static int cameraTileZ;
	public static int cameraX;
	public static int cameraY;
	public static int cameraZ;
	public static int pitchSin;
	public static int pitchCos;
	public static int yawSin;
	public static int yawCos;
	public static Loc[] locBuffer = new Loc[100];

	public static final int[] DECO_TYPE1_OFFSET_X = {53, -53, -53, 53};
	public static final int[] DECO_TYPE1_OFFSET_Z = {-53, -53, 53, 53};
	public static final int[] DECO_TYPE2_OFFSET_X = {-45, 45, 45, -45};
	public static final int[] DECO_TYPE2_OFFSET_Z = {45, 45, -45, -45};

	public static final int MAX_OCCLUDER_LEVELS = 4;
	public static int[] levelOccluderCount = new int[MAX_OCCLUDER_LEVELS];
	public static Occluder[][] levelOccluders = new Occluder[MAX_OCCLUDER_LEVELS][500];
	public static int activeOccluderCount;
	public static Occluder[] activeOccluders = new Occluder[500];
	public static LinkedList tileQueue = new LinkedList();

	// EAST NORTH WEST SOUTH
	//
	//WALL_ROTATION_TYPE1 = {0x1, 0x2, 0x4, 0x8} main piece
	//WALL_ROTATION_TYPE2 = {0x10, 0x20, 0x40, 0x80} extension piece
	//
	// [0] = cameraTileX > tileX && cameraTileZ < tileZ
	// [1] = cameraTileX == tileX && cameraTileZ < tileZ
	// [2] = cameraTileX < tileX && cameraTileZ < tileZ
	// [3] = cameraTileX > tileX && cameratileZ == tileZ
	// [4] = cameraTileX == tileX && cameraTileZ == tileZ
	// [5] = cameraTileX < tileX && cameraTileZ == tileZ
	// [6] = cameraTileX > tileX && cameraTileZ > tileZ
	// [7] = cameraTileX == tileX && cameraTileZ > tileZ
	// [8] = cameraTileX < tileX && cameraTileZ > tileZ
	//
	//
	// @formatter:off
	public static final int[] TILE_WALL_DRAW_FLAGS_0 = {
			0x10 | 0x2 | 0x1,
			0x20 | 0x10 | 0x4 | 0x2 | 0x1,
			0x20 | 0x4 | 0x2,
			0x80 | 0x10 | 0x8 | 0x2 | 0x1,
			0x80 | 0x40 | 0x20 | 0x10 | 0x8 | 0x4 | 0x2 | 0x1,
			0x40 | 0x20 | 0x8 | 0x4 | 0x2,
			0x80 | 0x8 | 0x1,
			0x80 | 0x40 | 0x8 | 0x4 | 0x1,
			0x40 | 0x8 | 0x4
	};

	public static final int[] WALL_DRAW_FLAGS = {
			0x80 | 0x20,
			0x80 | 0x40,
			0x40 | 0x10,
			0x40 | 0x20,
			0x0,
			0x80 | 0x10,
			0x40 | 0x10,
			0x20 | 0x10,
			0x80 | 0x20
	};

	public static final int[] TILE_WALL_DRAW_FLAGS_1 = {
			0x40 | 0x8 | 0x4,
			0x8,
			0x80 | 0x8 | 0x1,
			0x4,
			0x0,
			0x1,
			0x20 | 0x4 | 0x2,
			0x2,
			0x10 | 0x2 | 0x1
	};

	public static final int[] WALL_UNCULL_FLAGS_0 = {0, 0, 2, 0, 0, 2, 1, 1, 0};
	public static final int[] WALL_UNCULL_FLAGS_1 = {2, 0, 0, 2, 0, 0, 0, 4, 4};
	public static final int[] WALL_UNCULL_FLAGS_2 = {0, 4, 4, 8, 0, 0, 8, 0, 0};
	public static final int[] WALL_UNCULL_FLAGS_3 = {1, 1, 0, 0, 0, 8, 0, 0, 8};

	public static final int[] TEXTURE_HSL = {
			41, 39248, 41, 4643, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
			43086, 41, 41, 41, 41, 41, 41, 41, 8602, 41, 28992, 41, 41, 41, 41,
			41, 5056, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
			3131, 41, 41, 41
	};
	// @formatter:on

	public int[] vertexAMergeIndex = new int[10000];
	public int[] vertexBMergeIndex = new int[10000];
	public int normalMergeIndex;

	/* @formatter:off */
	public static final int[][] TILE_MASK_2D = {new int[16], {
			1, 1, 1, 1,
			1, 1, 1, 1,
			1, 1, 1, 1,
			1, 1, 1, 1
	}, {
			1, 0, 0, 0,
			1, 1, 0, 0,
			1, 1, 1, 0,
			1, 1, 1, 1
	}, {
			1, 1, 0, 0,
			1, 1, 0, 0,
			1, 0, 0, 0,
			1, 0, 0, 0
	}, {
			0, 0, 1, 1,
			0, 0, 1, 1,
			0, 0, 0, 1,
			0, 0, 0, 1
	}, {
			0, 1, 1, 1,
			0, 1, 1, 1,
			1, 1, 1, 1,
			1, 1, 1, 1
	}, {
			1, 1, 1, 0,
			1, 1, 1, 0,
			1, 1, 1, 1,
			1, 1, 1, 1
	}, {
			1, 1, 0, 0,
			1, 1, 0, 0,
			1, 1, 0, 0,
			1, 1, 0, 0
	}, {
			0, 0, 0, 0,
			0, 0, 0, 0,
			1, 0, 0, 0,
			1, 1, 0, 0
	}, {
			1, 1, 1, 1,
			1, 1, 1, 1,
			0, 1, 1, 1,
			0, 0, 1, 1
	}, {
			1, 1, 1, 1,
			1, 1, 0, 0,
			1, 0, 0, 0,
			1, 0, 0, 0
	}, {
			0, 0, 0, 0,
			0, 0, 1, 1,
			0, 1, 1, 1,
			0, 1, 1, 1
	}, {
			0, 0, 0, 0,
			0, 0, 0, 0,
			0, 1, 1, 0,
			1, 1, 1, 1
	}};

	public static final int[][] TILE_ROTATION_2D = {{
			0, 1, 2, 3,
			4, 5, 6, 7,
			8, 9, 10, 11,
			12, 13, 14, 15
	}, {
			12, 8, 4, 0,
			13, 9, 5, 1,
			14, 10, 6, 2,
			15, 11, 7, 3
	}, {
			15, 14, 13, 12,
			11, 10, 9, 8,
			7, 6, 5, 4,
			3, 2, 1, 0
	}, {
			3, 7, 11, 15,
			2, 6, 10, 14,
			1, 5, 9, 13,
			0, 4, 8, 12
	}};
	/* @formatter:on */

	public static boolean[][][][] visibilityMaps = new boolean[8][32][SceneBuilder.VIEW_DIAMETER + 1][SceneBuilder.VIEW_DIAMETER + 1];
	public static boolean[][] visibilityMap;
	public static int viewportCenterX;
	public static int viewportCenterY;
	public static int viewportLeft;
	public static int viewportTop;
	public static int viewportRight;
	public static int viewportBottom;

	public Scene(int tileCountX, int tileCountZ, int maxLevel, int[][][] heightmap) {
		this.maxLevel = maxLevel;
		this.tileCountX = tileCountX;
		this.tileCountZ = tileCountZ;
		levelTiles = new Tile[maxLevel][tileCountX][tileCountZ];
		levelTileCycles = new int[maxLevel][tileCountX + 1][tileCountZ + 1];
		this.heightmap = heightmap;
		reset();
	}

	public static void unload() {
		locBuffer = null;
		levelOccluderCount = null;
		levelOccluders = null;
		tileQueue = null;
		visibilityMaps = null;
		visibilityMap = null;
	}

	public void reset() {
		for (int level = 0; level < maxLevel; level++) {
			for (int x = 0; x < tileCountX; x++) {
				for (int z = 0; z < tileCountZ; z++) {
					levelTiles[level][x][z] = null;
				}
			}
		}

		for (int p = 0; p < MAX_OCCLUDER_LEVELS; p++) {
			for (int n = 0; n < levelOccluderCount[p]; n++) {
				levelOccluders[p][n] = null;
			}
			levelOccluderCount[p] = 0;
		}

		for (int i = 0; i < locCount; i++) {
			locs[i] = null;
		}

		locCount = 0;

		for (int i = 0; i < locBuffer.length; i++) {
			locBuffer[i] = null;
		}
	}

	public void setup(int level) {
		this.minLevel = level;
		for (int x = 0; x < tileCountX; x++) {
			for (int z = 0; z < tileCountZ; z++) {
				levelTiles[level][x][z] = new Tile(level, x, z);
			}
		}
	}

	public void setBridge(int x, int z) {
		Tile t = levelTiles[0][x][z];
		for (int level = 0; level < 3; level++) {
			levelTiles[level][x][z] = levelTiles[level + 1][x][z];
			if (levelTiles[level][x][z] != null) {
				levelTiles[level][x][z].level--;
			}
		}
		if (levelTiles[0][x][z] == null) {
			levelTiles[0][x][z] = new Tile(0, x, z);
		}
		levelTiles[0][x][z].bridge = t;
		levelTiles[3][x][z] = null;
	}

	public static void addOcclude(int type, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int level) {
		Occluder o = new Occluder();
		o.minTileX = minX / 128;
		o.maxTileX = maxX / 128;
		o.minTileZ = minZ / 128;
		o.maxTileZ = maxZ / 128;
		o.type = type;
		o.minX = minX;
		o.maxX = maxX;
		o.minZ = minZ;
		o.maxZ = maxZ;
		o.minY = minY;
		o.maxY = maxY;
		levelOccluders[level][levelOccluderCount[level]++] = o;
	}

	public void setPhysicalLevel(int level, int tileX, int tileZ, int physicalLevel) {
		Tile t = levelTiles[level][tileX][tileZ];
		if (t != null) {
			levelTiles[level][tileX][tileZ].physicalLevel = physicalLevel;
		}
	}

	public void addTile(int level, int tileX, int tileZ, int type, int rotation, int textureIndex, int southwestY, int southeastY, int northeastY, int northwestY, int southwestColor1, int southeastColor1, int northeastColor1, int northwestColor1, int southwestColor2, int southeastColor2, int northeastColor2, int northwestColor2, int rgb0, int rgb1) {
		if (type == 0) {
			TileUnderlay t = new TileUnderlay(southwestColor1, southeastColor1, northeastColor1, northwestColor1, -1, rgb0, false);

			for (int l = level; l >= 0; l--) {
				if (levelTiles[l][tileX][tileZ] == null) {
					levelTiles[l][tileX][tileZ] = new Tile(l, tileX, tileZ);
				}
			}

			levelTiles[level][tileX][tileZ].underlay = t;
		} else if (type == 1) {
			TileUnderlay t = new TileUnderlay(southwestColor2, southeastColor2, northeastColor2, northwestColor2, textureIndex, rgb1, (southwestY == southeastY && southwestY == northeastY && southwestY == northwestY));

			for (int l = level; l >= 0; l--) {
				if (levelTiles[l][tileX][tileZ] == null) {
					levelTiles[l][tileX][tileZ] = new Tile(l, tileX, tileZ);
				}
			}

			levelTiles[level][tileX][tileZ].underlay = t;
		} else {
			TileOverlay t = new TileOverlay(tileX, tileZ, northwestY, northeastY, southwestY, textureIndex, southwestColor1, southeastColor2, rotation, northeastColor1, northeastColor2, southwestColor2, northwestColor1, southeastY, southeastColor1, type, northwestColor2, (byte) -119, rgb0, rgb1);

			for (int l = level; l >= 0; l--) {
				if (levelTiles[l][tileX][tileZ] == null) {
					levelTiles[l][tileX][tileZ] = new Tile(l, tileX, tileZ);
				}
			}

			levelTiles[level][tileX][tileZ].overlay = t;
		}
	}

	public void addGroundDecoration(Model m, int level, int tileX, int tileZ, int sceneY, byte info, int bitset) {
		if (m == null) {
			return;
		}

		GroundDecoration l = new GroundDecoration();
		l.model = m;
		l.sceneX = (tileX * 128) + 64;
		l.sceneZ = (tileZ * 128) + 64;
		l.sceneY = sceneY;
		l.bitset = bitset;
		l.info = info;

		if (levelTiles[level][tileX][tileZ] == null) {
			levelTiles[level][tileX][tileZ] = new Tile(level, tileX, tileZ);
		}

		levelTiles[level][tileX][tileZ].groundDecoration = l;
	}

	public void addObject(Model m, int level, int tileX, int tileZ, int sceneY, int bitset) {
		ObjEntity l = new ObjEntity();
		l.model = m;
		l.sceneX = tileX * 128 + 64;
		l.sceneZ = tileZ * 128 + 64;
		l.sceneY = sceneY;
		l.bitset = bitset;

		int maxY = 0;
		Tile t = levelTiles[level][tileX][tileZ];
		if (t != null) {
			for (int n = 0; n < t.locationCount; n++) {
				int y = t.locs[n].model.objectOffsetY;
				if (y > maxY) {
					maxY = y;
				}
			}
		}

		l.offsetY = maxY;

		if (levelTiles[level][tileX][tileZ] == null) {
			levelTiles[level][tileX][tileZ] = new Tile(level, tileX, tileZ);
		}

		levelTiles[level][tileX][tileZ].object = l;
	}

	public void addWall(Model m1, Model m2, int level, int tileX, int tileZ, int sceneY, int bitset, byte info, int type1, int type2) {
		if (m1 != null || m2 != null) {
			Wall w = new Wall();
			w.bitset = bitset;
			w.info = info;
			w.sceneX = tileX * 128 + 64;
			w.sceneZ = tileZ * 128 + 64;
			w.sceneY = sceneY;
			w.model1 = m1;
			w.model2 = m2;
			w.type1 = type1;
			w.type2 = type2;

			for (int l = level; l >= 0; l--) {
				if (levelTiles[l][tileX][tileZ] == null) {
					levelTiles[l][tileX][tileZ] = new Tile(l, tileX, tileZ);
				}
			}

			levelTiles[level][tileX][tileZ].wall = w;
		}
	}

	public void addWallDecoration(Model model, int tileX, int tileZ, int sceneY, int sizeX, int sizeY, int level, int bitset, byte flags, int type, int rotation) {
		if (model != null) {
			WallDecoration d = new WallDecoration();
			d.bitset = bitset;
			d.info = flags;
			d.sceneX = (tileX * 128) + 64 + sizeX;
			d.sceneZ = (tileZ * 128) + 64 + sizeY;
			d.sceneY = sceneY;
			d.model = model;
			d.type = type;
			d.rotation = rotation;

			for (int l = level; l >= 0; l--) {
				if (levelTiles[l][tileX][tileZ] == null) {
					levelTiles[l][tileX][tileZ] = new Tile(l, tileX, tileZ);
				}
			}

			levelTiles[level][tileX][tileZ].wallDecoration = d;
		}
	}

	public boolean addLocation(Model m, Entity r, int minTileX, int minTileZ, int tileSizeX, int tileSizeZ, int sceneY, int level, int yaw, int bitset, byte flags) {
		if (m == null && r == null) {
			return true;
		}
		int x = (minTileX * 128) + (tileSizeX * 64);
		int z = (minTileZ * 128) + (tileSizeZ * 64);
		return add(r, m, minTileX, minTileZ, tileSizeX, tileSizeZ, level, x, z, sceneY, yaw, bitset, flags, false);
	}

	public boolean add(Entity r, Model m, int sceneX, int sceneY, int sceneZ, int level, int yaw, int size, int bitset, int renderPadding) {
		if (m == null && r == null) {
			return true;
		}

		int minX = sceneX - size;
		int minZ = sceneZ - size;
		int maxX = sceneX + size;
		int maxZ = sceneZ + size;

		if (renderPadding > 0) {
			if (yaw > 768 && yaw < 1280) {
				maxZ += renderPadding;
			}

			if (yaw > 1280 && yaw < 1792) {
				maxX += renderPadding;
			}

			if (yaw > 1792 || yaw < 256) {
				minZ -= renderPadding;
			}

			if (yaw > 256 && yaw < 768) {
				maxX -= renderPadding;
			}
		}

		minX /= 128;
		minZ /= 128;
		maxX /= 128;
		maxZ /= 128;

		return add(r, m, minX, minZ, maxX - minX + 1, maxZ - minZ + 1, level, sceneX, sceneZ, sceneY, yaw, bitset, (byte) 0, true);
	}

	public boolean add(Entity r, Model m, int sceneX, int sceneY, int sceneZ, int minTileX, int minTileZ, int maxTileX, int maxTileZ, int tileZ, int yaw, int bitset) {
		if (m == null && r == null) {
			return true;
		}
		return add(r, m, minTileX, minTileZ, maxTileX - minTileX + 1, maxTileZ - minTileZ + 1, tileZ, sceneX, sceneZ, sceneY, yaw, bitset, (byte) 0, true);
	}

	public boolean add(Entity r, Model m, int tileX, int tileZ, int sizeX, int sizeZ, int level, int sceneX, int sceneZ, int sceneY, int yaw, int bitset, byte info, boolean temporary) {
		if (m == null && r == null) {
			return false;
		}

		for (int x = tileX; x < tileX + sizeX; x++) {
			for (int z = tileZ; z < tileZ + sizeZ; z++) {
				if (x < 0 || z < 0 || x >= tileCountX || z >= tileCountZ) {
					return false;
				}

				Tile t = levelTiles[level][x][z];

				if (t != null && t.locationCount >= 5) {
					return false;
				}
			}
		}

		Loc loc = new Loc();
		loc.bitset = bitset;
		loc.info = info;
		loc.tileLevel = level;
		loc.sceneX = sceneX;
		loc.sceneZ = sceneZ;
		loc.sceneY = sceneY;
		loc.model = m;
		loc.entity = r;
		loc.yaw = yaw;
		loc.minTileX = tileX;
		loc.minTileZ = tileZ;
		loc.maxTileX = tileX + sizeX - 1;
		loc.maxTileZ = tileZ + sizeZ - 1;

		for (int x = tileX; x < tileX + sizeX; x++) {
			for (int z = tileZ; z < tileZ + sizeZ; z++) {
				int flags = 0;

				if (x > tileX) {
					flags++;
				}

				if (x < tileX + sizeX - 1) {
					flags += 4;
				}

				if (z > tileZ) {
					flags += 8;
				}

				if (z < tileZ + sizeZ - 1) {
					flags += 2;
				}

				for (int l = level; l >= 0; l--) {
					if (levelTiles[l][x][z] == null) {
						levelTiles[l][x][z] = new Tile(l, x, z);
					}
				}

				Tile t = levelTiles[level][x][z];
				t.locs[t.locationCount] = loc;
				t.locFlags[t.locationCount] = flags;
				t.flags |= flags;
				t.locationCount++;
			}
		}

		if (temporary) {
			locs[locCount++] = loc;
		}
		return true;
	}

	public void clearFrameLocs() {
		for (int n = 0; n < locCount; n++) {
			removeLocation(locs[n]);
			locs[n] = null;
		}
		locCount = 0;
	}

	public  void removeLocation(Loc l) {
		for (int x = l.minTileX; x <= l.maxTileX; x++) {
			for (int z = l.minTileZ; z <= l.maxTileZ; z++) {
				Tile t = levelTiles[l.tileLevel][x][z];

				if (t != null) {
					for (int n = 0; n < t.locationCount; n++) {
						if (t.locs[n] == l) {
							t.locationCount--;

							// shift all locs on this tile down an index
							for (int m = n; m < t.locationCount; m++) {
								t.locs[m] = t.locs[m + 1];
								t.locFlags[m] = t.locFlags[m + 1];
							}

							// then remove the reference to l
							t.locs[t.locationCount] = null;
							break;
						}
					}

					// clear flags
					t.flags = 0;

					// re-evaluate flags
					for (int n = 0; n < t.locationCount; n++) {
						t.flags |= t.locFlags[n];
					}
				}
			}
		}
	}

	public void setLocModel(Model m, int x, int z, int level) {
		if (m != null) {
			Tile t = levelTiles[level][x][z];
			if (t != null) {
				for (int n = 0; n < t.locationCount; n++) {
					Loc l = t.locs[n];
					if ((l.bitset >> 29 & 0x3) == LocType.TYPE_NORMAL) {
						l.model = m;
						return;
					}
				}
			}
		}
	}

	public void setWallDecorationModel(Model m, int x, int z, int level) {
		if (m != null) {
			Tile t = levelTiles[level][x][z];
			if (t != null) {
				WallDecoration l = t.wallDecoration;
				if (l != null) {
					l.model = m;
				}
			}
		}
	}

	public void removeWall(int x, int z, int level) {
		Tile t = levelTiles[level][x][z];
		if (t != null) {
			t.wall = null;
		}
	}

	public void removeWallDecoration(int x, int z, int level) {
		Tile t = levelTiles[level][x][z];
		if (t != null) {
			t.wallDecoration = null;
		}
	}

	public void removeLocations(int x, int z, int level) {
		Tile t = levelTiles[level][x][z];
		if (t != null) {
			for (int n = 0; n < t.locationCount; n++) {
				Loc l = t.locs[n];
				if (l.bitset >> 29 == LocType.TYPE_NORMAL && l.minTileX == x && l.minTileZ == z) {
					removeLocation(l);
					return;
				}
			}
		}
	}

	public void removeGroundDecoration(int x, int z, int level) {
		Tile t = levelTiles[level][x][z];
		if (t != null) {
			t.groundDecoration = null;
		}
	}

	public void removeObject(int x, int z, int level) {
		Tile t = levelTiles[level][x][z];
		if (t != null) {
			t.object = null;
		}
	}

	public int getWallBitset(int x, int z, int level) {
		Tile t = levelTiles[level][x][z];
		if (t == null || t.wall == null) {
			return 0;
		}
		return t.wall.bitset;
	}

	public int getWallDecorationBitset(int x, int z, int level) {
		Tile t = levelTiles[level][x][z];
		if (t == null || t.wallDecoration == null) {
			return 0;
		}
		return t.wallDecoration.bitset;
	}

	public int getLocationBitset(int x, int z, int level) {
		Tile t = levelTiles[level][x][z];
		if (t == null) {
			return 0;
		}
		for (int n = 0; n < t.locationCount; n++) {
			Loc l = t.locs[n];
			if ((l.bitset >> 29 & 0x3) == LocType.TYPE_NORMAL && l.minTileX == x && l.minTileZ == z) {
				return l.bitset;
			}
		}
		return 0;
	}

	public int getGroundDecorationBitset(int x, int z, int y) {
		Tile t = levelTiles[y][x][z];
		if (t == null || t.groundDecoration == null) {
			return 0;
		}
		return t.groundDecoration.bitset;
	}

	public int getInfo(int x, int z, int y, int bitset) {
		Tile t = levelTiles[y][x][z];

		if (t == null) {
			return -1;
		}

		if (t.wall != null && t.wall.bitset == bitset) {
			return t.wall.info & 0xFF;
		}

		if (t.wallDecoration != null && t.wallDecoration.bitset == bitset) {
			return t.wallDecoration.info & 0xFF;
		}

		if (t.groundDecoration != null && t.groundDecoration.bitset == bitset) {
			return t.groundDecoration.info & 0xFF;
		}

		for (int n = 0; n < t.locationCount; n++) {
			if (t.locs[n].bitset == bitset) {
				return t.locs[n].info & 0xFF;
			}
		}
		return -1;
	}

	public void applyLighting(int lightX, int lightY, int lightZ, int lightness, int baseIntensity) {
		int length = (int) Math.sqrt((double) (lightX * lightX + lightY * lightY + lightZ * lightZ));
		int intensity = (baseIntensity * length) >> 8;

		for (int level = 0; level < maxLevel; level++) {
			for (int tileX = 0; tileX < tileCountX; tileX++) {
				for (int tileZ = 0; tileZ < tileCountZ; tileZ++) {
					Tile t = levelTiles[level][tileX][tileZ];

					if (t != null) {
						Wall w = t.wall;

						if (w != null && w.model1 != null && w.model1.vertexNormals != null) {
							mergeLocNormals(w.model1, tileX, tileZ, level, 1, 1);

							if (w.model2 != null && w.model2.vertexNormals != null) {
								mergeLocNormals(w.model2, tileX, tileZ, level, 1, 1);
								mergeNormals(w.model1, w.model2, 0, 0, 0, false);
								w.model2.calculateLighting(lightness, intensity, lightX, lightY, lightZ);
							}

							w.model1.calculateLighting(lightness, intensity, lightX, lightY, lightZ);
						}

						for (int n = 0; n < t.locationCount; n++) {
							Loc l = t.locs[n];

							if (l != null && l.model != null && l.model.vertexNormals != null) {
								mergeLocNormals(l.model, tileX, tileZ, level, (l.maxTileX - l.minTileX + 1), (l.maxTileZ - l.minTileZ + 1));
								l.model.calculateLighting(lightness, intensity, lightX, lightY, lightZ);
							}
						}

						GroundDecoration d = t.groundDecoration;

						if (d != null && d.model.vertexNormals != null) {
							mergeGroundDecorationNormals(d.model, tileX, tileZ, level);
							d.model.calculateLighting(lightness, intensity, lightX, lightY, lightZ);
						}
					}
				}
			}
		}
	}

	private void mergeGroundDecorationNormals(Model m, int x, int z, int y) {
		if (x < tileCountX) {
			Tile t = levelTiles[y][x + 1][z];

			if (t != null && t.groundDecoration != null && t.groundDecoration.model.vertexNormals != null) {
				mergeNormals(m, t.groundDecoration.model, 128, 0, 0, true);
			}
		}

		if (z < tileCountZ) {
			Tile t = levelTiles[y][x][z + 1];

			if (t != null && t.groundDecoration != null && t.groundDecoration.model.vertexNormals != null) {
				mergeNormals(m, t.groundDecoration.model, 0, 0, 128, true);
			}
		}

		if (x < tileCountX && z < tileCountZ) {
			Tile t = levelTiles[y][x + 1][z + 1];

			if (t != null && t.groundDecoration != null && t.groundDecoration.model.vertexNormals != null) {
				mergeNormals(m, t.groundDecoration.model, 128, 0, 128, true);
			}
		}

		if (x < tileCountX && z > 0) {
			Tile t = levelTiles[y][x + 1][z - 1];

			if (t != null && t.groundDecoration != null && t.groundDecoration.model.vertexNormals != null) {
				mergeNormals(m, t.groundDecoration.model, 128, 0, -128, true);
			}
		}
	}

	private void mergeLocNormals(Model m, int tileX, int tileZ, int level, int locTileSizeX, int locTileSizeZ) {
		boolean hideTriangles = true;

		int minTileX = tileX;
		int maxTileX = tileX + locTileSizeX;

		int minTileZ = tileZ - 1;
		int maxTileZ = tileZ + locTileSizeZ;

		int baseAverageY = (heightmap[level][tileX][tileZ] + heightmap[level][tileX + 1][tileZ] + heightmap[level][tileX][tileZ + 1] + heightmap[level][tileX + 1][tileZ + 1]) / 4;

		for (int l = level; l <= level + 1; l++) {
			if (l == maxLevel) {
				continue;
			}

			for (int x = minTileX; x <= maxTileX; x++) {
				if (x < 0 || x >= tileCountX) {
					continue;
				}

				for (int z = minTileZ; z <= maxTileZ; z++) {
					if (z < 0 || z >= tileCountZ) {
						continue;
					}

					if (!hideTriangles || x >= maxTileX || z >= maxTileZ || z < tileZ && x != tileX) {
						Tile t = levelTiles[l][x][z];

						if (t != null) {
							int averageY = ((heightmap[l][x][z] + heightmap[l][x + 1][z] + heightmap[l][x][z + 1] + heightmap[l][x + 1][z + 1]) / 4) - baseAverageY;

							Wall wall = t.wall;

							if (wall != null && wall.model1 != null && wall.model1.vertexNormals != null) {
								mergeNormals(m, wall.model1, ((x - tileX) * 128 + (1 - locTileSizeX) * 64), averageY, ((z - tileZ) * 128 + (1 - locTileSizeZ) * 64), hideTriangles);
							}

							if (wall != null && wall.model2 != null && wall.model2.vertexNormals != null) {
								mergeNormals(m, wall.model2, ((x - tileX) * 128 + (1 - locTileSizeX) * 64), averageY, ((z - tileZ) * 128 + (1 - locTileSizeZ) * 64), hideTriangles);
							}

							for (int n = 0; n < t.locationCount; n++) {
								Loc loc = t.locs[n];

								if (loc != null && loc.model != null && loc.model.vertexNormals != null) {
									int tileSizeX = (loc.maxTileX - loc.minTileX + 1);
									int tileSizeZ = (loc.maxTileZ - loc.minTileZ + 1);
									mergeNormals(m, loc.model, (((loc.minTileX - tileX) * 128) + (tileSizeX - locTileSizeX) * 64), averageY, (((loc.minTileZ - tileZ) * 128) + (tileSizeZ - locTileSizeZ) * 64), hideTriangles);
								}
							}
						}
					}
				}
			}

			minTileX--;
			hideTriangles = false;
		}
	}

	private void mergeNormals(Model a, Model b, int offsetX, int offsetY, int offsetZ, boolean hideTriangles) {
		this.normalMergeIndex++;
		int counter = 0;

		for (int vertexA = 0; vertexA < a.vertexCount; vertexA++) {
			VertexNormal vertexNormalA = a.vertexNormals[vertexA];
			VertexNormal unmodifiedVertexNormalA = a.unmodifiedVertexNormals[vertexA];

			if (unmodifiedVertexNormalA.magnitude == 0) {
				continue;
			}

			int vertexYA = a.vertexY[vertexA] - offsetY;

			if (vertexYA > b.minBoundY) {
				continue;
			}

			int vertexXA = a.vertexX[vertexA] - offsetX;

			if (vertexXA < b.minBoundX || vertexXA > b.maxBoundX) {
				continue;
			}

			int vertexZA = a.vertexZ[vertexA] - offsetZ;

			if (vertexZA < b.minBoundZ || vertexZA > b.maxBoundZ) {
				continue;
			}

			for (int vertexB = 0; vertexB < b.vertexCount; vertexB++) {

				VertexNormal vertexNormalB = b.vertexNormals[vertexB];
				VertexNormal unmodifiedVertexNormalB = b.unmodifiedVertexNormals[vertexB];

				if (vertexXA == b.vertexX[vertexB] && vertexZA == b.vertexZ[vertexB] && vertexYA == b.vertexY[vertexB] && unmodifiedVertexNormalB.magnitude != 0) {
					vertexNormalA.x += unmodifiedVertexNormalB.x;
					vertexNormalA.y += unmodifiedVertexNormalB.y;
					vertexNormalA.z += unmodifiedVertexNormalB.z;
					vertexNormalA.magnitude += unmodifiedVertexNormalB.magnitude;

					vertexNormalB.x += unmodifiedVertexNormalA.x;
					vertexNormalB.y += unmodifiedVertexNormalA.y;
					vertexNormalB.z += unmodifiedVertexNormalA.z;
					vertexNormalB.magnitude += unmodifiedVertexNormalA.magnitude;

					counter++;
					this.vertexAMergeIndex[vertexA] = this.normalMergeIndex;
					this.vertexBMergeIndex[vertexB] = this.normalMergeIndex;
				}
			}
		}

		if (counter >= 3 && hideTriangles) {
			for (int t = 0; t < a.triangleCount; t++) {
				if (this.vertexAMergeIndex[a.triangleVertexA[t]] == this.normalMergeIndex && this.vertexAMergeIndex[a.triangleVertexB[t]] == this.normalMergeIndex && this.vertexAMergeIndex[a.triangleVertexC[t]] == this.normalMergeIndex) {
					a.triangleInfo[t] = -1; // do not draw this triangle
				}
			}

			for (int t = 0; t < b.triangleCount; t++) {
				if (this.vertexBMergeIndex[b.triangleVertexA[t]] == this.normalMergeIndex && this.vertexBMergeIndex[b.triangleVertexB[t]] == this.normalMergeIndex && this.vertexBMergeIndex[b.triangleVertexC[t]] == this.normalMergeIndex) {
					b.triangleInfo[t] = -1;// do not draw this triangle
				}
			}
		}
	}

	public void drawMinimapTile(int[] dst, int dstOff, int dstStep, int level, int x, int z) {
		Tile t = levelTiles[level][x][z];

		if (t != null) {
			TileUnderlay underlay = t.underlay;

			if (underlay != null) {
				int rgb = underlay.rgb;
				if (rgb != 0) {
					for (int i = 0; i < 4; i++) {
						dst[dstOff] = rgb;
						dst[dstOff + 1] = rgb;
						dst[dstOff + 2] = rgb;
						dst[dstOff + 3] = rgb;
						dstOff += dstStep;
					}
				}
			} else {
				TileOverlay overlay = t.overlay;

				if (overlay != null) {
					int shape = overlay.shape;
					int rotation = overlay.rotation;
					int underlayRGB = overlay.underlayRGB;
					int overlayRGB = overlay.overlayRGB;
					int[] mask = TILE_MASK_2D[shape];
					int[] rotated = TILE_ROTATION_2D[rotation];
					int srcOff = 0;

					if (underlayRGB != 0) {
						for (int i = 0; i < 4; i++) {
							dst[dstOff] = (mask[rotated[srcOff++]] == 0 ? underlayRGB : overlayRGB);
							dst[dstOff + 1] = (mask[rotated[srcOff++]] == 0 ? underlayRGB : overlayRGB);
							dst[dstOff + 2] = (mask[rotated[srcOff++]] == 0 ? underlayRGB : overlayRGB);
							dst[dstOff + 3] = (mask[rotated[srcOff++]] == 0 ? underlayRGB : overlayRGB);
							dstOff += dstStep;
						}
					} else {
						for (int n = 0; n < 4; n++) {
							if (mask[rotated[srcOff++]] != 0) {
								dst[dstOff] = overlayRGB;
							}
							if (mask[rotated[srcOff++]] != 0) {
								dst[dstOff + 1] = overlayRGB;
							}
							if (mask[rotated[srcOff++]] != 0) {
								dst[dstOff + 2] = overlayRGB;
							}
							if (mask[rotated[srcOff++]] != 0) {
								dst[dstOff + 3] = overlayRGB;
							}
							dstOff += dstStep;
						}
					}
				}
			}
		}
	}

	public static void init(int width, int height, int minZ, int maxZ) {
		viewportLeft = 0;
		viewportTop = 0;
		viewportRight = width;
		viewportBottom = height;
		viewportCenterX = width / 2;
		viewportCenterY = height / 2;

		int[] pitchZ = new int[9];

		for (int n = 0; n < 9; n++) {
			int angle = (n * 32) + 128 + 15;
			int zoom = (angle * 3) + 600;
			pitchZ[n] = (zoom * Draw3D.sin[angle]) >> 16;
		}

		// some padding?
		final int diameter = SceneBuilder.VIEW_DIAMETER + 3;
		final int radius = SceneBuilder.VIEW_DIAMETER / 2;

		boolean[][][][] visibilityMap = new boolean[9][32][diameter][diameter];

		for (int pitch = 128; pitch <= 384; pitch += 32) {
			for (int yaw = 0; yaw < 2048; yaw += 64) {
				pitchSin = Model.sin[pitch];
				pitchCos = Model.cos[pitch];
				yawSin = Model.sin[yaw];
				yawCos = Model.cos[yaw];

				int pitchIndex = (pitch - 128) / 32;
				int yawIndex = yaw / 64;

				for (int x = -radius; x <= radius; x++) {
					for (int y = -radius; y <= radius; y++) {
						int sceneX = x * 128;
						int sceneY = y * 128;
						boolean visible = false;

						for (int sceneZ = -minZ; sceneZ <= maxZ; sceneZ += 128) {
							if (isPointVisible(sceneX, sceneY, pitchZ[pitchIndex] + sceneZ)) {
								visible = true;
								break;
							}
						}

						visibilityMap[pitchIndex][yawIndex][x + SceneBuilder.VIEW_RADIUS + 1][y + SceneBuilder.VIEW_RADIUS + 1] = visible;
					}
				}
			}
		}

		for (int pitch = 0; pitch < 8; pitch++) {
			for (int yaw = 0; yaw < 32; yaw++) {
				for (int x = -SceneBuilder.VIEW_RADIUS; x < SceneBuilder.VIEW_RADIUS; x++) {
					for (int y = -SceneBuilder.VIEW_RADIUS; y < SceneBuilder.VIEW_RADIUS; y++) {
						boolean visible = false;

						LOOP:
						{
							for (int dx = -1; dx <= 1; dx++) {
								for (int dy = -1; dy <= 1; dy++) {
									if (visibilityMap[pitch][yaw][x + dx + SceneBuilder.VIEW_RADIUS + 1][y + dy + SceneBuilder.VIEW_RADIUS + 1]) {
										visible = true;
										break LOOP;
									}

									if (visibilityMap[pitch][(yaw + 1) % 31][x + dx + SceneBuilder.VIEW_RADIUS + 1][y + dy + SceneBuilder.VIEW_RADIUS + 1]) {
										visible = true;
										break LOOP;
									}

									if (visibilityMap[pitch + 1][yaw][x + dx + SceneBuilder.VIEW_RADIUS + 1][y + dy + SceneBuilder.VIEW_RADIUS + 1]) {
										visible = true;
										break LOOP;
									}

									if (visibilityMap[pitch + 1][(yaw + 1) % 31][x + dx + SceneBuilder.VIEW_RADIUS + 1][y + dy + SceneBuilder.VIEW_RADIUS + 1]) {
										visible = true;
										break LOOP;
									}
								}
							}
						}

						Scene.visibilityMaps[pitch][yaw][x + SceneBuilder.VIEW_RADIUS][y + SceneBuilder.VIEW_RADIUS] = visible;
					}
				}
			}
		}
	}

	public static boolean isPointVisible(int sceneX, int sceneY, int sceneZ) {
		int x = sceneY * yawSin + sceneX * yawCos >> 16;
		int w = sceneY * yawCos - sceneX * yawSin >> 16;
		int z = sceneZ * pitchSin + w * pitchCos >> 16;
		int y = sceneZ * pitchCos - w * pitchSin >> 16;

		if (z < SceneBuilder.NEAR_Z || z > SceneBuilder.FAR_Z) {
			return false;
		}

		int screenX = viewportCenterX + (x * Draw3D.viewportWidth) / z;
		int screenY = viewportCenterY + (y * Draw3D.viewportWidth) / z;

		return !(screenX < viewportLeft || screenX > viewportRight || screenY < viewportTop || screenY > viewportBottom);
	}

	public void setClick(int clickX, int clickY) {
		SceneBuilder.checkClick = true;
		SceneBuilder.clickX = clickX;
		SceneBuilder.clickY = clickY;
		SceneBuilder.clickedTileX = -1;
		SceneBuilder.clickedTileZ = -1;
	}

	public void draw(int cameraX, int cameraY, int cameraZ, int pitch, int yaw, int topLevel) {
		if (cameraX < 0) {
			cameraX = 0;
		} else if (cameraX >= tileCountX * 128) {
			cameraX = tileCountX * 128 - 1;
		}

		if (cameraZ < 0) {
			cameraZ = 0;
		} else if (cameraZ >= tileCountZ * 128) {
			cameraZ = tileCountZ * 128 - 1;
		}

		cycle++;
		Scene.pitchSin = Model.sin[pitch];
		Scene.pitchCos = Model.cos[pitch];
		Scene.yawSin = Model.sin[yaw];
		Scene.yawCos = Model.cos[yaw];
		Scene.visibilityMap = visibilityMaps[(pitch - 128) / 32][yaw / 64];
		Scene.cameraX = cameraX;
		Scene.cameraY = cameraY;
		Scene.cameraZ = cameraZ;
		Scene.cameraTileX = cameraX / 128;
		Scene.cameraTileZ = cameraZ / 128;
		Scene.activeLevel = topLevel;

		minTileX = cameraTileX - SceneBuilder.VIEW_RADIUS;
		maxTileX = cameraTileX + SceneBuilder.VIEW_RADIUS;

		minTileZ = cameraTileZ - SceneBuilder.VIEW_RADIUS;
		maxTileZ = cameraTileZ + SceneBuilder.VIEW_RADIUS;

		if (minTileX < 0) {
			minTileX = 0;
		}

		if (minTileZ < 0) {
			minTileZ = 0;
		}

		if (maxTileX > tileCountX) {
			maxTileX = tileCountX;
		}

		if (maxTileZ > tileCountZ) {
			maxTileZ = tileCountZ;
		}

		updateOccluders();

		tileUpdateCount = 0;

		for (int level = minLevel; level < this.maxLevel; level++) {
			Tile[][] tiles = levelTiles[level];

			for (int x = minTileX; x < maxTileX; x++) {
				for (int z = minTileZ; z < maxTileZ; z++) {
					Tile t = tiles[x][z];

					if (t != null) {
						if (t.physicalLevel > topLevel || !visibilityMap[x - cameraTileX + SceneBuilder.VIEW_RADIUS][z - cameraTileZ + SceneBuilder.VIEW_RADIUS] && heightmap[level][x][z] - cameraY < 2000) {
							t.draw = false;
							t.isVisible = false;
							t.wallCullDirection = 0;
						} else {
							t.draw = true;
							t.isVisible = true;
							t.drawLocations = t.locationCount > 0;
							tileUpdateCount++;
						}
					}
				}
			}
		}

		lastTileUpdateCount = tileUpdateCount;

		for (int level = minLevel; level < this.maxLevel; level++) {
			Tile[][] tiles = levelTiles[level];

			for (int x = -SceneBuilder.VIEW_RADIUS; x <= 0; x++) {
				int x0 = cameraTileX + x;
				int x1 = cameraTileX - x;

				if (x0 >= minTileX || x1 < maxTileX) {
					for (int z = -SceneBuilder.VIEW_RADIUS; z <= 0; z++) {
						int z0 = cameraTileZ + z;
						int z1 = cameraTileZ - z;

						if (x0 >= minTileX) {
							if (z0 >= minTileZ) {
								Tile t = tiles[x0][z0];

								if (t != null && t.draw) {
									draw(t, true);
								}
							}

							if (z1 < maxTileZ) {
								Tile t = tiles[x0][z1];

								if (t != null && t.draw) {
									draw(t, true);
								}
							}
						}

						if (x1 < maxTileX) {
							if (z0 >= minTileZ) {
								Tile t = tiles[x1][z0];

								if (t != null && t.draw) {
									draw(t, true);
								}
							}

							if (z1 < maxTileZ) {
								Tile t = tiles[x1][z1];

								if (t != null && t.draw) {
									draw(t, true);
								}
							}
						}

						if (tileUpdateCount == 0) {
							SceneBuilder.checkClick = false;
							return;
						}
					}
				}
			}
		}

		for (int level = minLevel; level < this.maxLevel; level++) {
			Tile[][] tiles = levelTiles[level];

			for (int x = -SceneBuilder.VIEW_RADIUS; x <= 0; x++) {
				int x0 = cameraTileX + x;
				int x1 = cameraTileX - x;

				if (x0 >= minTileX || x1 < maxTileX) {
					for (int z = -SceneBuilder.VIEW_RADIUS; z <= 0; z++) {
						int z0 = cameraTileZ + z;
						int z1 = cameraTileZ - z;

						if (x0 >= minTileX) {
							if (z0 >= minTileZ) {
								Tile t = tiles[x0][z0];
								if (t != null && t.draw) {
									draw(t, false);
								}
							}
							if (z1 < maxTileZ) {
								Tile t = tiles[x0][z1];
								if (t != null && t.draw) {
									draw(t, false);
								}
							}
						}

						if (x1 < maxTileX) {
							if (z0 >= minTileZ) {
								Tile t = tiles[x1][z0];
								if (t != null && t.draw) {
									draw(t, false);
								}
							}
							if (z1 < maxTileZ) {
								Tile t = tiles[x1][z1];
								if (t != null && t.draw) {
									draw(t, false);
								}
							}
						}

						if (tileUpdateCount == 0) {
							SceneBuilder.checkClick = false;
							return;
						}
					}
				}
			}
		}
	}

	public void draw(Tile t, boolean bool) {
		tileQueue.push(t);

		for (; ; ) {
			t = (Tile) tileQueue.poll();

			if (t == null) {
				break;
			}

			if (!t.isVisible) {
				continue;
			}

			int x = t.x;
			int z = t.z;
			int level = t.level;
			int renderLevel = t.renderLevel;
			Tile[][] tiles = levelTiles[level];

			if (t.draw) {
				if (bool) {
					if (level > 0) {
						Tile t0 = levelTiles[level - 1][x][z];

						if (t0 != null && t0.isVisible) {
							continue;
						}
					}

					if (x <= cameraTileX && x > minTileX) {
						Tile t0 = tiles[x - 1][z];

						if (t0 != null && t0.isVisible && (t0.draw || ((t.flags & 0x1) == 0))) {
							continue;
						}
					}

					if (x >= cameraTileX && x < maxTileX - 1) {
						Tile t0 = tiles[x + 1][z];

						if (t0 != null && t0.isVisible && (t0.draw || ((t.flags & 0x4) == 0))) {
							continue;
						}
					}

					if (z <= cameraTileZ && z > minTileZ) {
						Tile t0 = tiles[x][z - 1];

						if (t0 != null && t0.isVisible && (t0.draw || ((t.flags & 0x8) == 0))) {
							continue;
						}
					}

					if (z >= cameraTileZ && z < maxTileZ - 1) {
						Tile t0 = tiles[x][z + 1];

						if (t0 != null && t0.isVisible && (t0.draw || ((t.flags & 0x2) == 0))) {
							continue;
						}
					}
				} else {
					bool = true;
				}

				t.draw = false;

				if (t.bridge != null) {
					Tile b = t.bridge;

					if (b.underlay != null) {
						drawTileUnderlay(b.underlay, 0, x, z, pitchSin, pitchCos, yawSin, yawCos);
					} else if (b.overlay != null) {
						drawTileOverlay(b.overlay, 0, x, z, pitchSin, pitchCos, yawSin, yawCos);
					} else {
						drawTileUnderlay(null, 0, x, z, pitchSin, pitchCos, yawSin, yawCos);
					}

					Wall w = b.wall;

					if (w != null) {
						w.model1.draw(0, pitchSin, pitchCos, yawSin, yawCos, w.sceneX - cameraX, w.sceneY - cameraY, w.sceneZ - cameraZ, w.bitset);
					}

					for (int n = 0; n < b.locationCount; n++) {
						Loc l = b.locs[n];

						if (l != null) {
							Model m = l.model;

							if (m == null) {
								m = l.entity.getDrawModel();
							}

							m.draw(l.yaw, pitchSin, pitchCos, yawSin, yawCos, l.sceneX - cameraX, l.sceneY - cameraY, l.sceneZ - cameraZ, l.bitset);
						}
					}
				}

				boolean visible = false;

				if (t.underlay != null) {
					if (!isTileOccluded(renderLevel, x, z)) {
						visible = true;
						drawTileUnderlay(t.underlay, renderLevel, x, z, pitchSin, pitchCos, yawSin, yawCos);
					}
				} else if (t.overlay != null) {
					if (!isTileOccluded(renderLevel, x, z)) {
						visible = true;
						drawTileOverlay(t.overlay, 0, x, z, pitchSin, pitchCos, yawSin, yawCos);
					}
				} else {
					drawTileUnderlay(null, renderLevel, x, z, pitchSin, pitchCos, yawSin, yawCos);
				}

				int direction = 0;
				int wallDrawFlags = 0;
				Wall w = t.wall;
				WallDecoration decoration = t.wallDecoration;

				if (w != null || decoration != null) {
					if (cameraTileX == x) {
						direction += 1;
					} else if (cameraTileX < x) {
						direction += 2;
					}

					if (cameraTileZ == z) {
						direction += 3;
					} else if (cameraTileZ > z) {
						direction += 6;
					}

					wallDrawFlags = TILE_WALL_DRAW_FLAGS_0[direction];
					t.wallDrawFlags = TILE_WALL_DRAW_FLAGS_1[direction];

					// 0[0] = cameraTileX > tileX && cameraTileZ < tileZ
					// 0[1] = cameraTileX == tileX && cameraTileZ < tileZ
					// 2[2] = cameraTileX < tileX && cameraTileZ < tileZ
					// 0[3] = cameraTileX > tileX && cameratileZ == tileZ
					// 0[4] = cameraTileX == tileX && cameraTileZ == tileZ
					// 2[5] = cameraTileX < tileX && cameraTileZ == tileZ
					// 1[6] = cameraTileX > tileX && cameraTileZ > tileZ
					// 1[7] = cameraTileX == tileX && cameraTileZ > tileZ
					// 0[8] = cameraTileX < tileX && cameraTileZ > tileZ
				}

				if (w != null) {
					if ((w.type1 & WALL_DRAW_FLAGS[direction]) != 0) {
						// these are clearly all directional flags. But I need to figure out correct naming for them.
						//
						//
						// nibble = delta
						// 0001 = x > minX
						// 0010 = z < maxZ
						// 0100 = x < maxX
						// 1000 = z > minZ
						//
						// CAMERA_DELTA_FLAGS:
						//
						// 0[0] = cameraTileX > tileX && cameraTileZ < tileZ
						// 0[1] = cameraTileX == tileX && cameraTileZ < tileZ
						// 2[2] = cameraTileX < tileX && cameraTileZ < tileZ
						// 0[3] = cameraTileX > tileX && cameratileZ == tileZ
						// 0[4] = cameraTileX == tileX && cameraTileZ == tileZ
						// 2[5] = cameraTileX < tileX && cameraTileZ == tileZ
						// 1[6] = cameraTileX > tileX && cameraTileZ > tileZ
						// 1[7] = cameraTileX == tileX && cameraTileZ > tileZ
						// 0[8] = cameraTileX < tileX && cameraTileZ > tileZ
						//
						//

						if (w.type1 == 16) {
							// the direction the wall can cull
							t.wallCullDirection = 0b0011; // x > minX && z < maxZ

							// the directions the wall can't cull relative to the camera
							t.wallUncullDirection = WALL_UNCULL_FLAGS_0[direction];

							// the direction the wall is culling
							t.wallCullOppositeDirection = 0b0011 - t.wallUncullDirection;
						} else if (w.type1 == 32) {
							t.wallCullDirection = 0b0110; // x < maxX && z < maxZ
							t.wallUncullDirection = WALL_UNCULL_FLAGS_1[direction];
							t.wallCullOppositeDirection = 0b0110 - t.wallUncullDirection;
						} else if (w.type1 == 64) {
							t.wallCullDirection = 0b1100; // x < maxX && z > minZ
							t.wallUncullDirection = WALL_UNCULL_FLAGS_2[direction];
							t.wallCullOppositeDirection = 0b1100 - t.wallUncullDirection;
						} else {
							t.wallCullDirection = 0b1001; // x > minX && z > minZ
							t.wallUncullDirection = WALL_UNCULL_FLAGS_3[direction];
							t.wallCullOppositeDirection = 0b1001 - t.wallUncullDirection;
						}
					} else {
						t.wallCullDirection = 0b0000;
					}

					if ((w.type1 & wallDrawFlags) != 0 && !isWallOccluded(renderLevel, x, z, w.type1)) {
						w.model1.draw(
								0,
								pitchSin, pitchCos,
								yawSin, yawCos,
								w.sceneX - cameraX,
								w.sceneY - cameraY,
								w.sceneZ - cameraZ,
								w.bitset
						);
					}

					if ((w.type2 & wallDrawFlags) != 0 && !isWallOccluded(renderLevel, x, z, w.type2)) {
						w.model2.draw(
								0,
								pitchSin, pitchCos,
								yawSin, yawCos,
								w.sceneX - cameraX,
								w.sceneY - cameraY,
								w.sceneZ - cameraZ,
								w.bitset
						);
					}
				}

				if (decoration != null && !isOccluded(renderLevel, x, z, decoration.model.maxBoundY)) {
					if ((decoration.type & wallDrawFlags) != 0) {
						decoration.model.draw(decoration.rotation, pitchSin, pitchCos, yawSin, yawCos, decoration.sceneX - cameraX, decoration.sceneY - cameraY, decoration.sceneZ - cameraZ, decoration.bitset);
					} else if ((decoration.type & 0x300) != 0) {
						int relativeX = decoration.sceneX - cameraX;
						int relativeY = decoration.sceneY - cameraY;
						int relativeZ = decoration.sceneZ - cameraZ;
						int rotation = decoration.rotation;

						int rx;

						if (rotation == 1 || rotation == 2) {
							rx = -relativeX;
						} else {
							rx = relativeX;
						}

						int rz;

						if (rotation == 2 || rotation == 3) {
							rz = -relativeZ;
						} else {
							rz = relativeZ;
						}

						if ((decoration.type & 0x100) != 0 && rz < rx) {
							decoration.model.draw(
									(rotation * 512) + 256,
									pitchSin, pitchCos,
									yawSin, yawCos,
									relativeX + DECO_TYPE1_OFFSET_X[rotation],
									relativeY,
									relativeZ + DECO_TYPE1_OFFSET_Z[rotation],
									decoration.bitset
							);
						}

						if ((decoration.type & 0x200) != 0 && rz > rx) {
							decoration.model.draw(
									((rotation * 512) + 1280) & 0x7ff,
									pitchSin, pitchCos,
									yawSin, yawCos,
									relativeX + DECO_TYPE2_OFFSET_X[rotation],
									relativeY,
									relativeZ + DECO_TYPE2_OFFSET_Z[rotation],
									decoration.bitset
							);
						}
					}
				}

				if (visible) {
					GroundDecoration d = t.groundDecoration;

					if (d != null) {
						d.model.draw(
								0,
								pitchSin, pitchCos,
								yawSin, yawCos,
								d.sceneX - cameraX,
								d.sceneY - cameraY,
								d.sceneZ - cameraZ,
								d.bitset
						);
					}

					ObjEntity o = t.object;

					if (o != null && o.offsetY == 0) {
						o.model.draw(
								0,
								pitchSin, pitchCos,
								yawSin, yawCos,
								o.sceneX - cameraX,
								o.sceneY - cameraY,
								o.sceneZ - cameraZ,
								o.bitset
						);
					}
				}

				int flags = t.flags;

				if (flags != 0) {
					if (x < cameraTileX && (flags & 0x4) != 0) {
						Tile t0 = tiles[x + 1][z];

						if (t0 != null && t0.isVisible) {
							tileQueue.push(t0);
						}
					}

					if (z < cameraTileZ && (flags & 0x2) != 0) {
						Tile t0 = tiles[x][z + 1];

						if (t0 != null && t0.isVisible) {
							tileQueue.push(t0);
						}
					}

					if (x > cameraTileX && (flags & 0x1) != 0) {
						Tile t0 = tiles[x - 1][z];

						if (t0 != null && t0.isVisible) {
							tileQueue.push(t0);
						}
					}

					if (z > cameraTileZ && (flags & 0x8) != 0) {
						Tile t0 = tiles[x][z - 1];

						if (t0 != null && t0.isVisible) {
							tileQueue.push(t0);
						}
					}
				}
			}

			if (t.wallCullDirection != 0) {
				boolean visible = true;

				for (int n = 0; n < t.locationCount; n++) {
					if (t.locs[n].cycle != cycle && ((t.locFlags[n] & t.wallCullDirection) == t.wallUncullDirection)) {
						visible = false;
						break;
					}
				}

				if (visible) {
					Wall w = t.wall;

					if (!isWallOccluded(renderLevel, x, z, w.type1)) {
						w.model1.draw(
								0,
								pitchSin, pitchCos,
								yawSin, yawCos,
								w.sceneX - cameraX,
								w.sceneY - cameraY,
								w.sceneZ - cameraZ,
								w.bitset
						);
					}

					t.wallCullDirection = 0;
				}
			}

			if (t.drawLocations) {
				int locCount = t.locationCount;
				t.drawLocations = false;
				int locBufferSize = 0;

				LOOP:
				for (int n = 0; n < locCount; n++) {
					Loc l = t.locs[n];

					// this loc hasn't been drawn this cycle yet.
					if (l.cycle != cycle) {
						for (int x0 = l.minTileX; x0 <= l.maxTileX; x0++) {
							for (int z0 = l.minTileZ; z0 <= l.maxTileZ; z0++) {
								Tile t0 = tiles[x0][z0];

								// all tiles have to exist!
								if (t0 == null) {
									continue;
								}

								if (t0.draw) {
									t.drawLocations = true;
									continue LOOP;
								}

								if (t0.wallCullDirection != 0) {
									int flags = 0;

									if (x0 > l.minTileX) {
										flags += 0b0001;
									}

									if (x0 < l.maxTileX) {
										flags += 0b0100;
									}

									if (z0 > l.minTileZ) {
										flags += 0b1000;
									}

									if (z0 < l.maxTileZ) {
										flags += 0b0010;
									}

									// 0001 = x > minX
									// 0010 = z < maxZ
									// 0100 = x < maxX
									// 1000 = z > minZ
									if ((flags & t0.wallCullDirection) == t.wallCullOppositeDirection) {
										t.drawLocations = true;
										continue LOOP;
									}
								}
							}
						}

						locBuffer[locBufferSize++] = l;

						int dx0 = cameraTileX - l.minTileX;
						int dx1 = l.maxTileX - cameraTileX;

						if (dx1 > dx0) {
							dx0 = dx1;
						}

						int dz0 = cameraTileZ - l.minTileZ;
						int dz1 = l.maxTileZ - cameraTileZ;

						if (dz1 > dz0) {
							l.drawPriority = dx0 + dz1;
						} else {
							l.drawPriority = dx0 + dz0;
						}
					}
				}

				while (locBufferSize > 0) {
					int maxPriority = -50;
					int index = -1;

					for (int n = 0; n < locBufferSize; n++) {
						Loc l = locBuffer[n];

						if (l.drawPriority > maxPriority && l.cycle != cycle) {
							maxPriority = l.drawPriority;
							index = n;
						}
					}

					if (index == -1) {
						break;
					}

					Loc l = locBuffer[index];
					l.cycle = cycle;
					Model m = l.model;

					if (m == null) {
						m = l.entity.getDrawModel();
					}

					if (!isAreaOccluded(renderLevel, l.minTileX, l.maxTileX, l.minTileZ, l.maxTileZ, m.maxBoundY)) {
						m.draw(
								l.yaw,
								pitchSin, pitchCos,
								yawSin, yawCos,
								l.sceneX - cameraX,
								l.sceneY - cameraY,
								l.sceneZ - cameraZ,
								l.bitset
						);
					}

					for (int x0 = l.minTileX; x0 <= l.maxTileX; x0++) {
						for (int z0 = l.minTileZ; z0 <= l.maxTileZ; z0++) {
							Tile t0 = tiles[x0][z0];

							if (t0 == null) {
								continue;
							}

							if (t0.wallCullDirection != 0) {
								tileQueue.push(t0);
							} else if ((x0 != x || z0 != z) && t0.isVisible) {
								tileQueue.push(t0);
							}
						}
					}
				}

				if (t.drawLocations) {
					continue;
				}
			}

			if (t.isVisible && t.wallCullDirection == 0) {
				if (x <= cameraTileX && x > minTileX) {
					Tile t0 = tiles[x - 1][z];

					if (t0 != null && t0.isVisible) {
						continue;
					}
				}

				if (x >= cameraTileX && x < maxTileX - 1) {
					Tile t0 = tiles[x + 1][z];

					if (t0 != null && t0.isVisible) {
						continue;
					}
				}

				if (z <= cameraTileZ && z > minTileZ) {
					Tile t0 = tiles[x][z - 1];

					if (t0 != null && t0.isVisible) {
						continue;
					}
				}

				if (z >= cameraTileZ && z < maxTileZ - 1) {
					Tile t0 = tiles[x][z + 1];

					if (t0 != null && t0.isVisible) {
						continue;
					}
				}

				t.isVisible = false;
				tileUpdateCount--;

				ObjEntity o = t.object;

				if (o != null && o.offsetY != 0) {
					o.model.draw(
							0,
							pitchSin, pitchCos,
							yawSin, yawCos,
							o.sceneX - cameraX,
							o.sceneY - cameraY - o.offsetY,
							o.sceneZ - cameraZ,
							o.bitset
					);
				}

				if (t.wallDrawFlags != 0) {
					WallDecoration d = t.wallDecoration;

					if (d != null && !isOccluded(renderLevel, x, z, d.model.maxBoundY)) {
						if ((d.type & t.wallDrawFlags) != 0) {
							d.model.draw(
									d.rotation,
									pitchSin, pitchCos,
									yawSin, yawCos,
									d.sceneX - cameraX,
									d.sceneY - cameraY,
									d.sceneZ - cameraZ,
									d.bitset
							);
						} else if ((d.type & 0x300) != 0) {
							int relativeX = d.sceneX - cameraX;
							int relativeY = d.sceneY - cameraY;
							int relativeZ = d.sceneZ - cameraZ;
							int rotation = d.rotation;

							int rx; // postiive draw x
							int rz; // positive draw y

							if (rotation == 1 || rotation == 2) {
								rx = -relativeX;
							} else {
								rx = relativeX;
							}

							if (rotation == 2 || rotation == 3) {
								rz = -relativeZ;
							} else {
								rz = relativeZ;
							}

							if ((d.type & 0x100) != 0 && rz >= rx) {
								relativeX += DECO_TYPE1_OFFSET_X[rotation];
								relativeZ += DECO_TYPE1_OFFSET_Z[rotation];

								d.model.draw(
										(rotation * 512) + 256,
										pitchSin, pitchCos,
										yawSin, yawCos,
										relativeX,
										relativeY,
										relativeZ,
										d.bitset
								);
							}

							// relativeX/Z is still affected by += of previous type. keep in mind

							if ((d.type & 0x200) != 0 && rz <= rx) {
								relativeX += DECO_TYPE2_OFFSET_X[rotation];
								relativeZ += DECO_TYPE2_OFFSET_Z[rotation];

								d.model.draw(
										((rotation * 512) + 1280) & 0x7ff,
										pitchSin, pitchCos,
										yawSin, yawCos,
										relativeX,
										relativeY,
										relativeZ,
										d.bitset
								);
							}
						}
					}

					Wall w = t.wall;

					if (w != null) {
						if ((w.type2 & t.wallDrawFlags) != 0 && !isWallOccluded(renderLevel, x, z, w.type2)) {
							w.model2.draw(
									0,
									pitchSin, pitchCos,
									yawSin, yawCos,
									w.sceneX - cameraX,
									w.sceneY - cameraY,
									w.sceneZ - cameraZ,
									w.bitset
							);
						}

						if ((w.type1 & t.wallDrawFlags) != 0 && !isWallOccluded(renderLevel, x, z, w.type1)) {
							w.model1.draw(
									0,
									pitchSin, pitchCos,
									yawSin, yawCos,
									w.sceneX - cameraX,
									w.sceneY - cameraY,
									w.sceneZ - cameraZ,
									w.bitset
							);
						}
					}
				}

				if (level < maxLevel - 1) {
					Tile t0 = (levelTiles[level + 1][x][z]);

					if (t0 != null && t0.isVisible) {
						tileQueue.push(t0);
					}
				}

				if (x < cameraTileX) {
					Tile t0 = tiles[x + 1][z];

					if (t0 != null && t0.isVisible) {
						tileQueue.push(t0);
					}
				}

				if (z < cameraTileZ) {
					Tile t0 = tiles[x][z + 1];

					if (t0 != null && t0.isVisible) {
						tileQueue.push(t0);
					}
				}

				if (x > cameraTileX) {
					Tile t0 = tiles[x - 1][z];

					if (t0 != null && t0.isVisible) {
						tileQueue.push(t0);
					}
				}

				if (z > cameraTileZ) {
					Tile t0 = tiles[x][z - 1];

					if (t0 != null && t0.isVisible) {
						tileQueue.push(t0);
					}
				}
			}
		}
	}

	public void drawTileUnderlay(TileUnderlay u, int level, int tileX, int tileZ, int pitchSin, int pitchCos, int yawSin, int yawCos) {
		int rx3;
		int rx0 = rx3 = (tileX << 7) - cameraX;
		int rz1;
		int rz0 = rz1 = (tileZ << 7) - cameraZ;
		int rx2;
		int rx1 = rx2 = rx0 + 128;
		int rz3;
		int rz2 = rz3 = rz0 + 128;

		int ry0 = heightmap[level][tileX][tileZ] - cameraY;
		int ry1 = heightmap[level][tileX + 1][tileZ] - cameraY;
		int ry2 = heightmap[level][tileX + 1][tileZ + 1] - cameraY;
		int ry3 = heightmap[level][tileX][tileZ + 1] - cameraY;

		int w = rz0 * yawSin + rx0 * yawCos >> 16;
		rz0 = rz0 * yawCos - rx0 * yawSin >> 16;
		rx0 = w;

		w = ry0 * pitchCos - rz0 * pitchSin >> 16;
		rz0 = ry0 * pitchSin + rz0 * pitchCos >> 16;
		ry0 = w;

		if (rz0 < 50) {
			return;
		}

		w = rz1 * yawSin + rx1 * yawCos >> 16;
		rz1 = rz1 * yawCos - rx1 * yawSin >> 16;
		rx1 = w;

		w = ry1 * pitchCos - rz1 * pitchSin >> 16;
		rz1 = ry1 * pitchSin + rz1 * pitchCos >> 16;
		ry1 = w;

		if (rz1 < 50) {
			return;
		}

		w = rz2 * yawSin + rx2 * yawCos >> 16;
		rz2 = rz2 * yawCos - rx2 * yawSin >> 16;
		rx2 = w;

		w = ry2 * pitchCos - rz2 * pitchSin >> 16;
		rz2 = ry2 * pitchSin + rz2 * pitchCos >> 16;
		ry2 = w;

		if (rz2 < 50) {
			return;
		}

		w = rz3 * yawSin + rx3 * yawCos >> 16;
		rz3 = rz3 * yawCos - rx3 * yawSin >> 16;
		rx3 = w;

		w = ry3 * pitchCos - rz3 * pitchSin >> 16;
		rz3 = ry3 * pitchSin + rz3 * pitchCos >> 16;
		ry3 = w;

		if (rz3 < 50) {
			return;
		}

		int x0 = (Draw3D.centerX + (rx0 * Draw3D.viewportWidth) / rz0);
		int y0 = (Draw3D.centerY + (ry0 * Draw3D.viewportWidth) / rz0);
		int x1 = (Draw3D.centerX + (rx1 * Draw3D.viewportWidth) / rz1);
		int y1 = (Draw3D.centerY + (ry1 * Draw3D.viewportWidth) / rz1);
		int x2 = (Draw3D.centerX + (rx2 * Draw3D.viewportWidth) / rz2);
		int y2 = (Draw3D.centerY + (ry2 * Draw3D.viewportWidth) / rz2);
		int x3 = (Draw3D.centerX + (rx3 * Draw3D.viewportWidth) / rz3);
		int y3 = (Draw3D.centerY + (ry3 * Draw3D.viewportWidth) / rz3);

		Draw3D.alpha = 0;

		if (((x2 - x3) * (y1 - y3) - (y2 - y3) * (x1 - x3)) > 0) {
			Draw3D.testX = false;

			if (x2 < 0 || x3 < 0 || x1 < 0 || x2 > Draw2D.rightX || x3 > Draw2D.rightX || x1 > Draw2D.rightX) {
				Draw3D.testX = true;
			}

			if (SceneBuilder.checkClick) {
				if (withinTriangle(SceneBuilder.mouseX, SceneBuilder.mouseY, y2, y3, y1, x2, x3, x1)) {
					SceneBuilder.clickedTileX = tileX;
					SceneBuilder.clickedTileZ = tileZ;
				}
			}

			if (u != null) {
				if (u.textureIndex == -1) {
					if (u.northeastColor != 12345678) {
						Draw3D.fillShadedTriangle(x2, y2, u.northeastColor, x3, y3, u.northwestColor, x1, y1, u.southeastColor);
					}
				} else if (!lowmemory) {
					if (u.isFlat) {
						Draw3D.fillTexturedTriangle(y2, y3, y1, x2, x3, x1, u.northeastColor, u.northwestColor, u.southeastColor, rx0, rx1, rx3, ry0, ry1, ry3, rz0, rz1, rz3, u.textureIndex);
					} else {
						Draw3D.fillTexturedTriangle(y2, y3, y1, x2, x3, x1, u.northeastColor, u.northwestColor, u.southeastColor, rx2, rx3, rx1, ry2, ry3, ry1, rz2, rz3, rz1, u.textureIndex);
					}
				} else {
					int hsl = TEXTURE_HSL[u.textureIndex];
					Draw3D.fillShadedTriangle(x2, y2, adjustHSLLightness(hsl, u.northeastColor), x3, y3, adjustHSLLightness(hsl, u.northwestColor), x1, y1, adjustHSLLightness(hsl, u.southeastColor));
				}
			}
		}

		if (((x0 - x1) * (y3 - y1) - (y0 - y1) * (x3 - x1)) > 0) {
			Draw3D.testX = false;

			if (x0 < 0 || x1 < 0 || x3 < 0 || x0 > Draw2D.rightX || x1 > Draw2D.rightX || x3 > Draw2D.rightX) {
				Draw3D.testX = true;
			}

			if (SceneBuilder.checkClick) {
				if (withinTriangle(SceneBuilder.mouseX, SceneBuilder.mouseY, y0, y1, y3, x0, x1, x3)) {
					SceneBuilder.clickedTileX = tileX;
					SceneBuilder.clickedTileZ = tileZ;
				}
			}

			if (u != null) {
				if (u.textureIndex == -1) {
					if (u.southwestColor != 12345678) {
						Draw3D.fillShadedTriangle(x0, y0, u.southwestColor, x1, y1, u.southeastColor, x3, y3, u.northwestColor);
					}
				} else if (!lowmemory) {
					Draw3D.fillTexturedTriangle(y0, y1, y3, x0, x1, x3, u.southwestColor, u.southeastColor, u.northwestColor, rx0, rx1, rx3, ry0, ry1, ry3, rz0, rz1, rz3, u.textureIndex);
				} else {
					int hsl = TEXTURE_HSL[u.textureIndex];
					Draw3D.fillShadedTriangle(x0, y0, adjustHSLLightness(hsl, u.southwestColor), x1, y1, adjustHSLLightness(hsl, u.southeastColor), x3, y3, adjustHSLLightness(hsl, u.northwestColor));
				}
			}
		}
	}

	public void drawTileOverlay(TileOverlay o, int level, int tileX, int tileZ, int pitchSin, int pitchCos, int yawSin, int yawCos) {
		int count = o.vertexX.length;

		for (int v = 0; v < count; v++) {
			int sceneX = o.vertexX[v] - cameraX;
			int sceneY = o.vertexY[v] - cameraY;
			int sceneZ = o.vertexZ[v] - cameraZ;

			int w = sceneZ * yawSin + sceneX * yawCos >> 16;
			sceneZ = sceneZ * yawCos - sceneX * yawSin >> 16;
			sceneX = w;

			w = sceneY * pitchCos - sceneZ * pitchSin >> 16;
			sceneZ = sceneY * pitchSin + sceneZ * pitchCos >> 16;
			sceneY = w;

			if (sceneZ < SceneBuilder.NEAR_Z) {
				return;
			}

			if (o.triangleTextureIndex != null) {
				TileOverlay.vertexSceneX[v] = sceneX;
				TileOverlay.vertexSceneY[v] = sceneY;
				TileOverlay.vertexSceneZ[v] = sceneZ;
			}

			TileOverlay.tmpScreenX[v] = Draw3D.centerX + (sceneX * Draw3D.viewportWidth) / sceneZ;
			TileOverlay.tmpScreenY[v] = Draw3D.centerY + (sceneY * Draw3D.viewportWidth) / sceneZ;
		}

		Draw3D.alpha = 0;
		count = o.triangleVertexA.length;

		for (int t = 0; t < count; t++) {
			int a = o.triangleVertexA[t];
			int b = o.triangleVertexB[t];
			int c = o.triangleVertexC[t];

			int x0 = TileOverlay.tmpScreenX[a];
			int x1 = TileOverlay.tmpScreenX[b];
			int x2 = TileOverlay.tmpScreenX[c];

			int y0 = TileOverlay.tmpScreenY[a];
			int y1 = TileOverlay.tmpScreenY[b];
			int y2 = TileOverlay.tmpScreenY[c];

			if (((x0 - x1) * (y2 - y1) - (y0 - y1) * (x2 - x1)) > 0) {
				Draw3D.testX = false;

				if (x0 < 0 || x1 < 0 || x2 < 0 || x0 > Draw2D.rightX || x1 > Draw2D.rightX || x2 > Draw2D.rightX) {
					Draw3D.testX = true;
				}

				if (SceneBuilder.checkClick) {
					if (withinTriangle(SceneBuilder.mouseX, SceneBuilder.mouseY, y0, y1, y2, x0, x1, x2)) {
						SceneBuilder.clickedTileX = tileX;
						SceneBuilder.clickedTileZ = tileZ;
					}
				}

				if (o.triangleTextureIndex == null || o.triangleTextureIndex[t] == -1) {
					if (o.triangleColorA[t] != 12345678) {
						Draw3D.fillShadedTriangle(x0, y0, o.triangleColorA[t], x1, y1, o.triangleColorB[t], x2, y2, o.triangleColorC[t]);
					}
				} else if (!lowmemory) {
					if (o.isFlat) {
						Draw3D.fillTexturedTriangle(y0, y1, y2, x0, x1, x2, o.triangleColorA[t], o.triangleColorB[t], o.triangleColorC[t], TileOverlay.vertexSceneX[0], TileOverlay.vertexSceneX[1], TileOverlay.vertexSceneX[3], TileOverlay.vertexSceneY[0], TileOverlay.vertexSceneY[1], TileOverlay.vertexSceneY[3], TileOverlay.vertexSceneZ[0], TileOverlay.vertexSceneZ[1], TileOverlay.vertexSceneZ[3], o.triangleTextureIndex[t]);
					} else {
						Draw3D.fillTexturedTriangle(y0, y1, y2, x0, x1, x2, o.triangleColorA[t], o.triangleColorB[t], o.triangleColorC[t], TileOverlay.vertexSceneX[a], TileOverlay.vertexSceneX[b], TileOverlay.vertexSceneX[c], TileOverlay.vertexSceneY[a], TileOverlay.vertexSceneY[b], TileOverlay.vertexSceneY[c], TileOverlay.vertexSceneZ[a], TileOverlay.vertexSceneZ[b], TileOverlay.vertexSceneZ[c], o.triangleTextureIndex[t]);
					}
				} else {
					int hsl = TEXTURE_HSL[o.triangleTextureIndex[t]];
					Draw3D.fillShadedTriangle(x0, y0, adjustHSLLightness(hsl, o.triangleColorA[t]), x1, y1, adjustHSLLightness(hsl, o.triangleColorB[t]), x2, y2, adjustHSLLightness(hsl, o.triangleColorC[t]));
				}
			}
		}
	}

	public static final int adjustHSLLightness(int hsl, int lightness) {
		lightness = 127 - lightness;
		lightness = lightness * (hsl & 0x7f) / 160;
		if (lightness < 2) {
			lightness = 2;
		} else if (lightness > 126) {
			lightness = 126;
		}
		return (hsl & 0xff80) + lightness;
	}

	public static final boolean withinTriangle(int x, int y, int y0, int y1, int y2, int x0, int x1, int x2) {
		if (y < y0 && y < y1 && y < y2) {
			return false;
		}

		if (y > y0 && y > y1 && y > y2) {
			return false;
		}

		if (x < x0 && x < x1 && x < x2) {
			return false;
		}

		if (x > x0 && x > x1 && x > x2) {
			return false;
		}

		int a = ((y - y0) * (x1 - x0) - (x - x0) * (y1 - y0));
		int b = ((y - y2) * (x0 - x2) - (x - x2) * (y0 - y2));
		int c = ((y - y1) * (x2 - x1) - (x - x1) * (y2 - y1));

		return a * c > 0 && c * b > 0;
	}

	public void updateOccluders() {
		int count = levelOccluderCount[activeLevel];
		Occluder occluders[] = levelOccluders[activeLevel];

		activeOccluderCount = 0;

		for (int n = 0; n < count; n++) {
			Occluder o = occluders[n];

			if (o.type == 1) {
				int x = (o.minTileX - cameraTileX) + 25;

				if (x < 0 || x > 50) {
					continue;
				}

				int minZ = (o.minTileZ - cameraTileZ) + 25;

				if (minZ < 0) {
					minZ = 0;
				}

				int maxZ = (o.maxTileZ - cameraTileZ) + 25;

				if (maxZ > 50) {
					maxZ = 50;
				}

				boolean visible = false;

				while (minZ <= maxZ) {
					if (visibilityMap[x][minZ++]) {
						visible = true;
						break;
					}
				}

				if (!visible) {
					continue;
				}

				int dx = cameraX - o.minX;

				if (dx > 32) {
					o.testDirection = 1;
				} else {
					if (dx >= -32) {
						continue;
					}
					o.testDirection = 2;
					dx = -dx;
				}

				o.minNormalZ = ((o.minZ - cameraZ) << 8) / dx;
				o.maxNormalZ = ((o.maxZ - cameraZ) << 8) / dx;
				o.minNormalY = ((o.minY - cameraY) << 8) / dx;
				o.maxNormalY = ((o.maxY - cameraY) << 8) / dx;
				activeOccluders[activeOccluderCount++] = o;
				continue;
			}

			if (o.type == 2) {
				int z = (o.minTileZ - cameraTileZ) + 25;

				if (z < 0 || z > 50) {
					continue;
				}

				int minX = (o.minTileX - cameraTileX) + 25;

				if (minX < 0) {
					minX = 0;
				}

				int maxX = (o.maxTileX - cameraTileX) + 25;

				if (maxX > 50) {
					maxX = 50;
				}

				boolean visible = false;

				while (minX <= maxX) {
					if (visibilityMap[minX++][z]) {
						visible = true;
						break;
					}
				}

				if (!visible) {
					continue;
				}

				int dz = cameraZ - o.minZ;

				if (dz > 32) {
					o.testDirection = 3;
				} else {
					if (dz >= -32) {
						continue;
					}
					o.testDirection = 4;
					dz = -dz;
				}

				o.minNormalX = ((o.minX - cameraX) << 8) / dz;
				o.maxNormalX = ((o.maxX - cameraX) << 8) / dz;
				o.minNormalY = ((o.minY - cameraY) << 8) / dz;
				o.maxNormalY = ((o.maxY - cameraY) << 8) / dz;
				activeOccluders[activeOccluderCount++] = o;
			} else if (o.type == 4) {
				int y = o.minY - cameraY;

				if (y > 128) {
					int minZ = (o.minTileZ - cameraTileZ) + 25;

					if (minZ < 0) {
						minZ = 0;
					}

					int maxZ = (o.maxTileZ - cameraTileZ) + 25;

					if (maxZ > 50) {
						maxZ = 50;
					}

					if (minZ <= maxZ) {
						int minX = (o.minTileX - cameraTileX) + 25;

						if (minX < 0) {
							minX = 0;
						}

						int maxX = (o.maxTileX - cameraTileX) + 25;

						if (maxX > 50) {
							maxX = 50;
						}

						boolean visible = false;

						visibilityCheck:
						{
							for (int x = minX; x <= maxX; x++) {
								for (int z = minZ; z <= maxZ; z++) {
									if (!visibilityMap[x][z]) {
										continue;
									}
									visible = true;
									break visibilityCheck;
								}
							}
						}

						if (visible) {
							o.testDirection = 5;
							o.minNormalX = (o.minX - cameraX << 8) / y;
							o.maxNormalX = (o.maxX - cameraX << 8) / y;
							o.minNormalZ = (o.minZ - cameraZ << 8) / y;
							o.maxNormalZ = (o.maxZ - cameraZ << 8) / y;
							activeOccluders[activeOccluderCount++] = o;
						}
					}
				}
			}
		}
	}

	private boolean isTileOccluded(int level, int tileX, int tileZ) {
		int cycle = levelTileCycles[level][tileX][tileZ];

		if (cycle == -Scene.cycle) {
			return false;
		}

		if (cycle == Scene.cycle) {
			return true;
		}

		int sceneX = tileX << 7;
		int sceneZ = tileZ << 7;

		if (isOccluded(sceneX + 1, heightmap[level][tileX][tileZ], sceneZ + 1) && isOccluded(sceneX + 128 - 1, heightmap[level][tileX + 1][tileZ], sceneZ + 1) && isOccluded(sceneX + 128 - 1, heightmap[level][tileX + 1][tileZ + 1], sceneZ + 128 - 1) && isOccluded(sceneX + 1, heightmap[level][tileX][tileZ + 1], sceneZ + 128 - 1)) {
			levelTileCycles[level][tileX][tileZ] = Scene.cycle;
			return true;
		}

		levelTileCycles[level][tileX][tileZ] = -Scene.cycle;
		return false;
	}

	private boolean isWallOccluded(int level, int tileX, int tileZ, int type) {
		if (!isTileOccluded(level, tileX, tileZ)) {
			return false;
		}

		int x = tileX << 7;
		int z = tileZ << 7;
		int y = heightmap[level][tileX][tileZ] - 1;

		int ly0 = y - 120;
		int ly1 = y - 230;
		int ly2 = y - 238;

		if (type < 16) {
			if (type == 1) {
				if (x > cameraX) {
					if (!isOccluded(x, y, z)) {
						return false;
					}
					if (!isOccluded(x, y, z + 128)) {
						return false;
					}
				}

				if (level > 0) {
					if (!isOccluded(x, ly0, z)) {
						return false;
					}
					if (!isOccluded(x, ly0, z + 128)) {
						return false;
					}
				}

				if (!isOccluded(x, ly1, z)) {
					return false;
				}

				return isOccluded(x, ly1, z + 128);
			}

			if (type == 2) {
				if (z < cameraZ) {
					if (!isOccluded(x, y, z + 128)) {
						return false;
					}
					if (!isOccluded(x + 128, y, z + 128)) {
						return false;
					}
				}
				if (level > 0) {
					if (!isOccluded(x, ly0, z + 128)) {
						return false;
					}
					if (!isOccluded(x + 128, ly0, z + 128)) {
						return false;
					}
				}
				if (!isOccluded(x, ly1, z + 128)) {
					return false;
				}
				return isOccluded(x + 128, ly1, z + 128);
			}

			if (type == 4) {
				if (x < cameraZ) {
					if (!isOccluded(x + 128, y, z)) {
						return false;
					}
					if (!isOccluded(x + 128, y, z + 128)) {
						return false;
					}
				}

				if (level > 0) {
					if (!isOccluded(x + 128, ly0, z)) {
						return false;
					}
					if (!isOccluded(x + 128, ly0, z + 128)) {
						return false;
					}
				}

				if (!isOccluded(x + 128, ly1, z)) {
					return false;
				}

				return isOccluded(x + 128, ly1, z + 128);
			}

			if (type == 8) {
				if (z > cameraZ) {
					if (!isOccluded(x, y, z)) {
						return false;
					}
					if (!isOccluded(x + 128, y, z)) {
						return false;
					}
				}

				if (level > 0) {
					if (!isOccluded(x, ly0, z)) {
						return false;
					}
					if (!isOccluded(x + 128, ly0, z)) {
						return false;
					}
				}

				if (!isOccluded(x, ly1, z)) {
					return false;
				}

				return isOccluded(x + 128, ly1, z);
			}
		}

		if (!isOccluded(x + 64, ly2, z + 64)) {
			return false;
		}

		if (type == 16) {
			return isOccluded(x, ly1, z + 128);
		} else if (type == 32) {
			return isOccluded(x + 128, ly1, z + 128);
		} else if (type == 64) {
			return isOccluded(x + 128, ly1, z);
		} else if (type == 128) {
			return isOccluded(x, ly1, z);
		}

		System.out.println("Warning unsupported wall type");
		return true;
	}

	private boolean isOccluded(int level, int tileX, int tileZ, int height) {
		if (!isTileOccluded(level, tileX, tileZ)) {
			return false;
		}

		int sceneX = tileX << 7;
		int sceneZ = tileZ << 7;

		return isOccluded(sceneX + 1, heightmap[level][tileX][tileZ] - height, sceneZ + 1) && isOccluded(sceneX + 128 - 1, (heightmap[level][tileX + 1][tileZ] - height), sceneZ + 1) && isOccluded(sceneX + 128 - 1, (heightmap[level][tileX + 1][tileZ + 1] - height), sceneZ + 128 - 1) && isOccluded(sceneX + 1, (heightmap[level][tileX][tileZ + 1] - height), sceneZ + 128 - 1);
	}

	private boolean isAreaOccluded(int level, int minTileX, int maxTileX, int minTileZ, int maxTileZ, int height) {
		if (minTileX == maxTileX && minTileZ == maxTileZ) {
			if (!isTileOccluded(level, minTileX, minTileZ)) {
				return false;
			}

			int x = minTileX << 7;
			int z = minTileZ << 7;

			return isOccluded(x + 1, heightmap[level][minTileX][minTileZ] - height, z + 1) && isOccluded(x + 128 - 1, (heightmap[level][minTileX + 1][minTileZ] - height), z + 1) && isOccluded(x + 128 - 1, (heightmap[level][minTileX + 1][minTileZ + 1]) - height, z + 128 - 1) && isOccluded(x + 1, (heightmap[level][minTileX][minTileZ + 1] - height), z + 128 - 1);
		}

		for (int x = minTileX; x <= maxTileX; x++) {
			for (int z = minTileZ; z <= maxTileZ; z++) {
				if (levelTileCycles[level][x][z] == -cycle) {
					return false;
				}
			}
		}

		int minX = (minTileX << 7) + 1;
		int minZ = (minTileZ << 7) + 2;
		int minY = heightmap[level][minTileX][minTileZ] - height;

		if (!isOccluded(minX, minY, minZ)) {
			return false;
		}

		int maxX = (maxTileX << 7) - 1;

		if (!isOccluded(maxX, minY, minZ)) {
			return false;
		}

		int maxY = (maxTileZ << 7) - 1;

		if (!isOccluded(minX, minY, maxY)) {
			return false;
		}

		return isOccluded(maxX, minY, maxY);
	}

	public boolean isOccluded(int x, int y, int z) {
		for (int n = 0; n < activeOccluderCount; n++) {
			Occluder o = activeOccluders[n];

			if (o.testDirection == 1) {
				int dx = o.minX - x;

				if (dx > 0) {
					int minZ = o.minZ + ((o.minNormalZ * dx) >> 8);
					int maxZ = o.maxZ + ((o.maxNormalZ * dx) >> 8);
					int minY = o.minY + ((o.minNormalY * dx) >> 8);
					int maxY = o.maxY + ((o.maxNormalY * dx) >> 8);

					if (z >= minZ && z <= maxZ && y >= minY && y <= maxY) {
						return true;
					}
				}
			} else if (o.testDirection == 2) {
				int dx = x - o.minX;

				if (dx > 0) {
					int minZ = o.minZ + (o.minNormalZ * dx >> 8);
					int maxZ = o.maxZ + (o.maxNormalZ * dx >> 8);
					int minY = o.minY + (o.minNormalY * dx >> 8);
					int maxY = o.maxY + (o.maxNormalY * dx >> 8);

					if (z >= minZ && z <= maxZ && y >= minY && y <= maxY) {
						return true;
					}
				}
			} else if (o.testDirection == 3) {
				int dz = o.minZ - z;

				if (dz > 0) {
					int minX = o.minX + (o.minNormalX * dz >> 8);
					int maxX = o.maxX + (o.maxNormalX * dz >> 8);
					int minY = o.minY + (o.minNormalY * dz >> 8);
					int maxY = o.maxY + (o.maxNormalY * dz >> 8);

					if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
						return true;
					}
				}
			} else if (o.testDirection == 4) {
				int dz = z - o.minZ;

				if (dz > 0) {
					int minX = o.minX + (o.minNormalX * dz >> 8);
					int maxX = o.maxX + (o.maxNormalX * dz >> 8);
					int minY = o.minY + (o.minNormalY * dz >> 8);
					int maxY = o.maxY + (o.maxNormalY * dz >> 8);

					if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
						return true;
					}
				}
			} else if (o.testDirection == 5) {
				int dy = y - o.minY;

				if (dy > 0) {
					int minX = o.minX + (o.minNormalX * dy >> 8);
					int maxX = o.maxX + (o.maxNormalX * dy >> 8);
					int minZ = o.minZ + (o.minNormalZ * dy >> 8);
					int maxZ = o.maxZ + (o.maxNormalZ * dy >> 8);

					if (x >= minX && x <= maxX && z >= minZ && z <= maxZ) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
