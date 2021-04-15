package rs.data;

import rs.io.Archive;
import rs.io.Buffer;
import rs.media.IndexedFont;
import rs.media.Sprite;
import rs.util.Cache;
import rs.util.Strings;

public class Component {

	public static final int TYPE_PARENT = 0;
	public static final int TYPE_UNUSED = 1;
	public static final int TYPE_INVENTORY = 2;
	public static final int TYPE_RECT = 3;
	public static final int TYPE_TEXT = 4;
	public static final int TYPE_SPRITE = 5;
	public static final int TYPE_MODEL = 6;
	public static final int TYPE_INVENTORY_TEXT = 7;

	public static final int NO_BUTTON = 0;
	public static final int BUTTON = 1;
	public static final int TARGET_BUTTON = 2;
	public static final int CLOSE_BUTTON = 3;
	public static final int TOGGLE_BUTTON = 4;
	public static final int SELECT_BUTTON = 5;
	public static final int PAUSE_BUTTON = 6;

	public static Component[] instances;

	private static Cache spriteCache;
	private static Cache modelCache;

	private static Sprite getSprite(String name, Archive media, int index) {
		long uid = (Strings.getHash(name) << 4) + (long) index;
		Sprite s = (Sprite) spriteCache.get(uid);

		if (s != null) {
			return s;
		}

		s = new Sprite(media, name, index);
		spriteCache.put(uid, s);
		return s;
	}

	private static Model getModel(int index) {
		Model m = (Model) modelCache.get((long) index);

		if (m != null) {
			return m;
		}

		m = new Model(index);
		modelCache.put((long) index, m);
		return m;
	}

	public static void load(IndexedFont[] fonts, Archive media, Archive interfaces) {
		spriteCache = new Cache(50000);
		modelCache = new Cache(50000);

		Buffer b = new Buffer(interfaces.get("data", null));
		instances = new Component[b.get2U()];

		int parent = -1;
		while (b.position < b.data.length) {
			int index = b.get2U();

			if (index == 0xFFFF) {
				parent = b.get2U();
				index = b.get2U();
			}

			Component w = instances[index] = new Component();
			w.index = index;
			w.parent = parent;
			w.type = b.get1U();
			w.buttonType = b.get1U();
			w.clientCode = b.get2U();
			w.width = b.get2U();
			w.height = b.get2U();
			w.hoverParentIndex = b.get1U();

			if (w.hoverParentIndex != 0) {
				w.hoverParentIndex = ((w.hoverParentIndex - 1 << 8) + b.get1U());
			} else {
				w.hoverParentIndex = -1;
			}

			int comparatorCount = b.get1U();

			if (comparatorCount > 0) {
				w.scriptCompareType = new int[comparatorCount];
				w.scriptCompareValue = new int[comparatorCount];

				for (int n = 0; n < comparatorCount; n++) {
					w.scriptCompareType[n] = b.get1U();
					w.scriptCompareValue[n] = b.get2U();
				}
			}

			int scriptCount = b.get1U();

			if (scriptCount > 0) {
				w.script = new int[scriptCount][];
				for (int script = 0; script < scriptCount; script++) {
					int opcodeCount = b.get2U();
					w.script[script] = new int[opcodeCount];
					for (int opcode = 0; opcode < opcodeCount; opcode++) {
						w.script[script][opcode] = b.get2U();
					}
				}
			}

			if (w.type == TYPE_PARENT) {
				w.scrollHeight = b.get2U();
				w.hidden = b.get1U() == 1;

				int n = b.get1U();
				w.children = new int[n];
				w.childX = new int[n];
				w.childY = new int[n];

				for (int m = 0; m < n; m++) {
					w.children[m] = b.get2U();
					w.childX[m] = b.get2();
					w.childY[m] = b.get2();
				}
			}

			if (w.type == TYPE_UNUSED) {
				w.unusedInt = b.get2U();
				w.unusedBool = b.get1U() == 1;
			}

			if (w.type == TYPE_INVENTORY) {
				w.inventoryIndices = new int[w.width * w.height];
				w.inventoryAmount = new int[w.width * w.height];

				w.inventoryDummy = b.get1U() == 1;
				w.inventoryHasOptions = b.get1U() == 1;
				w.inventoryIsUsable = b.get1U() == 1;
				w.inventoryMarginX = b.get1U();
				w.inventoryMarginY = b.get1U();
				w.inventoryOffsetX = new int[20];
				w.inventoryOffsetY = new int[20];
				w.inventorySprite = new Sprite[20];

				for (int n = 0; n < 20; n++) {
					if (b.get1U() == 1) {
						w.inventoryOffsetX[n] = b.get2();
						w.inventoryOffsetY[n] = b.get2();

						String s = b.getString();

						if (media != null && s.length() > 0) {
							int j = s.lastIndexOf(",");
							w.inventorySprite[n] = getSprite(s.substring(0, j), media, (Integer.parseInt(s.substring(j + 1))));
						}
					}
				}

				w.inventoryOptions = new String[5];

				for (int n = 0; n < 5; n++) {
					w.inventoryOptions[n] = b.getString();

					if (w.inventoryOptions[n].length() == 0) {
						w.inventoryOptions[n] = null;
					}
				}
			}

			if (w.type == TYPE_RECT) {
				w.fill = b.get1U() == 1;
			}

			if (w.type == TYPE_TEXT || w.type == TYPE_UNUSED) {
				w.centered = b.get1U() == 1;
				w.font = fonts[b.get1U()];
				w.shadow = b.get1U() == 1;
			}

			if (w.type == TYPE_TEXT) {
				w.messageDisabled = b.getString();
				w.messageEnabled = b.getString();
			}

			if (w.type == TYPE_UNUSED || w.type == TYPE_RECT || w.type == TYPE_TEXT) {
				w.colorDisabled = b.get4();
			}

			if (w.type == TYPE_RECT || w.type == TYPE_TEXT) {
				w.colorEnabled = b.get4();
				w.hoverColor = b.get4();
			}

			if (w.type == TYPE_SPRITE) {
				String s = b.getString();

				if (media != null && s.length() > 0) {
					int j = s.lastIndexOf(",");
					w.spriteDisabled = getSprite(s.substring(0, j), media, Integer.parseInt(s.substring(j + 1)));
				}

				s = b.getString();

				if (media != null && s.length() > 0) {
					int j = s.lastIndexOf(",");
					w.spriteEnabled = getSprite(s.substring(0, j), media, Integer.parseInt(s.substring(j + 1)));
				}
			}

			if (w.type == TYPE_MODEL) {
				index = b.get1U();

				if (index != 0) {
					w.modelDisabled = getModel(((index - 1 << 8) + b.get1U()));
				}

				index = b.get1U();

				if (index != 0) {
					w.modelEnabled = getModel(((index - 1 << 8) + b.get1U()));
				}

				index = b.get1U();

				if (index != 0) {
					w.seqDisabled = (index - 1 << 8) + b.get1U();
				} else {
					w.seqDisabled = -1;
				}

				index = b.get1U();

				if (index != 0) {
					w.seqEnabled = (index - 1 << 8) + b.get1U();
				} else {
					w.seqEnabled = -1;
				}

				w.modelZoom = b.get2U();
				w.modelCameraPitch = b.get2U();
				w.modelYaw = b.get2U();
			}

			if (w.type == TYPE_INVENTORY_TEXT) {
				w.inventoryIndices = new int[w.width * w.height];
				w.inventoryAmount = new int[w.width * w.height];
				w.centered = b.get1U() == 1;

				int font = b.get1U();

				if (fonts != null) {
					w.font = fonts[font];
				}

				w.shadow = b.get1U() == 1;
				w.colorDisabled = b.get4();
				w.inventoryMarginX = b.get2();
				w.inventoryMarginY = b.get2();
				w.inventoryHasOptions = b.get1U() == 1;
				w.inventoryOptions = new String[5];

				for (int n = 0; n < 5; n++) {
					w.inventoryOptions[n] = b.getString();

					if (w.inventoryOptions[n].length() == 0) {
						w.inventoryOptions[n] = null;
					}
				}
			}

			if (w.buttonType == Component.TARGET_BUTTON || w.type == TYPE_INVENTORY) {
				w.optionCircumfix = b.getString();
				w.optionSuffix = b.getString();
				w.optionFlags = b.get2U();
			}

			if (w.buttonType == BUTTON || w.buttonType == TOGGLE_BUTTON || w.buttonType == SELECT_BUTTON || w.buttonType == PAUSE_BUTTON) {
				w.option = b.getString();

				if (w.option.length() == 0) {
					if (w.buttonType == BUTTON) {
						w.option = "Ok";
					}

					if (w.buttonType == TOGGLE_BUTTON || w.buttonType == SELECT_BUTTON) {
						w.option = "Select";
					}

					if (w.buttonType == PAUSE_BUTTON) {
						w.option = "Continue";
					}
				}
			}
		}
		spriteCache = null;
	}

	public int[] inventoryIndices;
	public int[] inventoryAmount;
	public int seqFrame;
	public int seqCycle;
	public int index;
	public int parent;
	public int type;
	public int buttonType;
	public int clientCode;
	public int width;
	public int height;
	public int[][] script;
	public int[] scriptCompareType;
	public int[] scriptCompareValue;
	public int hoverParentIndex = -1;
	public int scrollHeight;
	public int scrollAmount;
	public boolean hidden;
	public int[] children;
	public int[] childX;
	public int[] childY;
	public int unusedInt;
	public boolean unusedBool;
	public boolean inventoryDummy;
	public boolean inventoryHasOptions;
	public boolean inventoryIsUsable;
	public int inventoryMarginX;
	public int inventoryMarginY;
	public Sprite[] inventorySprite;
	public int[] inventoryOffsetX;
	public int[] inventoryOffsetY;
	public String[] inventoryOptions;
	public boolean fill;
	public boolean centered;
	public boolean shadow;
	public IndexedFont font;
	public String messageDisabled;
	public String messageEnabled;
	public int colorDisabled;
	public int colorEnabled;
	public int hoverColor;
	public Sprite spriteDisabled;
	public Sprite spriteEnabled;
	public Model modelDisabled;
	public Model modelEnabled;
	public int seqDisabled;
	public int seqEnabled;
	public int modelZoom;
	public int modelCameraPitch;
	public int modelYaw;
	public String optionCircumfix;
	public String optionSuffix;
	public int optionFlags;
	public String option;

	public Model getModel(int primaryFrame, int secondaryFrame, boolean enabled) {
		Model m = modelDisabled;

		if (enabled) {
			m = modelEnabled;
		}

		if (m == null) {
			return null;
		}

		if (primaryFrame == -1 && secondaryFrame == -1 && m.unmodifiedTriangleColor == null) {
			return m;
		}

		m = new Model(m, false, true, true, true);

		if (primaryFrame != -1 || secondaryFrame != -1) {
			m.applyGroups();
		}

		if (primaryFrame != -1) {
			m.applyFrame(primaryFrame);
		}

		if (secondaryFrame != -1) {
			m.applyFrame(secondaryFrame);
		}

		m.applyLighting(64, 768, -50, -10, -50, true);
		return m;
	}
}
