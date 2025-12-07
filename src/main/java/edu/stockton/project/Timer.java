package edu.stockton.project;

public class Timer {
		private long startTime;

	public Timer() {
		this.startTime = System.currentTimeMillis();
	}

	public enum units {
		Miliseconds,
		Seconds,
		Minutes
	}

	public long stopTimer() {
		return (System.currentTimeMillis() - startTime);
	}

	public void stopTimer(String message, units unit) {
		long endTime = (System.currentTimeMillis() - startTime);
		int timeMultiplier = switch(unit) {
			case units.Miliseconds:
				yield timeMultiplier = 1;
			case units.Seconds:
				yield timeMultiplier = 1000;
			case units.Minutes:
				yield timeMultiplier = 1000*60;
		};
		System.out.printf(message, ((double) endTime / (double) timeMultiplier));
	}
}
