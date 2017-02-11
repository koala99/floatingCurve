package com.anytum.mobipower.wavepager;

import java.util.Random;

public class WaveMark {
	public static final int COLOR_NONE = 0;
	public static final int COLOR_WHITE = 1;
	public static final int COLOR_BLUE = 2;
	public static final int COLOR_YELLOW = 3;
	public static final int COLOR_ORANGE = 4;

	public String timeStr;
	public String stepStr;
	public int step;
	public int alpha = 0;
	public int colorType = COLOR_NONE;

	public WaveMark() {
		Random r = new Random();
		timeStr = Integer.toString(r.nextInt(100)) + ":"
				+ Integer.toString(r.nextInt(100)) + ":"
				+ Integer.toString(r.nextInt(100));
		step = r.nextInt(10000);
		stepStr = Integer.toString(step);
	}

	public WaveMark(int step) {
		timeStr = "--:--:--";
		this.step = step;
		if (step < 0) {
			stepStr = "--";
		} else {
			stepStr = Integer.toString(step);
		}
	}

	public WaveMark(String timeStr, int step) {
		if ((timeStr == null) || (timeStr.equals(""))) {
			this.timeStr = "--:--:--";
		} else {
			this.timeStr = timeStr;
		}
		this.step = step;
		if (step < 0) {
			stepStr = "--";
		} else {
			stepStr = Integer.toString(step);
		}
	}

	public WaveMark(String top, String inside, int value) {
		this.timeStr = top;
		this.stepStr = inside;
		this.step = value;
	}

	public void copy(WaveMark mark) {
		this.timeStr = mark.timeStr;
		this.step = mark.step;
		this.stepStr = mark.stepStr;
		this.alpha = mark.alpha;
		this.colorType = mark.colorType;
	}
}