package rs.util;

import java.io.*;
import java.util.zip.*;

public final class Signlink implements Runnable {

	private static boolean active;
	private static String loadreq = null;
	private static byte[] loadbuf = null;
	private static String savereq = null;
	private static byte[] savebuf = null;
	public static String midi = "none";
	public static String jingle = null;
	public static int looprate = 100;
	public static File cacheDirectory;

	static {
		cacheDirectory = findCachePath();
	}

	public static void start() {
		if (!active) {
			Thread t = new Thread(new Signlink());
			t.setPriority(Thread.MIN_PRIORITY);
			t.setDaemon(true);
			t.start();
		}
	}

	@Override
	public final void run() {
		if (!active) {
			active = true;

			File path = findCachePath();

			System.out.println(path.getAbsolutePath());

			for (; ; ) {
				if (loadreq != null) {
					loadbuf = null;

					File f = new File(path, loadreq);

					if (f.exists()) {
						loadbuf = new byte[(int) f.length()];
						try (DataInputStream dis = new DataInputStream(new FileInputStream(f))) {
							dis.readFully(loadbuf, 0, loadbuf.length);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					loadreq = null;
				}

				if (savereq != null) {
					try (FileOutputStream out = new FileOutputStream(new File(path, savereq))) {
						out.write(savebuf, 0, savebuf.length);
					} catch (Exception e) {
						e.printStackTrace();
					}
					savereq = null;
				}

				try {
					Thread.sleep((long) looprate);
				} catch (Exception e) {
					/* empty */
				}
			}
		}
	}

	public static File findCachePath() {
		String[] paths = {"c:/windows/", "c:/winnt/", "d:/windows/", "d:/winnt/", "e:/windows/", "e:/winnt/", "f:/windows/", "f:/winnt/", "c:/", "~/", "/tmp/", ""};
		String folder = ".file_store_32/";

		for (String path : paths) {
			try {
				if (path.length() > 0) {
					if (!new File(path).exists()) {
						continue;
					}
				}

				File f = new File(path + folder);

				if ((f.exists() && f.isDirectory()) || f.mkdir()) {
					return f;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static void setRate(int rate) {
		looprate = rate;
	}

	public static byte[] getDecompressed(byte[] src) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		try (ByteArrayOutputStream out = new ByteArrayOutputStream(); GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(src))) {
			while ((read = in.read(buffer)) > 0) {
				out.write(buffer, 0, read);
			}
			return out.toByteArray();
		}
	}

	public static byte[] load(String file) {
		if (!active) {
			return null;
		}

		if (loadreq != null) {
			return null;
		}

		loadreq = file;
		while (loadreq != null) {
			try {
				Thread.sleep(1L);
			} catch (Exception e) {
				/* empty */
			}
		}
		return loadbuf;
	}

	public static void save(String file, byte[] src) {
		if (active && savereq == null && src.length <= 2000000) {
			savebuf = src;
			savereq = file;
			while (savereq != null) {
				try {
					Thread.sleep(1L);
				} catch (Exception ignored) {
				}
			}
		}
	}

}
