package com.anytum.mobipower.wavepager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ThreadSurfaceView extends SurfaceView implements Runnable,
		SurfaceHolder.Callback {

	SurfaceHolder mSurfaceHolder;
	Thread mThread = null;
	int mSleepDuration = 25;
	volatile boolean mRunning = false;
	private Canvas canvas;

	public ThreadSurfaceView(final Context context) {
		this(context, null);
	}

	public ThreadSurfaceView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ThreadSurfaceView(final Context context, final AttributeSet attrs,
			final int defStyle) {
		super(context, attrs, defStyle);

		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		this.setZOrderOnTop(true);
		mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
	}

	@Override
	public void run() {
		while (mRunning) {
			try {
				if (mSurfaceHolder.getSurface().isValid()) {
					canvas = mSurfaceHolder.lockCanvas();
					// saveSurfaceViewBitmap(canvas);
					if (canvas != null) {
						synchronized (this) {
							drawWave(canvas);
						}
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (mSurfaceHolder != null && canvas != null) {
						mSurfaceHolder.unlockCanvasAndPost(canvas);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			try {
				Thread.sleep(mSleepDuration);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	@Override
	public synchronized void onSizeChanged(int w, int h, int oldw, int oldh) {
		/*
		 * This is called during layout when the size of this view has changed.
		 * If view was just added to the view hierarchy, it was called with the
		 * old values of 0.
		 */
	}

	public void onResume() {
		if (!mRunning) {
			mRunning = true;
			mThread = new Thread(this, "ThreadSurfaceView");
			mThread.start();
		}
	}

	public void onPause() {
		if (mRunning) {
			mRunning = false;
			try {
				mThread.join();

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	protected void drawWave(final Canvas canvas) {
	}

	public void onSurfaceChanged(int w, int h) {
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		onSurfaceChanged(width, height);
		onResume();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		onPause();
	}

	/*
	 * Note: the caller is responsible for releasing the returned Bitmap.
	 */
	public Bitmap getDrawingCache() {
		Bitmap mCache = Bitmap.createBitmap(this.getWidth(), this.getHeight(),
				Bitmap.Config.ARGB_8888);
		;
		Canvas mCanvas = new Canvas(mCache);
		synchronized (this) {
			drawWave(mCanvas);
		}
		return mCache;
	}

}