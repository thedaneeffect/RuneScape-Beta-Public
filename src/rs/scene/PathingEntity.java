package rs.scene;

import rs.data.SeqType;

public class PathingEntity extends Entity {

	public int sceneX;
	public int sceneZ;
	public int yaw;

	public int renderPadding;
	public int size = 1;

	public int walkSeq = -1;
	public int turnAroundSeq = -1;
	public int turnRightSeq = -1;
	public int turnLeftSeq = -1;
	public int standSeq = -1;
	public int turnSeq = -1;

	public String spoken;
	public int spokenLife = 100;
	public int spokenColor;
	public int spokenEffect;

	public int damageTaken;
	public int damageType;

	public int lastCombatCycle = -1000;

	public int currentHealth;
	public int maxHealth;

	public int targetEntity = -1;

	public int focusX;
	public int focusY;

	public int secondarySeq = -1;
	public int secondarySeqFrame;
	public int secondarySeqCycle;

	public int primarySeq = -1;
	public int primarySeqFrame;
	public int primarySeqCycle;
	public int primarySeqDelay;
	public int primarySeqPlays;

	public int spotanimIndex = -1;
	public int spotanimFrame;
	public int spotanimCycle;
	public int lastSpotanimCycle;
	public int spotanimOffsetY;

	public int srcTileX;
	public int dstTileX;
	public int srcTileY;
	public int dstTileY;

	public int firstMoveCycle;
	public int lastMoveCycle;

	public int faceDirection;
	public boolean remove = false;
	public int height;
	public int dstYaw;

	public int pathStepCount;
	public int[] pathX = new int[10];
	public int[] pathY = new int[10];

	public int catchupCycles;

	public final void setPosition(int x, int y) {
		if (x != pathX[0] || y != pathY[0]) {
			if (primarySeq != -1 && SeqType.instances[primarySeq].priority <= 1) {
				primarySeq = -1;
			}

			if (pathStepCount < 9) {
				pathStepCount++;
				for (int n = pathStepCount; n > 0; n--) {
					pathX[n] = pathX[n - 1];
					pathY[n] = pathY[n - 1];
				}
			} else {
				for (int n = 8; n > 0; n--) {
					pathX[n] = pathX[n - 1];
					pathY[n] = pathY[n - 1];
				}
			}
			pathX[0] = x;
			pathY[0] = y;
		}
	}

	public final void moveBy(int dx, int dy) {
		if (dx != 0 || dy != 0) {
			int x = pathX[0] + dx;
			int y = pathY[0] + dy;

			if (primarySeq != -1 && SeqType.instances[primarySeq].priority <= 1) {
				primarySeq = -1;
			}

			if (pathStepCount < 9) {
				pathStepCount++;
				for (int n = pathStepCount; n > 0; n--) {
					pathX[n] = pathX[n - 1];
					pathY[n] = pathY[n - 1];
				}
			} else {
				for (int n = 8; n > 0; n--) {
					pathX[n] = pathX[n - 1];
					pathY[n] = pathY[n - 1];
				}
			}
			pathX[0] = x;
			pathY[0] = y;
		}
	}
}
