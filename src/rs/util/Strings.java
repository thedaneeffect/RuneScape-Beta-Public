package rs.util;

import rs.io.Buffer;

import java.util.*;

public final class Strings {

	private static final char[] BASE37_LOOKUP = {
			'_', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
			'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
			't', 'u', 'v', 'w', 'x', 'y', 'z',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
	};

	private static final char[] CHAR_TABLE = {
			' ', 'e', 't', 'a', 'o', 'i', 'h', 'n', 's', 'r',
			'd', 'l', 'u', 'm', 'w', 'c', 'y', 'f', 'g', 'p',
			'b', 'v', 'k', 'x', 'j', 'q', 'z',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			' ', '!', '?', '.', ',', ':', ';', '(', ')', '-',
			'&', '*', '\\', '\'', '@', '#', '+', '=', '\u00a3',
			'$', '%', '\"', '[', ']'
	};

	public static String read(Buffer buffer, int len) {
		int pos = 0;
		int last = -1;

		StringBuilder sb = new StringBuilder();

		for (int n = 0; n < len; n++) {
			int c = buffer.get1U();
			int value = (c >> 4) & 0xF;

			if (last == -1) {
				if (value < 13) {
					sb.append(CHAR_TABLE[value]);
				} else {
					last = value;
				}
			} else {
				sb.append(CHAR_TABLE[((last << 4) + value) - 195]);
				last = -1;
			}

			value = c & 0xF;

			if (last == -1) {
				if (value < 13) {
					sb.append(CHAR_TABLE[value]);
				} else {
					last = value;
				}
			} else {
				sb.append(CHAR_TABLE[((last << 4) + value) - 195]);
				last = -1;
			}
		}

		boolean capitalize = true;
		for (int n = 0; n < pos; n++) {
			char c = sb.charAt(n);
			if (capitalize && c >= 'a' && c <= 'z') {
				sb.setCharAt(n, (char) (c - 32));
				capitalize = false;
			}
			if (c == '.' || c == '!') {
				capitalize = true;
			}
		}
		return sb.toString();
	}

	public static void write(Buffer buffer, String string) {
		if (string.length() > 80) {
			string = string.substring(0, 80);
		}

		string = string.toLowerCase();

		int msb = -1;
		for (int n = 0; n < string.length(); n++) {
			char c = string.charAt(n);
			int lsb = 0;

			for (int m = 0; m < CHAR_TABLE.length; m++) {
				if (c == CHAR_TABLE[m]) {
					lsb = m;
					break;
				}
			}

			if (lsb > 12) {
				lsb += 195;
			}

			if (msb == -1) {
				if (lsb < 13) {
					msb = lsb;
				} else {
					buffer.put1(lsb);
				}
			} else if (lsb < 13) {
				buffer.put1((msb << 4) + lsb);
				msb = -1;
			} else {
				buffer.put1((msb << 4) + (lsb >> 4));
				msb = lsb & 0xf;
			}
		}

		if (msb != -1) {
			buffer.put1(msb << 4);
		}
	}

	public static long getBase37(String string) {
		string = string.trim();
		long l = 0L;

		for (int i = 0; i < string.length() && i < 12; i++) {
			char c = string.charAt(i);
			l *= 37L;

			if (c >= 'A' && c <= 'Z') {
				l += (c + 1) - 'A';
			} else if (c >= 'a' && c <= 'z') {
				l += (c + 1) - 'a';
			} else if (c >= '0' && c <= '9') {
				l += (c + 27) - '0';
			}
		}
		return l;
	}

	public static String fromBase37(long value) {
		// >= 37 to the 12th power
		if (value < 0L || value >= 6582952005840035281L) {
			return "invalid_name";
		}

		int len = 0;
		char[] chars = new char[12];
		while (value != 0L) {
			long l1 = value;
			value /= 37L;
			chars[11 - len++] = BASE37_LOOKUP[(int) (l1 - value * 37L)];
		}
		return new String(chars, 12 - len, len);
	}

	public static int getHash(String string) {
		int hash = 0;
		string = string.toUpperCase();
		for (int i = 0; i < string.length(); i++) {
			hash = ((hash * 61) + string.charAt(i)) - 32;
		}
		return hash;
	}


	public static String getClean(String string) {
		if (string.length() == 0) {
			return string;
		}

		string = string.toLowerCase().trim();
		StringBuilder sb = new StringBuilder();

		for (int n = 0; n < string.length(); n++) {
			if (n >= 12) {
				break;
			}
			char c = string.charAt(n);
			if (isLowercaseAlpha(c) || isNumeral(c)) {
				sb.append(c);
			} else {
				sb.append('_');
			}
		}

		string = sb.toString();

		while (string.charAt(0) == '_') {
			string = string.substring(1);
		}

		while (string.charAt(string.length() - 1) == '_') {
			string = string.substring(0, string.length() - 1);
		}
		return string;
	}

	public static String toStartCase(String s) {
		if (s.length() > 0) {
			char[] chars = s.toCharArray();

			for (int n = 0; n < chars.length; n++) {
				if (chars[n] == '_') {
					chars[n] = ' ';

					// next letter will be upper case
					int m = n + 1;
					if (m < chars.length && isLowercaseAlpha(chars[m])) {
						chars[m] = (char) ((chars[m] + 'A') - 'a');
					}
				}
			}

			// First letter always upper case
			if (isLowercaseAlpha(chars[0])) {
				chars[0] = (char) ((chars[0] + 'A') - 'a');
			}
			return new String(chars);
		}
		return s;
	}

	public static String toSentence(String string) {
		char[] chars = string.toLowerCase().toCharArray();

		boolean capitalize = true;
		for (int n = 0; n < chars.length; n++) {
			char c = chars[n];

			if (capitalize && isLowercaseAlpha(c)) {
				chars[n] -= 32;
				capitalize = false;
			}

			if (c == '.' || c == '!') {
				capitalize = true;
			}
		}
		return new String(chars);
	}

	public static String toAsterisks(String string) {
		char[] c = new char[string.length()];
		Arrays.fill(c, '*');
		return new String(c);
	}

	public static boolean isSymbol(char c) {
		return !isAlpha(c) && !isNumeral(c);
	}

	public static boolean isNotLowercaseAlpha(char c) {
		if (c < 'a' || c > 'z') {
			return true;
		}
		return c == 'v' || c == 'x' || c == 'j' || c == 'q' || c == 'z';
	}

	public static boolean isAlpha(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
	}

	public static boolean isNumeral(char c) {
		return c >= '0' && c <= '9';
	}

	public static boolean isLowercaseAlpha(char c) {
		return c >= 'a' && c <= 'z';
	}

	public static boolean isUppercaseAlpha(char c) {
		return c >= 'A' && c <= 'Z';
	}

	public static boolean isASCII(char c) {
		return c >= ' ' && c <= '~';
	}

}
