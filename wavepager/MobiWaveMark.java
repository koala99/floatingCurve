package com.anytum.mobipower.wavepager;

import android.app.IntentService;

public class MobiWaveMark extends WaveMark {
	public static final int TYPE_NONE = 0;
	public static final int TYPE_ENTER = 1;
	public static final int TYPE_EXIT = 2;
	public static final int TYPE_SUMMARY = 3;

	public String timeStr;
	public String stepStr;
	public String dateStr;
	public int type = TYPE_NONE;
    public IntentService s
	public MobiWaveMark(String top, String inside, int type, String date) {
		this.timeStr = top;
		this.stepStr = inside;
		this.type = type;
		this.dateStr = date;
	}

	public void copy(MobiWaveMark mark) {
		this.timeStr = mark.timeStr;
		this.stepStr = mark.stepStr;
		this.type = mark.type;
		this.dateStr = mark.dateStr;
	}
}