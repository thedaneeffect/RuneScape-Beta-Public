package rs.data;

import rs.io.Archive;
import rs.io.Buffer;
import rs.util.Cache;

public class NPCType {

	public static int count;
	public static int[] pointers;
	public static Buffer data;
	public static NPCType[] cache;
	public static int cachePosition;
	public static Cache models = new Cache(30);

	public int index;
	public String name;
	public byte[] description;
	public byte size = 1;
	public int[] modelIndices;
	public int[] headModelIndices;
	public int standSeq = -1;
	public int walkSeq = -1;
	public int turnAroundSeq = -1;
	public int turnRightSeq = -1;
	public int turnLeftSeq = -1;
	public boolean disposeAlpha = false;
	public int[] oldColors;
	public int[] newColors;
	public String[] options;
	public boolean showOnMinimap = true;
	public int level = -1;
	public int scaleX = 128;
	public int scaleY = 128;

	public static int getCount() {
		return count;
	}

	public static void load(Archive a) {
		data = new Buffer(a.get("npc.dat", null));
		Buffer idx = new Buffer(a.get("npc.idx", null));
		count = idx.get2U();
		pointers = new int[count];

		int off = 2;
		for (int n = 0; n < count; n++) {
			pointers[n] = off;
			off += idx.get2U();
		}

		cache = new NPCType[20];
		for (int n = 0; n < 20; n++) {
			cache[n] = new NPCType();
		}
	}

	public static void unload() {
		models = null;
		pointers = null;
		cache = null;
		data = null;
	}

	public static NPCType get(int index) {
		for (int n = 0; n < 20; n++) {
			if (cache[n].index == index) {
				return cache[n];
			}
		}
		cachePosition = (cachePosition + 1) % 20;
		NPCType info = cache[cachePosition] = new NPCType();
		data.position = pointers[index];
		info.index = index;
		info.read(data);
		return info;
	}

	public NPCType() {
		this.index = -1;
	}

	public void read(Buffer b) {
		for (; ; ) {
			int opcode = b.get1U();

			if (opcode == 0) {
				break;
			}

			if (opcode == 1) {
				int count = b.get1U();
				modelIndices = new int[count];
				for (int n = 0; n < count; n++) {
					modelIndices[n] = b.get2U();
				}
			} else if (opcode == 2) {
				name = b.getString();
			} else if (opcode == 3) {
				description = b.getStringRaw();
			} else if (opcode == 12) {
				size = b.get1();
			} else if (opcode == 13) {
				standSeq = b.get2U();
			} else if (opcode == 14) {
				walkSeq = b.get2U();
			} else if (opcode == 16) {
				disposeAlpha = true;
			} else if (opcode == 17) {
				walkSeq = b.get2U();
				turnAroundSeq = b.get2U();
				turnRightSeq = b.get2U();
				turnLeftSeq = b.get2U();
			} else if (opcode >= 30 && opcode < 40) {
				if (options == null) {
					options = new String[5];
				}
				options[opcode - 30] = b.getString();
			} else if (opcode == 40) {
				int count = b.get1U();
				oldColors = new int[count];
				newColors = new int[count];
				for (int n = 0; n < count; n++) {
					oldColors[n] = b.get2U();
					newColors[n] = b.get2U();
				}
			} else if (opcode == 60) {
				int n = b.get1U();
				headModelIndices = new int[n];
				for (int m = 0; m < n; m++) {
					headModelIndices[m] = b.get2U();
				}
			} else if (opcode == 90) {
				b.get2U();
			} else if (opcode == 91) {
				b.get2U();
			} else if (opcode == 92) {
				b.get2U();
			} else if (opcode == 93) {
				showOnMinimap = false;
			} else if (opcode == 95) {
				level = b.get2U();
			} else if (opcode == 97) {
				scaleX = b.get2U();
			} else if (opcode == 98) {
				scaleY = b.get2U();
			}
		}
	}

	public final Model getModel(int primaryFrame, int secondaryFrame, int[] labelGroups) {
		Model m = (Model) models.get(index);

		if (m == null) {
			Model[] models = new Model[modelIndices.length];

			for (int n = 0; n < modelIndices.length; n++) {
				models[n] = new Model(modelIndices[n]);
			}

			if (models.length == 1) {
				m = models[0];
			} else {
				m = new Model(models, models.length);
			}

			if (oldColors != null) {
				for (int n = 0; n < oldColors.length; n++) {
					m.recolor(oldColors[n], newColors[n]);
				}
			}

			m.applyGroups();
			m.applyLighting(64, 850, -30, -50, -30, true);
			NPCType.models.put(index, m);
		}

		m = new Model(m, !disposeAlpha);

		if (primaryFrame != -1 && secondaryFrame != -1) {
			m.applyFrames(primaryFrame, secondaryFrame, labelGroups);
		} else if (primaryFrame != -1) {
			m.applyFrame(primaryFrame);
		}

		if (scaleX != 128 || scaleY != 128) {
			m.scale(scaleX, scaleY, scaleX);
		}

		m.calculateYBoundaries();
		m.skinTriangle = null;
		m.labelVertices = null;
		return m;
	}

	public final Model getHeadModel() {
		if (headModelIndices == null) {
			return null;
		}

		Model[] models = new Model[headModelIndices.length];

		for (int n = 0; n < headModelIndices.length; n++) {
			models[n] = new Model(headModelIndices[n]);
		}

		Model m;

		if (models.length == 1) {
			m = models[0];
		} else {
			m = new Model(models, models.length);
		}

		if (oldColors != null) {
			for (int n = 0; n < oldColors.length; n++) {
				m.recolor(oldColors[n], newColors[n]);
			}
		}
		return m;
	}

}
