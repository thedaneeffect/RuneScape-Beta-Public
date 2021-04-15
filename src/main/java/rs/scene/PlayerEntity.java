package rs.scene;

import rs.Game;
import rs.data.*;
import rs.io.Buffer;
import rs.util.Cache;
import rs.util.Strings;

public final class PlayerEntity extends PathingEntity {

	public static Cache models = new Cache(200);

	public String name;
	public boolean visible = false;
	public int gender;
	public int headicons;
	public int[] appearanceIndices = new int[13];
	public int[] appearanceColors = new int[5];
	public int level;
	public long uid;
	public int sceneY;
	public int locFirstCycle;
	public int locLastCycle;
	public int locSceneX;
	public int locSceneY;
	public int locSceneZ;
	public Model locModel;
	public int locMinTileX;
	public int locMinTileZ;
	public int locMaxTileX;
	public int locMaxTileZ;
	public boolean lowmemory = false;

	public final void read(Buffer b) {
		b.position = 0;
		gender = b.get1U();
		headicons = b.get1U();

		for (int n = 0; n < 13; n++) {
			int msb = b.get1U();

			if (msb == 0) {
				appearanceIndices[n] = 0;
			} else {
				appearanceIndices[n] = (msb << 8) + b.get1U();
			}
		}

		for (int n = 0; n < 5; n++) {
			int i = b.get1U();

			if (i < 0 || i >= IdkType.APPEARANCE_COLORS[n].length) {
				i = 0;
			}

			appearanceColors[n] = i;
		}

		standSeq = b.get2U();
		turnSeq = b.get2U();
		walkSeq = b.get2U();
		turnAroundSeq = b.get2U();
		turnRightSeq = b.get2U();
		turnLeftSeq = b.get2U();

		name = Strings.toStartCase(Strings.fromBase37(b.get8()));
		level = b.get1U();

		visible = true;
		uid = 0L;

		for (int n = 0; n < 12; n++) {
			uid <<= 4;

			if (appearanceIndices[n] >= 0x100) {
				uid += (long) (appearanceIndices[n] - 0x100);
			}
		}

		for (int n = 0; n < 5; n++) {
			uid <<= 3;
			uid += (long) appearanceColors[n];
		}

		uid <<= 1;
		uid += (long) gender;
	}

	@Override
	public final Model getDrawModel() {
		if (!visible) {
			return null;
		}

		Model model = getModel();

		height = model.maxBoundY;

		if (lowmemory) {
			return model;
		}

		if (spotanimIndex != -1 && spotanimFrame != -1) {
			SpotAnimType s = SpotAnimType.instance[spotanimIndex];
			Model m = new Model(s.getModel(), false, true, !s.disposeAlpha, true);
			m.translate(0, -spotanimOffsetY, 0);
			m.applyGroups();
			m.applyFrame(s.seq.primaryFrames[spotanimFrame]);
			m.skinTriangle = null;
			m.labelVertices = null;
			m.applyLighting(64, 850, -30, -50, -30, true);

			model = new Model(new Model[]{model, m}, 2, true, 0xCAFEBABE);
		}

		if (locModel != null) {
			if (Game.clientclock >= locLastCycle) {
				locModel = null;
			}

			if (Game.clientclock >= locFirstCycle && Game.clientclock < locLastCycle) {
				Model m = locModel;
				m.translate(locSceneX - sceneX, locSceneY - sceneY, locSceneZ - sceneZ);

				if (dstYaw == 512) {
					m.rotateCounterClockwise();
					m.rotateCounterClockwise();
					m.rotateCounterClockwise();
				} else if (dstYaw == 1024) {
					m.rotateCounterClockwise();
					m.rotateCounterClockwise();
				} else if (dstYaw == 1536) {
					m.rotateCounterClockwise();
				}

				// merge player model with loc model (like chris said lel)
				model = new Model(new Model[]{model, m}, 2, true, 0xFACEFAC);

				if (dstYaw == 512) {
					m.rotateCounterClockwise();
				} else if (dstYaw == 1024) {
					m.rotateCounterClockwise();
					m.rotateCounterClockwise();
				} else if (dstYaw == 1536) {
					m.rotateCounterClockwise();
					m.rotateCounterClockwise();
					m.rotateCounterClockwise();
				}

				m.translate(sceneX - locSceneX, sceneY - locSceneY, sceneZ - locSceneZ);
			}
		}
		return model;
	}

	public final Model getModel() {
		long bitset = uid;
		int primaryFrame = -1;
		int secondaryFrame = -1;
		int shieldOverride = -1;
		int weaponOverride = -1;

		if (primarySeq >= 0 && primarySeqDelay == 0) {
			SeqType s = SeqType.instances[primarySeq];
			primaryFrame = s.primaryFrames[primarySeqFrame];

			if (secondarySeq >= 0 && secondarySeq != standSeq) {
				secondaryFrame = SeqType.instances[secondarySeq].primaryFrames[secondarySeqFrame];
			}

			if (s.shieldOverride >= 0) {
				shieldOverride = s.shieldOverride;
				bitset += (long) (shieldOverride - appearanceIndices[5] << 8);
			}

			if (s.weaponOverride >= 0) {
				weaponOverride = s.weaponOverride;
				bitset += (long) (shieldOverride - appearanceIndices[3] << 16);
			}
		} else if (secondarySeq >= 0) {
			primaryFrame = SeqType.instances[secondarySeq].primaryFrames[secondarySeqFrame];
		}

		Model m = (Model) models.get(bitset);

		if (m == null) {
			Model[] models = new Model[13];
			int n = 0;

			for (int i = 0; i < 13; i++) {
				int index = appearanceIndices[i];

				if (weaponOverride >= 0 && i == 3) {
					index = weaponOverride;
				}

				if (shieldOverride >= 0 && i == 5) {
					index = shieldOverride;
				}

				if (index >= 0x100 && index < 0x200) {
					models[n++] = IdkType.instances[index - 0x100].getModel();
				}

				if (index >= 0x200) {
					ObjType o = ObjType.get(index - 0x200);
					Model model = o.getWornModel(gender);

					if (model != null) {
						models[n++] = model;
					}
				}
			}

			m = new Model(models, n);

			for (int part = 0; part < 5; part++) {
				if (appearanceColors[part] != 0) {
					m.recolor(IdkType.APPEARANCE_COLORS[part][0], IdkType.APPEARANCE_COLORS[part][appearanceColors[part]]);

					if (part == 1) {
						m.recolor(IdkType.BEARD_COLORS[0], IdkType.BEARD_COLORS[appearanceColors[part]]);
					}
				}
			}

			m.applyGroups();
			m.applyLighting(64, 850, -30, -50, -30, true);
			PlayerEntity.models.put(bitset, m);
		}

		if (lowmemory) {
			return m;
		}

		m = new Model(m, true);

		if (primaryFrame != -1 && secondaryFrame != -1) {
			m.applyFrames(primaryFrame, secondaryFrame, SeqType.instances[primarySeq].labelGroups);
		} else if (primaryFrame != -1) {
			m.applyFrame(primaryFrame);
		}

		m.calculateYBoundaries();
		m.skinTriangle = null;
		m.labelVertices = null;
		return m;
	}

	public final Model getHeadModel() {
		if (!visible) {
			return null;
		}

		Model[] models = new Model[13];
		int count = 0;
		for (int n = 0; n < 13; n++) {
			int i = appearanceIndices[n];

			if (i >= 0x100 && i < 0x200) {
				models[count++] = IdkType.instances[i - 256].getHeadModel();
			}

			if (i >= 0x200) {
				Model m = ObjType.get(i - 512).getHeadModel(gender);

				if (m != null) {
					models[count++] = m;
				}
			}
		}

		Model m = new Model(models, count);

		for (int n = 0; n < 5; n++) {
			if (appearanceColors[n] != 0) {
				m.recolor((IdkType.APPEARANCE_COLORS[n][0]), (IdkType.APPEARANCE_COLORS[n][appearanceColors[n]]));

				if (n == 1) {
					m.recolor(IdkType.BEARD_COLORS[0], (IdkType.BEARD_COLORS[appearanceColors[n]]));
				}
			}
		}
		return m;
	}

	public final boolean isVisible() {
		return visible;
	}
}
