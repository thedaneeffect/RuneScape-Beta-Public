package rs.media;

import java.awt.*;
import java.awt.image.*;

public final class DrawArea {

	public int[] pixels;
	public int width;
	public int height;
	public BufferedImage image;

	public DrawArea(int width, int height) {
		this.width = width;
		this.height = height;
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		prepare();
	}

	public void prepare() {
		Draw2D.prepare(pixels, width, height);
	}

	public void draw(Graphics g, int x, int y) {
		g.drawImage(image, x, y, null);
	}

}
