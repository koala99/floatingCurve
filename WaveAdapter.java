package com.anytum.mobipower.wavepager;

public class WaveAdapter {

	int currentIndex = 0;
	boolean dirtFlag = false;

	/*
	 * Override: Override this function to response navigation event.
	 */
	public void onSelected() {
		// Log.d("gcy", "onSelected");
	}

	public synchronized int getCurrentIndex() {
		return currentIndex;
	}

	public synchronized void setCurrentIndex(int index) {
//		if (index < 0) {
//			currentIndex = 0;
		if (index < -1) {
			currentIndex = -1;
		} else if (index >= getCount()) {
			currentIndex = getCount() - 1;
		} else {
			currentIndex = index;
		}
	}

	/*
	 * Override: return number of items.
	 */
	public synchronized int getCount() {
		return 0;
	}

	/*
	 * Override: return a new WaveMark if position is valid. return null if
	 * position is invalid.
	 */
	public synchronized WaveMark getItemMark(int position) {
		return null;
	}

	public synchronized void invalidate() {
		dirtFlag = true;
	}

	public synchronized boolean cleanDirt() {
		boolean wasDirt = dirtFlag;
		dirtFlag = false;
		return wasDirt;
	}
}