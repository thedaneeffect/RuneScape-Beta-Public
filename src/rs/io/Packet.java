package rs.io;

public final class Packet {

	public static final int SIZE_VAR_BYTE = -1;
	public static final int SIZE_VAR_SHORT = -2;

	public static final int[] SIZE = new int[256];

	static {
		SIZE[6] = SIZE_VAR_SHORT;
		SIZE[9] = SIZE_VAR_SHORT;
		SIZE[22] = 4;
		SIZE[27] = 2;
		SIZE[35] = 2;
		SIZE[42] = 4;
		SIZE[44] = 2;
		SIZE[47] = SIZE_VAR_SHORT;
		SIZE[48] = SIZE_VAR_SHORT;
		SIZE[51] = 2;
		SIZE[53] = 2;
		SIZE[59] = 6;
		SIZE[60] = 15;
		SIZE[61] = 3;
		SIZE[68] = SIZE_VAR_SHORT;
		SIZE[85] = SIZE_VAR_BYTE;
		SIZE[90] = 5;
		SIZE[95] = 2;
		SIZE[98] = 6;
		SIZE[100] = SIZE_VAR_SHORT;
		SIZE[107] = 6;
		SIZE[114] = 9;
		SIZE[116] = 2;
		SIZE[119] = 6;
		SIZE[123] = 2;
		SIZE[124] = 2;
		SIZE[127] = 3;
		SIZE[138] = 4;
		SIZE[149] = 4;
		SIZE[153] = 14;
		SIZE[156] = SIZE_VAR_BYTE;
		SIZE[159] = SIZE_VAR_BYTE;
		SIZE[171] = 2;
		SIZE[175] = 4;
		SIZE[181] = 4;
		SIZE[188] = 2;
		SIZE[197] = SIZE_VAR_SHORT;
		SIZE[210] = 2;
		SIZE[217] = 1;
		SIZE[220] = 3;
		SIZE[225] = SIZE_VAR_SHORT;
		SIZE[227] = SIZE_VAR_SHORT;
		SIZE[229] = SIZE_VAR_BYTE;
		SIZE[232] = 4;
		SIZE[234] = 3;
		SIZE[235] = 3;
		SIZE[248] = 4;
		SIZE[250] = 3;
		SIZE[254] = 1;
	}

	private Packet() {

	}

}
