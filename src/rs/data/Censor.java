package rs.data;

import rs.io.Archive;
import rs.io.Buffer;
import rs.util.Strings;

public class Censor {

	public static void load(Archive a) {
		Buffer fragmentsenc = new Buffer(a.get("fragmentsenc.txt"));
		Buffer badenc = new Buffer(a.get("badenc.txt"));
		Buffer domainenc = new Buffer(a.get("domainenc.txt"));
		Buffer tldlist = new Buffer(a.get("tldlist.txt"));
		unpack(fragmentsenc, badenc, domainenc, tldlist);
	}

	public static void unpack(Buffer fragmentsenc, Buffer badenc, Buffer domainenc, Buffer tldlist) {
		loadBadEnc(badenc);
		loadDomainEnc(domainenc);
		loadFragmentsEnc(fragmentsenc);
		loadTldList(tldlist);
	}

	public static void loadTldList(Buffer b) {
		int length = b.get4();
		tlds = new char[length][];
		tldTypes = new int[length];

		for (int n = 0; n < length; n++) {
			tldTypes[n] = b.get1U();

			char[] string = new char[b.get1U()];
			for (int k = 0; k < string.length; k++) {
				string[k] = (char) b.get1U();
			}

			tlds[n] = string;
		}
	}

	public static void loadBadEnc(Buffer b) {
		int length = b.get4();
		bads = new char[length][];
		badCombinations = new byte[length][][];
		loadBadEnc(b, badCombinations, bads);
	}

	public static void loadDomainEnc(Buffer b) {
		int i = b.get4();
		domains = new char[i][];
		loadDomainEnc(b, domains);
	}

	public static void loadFragmentsEnc(Buffer b) {
		fragments = new int[b.get4()];
		for (int i = 0; i < fragments.length; i++) {
			fragments[i] = b.get2U();
		}
	}

	public static char getChar(int index) {
		if (index >= 0 && index <= 27) {
			return (char) ('a' + index);
		} else if (index == 28) {
			return '\'';
		} else if (index >= 29 && index <= 38) {
			return (char) ('0' + index);
		}
		return '\n';
	}

	public static void loadBadEnc(Buffer b, byte badCombinations[][][], char bads[][]) {
		for (int n = 0; n < bads.length; n++) {
			char[] chars = new char[b.get1U()];
			for (int i = 0; i < chars.length; i++) {
				chars[i] = (char) b.get1U();
			}
			bads[n] = chars;

			byte combo[][] = new byte[b.get1U()][2];

			for (byte[] ba : combo) {
				ba[0] = (byte) b.get1U();
				ba[1] = (byte) b.get1U();
			}

			if (combo.length > 0) {
				badCombinations[n] = combo;
			}
		}

	}

	public static void loadDomainEnc(Buffer b, char domains[][]) {
		for (int n = 0; n < domains.length; n++) {
			char[] string = new char[b.get1U()];
			for (int k = 0; k < string.length; k++) {
				string[k] = (char) b.get1U();
			}
			domains[n] = string;
		}

	}

	public static void trimWhitespaces(char[] chars) {
		int off = 0;

		for (int n = 0; n < chars.length; n++) {
			// allow all ascii, spaces, newlines, tabs, and 2 currency symbols.
			if (isValid(chars[n])) {
				chars[off] = chars[n];
			} // if it's bad, replace with a space
			else {
				chars[off] = ' ';
			}

			// increase position only if we are just starting or don't have a
			// space ahead of us.
			if (off == 0 || chars[off] != ' ' || chars[off - 1] != ' ') {
				off++;
			}
		}

		// replace the wrest with spaces.
		for (int n = off; n < chars.length; n++) {
			chars[n] = ' ';
		}
	}

	public static boolean isValid(char c) {
		return c >= ' ' && c <= 127 || c == ' ' || c == '\n' || c == '\t' || c == '\u00A3' || c == '\u20AC';
	}

	public static String getFiltered(String s) {
		char[] chars = s.toCharArray();
		trimWhitespaces(chars);

		String trimmed = new String(chars).trim();
		chars = trimmed.toLowerCase().toCharArray();

		filterTlds(chars);
		filterBad(chars);
		filterDomains(chars);
		filterNumFragments(chars);

		String lowercase = trimmed.toLowerCase();

		for (String word : WHITELIST) {
			for (int index = -1; (index = lowercase.indexOf(word, index + 1)) != -1;) {
				char[] wchars = word.toCharArray();
				System.arraycopy(wchars, 0, chars, index, wchars.length);
			}
		}

		replaceUppercases(trimmed.toCharArray(), chars);
		formatUppercases(chars);
		return new String(chars).trim();
	}

	public static void replaceUppercases(char[] from, char[] to) {
		for (int i = 0; i < from.length; i++) {
			if (to[i] != '*' && Strings.isUppercaseAlpha(from[i])) {
				to[i] = from[i];
			}
		}
	}

	public static void formatUppercases(char[] chars) {
		boolean flag = true;
		for (int n = 0; n < chars.length; n++) {
			char c = chars[n];

			if (Strings.isAlpha(c)) {
				if (flag) {
					if (Strings.isLowercaseAlpha(c)) {
						flag = false;
					}
				} else if (Strings.isUppercaseAlpha(c)) {
					chars[n] = (char) ((c + 'a') - 'A');
				}
			} else {
				flag = true;
			}
		}
	}

	public static void filterBad(char[] chars) {
		for (int iterations = 0; iterations < 2; iterations++) {
			for (int n = bads.length - 1; n >= 0; n--) {
				filterBad(chars, bads[n], badCombinations[n]);
			}
		}
	}

	public static void filterDomains(char[] chars) {
		char ac1[] = (char[]) chars.clone();
		filterBad(ac1, new char[]{'(', 'a', ')'}, null);

		char ac3[] = (char[]) chars.clone();
		filterBad(ac3, new char[]{'d', 'o', 't'}, null);

		for (int n = domains.length - 1; n >= 0; n--) {
			filterDomain(chars, domains[n], ac3, ac1);
		}
	}

	public static void filterDomain(char[] chars, char[] domain, char[] filteredDots, char[] filteredAts) {
		if (domain.length > chars.length) {
			return;
		}

		int stride;
		for (int start = 0; start <= chars.length - domain.length; start += stride) {
			int end = start;
			int off = 0;
			stride = 1;
			while (end < chars.length) {
				int charLen = 0;
				char b = chars[end];
				char c = '\0';

				if (end + 1 < chars.length) {
					c = chars[end + 1];
				}

				if (off < domain.length && (charLen = getEmulatedDomainCharLen(domain[off], b, c)) > 0) {
					end += charLen;
					off++;
					continue;
				}

				if (off == 0) {
					break;
				}

				if ((charLen = getEmulatedDomainCharLen(domain[off - 1], b, c)) > 0) {
					end += charLen;
					if (off == 1) {
						stride++;
					}
					continue;
				}
				if (off >= domain.length || !Strings.isSymbol(b)) {
					break;
				}
				end++;
			}

			if (off >= domain.length) {
				boolean bad = false;
				int status0 = getDomainAtFilterStatus(start, chars, filteredAts);
				int status1 = getDomainDotFilterStatus(end - 1, chars, filteredDots);

				if (status0 > 2 || status1 > 2) {
					bad = true;
				}

				if (bad) {
					for (int i = start; i < end; i++) {
						chars[i] = '*';
					}
				}
			}
		}

	}

	public static int getDomainAtFilterStatus(int end, char a[], char b[]) {
		// i aint got no type
		if (end == 0) {
			return 2;
		}

		// scan until it finds an @ or a non-symbol
		for (int i = end - 1; i >= 0; i--) {
			if (!Strings.isSymbol(a[i])) {
				break;
			}

			if (a[i] == '@') {
				return 3;
			}
		}

		// scan for series of asterisks
		int asteriskCount = 0;
		for (int i = end - 1; i >= 0; i--) {
			if (!Strings.isSymbol(b[i])) {
				break;
			}

			if (b[i] == '*') {
				asteriskCount++;
			}
		}

		if (asteriskCount >= 3) {
			return 4;
		}

		// return whether the last char is a symbol or not.
		return !Strings.isSymbol(a[end - 1]) ? 0 : 1;
	}

	public static int getDomainDotFilterStatus(int start, char a[], char b[]) {
		// out of bounds, no type
		if (start + 1 == a.length) {
			return 2;
		}

		// scan until it finds a period or comma or a non-symbol
		for (int i = start + 1; i < a.length; i++) {
			if (!Strings.isSymbol(a[i])) {
				break;
			}

			if (a[i] == '.' || a[i] == ',') {
				return 3;
			}
		}

		// scan for series of asterisks
		int asteriskCount = 0;
		for (int i = start + 1; i < a.length; i++) {
			if (!Strings.isSymbol(b[i])) {
				break;
			}

			if (b[i] == '*') {
				asteriskCount++;
			}
		}

		if (asteriskCount >= 3) {
			return 4;
		}

		// return whether the first char is a symbol or not
		return !Strings.isSymbol(a[start + 1]) ? 0 : 1;
	}

	public static void filterTlds(char[] chars) {
		char filteredDot[] = (char[]) chars.clone();
		filterBad(filteredDot, new char[]{'d', 'o', 't'}, null);

		char filteredSlash[] = (char[]) chars.clone();
		filterBad(filteredSlash, new char[]{'s', 'l', 'a', 's', 'h'}, null);

		for (int n = 0; n < tlds.length; n++) {
			filterTld(chars, tlds[n], tldTypes[n], filteredDot, filteredSlash);
		}
	}

	public static void filterTld(char chars[], char tld[], int type, char filteredDot[], char filteredSlash[]) {
		if (tld.length > chars.length) {
			return;
		}

		int stride;
		for (int start = 0; start <= chars.length - tld.length; start += stride) {
			int end = start;
			int off = 0;
			stride = 1;

			while (end < chars.length) {
				int charLen = 0;
				char b = chars[end];
				char c = '\0';

				if (end + 1 < chars.length) {
					c = chars[end + 1];
				}

				if (off < tld.length && (charLen = getEmulatedDomainCharLen(tld[off], b, c)) > 0) {
					end += charLen;
					off++;
					continue;
				}

				if (off == 0) {
					break;
				}

				if ((charLen = getEmulatedDomainCharLen(tld[off - 1], b, c)) > 0) {
					end += charLen;
					if (off == 1) {
						stride++;
					}
					continue;
				}

				if (off >= tld.length || !Strings.isSymbol(b)) {
					break;
				}

				end++;
			}

			if (off >= tld.length) {
				boolean bad = false;
				int status0 = getTldDotFilterStatus(chars, start, filteredDot);
				int status1 = getTldSlashFilterStatus(chars, end - 1, filteredSlash);

				// status0 number meanings
				// 0 = found no symbols
				// 1 = found symbol but not comma, period, or >= 3 asterisks
				// 2 = start pos was 0
				// 3 = found comma or period
				// 4 = found a string of 3 or more asterisks
				// status1 number meanings
				// 0 = found no symbols
				// 1 = found symbol but not comma, period, or >= 5 asterisks
				// 2 = end pos was 0
				// 3 = found forward or backwards slash
				// 4 = found a string of 5 or more asterisks
				if (type == 1 && status0 > 0 && status1 > 0) {
					bad = true;
				}
				if (type == 2 && (status0 > 2 && status1 > 0 || status0 > 0 && status1 > 2)) {
					bad = true;
				}
				if (type == 3 && status0 > 0 && status1 > 2) {
					bad = true;
				}

				if (bad) {
					int first = start;
					int last = end - 1;

					// if we found comma, period, or a string of 3 or more
					// asterisks in our filteredDot[]
					if (status0 > 2) {
						if (status0 == 4) {
							boolean findStart = false;
							for (int i = first - 1; i >= 0; i--) {
								if (findStart) {
									if (filteredDot[i] != '*') {
										break;
									}
									first = i;
								} else if (filteredDot[i] == '*') {
									first = i;
									findStart = true;
								}
							}
						}

						boolean findStart = false;
						for (int i = first - 1; i >= 0; i--) {
							if (findStart) {
								if (Strings.isSymbol(chars[i])) {
									break;
								}
								first = i;
							} else if (!Strings.isSymbol(chars[i])) {
								findStart = true;
								first = i;
							}
						}
					}

					// we found a slash or string of 5 or more asterisks in our
					// filteredSlash[]
					if (status1 > 2) {
						// there was a string of asterisks.
						if (status1 == 4) {
							boolean findLast = false;
							for (int i = last + 1; i < chars.length; i++) {
								if (findLast) {
									if (filteredSlash[i] != '*') {
										break;
									}
									last = i;
								} else if (filteredSlash[i] == '*') {
									last = i;
									findLast = true;
								}
							}
						}

						boolean findLast = false;
						for (int i = last + 1; i < chars.length; i++) {
							if (findLast) {
								if (Strings.isSymbol(chars[i])) {
									break;
								}
								last = i;
							} else if (!Strings.isSymbol(chars[i])) {
								findLast = true;
								last = i;
							}
						}
					}

					// finally! censor that shit!
					for (int i = first; i <= last; i++) {
						chars[i] = '*';
					}
				}
			}
		}
	}

	public static int getTldDotFilterStatus(char chars[], int start, char filteredDot[]) {
		if (start == 0) {
			return 2;
		}

		for (int i = start - 1; i >= 0; i--) {
			if (!Strings.isSymbol(chars[i])) {
				break;
			}
			if (chars[i] == ',' || chars[i] == '.') {
				return 3;
			}
		}

		int asteriskCount = 0;
		for (int i = start - 1; i >= 0; i--) {
			if (!Strings.isSymbol(filteredDot[i])) {
				break;
			}

			if (filteredDot[i] == '*') {
				asteriskCount++;
			}
		}

		if (asteriskCount >= 3) {
			return 4;
		}
		return !Strings.isSymbol(chars[start - 1]) ? 0 : 1;
	}

	public static int getTldSlashFilterStatus(char chars[], int end, char filteredSlash[]) {
		if (end + 1 == chars.length) {
			return 2;
		}

		for (int j = end + 1; j < chars.length; j++) {
			if (!Strings.isSymbol(chars[j])) {
				break;
			}
			if (chars[j] == '\\' || chars[j] == '/') {
				return 3;
			}
		}

		int asterisks = 0;
		for (int l = end + 1; l < chars.length; l++) {
			if (!Strings.isSymbol(filteredSlash[l])) {
				break;
			}
			if (filteredSlash[l] == '*') {
				asterisks++;
			}
		}

		if (asterisks >= 5) {
			return 4;
		}
		return !Strings.isSymbol(chars[end + 1]) ? 0 : 1;
	}

	public static void filterBad(char[] chars, char[] fragment, byte badCombinations[][]) {
		if (fragment.length > chars.length) {
			return;
		}

		int stride;
		for (int start = 0; start <= chars.length - fragment.length; start += stride) {
			int end = start;
			int fragOff = 0;
			int iterations = 0;
			stride = 1;

			boolean isSymbol = false;
			boolean isEmulated = false;
			boolean isNumeral = false;

			while (end < chars.length && (!isEmulated || !isNumeral)) {
				int charLen = 0;
				char b = chars[end];
				char c = '\0';

				if (end + 1 < chars.length) {
					c = chars[end + 1];
				}

				if (fragOff < fragment.length && (charLen = getEmulatedBadCharLen(fragment[fragOff], b, c)) > 0) {
					if (charLen == 1 && Strings.isNumeral(b)) {
						isEmulated = true;
					}

					if (charLen == 2 && (Strings.isNumeral(b) || Strings.isNumeral(c))) {
						isEmulated = true;
					}

					end += charLen;
					fragOff++;
					continue;
				}

				if (fragOff == 0) {
					break;
				}

				if ((charLen = getEmulatedBadCharLen(fragment[fragOff - 1], b, c)) > 0) {
					end += charLen;

					if (fragOff == 1) {
						stride++;
					}

					continue;
				}

				if (fragOff >= fragment.length || !Strings.isNotLowercaseAlpha(b)) {
					break;
				}

				if (Strings.isSymbol(b) && b != '\'') {
					isSymbol = true;
				}

				if (Strings.isNumeral(b)) {
					isNumeral = true;
				}

				end++;

				if ((++iterations * 100) / (end - start) > 90) {
					break;
				}
			}

			if (fragOff >= fragment.length && (!isEmulated || !isNumeral)) {
				boolean bad = true;

				if (!isSymbol) {
					char a = ' ';

					if (start - 1 >= 0) {
						a = chars[start - 1];
					}

					char b = ' ';

					if (end < chars.length) {
						b = chars[end];
					}

					if (badCombinations != null && comboMatches(getIndex(a), getIndex(b), badCombinations)) {
						bad = false;
					}
				} else {
					boolean badCurrent = false;
					boolean badNext = false;

					// if the previous is out of range or a symbol
					if (start - 1 < 0 || Strings.isSymbol(chars[start - 1]) && chars[start - 1] != '\'') {
						badCurrent = true;
					}

					// if the current is out of range or a symbol
					if (end >= chars.length || Strings.isSymbol(chars[end]) && chars[end] != '\'') {
						badNext = true;
					}

					if (!badCurrent || !badNext) {
						boolean good = false;
						int cur = start - 2;

						if (badCurrent) {
							cur = start;
						}

						for (; !good && cur < end; cur++) {
							if (cur >= 0 && (!Strings.isSymbol(chars[cur]) || chars[cur] == '\'')) {
								char[] frag = new char[3];
								int off;
								for (off = 0; off < 3; off++) {
									if (cur + off >= chars.length || Strings.isSymbol(chars[cur + off]) && chars[cur + off] != '\'') {
										break;
									}
									frag[off] = chars[cur + off];
								}

								boolean valid = true;

								// if we getUByte zero chars
								if (off == 0) {
									valid = false;
								}

								// if we getUByte less than 3 chars, our cur is
								// within bounds, and isn't a symbol
								if (off < 3 && cur - 1 >= 0 && (!Strings.isSymbol(chars[cur - 1]) || chars[cur - 1] == '\'')) {
									valid = false;
								}

								if (valid && !isBadFragment(frag)) {
									good = true;
								}
							}
						}

						if (!good) {
							bad = false;
						}
					}
				}

				if (bad) {
					int numeralCount = 0;
					int alphaCount = 0;
					int alphaIndex = -1;

					for (int n = start; n < end; n++) {
						if (Strings.isNumeral(chars[n])) {
							numeralCount++;
						} else if (Strings.isAlpha(chars[n])) {
							alphaCount++;
							alphaIndex = n;
						}
					}

					if (alphaIndex > -1) {
						numeralCount -= end - 1 - alphaIndex;
					}

					if (numeralCount <= alphaCount) {
						for (int n = start; n < end; n++) {
							chars[n] = '*';
						}
					} else {
						stride = 1;
					}
				}
			}
		}

	}

	public static boolean comboMatches(byte a, byte b, byte combos[][]) {
		int first = 0;
		if (combos[first][0] == a && combos[first][1] == b) {
			return true;
		}

		int last = combos.length - 1;
		if (combos[last][0] == a && combos[last][1] == b) {
			return true;
		}

		do {
			int middle = (first + last) / 2;

			if (combos[middle][0] == a && combos[middle][1] == b) {
				return true;
			}

			if (a < combos[middle][0] || a == combos[middle][0] && b < combos[middle][1]) {
				last = middle;
			} else {
				first = middle;
			}
		} while (first != last && first + 1 != last);
		return false;
	}

	/**
	 * Returns the lengths of the emulated characters for 'o', 'c', 'e', 's',
	 * and 'l' e.g.: "()" for 'o' would return 2.
	 *
	 * @param a the first char
	 * @param b the second char
	 * @param c the third char
	 * @return the length
	 */
	public static int getEmulatedDomainCharLen(char a, char b, char c) {
		if (a == b) {
			return 1;
		}
		if (a == 'o' && b == '0') {
			return 1;
		}
		if (a == 'o' && b == '(' && c == ')') {
			return 2;
		}
		if (a == 'c' && (b == '(' || b == '<' || b == '[')) {
			return 1;
		}
		if (a == 'e' && b == '\u20AC') {
			return 1;
		}
		if (a == 's' && b == '$') {
			return 1;
		}
		if (a == 'l' && b == 'i') {
			return 1;
		}
		return 0;
	}

	/**
	 * Used for getting the length of an emulated character. e.g.; [) for 'd'
	 * would return 2 since it uses 2 characters to emulate the letter d.
	 *
	 * @param a the first char
	 * @param b the second char
	 * @param c the third char
	 * @return the length
	 */
	public static int getEmulatedBadCharLen(char a, char b, char c) {
		if (a == b) {
			return 1;
		}

		if (a >= 'a' && a <= 'm') {
			if (a == 'a') {
				if (b == '4' || b == '@' || b == '^') {
					return 1;
				}
				return b != '/' || c != '\\' ? 0 : 2;
			}

			if (a == 'b') {
				if (b == '6' || b == '8') {
					return 1;
				}
				return (b != '1' || c != '3') && (b != 'i' || c != '3') ? 0 : 2;
			}

			if (a == 'c') {
				return b != '(' && b != '<' && b != '{' && b != '[' ? 0 : 1;
			}

			if (a == 'd') {
				return (b != '[' || c != ')') && (b != 'i' || c != ')') ? 0 : 2;
			}

			if (a == 'e') {
				return b != '3' && b != '\u20AC' ? 0 : 1;
			}

			if (a == 'f') {
				if (b == 'p' && c == 'h') {
					return 2;
				}
				return b != '\243' ? 0 : 1;
			}

			if (a == 'g') {
				return b != '9' && b != '6' && b != 'q' ? 0 : 1;
			}

			if (a == 'h') {
				return b != '#' ? 0 : 1;
			}

			if (a == 'i') {
				return b != 'y' && b != 'l' && b != 'j' && b != '1' && b != '!' && b != ':' && b != ';' && b != '|' ? 0 : 1;
			}

			if (a == 'j') {
				return 0;
			}

			if (a == 'k') {
				return 0;
			}

			if (a == 'l') {
				return b != '1' && b != '|' && b != 'i' ? 0 : 1;
			}

			if (a == 'm') {
				return 0;
			}
		}
		if (a >= 'n' && a <= 'z') {
			if (a == 'n') {
				return 0;
			}

			if (a == 'o') {
				if (b == '0' || b == '*') {
					return 1;
				}
				return (b != '(' || c != ')') && (b != '[' || c != ']') && (b != '{' || c != '}') && (b != '<' || c != '>') ? 0 : 2;
			}

			if (a == 'p') {
				return 0;
			}

			if (a == 'q') {
				return 0;
			}

			if (a == 'r') {
				return 0;
			}

			if (a == 's') {
				return b != '5' && b != 'z' && b != '$' && b != '2' ? 0 : 1;
			}

			if (a == 't') {
				return b != '7' && b != '+' ? 0 : 1;
			}

			if (a == 'u') {
				if (b == 'v') {
					return 1;
				}
				return (b != '\\' || c != '/') && (b != '\\' || c != '|') && (b != '|' || c != '/') ? 0 : 2;
			}

			if (a == 'v') {
				return (b != '\\' || c != '/') && (b != '\\' || c != '|') && (b != '|' || c != '/') ? 0 : 2;
			}

			if (a == 'w') {
				return b != 'v' || c != 'v' ? 0 : 2;
			}

			if (a == 'x') {
				return (b != ')' || c != '(') && (b != '}' || c != '{') && (b != ']' || c != '[') && (b != '>' || c != '<') ? 0 : 2;
			}

			if (a == 'y') {
				return 0;
			}

			if (a == 'z') {
				return 0;
			}
		}

		if (a >= '0' && a <= '9') {
			if (a == '0') {
				if (b == 'o' || b == 'O') {
					return 1;
				}
				return (b != '(' || c != ')') && (b != '{' || c != '}') && (b != '[' || c != ']') ? 0 : 2;
			}

			if (a == '1') {
				return b != 'l' ? 0 : 1;
			} else {
				return 0;
			}
		}

		if (a == ',') {
			return b != '.' ? 0 : 1;
		}

		if (a == '.') {
			return b != ',' ? 0 : 1;
		}

		if (a == '!') {
			return b != 'i' ? 0 : 1;
		} else {
			return 0;
		}
	}

	// [a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s,
	// t, u, v, w, x, y, z, null?, ', 0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
	public static byte getIndex(char c) {
		if (Strings.isLowercaseAlpha(c)) {
			return (byte) ((c - 'a') + 1);
		} else if (c == '\'') {
			return 28;
		} else if (Strings.isNumeral(c)) {
			return (byte) ((c - '0') + 29);
		}
		return 27;
	}

	public static void filterNumFragments(char[] chars) {
		int index = 0;
		int end = 0;
		int count = 0;
		int start = 0;

		while ((index = indexOfNumber(chars, end)) != -1) {
			boolean foundLowercase = false;

			// scan for lowercase char
			for (int i = end; i >= 0 && i < index && !foundLowercase; i++) {
				if (!Strings.isSymbol(chars[i]) && !Strings.isNotLowercaseAlpha(chars[i])) {
					foundLowercase = true;
				}
			}

			if (foundLowercase) {
				count = 0;
			}

			if (count == 0) {
				start = index;
			}

			// get the char index after our found number
			end = indexOfNonNumber(chars, index);

			// parsed number from string
			int value = 0;
			for (int n = index; n < end; n++) {
				value = ((value * 10) + chars[n]) - '0';
			}

			// if our value is over 0xFF or the number uses over 8 characters
			// then reset the counter
			if (value > 255 || end - index > 8) {
				count = 0;
			} else {
				count++;
			}

			// If we found 4 separate numbers with their parsed values under
			// 255 then replace everything from start to end of these number
			// with asterisks.
			if (count == 4) {
				for (int n = start; n < end; n++) {
					chars[n] = '*';
				}
				count = 0;
			}
		}
	}

	public static int indexOfNumber(char[] chars, int off) {
		for (int i = off; i < chars.length && i >= 0; i++) {
			if (chars[i] >= '0' && chars[i] <= '9') {
				return i;
			}
		}
		return -1;
	}

	public static int indexOfNonNumber(char[] chars, int off) {
		for (int i = off; i < chars.length && i >= 0; i++) {
			if (chars[i] < '0' || chars[i] > '9') {
				return i;
			}
		}
		return chars.length;
	}

	public static boolean isBadFragment(char[] chars) {
		boolean skip = true;

		for (int i = 0; i < chars.length; i++) {
			// if char not numeral and not null
			if (!Strings.isNumeral(chars[i]) && chars[i] != 0) {
				skip = false;
			}
		}

		// our string had a number or null character in it.
		if (skip) {
			return true;
		}

		int i = getInteger(chars);
		int start = 0;
		int end = fragments.length - 1;

		if (i == fragments[start] || i == fragments[end]) {
			return true;
		}

		do {
			int middle = (start + end) / 2;

			if (i == fragments[middle]) {
				return true;
			}

			if (i < fragments[middle]) {
				end = middle;
			} else {
				start = middle;
			}
		} while (start != end && start + 1 != end);
		return false;
	}

	public static int getInteger(char[] chars) {
		if (chars.length > 6) {
			return 0;
		}

		int k = 0;
		for (int n = 0; n < chars.length; n++) {
			// getUByte backwards
			char c = chars[chars.length - n - 1];

			if (Strings.isLowercaseAlpha(c)) {
				k = (k * 38) + ((c - 'a') + 1);
			} else if (c == '\'') {
				k = (k * 38) + 27;
			} else if (Strings.isNumeral(c)) {
				k = (k * 38) + ((c - '0') + 28);
			} else if (c != 0) {
				return 0;
			}
		}
		return k;
	}

	public static int fragments[];
	public static char bads[][];
	public static byte badCombinations[][][];
	public static char domains[][];
	public static char tlds[][];
	public static int tldTypes[];
	public static final String WHITELIST[] = {"cook", "cook's", "cooks", "seeks", "sheet", "woop", "woops", "faq", "noob", "noobs"};

}
