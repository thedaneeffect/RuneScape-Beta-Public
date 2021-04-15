package rs.audio;

import rs.util.Signlink;

import java.io.InputStream;

/**
 * A simple class written to handle playing music and fading in/out.
 */
public final class Audio implements Runnable {

	private static MidiPlayer player = new MidiPlayer();
	private static String current = "none";
	private static String playing = "none";
	private static String last = "none";
	private static int volume = 96;
	private static boolean active;

	public static void start() {
		active = true;
		Thread t = new Thread(new Audio());
		t.setDaemon(true);
		t.setName("AudioThread");
		t.start();
	}

	public static void stop() {
		active = false;
	}

	public static void play(String midi) {
		Signlink.midi = midi;
	}

	public static int getVolume() {
		return volume;
	}

	public static void setVolume(int volume) {
		Audio.volume = volume;
	}

	private static void update() {
		// TODO: synchronized lock

		// no song
		if (Signlink.midi == null) {
			if (player.isMuted()) {
				playing = "none";
			} else {
				player.adjustVolume(-1);
			}
			return;
		}

		boolean songChanging = !current.equals(Signlink.midi);

		if (songChanging) {
			current = Signlink.midi;

			if (current.equals("replay")) {
				current = last;
			}

			last = current;
		}

		boolean fadeOut = !playing.equals(current);

		if (fadeOut) {
			if (!player.isMuted()) {
				player.adjustVolume(-1);
				return;
			}

			playing = current;

			if (playing == null || playing.equals("none")) {
				return;
			}

			try (InputStream in = ClassLoader.getSystemResource("/" + playing).openStream()) {
				player.play(in, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// fade in
			int volume = player.getVolume();

			if (volume < Audio.volume) {
				player.adjustVolume(1);
			} else if (volume > Audio.volume) {
				player.adjustVolume(-1);
			}
		}
	}

	private Audio() {
	}

	@Override
	public void run() {
		if (player == null)
			return;
		while (active) {
			update();
		}
	}

}
