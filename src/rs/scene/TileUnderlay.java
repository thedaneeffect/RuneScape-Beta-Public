package rs.scene;

// should I call this something else?
public final class TileUnderlay {

	public int southwestColor;
	public int southeastColor;
	public int northeastColor;
	public int northwestColor;
	public int textureIndex;
	public boolean isFlat = true;
	public int rgb;

	public TileUnderlay(int southwestColor, int southeastColor, int northeastColor, int northwestColor, int textureIndex, int rgb, boolean flat) {
		this.southwestColor = southwestColor;
		this.southeastColor = southeastColor;
		this.northeastColor = northeastColor;
		this.northwestColor = northwestColor;
		this.textureIndex = textureIndex;
		this.rgb = rgb;
		this.isFlat = flat;
	}
}
