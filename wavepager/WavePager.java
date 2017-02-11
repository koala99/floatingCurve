package com.anytum.mobipower.wavepager;

import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.anytum.mobipower.R;
import com.anytum.mobipower.util.Constants;

public class WavePager extends ThreadSurfaceView {
	public static final int MARK_NUM = 6;

	final int WAVE_LENGTH = 51;
	final int HEIGHT_DIVIDER = 4;
	final boolean DYNAMIC_FOLLOWERS = false;
	final boolean DYNAMIC_WAVE = true;
	final boolean VIBERATION = false;
	final float WAVE_SPEED = 0.003f;

	private Bitmap mBmpMarkGlow;
	private Point mPointCenterMarkGlow;
	private Paint mPaintMarkGlow;

	private Bitmap mBmpMarkGlowBlue;
	private Bitmap mBmpMarkGlowOrange;
	private Point mPointCenterMarkHighlight;
	private Paint mPaintMarkHighlight;

	private Bitmap mBmpTag;
	private Bitmap mBmpTagSelected;
	private int mTagWidth;
	private int mTagHeight;
	private int mTagMiddle;
	private Paint mPaintTagTime;
	private Paint mPaintTagStep;
	private int mTagStepMiddle;

	private Paint mPaintTransparent;
	private Paint mPaintWave;
	private Path mPath;
	private float mInterval;
	private float mSegment;
	private float mOffsetX = 0;
	private float mOffsetTarget = 0;
	private boolean mTouching = false;
	private boolean mShifting = true;
	private boolean mSelected = true;
	private float mOriginalX;
	private int mWidth;
	private int mHeight;

	private WaveMark[] mMark = new WaveMark[MARK_NUM];
	private float[] wave = new float[WAVE_LENGTH];
	private SineParams[] mSineFollowers;
	private SineParams mSineWave;
	private float mPhaseTarget;
	private boolean mTouchable = true;

	private WaveAdapter mAdapter;

	public WavePager(final Context context) {
		this(context, null);
	}

	public WavePager(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WavePager(final Context context, final AttributeSet attrs,
			final int defStyle) {
		super(context, attrs, defStyle);

		// Paint for screen clean up
		mPaintTransparent = new Paint();
		mPaintTransparent.setXfermode(new PorterDuffXfermode(Mode.CLEAR));

		// Paint for wave and mMark circles.
		mPaintWave = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintWave.setStyle(Paint.Style.STROKE);
		mPaintWave.setStrokeWidth(5);
		mPaintWave.setColor(Color.WHITE);

		// Wave parameters.
		if (DYNAMIC_WAVE) {
			mSineWave = new SineParams(WAVE_SPEED, WAVE_SPEED);
			mSineFollowers = new SineParams[] { new SineParams(WAVE_SPEED,WAVE_SPEED) };
			for (int i = 0; i < mSineFollowers.length; i++) {
				mSineFollowers[i].changePhase(0.05f, 0.05f);
			}
		} else {
			mSineWave = new SineParams(WAVE_SPEED, WAVE_SPEED);
			if (DYNAMIC_FOLLOWERS) {
				mSineFollowers = new SineParams[] {
						new SineParams(0.0015f, 0.002f),
						new SineParams(0.0012f, 0.0023f) };
				for (int i = 0; i < mSineFollowers.length; i++) {
					mSineFollowers[i].setReference(mSineWave, 0.1f, 0.1f);
				}
			} else {
				mSineFollowers = new SineParams[] {
						new SineParams(WAVE_SPEED, WAVE_SPEED),
						new SineParams(WAVE_SPEED, WAVE_SPEED) };
				mSineFollowers[0].magnitude = new float[] { 1.25f, 1.2f };
				mSineFollowers[1].magnitude = new float[] { 0.8f, 0.8f };
			}
		}

		// Mark glowing setting.
		if (mBmpMarkGlow != null) {
			mBmpMarkGlow.recycle();
		}
		mBmpMarkGlow = BitmapFactory.decodeResource(getResources(),
				R.drawable.wave_mark_glow);
		mPointCenterMarkGlow = new Point(mBmpMarkGlow.getWidth() / 2,
				mBmpMarkGlow.getHeight() / 2);
		mPaintMarkGlow = new Paint();
		mPaintMarkGlow.setAlpha(255);

		// Mark highlighting setting.
		if (mBmpMarkGlowBlue != null) {
			mBmpMarkGlowBlue.recycle();
		}
		mBmpMarkGlowBlue = BitmapFactory.decodeResource(getResources(),
				R.drawable.wave_mark_glow_blue);
		if (mBmpMarkGlowOrange != null) {
			mBmpMarkGlowOrange.recycle();
		}
		mBmpMarkGlowOrange = BitmapFactory.decodeResource(getResources(),
				R.drawable.wave_mark_glow_orange);
		mPointCenterMarkHighlight = new Point(
				mBmpMarkGlowOrange.getWidth() / 2,
				mBmpMarkGlowOrange.getHeight() / 2);
		mPaintMarkHighlight = new Paint();
		mPaintMarkHighlight.setAlpha(255);

		// Tag settings.
		if (mBmpTag != null) {
			mBmpTag.recycle();
		}
		if (mBmpTagSelected != null) {
			mBmpTagSelected.recycle();
		}
		mBmpTag = BitmapFactory.decodeResource(getResources(),
				R.drawable.wave_tag);
		mBmpTagSelected = BitmapFactory.decodeResource(getResources(),
				R.drawable.wave_tag_selected);

		mTagWidth = mBmpTag.getWidth();
		mTagHeight = mBmpTag.getHeight();
		mTagMiddle = mTagHeight * 31 / 52;

		mPaintTagTime = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintTagTime.setTypeface(Typeface.SANS_SERIF);
		mPaintTagTime.setColor(0xFF888888);
		mPaintTagTime.setTextSize(30f);
		mPaintTagTime.setTextAlign(Paint.Align.CENTER);
		mPaintTagStep = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintTagStep.setTypeface(Typeface.SANS_SERIF);
		mPaintTagStep.setColor(Color.WHITE);
		mPaintTagStep.setTextSize(40f);
		mPaintTagStep.setTextAlign(Paint.Align.CENTER);
		Rect rect = new Rect();
		String maxStep = "99999";
		mPaintTagStep.getTextBounds(maxStep, 0, maxStep.length(), rect);
		mTagStepMiddle = rect.height() / 2;

		mAdapter = new WaveAdapter();
	}

	@Override
	public void onSurfaceChanged(int w, int h) {
		/*
		 * This is called during layout when the size of this view has changed.
		 * If view was just added to the view hierarchy, it was called with the
		 * old values of 0.
		 */
		mWidth = w;
		mHeight = h;
		mInterval = (float) w / (wave.length - 1);
		mPath = new Path();
		mSegment = (float) w / (MARK_NUM - 2);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	protected void drawWave(Canvas canvas) {
		boolean isTouching;
		boolean markChanged = false;
		synchronized (this) {
			isTouching = mTouching;
		}

		if (mAdapter.cleanDirt()) {
			for (int i = mAdapter.getCurrentIndex() - 3, j = 0; i <= mAdapter
					.getCurrentIndex() + 2; i++, j++) {
				mMark[j] = mAdapter.getItemMark(i);
			}
		}

		// Clean up SurfaceView.
		canvas.drawPaint(mPaintTransparent);

		// Calculate wave and mMark offset.
		synchronized (this) {
			if (mOffsetX != mOffsetTarget) {
				mShifting = true;
				float delta = (mOffsetTarget - mOffsetX) * 0.3f;
				if (Math.abs(delta) <= 1f) {
					mOffsetX = mOffsetTarget;
				} else {
					mOffsetX += delta;
					mOffsetX = (int) mOffsetX;
				}
			} else {
				mShifting = false;
			}
			if (mOffsetX >= mSegment) {
				mOffsetX -= mSegment;
				mOffsetTarget -= mSegment;
				synchronized (this) {
					// shift in a previous mark.
					mAdapter.setCurrentIndex(mAdapter.getCurrentIndex() - 1);
					pushPrevMark(mAdapter.getItemMark(mAdapter.getCurrentIndex() - 3));
					if (mAdapter.getCurrentIndex() == 0) {
						mOffsetX = 0;
						mOffsetTarget = 0;
					}
					markChanged = true;
				}
			} else if (mOffsetX <= -mSegment) {
				mOffsetX += mSegment;
				mOffsetTarget += mSegment;
				synchronized (this) {
					// shift in a next mark.
					mAdapter.setCurrentIndex(mAdapter.getCurrentIndex() + 1);
					pushNextMark(mAdapter.getItemMark(mAdapter
							.getCurrentIndex() + 2));
					if (mAdapter.getCurrentIndex() == mAdapter.getCount() - 1) {
						mOffsetX = 0;
						mOffsetTarget = 0;
					}
					markChanged = true;
				}
			}
		}

		if (mSelected) {
			if (isTouching || mShifting) {
				mSelected = false;
			}
		} else {
			if (!isTouching && !mShifting) {
				mSelected = true;
				mAdapter.onSelected();
			}
		}

		// Calculate phase target.
		if (DYNAMIC_WAVE) {
			// Move wave toward the mMark with bigger step count. And draw it.
			mSineWave.changePhase();
			mPaintWave.setAlpha(255);
			mPaintWave.setStrokeWidth(5);
			canvas.drawPath(updateWave(mSineWave), mPaintWave);

			// Move and draw wave followers.
			mPaintWave.setAlpha(128);
			mPaintWave.setStrokeWidth(3);
			for (int i = 0; i < mSineFollowers.length; i++) {
				mSineFollowers[i].changePhase();
				canvas.drawPath(updateWave(mSineFollowers[i]), mPaintWave);
			}
		} else {
			if (markChanged) {
				int maxMark = 4;
				for (int i = 1; i <= 4; i++) {
					if ((mMark[i] != null) && (mMark[maxMark] != null)) {
						if (mMark[i].step > mMark[maxMark].step) {
							maxMark = i;
						}
					}
				}
				switch (maxMark) {
				case 1:
					mPhaseTarget = 0.5f;
					break;
				case 2:
					mPhaseTarget = 0.25f;
					break;
				case 3:
					mPhaseTarget = 0f;
					break;
				case 4:
					mPhaseTarget = -0.25f;
					break;
				}
			}
			// Move wave toward the mMark with bigger step count. And draw it.
			mSineWave.targetPhase(mPhaseTarget, mPhaseTarget);
			mPaintWave.setAlpha(255);
			mPaintWave.setStrokeWidth(5);
			canvas.drawPath(updateWave(mSineWave), mPaintWave);

			// Move and draw wave followers.
			mPaintWave.setAlpha(128);
			mPaintWave.setStrokeWidth(3);
			if (DYNAMIC_FOLLOWERS) {
				for (int i = 0; i < mSineFollowers.length; i++) {
					mSineFollowers[i].changePhase();
					canvas.drawPath(updateWave(mSineFollowers[i]), mPaintWave);
				}
			} else {
				for (int i = 0; i < mSineFollowers.length; i++) {
					mSineFollowers[i].targetPhase(mPhaseTarget, mPhaseTarget);
					canvas.drawPath(updateWave(mSineFollowers[i]), mPaintWave);
				}
			}
		}

		// Draw marks.
		mPaintWave.setAlpha(255);
		for (int i = 0; i < MARK_NUM; i++) {
			if (mMark[i] != null) {
				float cx = -mSegment / 2 + mSegment * i + mOffsetX;
				float cr = cx / mWidth;
				float cy = calculateSine(mSineWave, cr) + mHeight / 2;

				// Highlight Mark
				if (mMark[i].colorType != WaveMark.COLOR_NONE) {
					// switch(mMark[i].colorType){
					// case WaveMark.COLOR_BLUE:
					// canvas.drawBitmap(mBmpMarkGlowBlue, cx -
					// mPointCenterMarkHighlight.x, cy -
					// mPointCenterMarkHighlight.y, mPaintMarkHighlight);
					// break;
					// case WaveMark.COLOR_ORANGE:
					// canvas.drawBitmap(mBmpMarkGlowOrange, cx -
					// mPointCenterMarkHighlight.x, cy -
					// mPointCenterMarkHighlight.y, mPaintMarkHighlight);
					// break;
					// default:
					// break;
					// }
				} else {
					if (Math.abs(cx - mSegment * 7 / 2) < mSegment / 4) {
						canvas.drawBitmap(mBmpMarkGlowOrange, cx
								- mPointCenterMarkHighlight.x, cy
								- mPointCenterMarkHighlight.y,
								mPaintMarkHighlight);
					} else if (Math.abs(cx - mSegment * 5 / 2) < mSegment / 4) {
						canvas.drawBitmap(mBmpMarkGlowBlue, cx
								- mPointCenterMarkHighlight.x, cy
								- mPointCenterMarkHighlight.y,
								mPaintMarkHighlight);
					}
				}

				canvas.drawCircle(cx, cy, 5, mPaintWave);
				canvas.drawCircle(cx, cy, 15, mPaintWave);

				// Selected Mark Glows.
				if (mSelected && (i == MARK_NUM - 3)) {
					if (mMark[i].alpha < 255) {
						mMark[i].alpha = 255 - (int) (0.8f * (255 - mMark[i].alpha));
					}
					mPaintMarkGlow.setAlpha(mMark[i].alpha);
					canvas.drawBitmap(mBmpMarkGlow,
							cx - mPointCenterMarkGlow.x, cy
									- mPointCenterMarkGlow.y, mPaintMarkGlow);
				} else {
					if (mMark[i].alpha > 0) {
						mMark[i].alpha = (int) (0.8f * mMark[i].alpha);
						mPaintMarkGlow.setAlpha(mMark[i].alpha);
						canvas.drawBitmap(mBmpMarkGlow, cx
								- mPointCenterMarkGlow.x, cy
								- mPointCenterMarkGlow.y, mPaintMarkGlow);
					}
				}

				// Draw tags
				if ((!mShifting && (i == MARK_NUM - 3))
						|| (mShifting && (mOffsetX == mOffsetTarget) && (i == MARK_NUM - 3))
						|| (mShifting && (mOffsetX < mOffsetTarget) && (i == MARK_NUM - 4))
						|| (mShifting && (mOffsetX > mOffsetTarget) && (i == MARK_NUM - 2))) {
					canvas.drawBitmap(mBmpTagSelected, cx - mTagWidth / 2, cy
							- mTagHeight, null);
				} else {
					canvas.drawBitmap(mBmpTag, cx - mTagWidth / 2, cy
							- mTagHeight, null);
				}
				canvas.drawText(mMark[i].timeStr, cx, cy - mTagHeight,
						mPaintTagTime);
				canvas.drawText(mMark[i].stepStr, cx, cy - mTagMiddle
						+ mTagStepMiddle, mPaintTagStep);
			}
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		/* UI Thread */
		if (mTouchable) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:

				synchronized (this) {
					mTouching = true;
				}
				mOriginalX = event.getX();
				break;
			case MotionEvent.ACTION_MOVE:
				if (scroll(event.getX() - mOriginalX)) {
					mOriginalX = event.getX();
				}
				break;
			case MotionEvent.ACTION_UP:
				
				synchronized (this) {
					mTouching = false;
				}
				break;
			}
			return true;
		} else {
			return false;
		}
	}

	public void setTouchable(boolean isTouchable) {
		mTouchable = isTouchable;
	}

	public void setTouch(boolean isTouch) {
		synchronized (this) {
			mTouching = isTouch;
		}
	}

	public boolean scroll(float deltaX) {
		/* return if scroll happens. */
		boolean isMoved = false;
		if (deltaX > mSegment / 2) {
			// Move to previous
			synchronized (this) {
				if (mAdapter.getCurrentIndex() > 0) {
					mOffsetTarget += mSegment;
					isMoved = true;
				}
			}
			if (isMoved) {
				lightVibrate();
				playSoundEffect();
			}
			return true;
		} else if (deltaX < -mSegment / 2) {
			// Move to next
			synchronized (this) {
				if (mAdapter.getCurrentIndex() < mAdapter.getCount() - 1) {
					mOffsetTarget -= mSegment;
					isMoved = true;
				}
			}
			if (isMoved) {
				lightVibrate();
				playSoundEffect();
			}
			return true;
		}

		return false;
	}

	public void setAdapter(WaveAdapter adapter) {
		synchronized (this) {
			this.mAdapter = adapter;
			for (int i = adapter.getCount() - 1; i >= adapter.getCount() - 5; i--) {
				pushPrevMark(adapter.getItemMark(i));
			}
			this.mAdapter.setCurrentIndex(this.mAdapter.getCount() - 2);

		}
	}

	public void pushNextMark(WaveMark nextMark) {
		synchronized (this) {
			for (int i = 0; i < mMark.length - 1; i++) {
				mMark[i] = mMark[i + 1];
			}
			mMark[mMark.length - 1] = nextMark;
		}
	}

	public void pushPrevMark(WaveMark prevMark) {
		synchronized (this) {
			for (int i = mMark.length - 1; i > 0; i--) {
				mMark[i] = mMark[i - 1];
			}
			mMark[0] = prevMark;
		}
	}

	private Path updateWave(SineParams sine) {
		// calculate wave
		float step = 1.0f / wave.length;
		float x = 0;
		for (int i = 0; i < wave.length; i++) {
			wave[i] = calculateSine(sine, x);
			x += step;
		}

		// update path
		mPath.reset();
		mPath.moveTo(0, wave[0]);
		x = 0;
		for (int i = 1; i < wave.length; i++) {
			x += mInterval;
			mPath.lineTo(x, wave[i]);
		}
		mPath.offset(0, mHeight / 2);

		return mPath;
	}

	private void lightVibrate() {
		if (VIBERATION) {
			((Vibrator) this.getContext().getSystemService(
					Context.VIBRATOR_SERVICE)).vibrate(20);
		}
	}

	private void playSoundEffect() {
		if(Constants.isVoicePlay){
			MediaPlayer mp = MediaPlayer.create(this.getContext(), R.raw.click);
			mp.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					mp.release();
				}
			});
			mp.start();
		}
	
	}

	private float calculateSine(SineParams sine, float in) {
		return ((float) (sine.magnitude[0]
				* Math.sin(2 * Math.PI * (in + sine.phase[0])) + sine.magnitude[1]
				* Math.cos(2 * Math.PI * (in + sine.phase[1])))
				/ 2 * mHeight / HEIGHT_DIVIDER);
	}

	private class SineParams {
		float[] phaseStep = { 0f, 0f };
		float[] magnitude = { 1f, 1f };
		float[] phase = { 0f, 0f };
		Random random = new Random();

		SineParams reference;
		float[] threshold = { 0f, 0f };
		boolean[] signPositive = { true, true };

		public SineParams() {
		};

		public SineParams(float ps0, float ps1) {
			phaseStep[0] = ps0;
			phaseStep[1] = ps1;
		}

		void changeMagnitudeRandom() {
			magnitude[0] += (random.nextFloat() - 0.5) / 20;
			magnitude[1] += (random.nextFloat() - 0.5) / 20;
		}

		void changePhaseRandom() {
			phase[0] += (random.nextFloat()) / 100;
			phase[1] += (random.nextFloat()) / 100;
		}

		void targetPhase(float t0, float t1) {
			if (t0 > (phase[0] + phaseStep[0])) {
				phase[0] += phaseStep[0];
			} else if (t0 < (phase[0] - phaseStep[0])) {
				phase[0] -= phaseStep[0];
			}

			if (t1 > (phase[1] + phaseStep[1])) {
				phase[1] += phaseStep[1];
			} else if (t1 < (phase[1] - phaseStep[1])) {
				phase[1] -= phaseStep[1];
			}
		}

		void changePhase() {
			changePhase(phaseStep[0], phaseStep[1]);
		}

		void changePhase(float v0, float v1) {
			if (reference == null) {
				phase[0] += v0;
				phase[1] += v1;
			} else {
				phase[0] += signPositive[0] ? v0 : -v0;
				phase[1] += signPositive[1] ? v1 : -v1;

				if (signPositive[0]
						&& ((phase[0] - reference.phase[0]) > threshold[0])) {
					signPositive[0] = false;
				} else if (!signPositive[0]
						&& ((reference.phase[0] - phase[0]) > threshold[0])) {
					signPositive[0] = true;
				}
				if (signPositive[1]
						&& ((phase[1] - reference.phase[1]) > threshold[1])) {
					signPositive[1] = false;
				} else if (!signPositive[1]
						&& ((reference.phase[1] - phase[1]) > threshold[1])) {
					signPositive[1] = true;
				}
			}
		}

		void setReference(SineParams params, float t0, float t1) {
			reference = params;
			threshold[0] = t0;
			threshold[1] = t1;
		}
	}
}