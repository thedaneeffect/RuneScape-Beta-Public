package rs.scene;

public final class CollisionMap {

	public static final int OPEN = 0x0;
	public static final int CLOSED = 0xFFFFFF;

	public static final int WALL_NORTHWEST = 0x1;
	public static final int WALL_NORTH = 0x2;
	public static final int WALL_NORTHEAST = 0x4;
	public static final int WALL_EAST = 0x8;
	public static final int WALL_SOUTHEAST = 0x10;
	public static final int WALL_SOUTH = 0x20;
	public static final int WALL_SOUTHWEST = 0x40;
	public static final int WALL_WEST = 0x80;

	public static final int OCCUPIED_TILE = 0x100;

	public static final int BLOCKED_NORTHWEST = 0x200;
	public static final int BLOCKED_NORTH = 0x400;
	public static final int BLOCKED_NORTHEAST = 0x800;
	public static final int BLOCKED_EAST = 0x1000;
	public static final int BLOCKED_SOUTHEAST = 0x2000;
	public static final int BLOCKED_SOUTH = 0x4000;
	public static final int BLOCKED_SOUTHWEST = 0x8000;
	public static final int BLOCKED_WEST = 0x10000;

	public static final int SOLID = 0x20000;
	public static final int BLOCKED_TILE = 0x200000;

	public int wide;
	public int tall;
	public int[][] flags;

	public CollisionMap(int wide, int tall) {
		this.wide = wide;
		this.tall = tall;
		this.flags = new int[wide][tall];
		reset();
	}

	public void reset() {
		for (int x = 0; x < wide; x++) {
			for (int z = 0; z < tall; z++) {
				if (x == 0 || z == 0 || x == wide - 1 || z == tall - 1) {
					flags[x][z] = CLOSED;
				} else {
					flags[x][z] = OPEN;
				}
			}
		}
	}

	public void setWall(int x, int z, int type, int rotation, boolean blocks) {
		if (type == 0) {
			if (rotation == 0) {
				add(x, z, WALL_WEST);
				add(x - 1, z, WALL_EAST);
			}
			if (rotation == 1) {
				add(x, z, WALL_NORTH);
				add(x, z + 1, WALL_SOUTH);
			}
			if (rotation == 2) {
				add(x, z, WALL_EAST);
				add(x + 1, z, WALL_WEST);
			}
			if (rotation == 3) {
				add(x, z, WALL_SOUTH);
				add(x, z - 1, WALL_NORTH);
			}
		}
		if (type == 1 || type == 3) {
			if (rotation == 0) {
				add(x, z, WALL_NORTHWEST);
				add(x - 1, z + 1, WALL_SOUTHEAST);
			}
			if (rotation == 1) {
				add(x, z, WALL_NORTHEAST);
				add(x + 1, z + 1, WALL_SOUTHWEST);
			}
			if (rotation == 2) {
				add(x, z, WALL_SOUTHEAST);
				add(x + 1, z - 1, WALL_NORTHWEST);
			}
			if (rotation == 3) {
				add(x, z, WALL_SOUTHWEST);
				add(x - 1, z - 1, WALL_NORTHEAST);
			}
		}
		if (type == 2) {
			if (rotation == 0) {
				add(x, z, WALL_WEST | WALL_NORTH);
				add(x - 1, z, WALL_EAST);
				add(x, z + 1, WALL_SOUTH);
			}
			if (rotation == 1) {
				add(x, z, WALL_EAST | WALL_NORTH);
				add(x, z + 1, WALL_SOUTH);
				add(x + 1, z, WALL_WEST);
			}
			if (rotation == 2) {
				add(x, z, WALL_SOUTH | WALL_EAST);
				add(x + 1, z, WALL_WEST);
				add(x, z - 1, WALL_NORTH);
			}
			if (rotation == 3) {
				add(x, z, WALL_WEST | WALL_SOUTH);
				add(x, z - 1, WALL_NORTH);
				add(x - 1, z, WALL_EAST);
			}
		}
		if (blocks) {
			if (type == 0) {
				if (rotation == 0) {
					add(x, z, 65536);
					add(x - 1, z, 4096);
				}
				if (rotation == 1) {
					add(x, z, 1024);
					add(x, z + 1, 16384);
				}
				if (rotation == 2) {
					add(x, z, 4096);
					add(x + 1, z, 65536);
				}
				if (rotation == 3) {
					add(x, z, 16384);
					add(x, z - 1, 1024);
				}
			}
			if (type == 1 || type == 3) {
				if (rotation == 0) {
					add(x, z, 512);
					add(x - 1, z + 1, 8192);
				}
				if (rotation == 1) {
					add(x, z, 2048);
					add(x + 1, z + 1, 32768);
				}
				if (rotation == 2) {
					add(x, z, 8192);
					add(x + 1, z - 1, 512);
				}
				if (rotation == 3) {
					add(x, z, 32768);
					add(x - 1, z - 1, 2048);
				}
			}
			if (type == 2) {
				if (rotation == 0) {
					add(x, z, 66560);
					add(x - 1, z, 4096);
					add(x, z + 1, 16384);
				}
				if (rotation == 1) {
					add(x, z, 5120);
					add(x, z + 1, 16384);
					add(x + 1, z, 65536);
				}
				if (rotation == 2) {
					add(x, z, 20480);
					add(x + 1, z, 65536);
					add(x, z - 1, 1024);
				}
				if (rotation == 3) {
					add(x, z, 81920);
					add(x, z - 1, 1024);
					add(x - 1, z, 4096);
				}
			}
		}
	}

	public void setLoc(int tileX, int tileY, int sizeX, int sizeY, int rotation, boolean solid) {
		int flag = OCCUPIED_TILE;

		if (solid) {
			flag += SOLID;
		}

		if (rotation == 1 || rotation == 3) {
			int y = sizeX;
			sizeX = sizeY;
			sizeY = y;
		}

		for (int x = tileX; x < tileX + sizeX; x++) {
			if (x >= 0 && x < wide) {
				for (int y = tileY; y < tileY + sizeY; y++) {
					if (y >= 0 && y < tall) {
						add(x, y, flag);
					}
				}
			}
		}
	}

	public void setBlocked(int x, int y) {
		flags[x][y] |= BLOCKED_TILE;
	}

	private void add(int x, int y, int flag) {
		flags[x][y] |= flag;
	}

	public void removeWall(int tileX, int tileZ, int type, int rotation, boolean blocks) {
		if (type == 0) {
			if (rotation == 0) {
				remove(tileX, tileZ, WALL_WEST);
				remove(tileX - 1, tileZ, WALL_EAST);
			}
			if (rotation == 1) {
				remove(tileX, tileZ, WALL_NORTH);
				remove(tileX, tileZ + 1, WALL_SOUTH);
			}
			if (rotation == 2) {
				remove(tileX, tileZ, WALL_EAST);
				remove(tileX + 1, tileZ, WALL_WEST);
			}
			if (rotation == 3) {
				remove(tileX, tileZ, WALL_SOUTH);
				remove(tileX, tileZ - 1, WALL_NORTH);
			}
		}
		if (type == 1 || type == 3) {
			if (rotation == 0) {
				remove(tileX, tileZ, WALL_NORTHWEST);
				remove(tileX - 1, tileZ + 1, WALL_SOUTHEAST);
			}
			if (rotation == 1) {
				remove(tileX, tileZ, WALL_NORTHEAST);
				remove(tileX + 1, tileZ + 1, WALL_SOUTHWEST);
			}
			if (rotation == 2) {
				remove(tileX, tileZ, WALL_SOUTHEAST);
				remove(tileX + 1, tileZ - 1, WALL_NORTHWEST);
			}
			if (rotation == 3) {
				remove(tileX, tileZ, WALL_SOUTHWEST);
				remove(tileX - 1, tileZ - 1, WALL_NORTHEAST);
			}
		}
		if (type == 2) {
			if (rotation == 0) {
				remove(tileX, tileZ, WALL_WEST | WALL_NORTH);
				remove(tileX - 1, tileZ, WALL_EAST);
				remove(tileX, tileZ + 1, WALL_SOUTH);
			}
			if (rotation == 1) {
				remove(tileX, tileZ, WALL_EAST | WALL_NORTH);
				remove(tileX, tileZ + 1, WALL_SOUTH);
				remove(tileX + 1, tileZ, WALL_WEST);
			}
			if (rotation == 2) {
				remove(tileX, tileZ, WALL_SOUTH | WALL_EAST);
				remove(tileX + 1, tileZ, WALL_WEST);
				remove(tileX, tileZ - 1, WALL_NORTH);
			}
			if (rotation == 3) {
				remove(tileX, tileZ, WALL_WEST | WALL_SOUTH);
				remove(tileX, tileZ - 1, WALL_NORTH);
				remove(tileX - 1, tileZ, WALL_EAST);
			}
		}
		if (blocks) {
			if (type == 0) {
				if (rotation == 0) {
					remove(tileX, tileZ, 65536);
					remove(tileX - 1, tileZ, 4096);
				}
				if (rotation == 1) {
					remove(tileX, tileZ, 1024);
					remove(tileX, tileZ + 1, 16384);
				}
				if (rotation == 2) {
					remove(tileX, tileZ, 4096);
					remove(tileX + 1, tileZ, 65536);
				}
				if (rotation == 3) {
					remove(tileX, tileZ, 16384);
					remove(tileX, tileZ - 1, 1024);
				}
			}
			if (type == 1 || type == 3) {
				if (rotation == 0) {
					remove(tileX, tileZ, 512);
					remove(tileX - 1, tileZ + 1, 8192);
				}
				if (rotation == 1) {
					remove(tileX, tileZ, 2048);
					remove(tileX + 1, tileZ + 1, 32768);
				}
				if (rotation == 2) {
					remove(tileX, tileZ, 8192);
					remove(tileX + 1, tileZ - 1, 512);
				}
				if (rotation == 3) {
					remove(tileX, tileZ, 32768);
					remove(tileX - 1, tileZ - 1, 2048);
				}
			}
			if (type == 2) {
				if (rotation == 0) {
					remove(tileX, tileZ, 66560);
					remove(tileX - 1, tileZ, 4096);
					remove(tileX, tileZ + 1, 16384);
				}
				if (rotation == 1) {
					remove(tileX, tileZ, 5120);
					remove(tileX, tileZ + 1, 16384);
					remove(tileX + 1, tileZ, 65536);
				}
				if (rotation == 2) {
					remove(tileX, tileZ, 20480);
					remove(tileX + 1, tileZ, 65536);
					remove(tileX, tileZ - 1, 1024);
				}
				if (rotation == 3) {
					remove(tileX, tileZ, 81920);
					remove(tileX, tileZ - 1, 1024);
					remove(tileX - 1, tileZ, 4096);
				}
			}
		}
	}

	public void removeLoc(int tileX, int tileY, int sizeX, int sizeY, int rotation, boolean solid) {
		int flag = OCCUPIED_TILE;

		if (solid) {
			flag += SOLID;
		}

		if (rotation == 1 || rotation == 3) {
			int w = sizeX;
			sizeX = sizeY;
			sizeY = w;
		}

		for (int x = tileX; x < tileX + sizeX; x++) {
			if (x >= 0 && x < sizeX) {
				for (int y = tileY; y < tileY + sizeY; y++) {
					if (y >= 0 && y < tall) {
						remove(x, y, flag);
					}
				}
			}
		}
	}

	private void remove(int x, int y, int flag) {
		flags[x][y] &= 0xFFFFFF - flag;
	}

	public void removeBlock(int x, int y) {
		flags[x][y] &= 0xFFFFFF - BLOCKED_TILE;
	}

	public boolean method101(int x0, int y0, int y1, int x1, int type, int direction) {
		if (x0 == x1 && y0 == y1) {
			return true;
		}

		if (type == 0) {
			if (direction == 0) {
				if (x0 == x1 - 1 && y0 == y1) {
					return true;
				}
				if (x0 == x1 && y0 == y1 + 1 && (flags[x0][y0] & 0x280120) == 0) {
					return true;
				}
				if (x0 == x1 && y0 == y1 - 1 && (flags[x0][y0] & 0x280102) == 0) {
					return true;
				}
			} else if (direction == 1) {
				if (x0 == x1 && y0 == y1 + 1) {
					return true;
				}
				if (x0 == x1 - 1 && y0 == y1 && (flags[x0][y0] & 0x280108) == 0) {
					return true;
				}
				if (x0 == x1 + 1 && y0 == y1 && (flags[x0][y0] & 0x280180) == 0) {
					return true;
				}
			} else if (direction == 2) {
				if (x0 == x1 + 1 && y0 == y1) {
					return true;
				}
				if (x0 == x1 && y0 == y1 + 1 && (flags[x0][y0] & 0x280120) == 0) {
					return true;
				}
				if (x0 == x1 && y0 == y1 - 1 && (flags[x0][y0] & 0x280102) == 0) {
					return true;
				}
			} else if (direction == 3) {
				if (x0 == x1 && y0 == y1 - 1) {
					return true;
				}
				if (x0 == x1 - 1 && y0 == y1 && (flags[x0][y0] & 0x280108) == 0) {
					return true;
				}
				if (x0 == x1 + 1 && y0 == y1 && (flags[x0][y0] & 0x280180) == 0) {
					return true;
				}
			}
		}

		if (type == 2) {
			if (direction == 0) {
				if (x0 == x1 - 1 && y0 == y1) {
					return true;
				}
				if (x0 == x1 && y0 == y1 + 1) {
					return true;
				}
				if (x0 == x1 + 1 && y0 == y1 && (flags[x0][y0] & 0x280180) == 0) {
					return true;
				}
				if (x0 == x1 && y0 == y1 - 1 && (flags[x0][y0] & 0x280102) == 0) {
					return true;
				}
			} else if (direction == 1) {
				if (x0 == x1 - 1 && y0 == y1 && (flags[x0][y0] & 0x280108) == 0) {
					return true;
				}
				if (x0 == x1 && y0 == y1 + 1) {
					return true;
				}
				if (x0 == x1 + 1 && y0 == y1) {
					return true;
				}
				if (x0 == x1 && y0 == y1 - 1 && (flags[x0][y0] & 0x280102) == 0) {
					return true;
				}
			} else if (direction == 2) {
				if (x0 == x1 - 1 && y0 == y1 && (flags[x0][y0] & 0x280108) == 0) {
					return true;
				}
				if (x0 == x1 && y0 == y1 + 1 && (flags[x0][y0] & 0x280120) == 0) {
					return true;
				}
				if (x0 == x1 + 1 && y0 == y1) {
					return true;
				}
				if (x0 == x1 && y0 == y1 - 1) {
					return true;
				}
			} else if (direction == 3) {
				if (x0 == x1 - 1 && y0 == y1) {
					return true;
				}
				if (x0 == x1 && y0 == y1 + 1 && (flags[x0][y0] & 0x280120) == 0) {
					return true;
				}
				if (x0 == x1 + 1 && y0 == y1 && (flags[x0][y0] & 0x280180) == 0) {
					return true;
				}
				if (x0 == x1 && y0 == y1 - 1) {
					return true;
				}
			}
		}

		if (type == 9) {
			if (x0 == x1 && y0 == y1 + 1 && (flags[x0][y0] & WALL_SOUTH) == 0) {
				return true;
			}
			if (x0 == x1 && y0 == y1 - 1 && (flags[x0][y0] & WALL_NORTH) == 0) {
				return true;
			}
			if (x0 == x1 - 1 && y0 == y1 && (flags[x0][y0] & WALL_EAST) == 0) {
				return true;
			}
			if (x0 == x1 + 1 && y0 == y1 && (flags[x0][y0] & WALL_WEST) == 0) {
				return true;
			}
		}
		return false;
	}

	public boolean method102(int x0, int y0, int x1, int y1, int type, int direction) {
		if (x0 == x1 && y0 == y1) {
			return true;
		}

		if (type == 6 || type == 7) {
			if (type == 7) {
				direction = direction + 2 & 0x3;
			}

			if (direction == 0) {
				if (x0 == x1 + 1 && y0 == y1 && (flags[x0][y0] & WALL_WEST) == 0) {
					return true;
				}
				if (x0 == x1 && y0 == y1 - 1 && (flags[x0][y0] & WALL_NORTH) == 0) {
					return true;
				}
			} else if (direction == 1) {
				if (x0 == x1 - 1 && y0 == y1 && (flags[x0][y0] & WALL_EAST) == 0) {
					return true;
				}
				if (x0 == x1 && y0 == y1 - 1 && (flags[x0][y0] & WALL_NORTH) == 0) {
					return true;
				}
			} else if (direction == 2) {
				if (x0 == x1 - 1 && y0 == y1 && (flags[x0][y0] & WALL_EAST) == 0) {
					return true;
				}
				if (x0 == x1 && y0 == y1 + 1 && (flags[x0][y0] & WALL_SOUTH) == 0) {
					return true;
				}
			} else if (direction == 3) {
				if (x0 == x1 + 1 && y0 == y1 && (flags[x0][y0] & WALL_WEST) == 0) {
					return true;
				}
				if (x0 == x1 && y0 == y1 + 1 && (flags[x0][y0] & WALL_SOUTH) == 0) {
					return true;
				}
			}
		}

		if (type == 8) {
			if (x0 == x1 && y0 == y1 + 1 && (flags[x0][y0] & WALL_SOUTH) == 0) {
				return true;
			}
			if (x0 == x1 && y0 == y1 - 1 && (flags[x0][y0] & WALL_NORTH) == 0) {
				return true;
			}
			if (x0 == x1 - 1 && y0 == y1 && (flags[x0][y0] & WALL_EAST) == 0) {
				return true;
			}
			if (x0 == x1 + 1 && y0 == y1 && (flags[x0][y0] & WALL_WEST) == 0) {
				return true;
			}
		}
		return false;
	}

	public boolean method103(int srcX, int srcY, int sizeX, int sizeY, int dstX, int dstY, int faceflags) {
		int maxX = dstX + sizeX - 1;
		int maxY = dstY + sizeY - 1;

		if (srcX >= dstX && srcX <= maxX && srcY >= dstY && srcY <= maxY) {
			return true;
		}

		if (srcX == dstX - 1 && srcY >= dstY && srcY <= maxY && (flags[srcX][srcY] & WALL_EAST) == 0 && (faceflags & 0x8) == 0) {
			return true;
		}

		if (srcX == maxX + 1 && srcY >= dstY && srcY <= maxY && ((flags[srcX][srcY] & WALL_WEST) == 0) && (faceflags & 0x2) == 0) {
			return true;
		}

		if (srcY == dstY - 1 && srcX >= dstX && srcX <= maxX && (flags[srcX][srcY] & WALL_NORTH) == 0 && (faceflags & 0x4) == 0) {
			return true;
		}

		return srcY == maxY + 1 && srcX >= dstX && srcX <= maxX && ((flags[srcX][srcY] & WALL_SOUTH) == 0) && (faceflags & 0x1) == 0;
	}
}
