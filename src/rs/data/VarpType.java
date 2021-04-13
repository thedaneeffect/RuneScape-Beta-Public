package rs.data;

import rs.io.Archive;
import rs.io.Buffer;

public class VarpType {

	public static int total;
	public static VarpType[] instance;
	public int type;

	public static void load(Archive a) {
		Buffer b = new Buffer(a.get("varp.dat", null));
		total = b.get2U();

		if (instance == null) {
			instance = new VarpType[total];
		}

		for (int n = 0; n < total; n++) {
			if (instance[n] == null) {
				instance[n] = new VarpType();
			}
			instance[n].read(b);
		}
	}

	public void read(Buffer b) {
		for (;;) {
			int opcode = b.get1U();

			if (opcode == 0) {
				break;
			}

			if (opcode == 5) {
				type = b.get2U();
			} else {
				System.out.println("Error unrecognised config code: " + opcode);
			}
		}
	}
}
