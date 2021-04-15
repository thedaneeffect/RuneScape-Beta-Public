package rs.data;

import rs.io.Archive;
import rs.io.Buffer;
import rs.media.Draw2D;
import rs.media.Draw3D;
import rs.media.Sprite;
import rs.util.Cache;

public class ObjType {

	public static int count;
	public static int[] pointers;
	public static Buffer buffer;
	public static ObjType[] cache;
	public static int cachepos;
	public static Cache models = new Cache(50);
	public static Cache sprites = new Cache(200);

	public int index = -1;
	public int modelIndex;
	public String name;
	public byte[] description;
	public int[] oldColors;
	public int[] newColors;
	public int iconZoom;
	public int iconCameraPitch;
	public int iconYaw;
	public int iconRoll;
	public int iconX;
	public int iconY;
	public boolean stackable;
	public int priority;
	public boolean members;
	public String[] groundOptions;
	public String[] options;
	public int maleModel0;
	public int maleModel1;
	public byte maleOffsetY;
	public int femaleModel0;
	public int femaleModel1;
	public byte femaleOffsetY;
	public int maleHeadModelA;
	public int maleHeadModelB;
	public int femaleHeadModelA;
	public int femaleHeadModelB;

	public static final int getCount() {
		return count;
	}

	public static final void load(Archive a) {
		buffer = new Buffer(a.get("obj.dat", null));
		Buffer b = new Buffer(a.get("obj.idx", null));
		count = b.get2U();
		pointers = new int[count];

		int i = 2;
		for (int n = 0; n < count; n++) {
			pointers[n] = i;
			i += b.get2U();
		}

		cache = new ObjType[10];

		for (i = 0; i < 10; i++) {
			cache[i] = new ObjType();
		}
	}

	public static final void unload() {
		models = null;
		sprites = null;
		pointers = null;
		cache = null;
		buffer = null;
	}

	public static ObjType get(int i) {
		for (int n = 0; n < 10; n++) {
			if (cache[n].index == i) {
				return cache[n];
			}
		}
		cachepos = (cachepos + 1) % 10;
		ObjType o = cache[cachepos];
		buffer.position = pointers[i];
		o.index = i;
		o.reset();
		o.read(buffer);
		return o;
	}

	public final void reset() {
		modelIndex = 0;
		name = null;
		description = null;
		oldColors = null;
		newColors = null;
		iconZoom = 2000;
		iconCameraPitch = 0;
		iconYaw = 0;
		iconRoll = 0;
		iconX = 0;
		iconY = 0;
		stackable = false;
		priority = 1;
		members = false;
		groundOptions = null;
		options = null;
		maleModel0 = -1;
		maleModel1 = -1;
		maleOffsetY = (byte) 0;
		femaleModel0 = -1;
		femaleModel1 = -1;
		femaleOffsetY = (byte) 0;
		maleHeadModelA = -1;
		maleHeadModelB = -1;
		femaleHeadModelA = -1;
		femaleHeadModelB = -1;
	}

	public final void read(Buffer b) {
		for (; ; ) {
			int opcode = b.get1U();

			if (opcode == 0) {
				break;
			}

			if (opcode == 1) {
				modelIndex = b.get2U();
			} else if (opcode == 2) {
				name = b.getString();
			} else if (opcode == 3) {
				description = b.getStringRaw();
			} else if (opcode == 4) {
				iconZoom = b.get2U();
			} else if (opcode == 5) {
				iconCameraPitch = b.get2U();
			} else if (opcode == 6) {
				iconYaw = b.get2U();
			} else if (opcode == 7) {
				iconX = b.get2U();
				if (iconX > 32767) {
					iconX -= 65536;
				}
			} else if (opcode == 8) {
				iconY = b.get2U();
				if (iconY > 32767) {
					iconY -= 65536;
				}
			} else if (opcode == 9) {
			} else if (opcode == 10) {
				b.get2U();
			} else if (opcode == 11) {
				stackable = true;
			} else if (opcode == 12) {
				priority = b.get4();
			} else if (opcode == 16) {
				members = true;
			} else if (opcode == 23) {
				maleModel0 = b.get2U();
				maleOffsetY = b.get1();
			} else if (opcode == 24) {
				maleModel1 = b.get2U();
			} else if (opcode == 25) {
				femaleModel0 = b.get2U();
				femaleOffsetY = b.get1();
			} else if (opcode == 26) {
				femaleModel1 = b.get2U();
			} else if (opcode >= 30 && opcode < 35) {
				if (groundOptions == null) {
					groundOptions = new String[5];
				}
				groundOptions[opcode - 30] = b.getString();
			} else if (opcode >= 35 && opcode < 40) {
				if (options == null) {
					options = new String[5];
				}
				options[opcode - 35] = b.getString();
			} else if (opcode == 40) {
				int n = b.get1U();
				oldColors = new int[n];
				newColors = new int[n];
				for (int i_5_ = 0; i_5_ < n; i_5_++) {
					oldColors[i_5_] = b.get2U();
					newColors[i_5_] = b.get2U();
				}
			} else if (opcode == 90) {
				maleHeadModelA = b.get2U();
			} else if (opcode == 91) {
				femaleHeadModelA = b.get2U();
			} else if (opcode == 92) {
				maleHeadModelB = b.get2U();
			} else if (opcode == 93) {
				femaleHeadModelB = b.get2U();
			} else if (opcode == 95) {
				iconRoll = b.get2U();
			}
		}
	}

	public final Model getModel() {
		Model m = (Model) models.get((long) index);

		if (m != null) {
			return m;
		}

		m = new Model(modelIndex);

		if (oldColors != null) {
			for (int i = 0; i < oldColors.length; i++) {
				m.recolor(oldColors[i], newColors[i]);
			}
		}

		m.applyLighting(64, 768, -50, -10, -50, true);
		models.put((long) index, m);
		return m;
	}

	public static final Sprite getSprite(int index) {
		Sprite s = (Sprite) sprites.get((long) index);

		if (s != null) {
			return s;
		}

		ObjType info = get(index);
		s = new Sprite(32, 32);

		int centerX = Draw3D.centerX;
		int centerY = Draw3D.centerY;
		int[] offsets = Draw3D.offsets;
		int[] data = Draw2D.dst;
		int width = Draw2D.dstWidth;
		int height = Draw2D.dstHeight;

		Draw2D.prepare(s.pixels, 32, 32);
		Draw2D.fillRect(0, 0, 32, 32, 0);
		Draw3D.prepareOffsets();

		Model m = info.getModel();

		int cameraY = (Draw3D.sin[info.iconCameraPitch] * info.iconZoom) >> 16;
		int cameraZ = (Draw3D.cos[info.iconCameraPitch] * info.iconZoom) >> 16;

		m.draw(0, info.iconYaw, info.iconRoll, info.iconX, (cameraY + (m.maxBoundY / 2) + info.iconY), cameraZ + info.iconY, info.iconCameraPitch);

		for (int x = 31; x >= 0; x--) {
			for (int y = 31; y >= 0; y--) {
				int i = x + (y * 32);
				if (s.pixels[i] == 0) {
					if (x > 0 && (s.pixels[((x - 1) + (y * 32))]) > 1) {
						s.pixels[i] = 1;
					} else if (y > 0 && (s.pixels[(x + ((y - 1) * 32))]) > 1) {
						s.pixels[i] = 1;
					} else if (x < 31 && (s.pixels[(x + 1 + (y * 32))]) > 1) {
						s.pixels[i] = 1;
					} else if (y < 31 && (s.pixels[(x + ((y + 1) * 32))]) > 1) {
						s.pixels[i] = 1;
					}
				}
			}
		}

		for (int x = 31; x >= 0; x--) {
			for (int y = 31; y >= 0; y--) {
				int i = x + (y * 32);

				if (s.pixels[i] != 0) {
					continue;
				}

				if (x <= 0 || y <= 0) {
					continue;
				}

				// up a row and left 1 pixel
				if (s.pixels[i - 33] > 0) {
					s.pixels[i] = 0x302020;
				}
			}
		}

		sprites.put((long) index, s);

		Draw2D.prepare(data, width, height);
		Draw3D.centerX = centerX;
		Draw3D.centerY = centerY;
		Draw3D.offsets = offsets;

		if (info.stackable) {
			s.clipWidth = 33;
		} else {
			s.clipWidth = 32;
		}

		return s;
	}

	public Model getWornModel(int gender) {
		int model0 = maleModel0;

		if (gender == 1) {
			model0 = femaleModel0;
		}

		if (model0 == -1) {
			return null;
		}

		int model1 = maleModel1;

		if (gender == 1) {
			model1 = femaleModel1;
		}

		Model m = new Model(model0);

		if (model1 != -1) {
			m = new Model(new Model[]{m, new Model(model1)}, 2);
		}

		if (gender == 0 && maleOffsetY != 0) {
			m.translate(0, maleOffsetY, 0);
		}

		if (gender == 1 && femaleOffsetY != 0) {
			m.translate(0, femaleOffsetY, 0);
		}

		if (oldColors != null) {
			for (int n = 0; n < oldColors.length; n++) {
				m.recolor(oldColors[n], newColors[n]);
			}
		}

		return m;
	}

	public final Model getHeadModel(int gender) {
		int modelA = maleHeadModelA;

		if (gender == 1) {
			modelA = femaleHeadModelA;
		}

		if (modelA == -1) {
			return null;
		}

		int modelB = maleHeadModelB;

		if (gender == 1) {
			modelB = femaleHeadModelB;
		}

		Model m = new Model(modelA);

		if (modelB != -1) {
			m = new Model(new Model[]{m, new Model(modelB)}, 2);
		}

		if (oldColors != null) {
			for (int n = 0; n < oldColors.length; n++) {
				m.recolor(oldColors[n], newColors[n]);
			}
		}

		return m;
	}

}
