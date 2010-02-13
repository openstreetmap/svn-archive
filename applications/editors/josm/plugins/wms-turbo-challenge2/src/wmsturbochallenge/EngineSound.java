/*
 * GPLv2 or 3, Copyright (c) 2010  Andrzej Zaborowski
 *
 * This class simulates a car engine.  What does a car engine do?  It
 * makes a pc-speaker-like buzz.  The PC Speaker could only emit
 * a (nearly) square wave and we simulate it here for maximum realism.
 */
package wmsturbochallenge;

import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat;

class engine {
	public engine() {
		rpm = 0.0;
	}

	public void start() {
		rpm = 0.3;
		speed = 0.0;
		n = 0;

		if (output != null)
			stop();

		AudioFormat output_format =
			new AudioFormat(S_RATE, 16, 1, true, true);
		DataLine.Info info =
			new DataLine.Info(SourceDataLine.class, output_format);

		/* Get the data line, open it and initialise the device */
		try {
			output = (SourceDataLine) AudioSystem.getLine(info);
			output.open(output_format);
			output.start();
			frames_written = 0;
			reschedule(0);
		} catch (Exception e) {
			output = null;
			System.out.println("Audio not available: " +
					e.getClass().getSimpleName());
		}
	}

	public void stop() {
		rpm = 0.0;
		n = 0;

		if (output == null)
			return;

		tick.cancel();
		tick.purge();

		output.stop();
		output.flush();
		output.close();
		output = null;
	}

	public void set_speed(double speed) {
		/* This engine is equipped with an automatic gear box that
		 * switches gears when the RPM becomes too high or too low.  */
		double new_speed = Math.abs(speed);
		double accel = new_speed - this.speed;
		this.speed = new_speed;

		if (accel > 0.05)
			accel = 0.05;
		else if (accel < -0.05)
			accel = -0.05;
		rpm += accel;

		if (accel > 0.0 && rpm > 1.0 + n * 0.2 && speed > 0.0) {
			rpm = 0.3 + n * 0.2;
			n ++;
		} else if (accel < 0.0 && rpm < 0.3) {
			if (n > 0) {
				rpm = 0.7 + n * 0.1;
				n --;
			} else
				rpm = 0.2;
		}
		if (speed < 2.0)
			n = 0;
	}

	public boolean is_on() {
		return output != null;
	}

	protected double speed;
	protected double rpm;
	protected int n;

	protected SourceDataLine output = null;
	protected long frames_written;
	protected Timer tick = new Timer();

	/* Audio parameters.  */
	protected static final int S_RATE = 44100;
	protected static final int MIN_BUFFER = 4096;
	protected static final double volume = 0.3;

	protected class audio_task extends TimerTask {
		public void run() {
			if (output == null)
				return;

			/* If more than a two buffers left to play,
			 * reschedule and try to wake up closer to the
			 * end of already written data.  */
			long frames_current = output.getLongFramePosition();
			if (frames_current < frames_written - MIN_BUFFER * 2) {
				reschedule(frames_current);
				return;
			}

			/* Build a new buffer */
			/* double freq = 20 * Math.pow(1.3, rpm * 5.0); */
			double freq = (rpm - 0.1) * 160.0;
			int wavelen = (int) (S_RATE / freq);
			int bufferlen = MIN_BUFFER - (MIN_BUFFER % wavelen) +
				wavelen;
			int value = (int) (0x7fff * volume);

			bufferlen *= 2;
			byte[] buffer = new byte[bufferlen];
			for (int b = 0; b < bufferlen; ) {
				int j;
				for (j = wavelen / 2; j > 0; j --) {
					buffer[b ++] = (byte) (value >> 8);
					buffer[b ++] = (byte) (value & 0xff);
				}
				value = 0x10000 - value;
				for (j = wavelen - wavelen / 2; j > 0; j --) {
					buffer[b ++] = (byte) (value >> 8);
					buffer[b ++] = (byte) (value & 0xff);
				}
				value = 0x10000 - value;
			}

			frames_written +=
				output.write(buffer, 0, bufferlen) / 2;

			reschedule(frames_current);
		}
	}

	protected void reschedule(long frames) {
		/* Send a new buffer as close to the end of the
		 * currently playing buffer as possible (aim at
		 * about half into the last frame).  */
		long delay = (frames_written - frames - MIN_BUFFER / 2) *
			1000 / S_RATE;
		if (delay < 0)
			delay = 0;
		tick.schedule(new audio_task(), delay);
	}
}
