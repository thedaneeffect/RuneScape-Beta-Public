package rs.data;

import rs.io.Archive;
import rs.io.Buffer;
import rs.util.Cache;

public class LocType {

	private static final Model[] tmp = new Model[4];
	public static int count;
	private static int[] pointers;
	private static Buffer data;
	private static LocType[] cache;
	private static int cachePosition;

	/* @formatter:off */
	public static final int[] TYPE_TO_CLASS = {0, // straight walls, fences
			0, // diagonal walls corner, fences etc connectors
			0, // entire walls, fences etc corners
			0, // straight wall corners, fences etc connectors
			1, // straight inside wall decoration
			1, // straight outside wall decoration
			1, // diagonal outside wall decoration
			1, // diagonal inside wall decoration
			1, // diagonal in wall decoration
			2, // diagonal walls, fences etc
			2, // all kinds of objects, trees, statues, signs, fountains etc etc
			2, // ground objects like daisies etc
			2, // straight sloped roofs
			2, // diagonal sloped roofs
			2, // diagonal slope connecting roofs
			2, // straight sloped corner connecting roofs
			2, // straight sloped corner roof
			2, // straight flat top roofs
			2, // straight bottom egde roofs
			2, // diagonal bottom edge connecting roofs
			2, // straight bottom edge connecting roofs
			2, // straight bottom edge connecting corner roofs
			3 // ground decoration + map signs (quests, water fountains, shops etc)
	};

	public static final int TYPE_WALL = 0;
	public static final int TYPE_WALL_DECORATION = 1;
	public static final int TYPE_NORMAL = 2;
	public static final int TYPE_GROUND_DECORATION = 3;

	public int index;
	public int[] modelIds;
	public int[] modelTypes;
	public String name;
	public String description;
	public int[] srcColor;
	public int[] dstColor;
	public int sizeX;
	public int sizeZ;
	/**
	 * <code>true</code> to apply collisions.
	 */
	public boolean solid;
	/**
	 * <code>true</code> to block projectiles.
	 */
	public boolean blocksProjectiles;
	/**
	 * Allows the loc to be picked. (Right clicked, or have actions)
	 */
	public boolean interactable;
	/**
	 * <code>true</code> to adjust the model to the terrain. Has the same side effect as {@link #dynamic}.
	 */
	public boolean adjustToTerrain;
	/**
	 * <code>true</code> if the model for this loc is expected to be modified. This prevents the model from being
	 * cached into {@link #builtModels}.
	 */
	public boolean dynamic;
	/**
	 * Sets occlude flags for the scene when this loc is used.
	 */
	public boolean occludes;
	/**
	 * The {@link SeqType} id.
	 */
	public int seqId;
	/**
	 * Only relevant for type 5 (wall decoration) locs.
	 */
	public int decorationPadding;
	public byte lightAmbient;
	public byte lightAttenuation;
	public String[] actions;
	/**
	 * <code>false</code> makes {@link #getModel(int, int, int, int, int, int, int)} reference face alpha values.
	 * <code>true</code> makes the method above <b>copy</b> face alpha values.
	 */
	public boolean animated;
	public int mapfunctionIcon;
	public int mapsceneIcon;
	public boolean invert;
	public boolean castsShadow;
	public int scaleX;
	public int scaleY;
	public int scaleZ;
	public int interactionSideFlags;

	public static Cache models = new Cache(500);
	public static Cache builtModels = new Cache(30);

	public static void load(Archive a) {
		data = new Buffer(a.get("loc.dat"));
		Buffer b = new Buffer(a.get("loc.idx"));

		count = b.get2U();
		pointers = new int[count];

		int i = 2;
		for (int n = 0; n < count; n++) {
			pointers[n] = i;
			i += b.get2U();
		}

		cache = new LocType[10];

		for (int n = 0; n < 10; n++) {
			cache[n] = new LocType();
		}
	}

	public static void unload() {
		models = null;
		builtModels = null;
		pointers = null;
		cache = null;
		data = null;
	}

	public static int getCount() {
		return count;
	}

	public static LocType get(int index) {
		if (index < 0 || index >= count) {
			return null;
		}

		for (int n = 0; n < 10; n++) {
			if (cache[n].index == index) {
				return cache[n];
			}
		}

		cachePosition = (cachePosition + 1) % 10;
		LocType info = cache[cachePosition];
		data.position = pointers[index];
		info.index = index;
		info.reset();
		info.read(data);
		return info;
	}

	public LocType() {
		this.index = -1;
	}

	private void reset() {
		modelIds = null;
		modelTypes = null;
		name = null;
		description = null;
		srcColor = null;
		dstColor = null;
		sizeX = 1;
		sizeZ = 1;
		solid = true;
		blocksProjectiles = true;
		interactable = false;
		adjustToTerrain = false;
		dynamic = false;
		occludes = false;
		seqId = -1;
		decorationPadding = 16;
		lightAmbient = (byte) 0;
		lightAttenuation = (byte) 0;
		actions = null;
		animated = false;
		mapfunctionIcon = -1;
		mapsceneIcon = -1;
		invert = false;
		castsShadow = true;
		scaleX = 128;
		scaleY = 128;
		scaleZ = 128;
		interactionSideFlags = 0;
	}

	private void read(Buffer b) {
		int bool = -1;

		for (; ; ) {
			int opcode = b.get1U();
			if (opcode == 0) {
				break;
			} else if (opcode == 1) {
				int n = b.get1U();
				modelTypes = new int[n];
				modelIds = new int[n];
				for (int m = 0; m < n; m++) {
					modelIds[m] = b.get2U();
					modelTypes[m] = b.get1U();
				}
			} else if (opcode == 2) {
				name = b.getString();
			} else if (opcode == 3) {
				description = b.getString();
			} else if (opcode == 5) { // 289-377 LOC.DAT BACKWARDS COMPATIBILITY
				int n = b.get1U();
				if (n > 0) {
					modelTypes = null;
					modelIds = new int[n];

					for (int m = 0; m < n; m++) {
						modelIds[m] = b.get2U();
					}
				}
			} else if (opcode == 14) {
				sizeX = b.get1U();
			} else if (opcode == 15) {
				sizeZ = b.get1U();
			} else if (opcode == 17) {
				solid = false;
			} else if (opcode == 18) {
				blocksProjectiles = false;
			} else if (opcode == 19) {
				bool = b.get1U();
				if (bool == 1) {
					interactable = true;
				}
			} else if (opcode == 21) {
				adjustToTerrain = true;
			} else if (opcode == 22) {
				dynamic = true;
			} else if (opcode == 23) {
				occludes = true;
			} else if (opcode == 24) {
				seqId = b.get2U();

				if (seqId == 65535) {
					seqId = -1;
				}
			} else if (opcode == 25) {
				animated = true;
			} else if (opcode == 28) {
				decorationPadding = b.get1U();
			} else if (opcode == 29) {
				lightAmbient = b.get1();
			} else if (opcode == 39) {
				lightAttenuation = b.get1();
			} else if (opcode >= 30 && opcode < 39) {
				if (actions == null) {
					actions = new String[5];
				}

				String s = b.getString();

				// 289 loc backwards compatibility
				if (!s.equals("hidden")) {
					actions[opcode - 30] = s;
				}
			} else if (opcode == 40) {
				int n = b.get1U();
				srcColor = new int[n];
				dstColor = new int[n];
				for (int m = 0; m < n; m++) {
					srcColor[m] = b.get2U();
					dstColor[m] = b.get2U();
				}
			} else if (opcode == 60) {
				mapfunctionIcon = b.get2U();
			} else if (opcode == 62) {
				invert = true;
			} else if (opcode == 64) {
				castsShadow = false;
			} else if (opcode == 65) {
				scaleX = b.get2U();
			} else if (opcode == 66) {
				scaleY = b.get2U();
			} else if (opcode == 67) {
				scaleZ = b.get2U();
			} else if (opcode == 68) {
				mapsceneIcon = b.get2U();
			} else if (opcode == 69) {
				interactionSideFlags = b.get1U();
			} else if (opcode >= 70 && opcode <= 72) {
				b.get2(); // 289 (translate x/y/z)
			} else if (opcode == 73 || opcode == 74) {
				// ignore 289 (73 = important, 74 = decorative)
			} else if (opcode == 75) {
				b.get1U(); // (supports obj)
			} else if (opcode == 77) { // ignore varbit
				int n = b.get2U();
				b.get1U();

				for (int j = 0; j < n; j++) {
					b.get2U();
				}
			} else {
				System.out.println("Invalid loc code: " + opcode);
			}
		}

		if (bool == -1) {
			interactable = modelTypes == null || (modelTypes.length > 0 && modelTypes[0] == 10);

			if (actions != null) {
				interactable = true;
			}
		}
	}

	public final Model getModel(int type, int rotation, int southwestY, int southeastY, int northeastY, int northwestY, int seqFrame) {
		long uid;
		Model m;

		if (modelTypes == null) {
			if (type != 10) {
				return null;
			}

			uid = ((long) index << 6) + rotation + ((long) (seqFrame + 1) << 32);

			m = (Model) builtModels.get(uid);

			if (m != null) {
				return m;
			}

			if (modelIds == null) {
				return null;
			}

			boolean flipBackwards = invert ^ (rotation > 3);
			int modelCount = modelIds.length;

			for (int n = 0; n < modelCount; n++) {
				int modelIndex = modelIds[n];

				if (flipBackwards) {
					modelIndex += 0x10000;
				}

				m = (Model) models.get(modelIndex);

				if (m == null) {
					m = new Model(modelIndex & 0xFFFF);

					if (flipBackwards) {
						m.flipBackwards();
					}

					models.put(modelIndex, m);
				}

				if (modelCount > 1) {
					tmp[n] = m;
				}
			}

			if (modelCount > 1) {
				m = new Model(tmp, modelCount);
			}
		} else {
			int typeIndex = -1;

			for (int n = 0; n < modelTypes.length; n++) {
				if (modelTypes[n] == type) {
					typeIndex = n;
					break;
				}
			}

			if (typeIndex == -1) {
				return null;
			}

			uid = (((long) index << 6) + ((long) typeIndex << 3) + rotation + ((long) (seqFrame + 1) << 32));

			if (!adjustToTerrain && !dynamic) {
				m = (Model) builtModels.get(uid);

				if (m != null) {
					return m;
				}
			}

			if (typeIndex >= modelIds.length) {
				return null;
			}

			int modelIndex = modelIds[typeIndex];

			if (modelIndex == -1) {
				return null;
			}

			boolean flipBackwards = invert ^ rotation > 3;

			if (flipBackwards) {
				modelIndex += 0x10000; // appends a flag to our modelIndex variable.
			}

			m = (Model) models.get((long) modelIndex);

			if (m == null) {
				m = new Model(modelIndex & 0xFFFF);

				if (flipBackwards) {
					m.flipBackwards();
				}

				models.put((long) modelIndex, m);
			}
		}

		boolean rescale = scaleX != 128 || scaleY != 128 || scaleZ != 128;

		m = new Model(m, rotation == 0 && !adjustToTerrain && seqFrame == -1 && !rescale, srcColor == null, !animated, !dynamic);

		if (seqFrame != -1) {
			m.applyGroups();
			m.applyFrame(seqFrame);
			m.skinTriangle = null;
			m.labelVertices = null;
		}

		while (rotation-- > 0) {
			m.rotateCounterClockwise();
		}

		if (srcColor != null) {
			for (int n = 0; n < srcColor.length; n++) {
				m.recolor(srcColor[n], dstColor[n]);
			}
		}

		if (rescale) {
			m.scale(scaleX, scaleY, scaleZ);
		}

		if (adjustToTerrain) {
			int averageY = (southwestY + southeastY + northeastY + northwestY) / 4;

			for (int v = 0; v < m.vertexCount; v++) {
				int x = m.vertexX[v];
				int z = m.vertexZ[v];

				// Gets the y value between the southwest and southeast corners
				int averageY1 = southwestY + (((southeastY - southwestY) * (x + 64)) / 128);

				// Gets the y value between the northwest and northeast corners
				int averageY2 = northwestY + (((northeastY - northwestY) * (x + 64)) / 128);

				// Gets the y value between
				int y = averageY1 + (((averageY2 - averageY1) * (z + 64)) / 128);

				m.vertexY[v] += y - averageY;
			}
		}

		m.applyLighting(lightAmbient + 64, (lightAttenuation * 5) + 768, -50, -10, -50, !dynamic);

		if (solid) {
			m.objectOffsetY = m.maxBoundY;
		}

		if (!adjustToTerrain && !dynamic) {
			builtModels.put(uid, m);
		}

		return m;
	}

}
