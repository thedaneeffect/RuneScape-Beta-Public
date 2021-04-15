/*
 * Copyright (c) 2015. Dane
 */

package rs.audio;

import javax.sound.midi.*;
import java.io.*;

public class MidiPlayer implements Receiver {

	public static final int PROGRAM_0 = 0;

	public static final int MSB_CHANNEL_VOLUME = 7;
	public static final int LSB_CHANNEL_VOLUME = 39;

	public static final int MSB_BANK_SELECT = 0;
	public static final int LSB_BANK_SELECT = 32;

	public static final int ALL_SOUND_OFF = 120;
	public static final int RESET_ALL_CONTROLLERS = 121;
	public static final int ALL_NOTES_OFF = 123;

	private final int[] volumes = new int[]{
			12800, 12800, 12800, 12800, 12800, 12800,
			12800, 12800, 12800, 12800, 12800, 12800,
			12800, 12800, 12800, 12800
	};

	private int volume;

	private Receiver receiver;
	private Sequencer sequencer;

	public MidiPlayer() {
		try {
			receiver = MidiSystem.getReceiver();
			sequencer = MidiSystem.getSequencer(false);
			sequencer.getTransmitter().setReceiver(this); // used to override messages
			sequencer.open();
		} catch (Exception e) {
			e.getMessage();
		}
	}

	public Sequence play(InputStream in, boolean loop) throws InvalidMidiDataException, IOException {
		if (sequencer == null) {
			return null;
		}

		Sequence s = MidiSystem.getSequence(in);
		sequencer.setSequence(s);
		sequencer.setLoopCount(loop ? Sequencer.LOOP_CONTINUOUSLY : 0);
		sequencer.start();
		return s;
	}

	public void setVolume(int volume) {
		if (sequencer == null) {
			return;
		}

		if (volume < 0) {
			volume = 0;
		} else if (volume > 256) {
			volume = 256;
		}

		if (this.volume != volume) {
			this.volume = volume;

			for (int c = 0; c < 16; c++) {
				int data = getChannelVolume(c);
				send(c + ShortMessage.CONTROL_CHANGE, MSB_CHANNEL_VOLUME, data >> 7);
				send(c + ShortMessage.CONTROL_CHANGE, LSB_CHANNEL_VOLUME, data & 0x7F);
			}
		}
	}

	public boolean isMuted() {
		return volume <= 0;
	}

	public void adjustVolume(int adjustment) {
		setVolume(volume + adjustment);
	}

	public int getVolume() {
		return volume;
	}

	public void stop() {
		if (sequencer != null) {
			sequencer.close();
			reset();
		}
	}

	private void reset() {
		for (int c = 0; c < 16; c++) {
			send(c + ShortMessage.CONTROL_CHANGE, ALL_NOTES_OFF, 0);
		}

		for (int c = 0; c < 16; c++) {
			send(c + ShortMessage.CONTROL_CHANGE, ALL_SOUND_OFF, 0);
		}

		for (int c = 0; c < 16; c++) {
			send(c + ShortMessage.CONTROL_CHANGE, RESET_ALL_CONTROLLERS, 0);
		}

		for (int c = 0; c < 16; c++) {
			send(c + ShortMessage.CONTROL_CHANGE, MSB_BANK_SELECT, 0);
		}

		for (int c = 0; c < 16; c++) {
			send(c + ShortMessage.CONTROL_CHANGE, LSB_BANK_SELECT, 0);
		}

		for (int c = 0; c < 16; c++) {
			send(c + ShortMessage.PROGRAM_CHANGE, PROGRAM_0, 0);
		}
	}

	@Override
	public void send(MidiMessage m, long timeStamp) {
		byte[] data = m.getMessage();
		
		if (data.length < 3 || !send0(data[0], data[1], data[2])) {
			receiver.send(m, timeStamp);
		}
	}

	@Override
	public void close() {
		if (sequencer != null) {
			sequencer.close();
			sequencer = null;
		}

		if (receiver != null) {
			receiver.close();
			receiver = null;
		}
	}

	private void send(int status, int data1, int data2) {
		try {
			receiver.send(new ShortMessage(status, data1, data2), -1);
		} catch (InvalidMidiDataException ignored) {
		}
	}

	private boolean send0(int status, int data1, int data2) {
		if ((status & 0xF0) == ShortMessage.CONTROL_CHANGE) {
			if (data1 == RESET_ALL_CONTROLLERS) {
				send(status, data1, data2);

				int channel = status & 0xF;
				volumes[channel] = 12800;
				int data = getChannelVolume(channel);

				send(status, MSB_CHANNEL_VOLUME, data >> 7);
				send(status, LSB_CHANNEL_VOLUME, data & 0x7f);
				return true;
			}

			if (data1 == MSB_CHANNEL_VOLUME || data1 == LSB_CHANNEL_VOLUME) {
				int channel = status & 0xF;

				if (data1 == MSB_CHANNEL_VOLUME) {
					volumes[channel] = (volumes[channel] & 0x7F) + (data2 << 7);
				} else {
					volumes[channel] = (volumes[channel] & 0x3F80) + data2;
				}

				int data = getChannelVolume(channel);
				send(status, MSB_CHANNEL_VOLUME, data >> 7);
				send(status, LSB_CHANNEL_VOLUME, data & 0x7f);
				return true;
			}
		}
		return false;
	}

	private int getChannelVolume(int channel) {
		int data = volumes[channel];
		data = ((data * volume) >> 8) * data;
		return (int) (Math.sqrt(data) + 0.5);
	}

}
