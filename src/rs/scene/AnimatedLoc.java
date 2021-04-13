package rs.scene;

import rs.data.SeqType;
import rs.util.Node;

public final class AnimatedLoc extends Node {

	public int level;
	public int classtype;
	public int tileX;
	public int tileZ;
	public int locIndex;
	public SeqType seq;
	public int seqFrame;
	public int seqCycle;

	public AnimatedLoc(SeqType seq, int locIndex, int type, int tileX, int tileY, int level) {
		this.level = level;
		this.classtype = type;
		this.tileX = tileX;
		this.tileZ = tileY;
		this.locIndex = locIndex;
		this.seq = seq;
		this.seqFrame = -1;
		this.seqCycle = 0;
	}
}
