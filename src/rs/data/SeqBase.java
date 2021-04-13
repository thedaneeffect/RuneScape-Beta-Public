package rs.data;

import rs.io.Archive;
import rs.io.Buffer;

public class SeqBase {

	public static SeqBase[] instance;
	public int length;
	public int id;
	public int[] types;
	public int[][] groupLabels;

	public static void load(Archive a) {
		Buffer head = new Buffer(a.get("base_head.dat", null));
		Buffer type = new Buffer(a.get("base_type.dat", null));
		Buffer label = new Buffer(a.get("base_label.dat", null));

		int total = head.get2U();

		instance = new SeqBase[head.get2U() + 1];

		for (int i = 0; i < total; i++) {
			int index = head.get2U();

			int length = head.get1U();
			int[] transformTypes = new int[length];
			int[][] groups = new int[length][];

			for (int n = 0; n < length; n++) {
				transformTypes[n] = type.get1U();

				int groupCount = label.get1U();
				groups[n] = new int[groupCount];

				for (int g = 0; g < groupCount; g++) {
					groups[n][g] = label.get1U();
				}
			}

			instance[index] = new SeqBase();
			instance[index].id = index;
			instance[index].length = length;
			instance[index].types = transformTypes;
			instance[index].groupLabels = groups;
		}
	}
}
