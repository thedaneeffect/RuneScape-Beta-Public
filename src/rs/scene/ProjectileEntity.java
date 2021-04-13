package rs.scene;

import rs.data.SpotAnim;

public final class ProjectileEntity extends Entity {

	public  final SpotAnim spotanim;
	public int level;
	public int sourceX;
	public int sourceY;
	public int sourceZ;
	public int baseZ;
	public int firstCycle;
	public int lastCycle;
	public int elevationPitch;
	public int arcScale;
	public int targetIndex;
	public boolean isMobile = false;
	public double x;
	public double y;
	public double z;
	public double velocityX;
	public double velocityY;
	public double velocity;
	public double velocityZ;
	public double accelerationZ;
	public int yaw;
	public int pitch;
	public int seqFrame;
	public int frameCycle;

	public ProjectileEntity(int spotanim, int target, int sourceX, int sourceY, int sourceZ, int level, int firstTick, int lastTick, int arcScale, int elevationPitch, int baseZ) {
		this.spotanim = SpotAnim.instance[spotanim];
		this.level = level;
		this.sourceX = sourceX;
		this.sourceY = sourceY;
		this.sourceZ = sourceZ;
		this.firstCycle = firstTick;
		this.lastCycle = lastTick;
		this.elevationPitch = elevationPitch;
		this.arcScale = arcScale;
		this.targetIndex = target;
		this.baseZ = baseZ;
		this.isMobile = false;
	}

	public final void setTarget(int destX, int destY, int destZ, int curTick) {
		if (!isMobile) {
			double dx = (double) (destX - sourceX);
			double dy = (double) (destY - sourceY);
			double d = Math.sqrt(dx * dx + dy * dy);
			x = (double) sourceX + dx * (double) arcScale / d;
			y = (double) sourceY + dy * (double) arcScale / d;
			z = (double) sourceZ;
		}

		double dt = (double) (lastCycle + 1 - curTick);
		velocityX = ((double) destX - x) / dt;
		velocityY = ((double) destY - y) / dt;
		velocity = Math.sqrt(velocityX * velocityX + velocityY * velocityY);

		if (!isMobile) {
			velocityZ = -velocity * Math.tan((double) elevationPitch * 0.02454369);
		}

		accelerationZ = 2.0 * ((double) destZ - z - velocityZ * dt) / (dt * dt);
	}

	public final void update(int cycle) {
		isMobile = true;
		x += velocityX * (double) cycle;
		y += velocityY * (double) cycle;
		z += (velocityZ * (double) cycle + 0.5 * accelerationZ * (double) cycle * (double) cycle);
		velocityZ += accelerationZ * (double) cycle;

		// sin : opposite / hypotenuse
		// cos : ajacent / hypotenuse
		// tan : opposite / ajacent
		yaw = ((int) (Math.atan2(velocityX, velocityY) * 325.949) + 1024) & 0x7FF;
		pitch = ((int) (Math.atan2(velocityZ, velocity) * 325.949) & 0x7FF);

		if (spotanim.seq != null) {
			frameCycle += cycle;
			while (frameCycle > spotanim.seq.frameDelay[seqFrame]) {
				frameCycle -= (spotanim.seq.frameDelay[seqFrame] + 1);
				seqFrame++;
				if (seqFrame >= spotanim.seq.frameCount) {
					seqFrame = 0;
				}
			}
		}
	}

	@Override
	public final Model getDrawModel() {
		Model sam = spotanim.getModel();
		Model m = new Model(sam, false, true, !spotanim.disposeAlpha, true);

		if (spotanim.seq != null) {
			m.applyGroups();
			m.applyFrame(spotanim.seq.primaryFrames[seqFrame]);
			m.skinTriangle = null;
			m.labelVertices = null;
		}

		m.rotatePitch(pitch);
		m.applyLighting(64, 850, -30, -50, -30, true);
		return m;
	}
}
