package dev.slimevr.gui;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class TimedResetTimer {
	private static Timer timer;
	private static boolean isRunning = false;

	public static void runTimer(int delayBeforeSound, int delayAfterSound, String soundFile, int soundVolume, Runnable runnable) {
		timer = new Timer();
		timer.schedule(new TimedResetTimer.ResetTimerTask(delayBeforeSound, delayAfterSound, soundFile, soundVolume, runnable), delayBeforeSound * 1000L);
		isRunning = true;
	}

	public static void cancelTimer() {
		timer.cancel();
		isRunning = false;
	}

	private static class ResetTimerTask extends TimerTask {

		private final int delayBeforeSound;
		private final int delayAfterSound;
		private final String soundFile;
		private final int soundVolume;
		private final Runnable runnable;

		private ResetTimerTask(int delayBeforeSound, int delayAfterSound, String soundFile, int soundVolume, Runnable runnable) {
			this.delayBeforeSound = delayBeforeSound;
			this.delayAfterSound = delayAfterSound;
			this.soundFile = soundFile;
			this.soundVolume = soundVolume;
			this.runnable = runnable;
		}

		@Override
		public void run() {
			playSound(soundFile, soundVolume);
			try {
				Thread.sleep(delayAfterSound * 1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			runnable.run();
			// restart timer
			if (isRunning)
				runTimer(delayBeforeSound, delayAfterSound, soundFile, soundVolume, runnable);
		}
	}

	private static synchronized void playSound(final String soundFilePath, final int soundVolume) {
		new Thread(new Runnable() {
			public void run() {
				try {
					// the music stops suddenly, but short audio is ok
					MediaPlayer mediaPlayer = new MediaPlayer(new Media(new File(soundFilePath).toURI().toString()));
					mediaPlayer.setVolume(soundVolume/100f);
					mediaPlayer.play();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
