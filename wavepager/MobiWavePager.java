package com.anytum.mobipower.wavepager;

import java.util.ArrayList;
import java.util.List;

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
import android.graphics.Region;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Toast;

import com.anytum.mobipower.R;
import com.anytum.mobipower.util.Constants;
import com.anytum.mobipower.util.Utils;

public class MobiWavePager extends WavePager {
	public static final int MARK_NUM = 6;
	private static final float SEGMENT_RATIO = 1f / 4;// MARK_NUM-2

	// final boolean MARK_GLOW_3TH = false;
	final int HEIGHT_DIVIDER = 8;
	final float WAVE_SPEED = 0.003f;
	// boolean mEnableRouteHighlight = true;

	private Bitmap mBmpMarkGlow;
	private Point mPointCenterMarkGlow;
	private Paint mPaintMarkGlow;

	private Bitmap mBmpMarkGlowBlue;
	private Bitmap mBmpMarkGlowOrange;
	private Point mPointCenterMarkHighlight;
	private Paint mPaintMarkHighlight;

	private Bitmap mBmpLine;

	private Bitmap mBmpTag;
	private Bitmap mBmpTagSelected;
	private int mTagWidth;
	private int mTagHeight;
	private int mTagMiddle;
	private Paint mPaintTagTime;
	private final int mTagTimeTextHeight;
	private Paint mPaintTagStep;
	private final int mTagStepTextHeight;
	private int mTagStepMiddle;

	private Paint mPaintTransparent;
	private Paint mPaintWave;
	private Paint mPaintMark;
	private Path mPath;
	private float mInterval;
	private float mSegment;
	private float mOffsetX = 0;
	private float mOffsetTarget = 0;
	private boolean mTouching = false;
	private boolean mShifting = true;
	private boolean mSelected = true;
	private float mOriginalX;
	private float mOriginalY;
	private int mWidth;
	private int mHeight;

	private MobiWaveMark[] mMark = new MobiWaveMark[MARK_NUM];

	private SineWave mSineFollower;
	private SineWave mSineWave;
	private boolean mTouchable = true;

	private WaveAdapter mAdapter;

	
	private OnWaveClickListener mOnWaveClickListener;
	private Region region1, region2, region3, region4;
	private List<Region> regions = new ArrayList<Region>();

	public MobiWavePager(final Context context) {
		this(context, null);
	}

	public MobiWavePager(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MobiWavePager(final Context context, final AttributeSet attrs,
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

		mPaintMark = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintMark.setStyle(Paint.Style.STROKE);
		mPaintMark.setStrokeWidth(5);
		mPaintMark.setColor(Color.WHITE);
		mPaintMark.setAlpha(128);

		// Wave parameters.
		mSineWave = new SineWave();
		mSineFollower = new SineWave();
		mSineFollower.changePhase(0.05f);

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
		if (mBmpLine != null) {
			mBmpLine.recycle();
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
		final float textScale = context.getResources().getDisplayMetrics().density;
		mTagTimeTextHeight = (int) (10f * textScale);
		mTagStepTextHeight = (int) (13f * textScale);
		mPaintMark.setStrokeWidth((int) (2f * textScale));
		mPaintTagTime = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintTagTime.setTypeface(Typeface.SANS_SERIF);
		mPaintTagTime.setColor(0xFF888888);
		mPaintTagTime.setTextSize(mTagTimeTextHeight);
		mPaintTagTime.setTextAlign(Paint.Align.CENTER);
		mPaintTagStep = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintTagStep.setTypeface(Typeface.SANS_SERIF);
		mPaintTagStep.setColor(Color.WHITE);
		mPaintTagStep.setTextSize(mTagStepTextHeight);
		mPaintTagStep.setTextAlign(Paint.Align.CENTER);
		Rect rect = new Rect();
		String maxStep = "99999";
		mPaintTagStep.getTextBounds(maxStep, 0, maxStep.length(), rect);
		mTagStepMiddle = rect.height() / 2;

		mAdapter = new WaveAdapter();

		region1 = new Region();
		region2 = new Region();
		region3 = new Region();
		region4 = new Region();
		regions.add(region1);
		regions.add(region2);
		regions.add(region3);
		regions.add(region4);
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
		mInterval = (float) w / (SineWave.WAVE_LENGTH - 1);
		mPath = new Path();
		mSegment = (float) w * SEGMENT_RATIO;
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
		// boolean isRouteHighlight;
		synchronized (this) {
			isTouching = mTouching;
			// isRouteHighlight = mEnableRouteHighlight;
		}

		if (mAdapter.cleanDirt()) {
			for (int i = mAdapter.getCurrentIndex() - 3, j = 0; i <= mAdapter
					.getCurrentIndex() + 2; i++, j++) {
				mMark[j] = (MobiWaveMark) mAdapter.getItemMark(i);
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
					pushPrevMark(mAdapter.getItemMark(mAdapter
							.getCurrentIndex() - 3));
					if (mAdapter.getCurrentIndex() == -1) {// 0
						mOffsetX = 0;
						mOffsetTarget = 0;
					}
				}
			} else if (mOffsetX <= -mSegment) {
				mOffsetX += mSegment;
				mOffsetTarget += mSegment;
				synchronized (this) {
					// shift in a next mark.
					mAdapter.setCurrentIndex(mAdapter.getCurrentIndex() + 1);
					pushNextMark(mAdapter.getItemMark(mAdapter
							.getCurrentIndex() + 2));
					if (mAdapter.getCurrentIndex() == mAdapter.getCount() - 2) {
						mOffsetX = 0;
						mOffsetTarget = 0;
					}
				}
			}
		}

		if (mSelected) {
			if (isTouching || mShifting) {
				mSelected = false;
			}
		} else {
			// Log.e("selected", isTouching+":"+mShifting);
			if (!isTouching && !mShifting) {
				mSelected = true;
				mAdapter.onSelected();
			}
		}

		// Move and draw wave followers.
		mPaintWave.setColor(0xFFFFFFFF);
		mPaintWave.setAlpha(128);
		mPaintWave.setStrokeWidth(3);
		mSineFollower.changePhase(WAVE_SPEED);
		mSineFollower.updateWave();
		canvas.drawPath(mSineFollower.getPath(), mPaintWave);
		// Move wave toward the mMark with bigger step count.
		mSineWave.changePhase(WAVE_SPEED);
		mSineWave.updateWave();

		// Draw route highlight.
		// if(isRouteHighlight){
		mPaintWave.setColor(0x8029E5FF); //
		mPaintWave.setStrokeWidth(15);
		for (int i = 0; i < MARK_NUM; i++) {
			if (mMark[i] != null) {
				float cr = (-mSegment / 2 + mSegment * i + mOffsetX) / mWidth;
				// Draw path
				if (mMark[i].type == MobiWaveMark.TYPE_ENTER) {
					canvas.drawPath(mSineWave.getPath(cr - SEGMENT_RATIO, cr),
							mPaintWave);
				} else if (mMark[i].type == MobiWaveMark.TYPE_EXIT) {
					canvas.drawPath(mSineWave.getPath(cr, cr + SEGMENT_RATIO),
							mPaintWave);

				}
			}
		}
		// Draw main wave
		mPaintWave.setColor(0xFFFFFFFF);
		mPaintWave.setAlpha(255);
		mPaintWave.setStrokeWidth(5);
		canvas.drawPath(mSineWave.getPath(), mPaintWave);

		// Draw marks.
		mPaintWave.setAlpha(255);
		for (int i = 0; i < MARK_NUM; i++) {
			if (mMark[i] != null) {
				float cx = -mSegment / 2 + mSegment * i + mOffsetX;
				float cr = cx / mWidth;
				float cy = mSineWave.getWaveAt(cr) + mHeight / 2;

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
						// »æÖÆÊúÏß
					}
					// else if(Math.abs(cx-mSegment*5/2) < mSegment/4){
					// canvas.drawBitmap(mBmpMarkGlowBlue, cx -
					// mPointCenterMarkHighlight.x, cy -
					// mPointCenterMarkHighlight.y, mPaintMarkHighlight);
					// }
				}

				// Draw Marks
				canvas.drawCircle(cx, cy, 5, mPaintMark);
				canvas.drawCircle(cx, cy, 15, mPaintMark);

				// Draw tags
				if ((!mShifting && (i == MARK_NUM - 2))//
						|| (mShifting && (mOffsetX == mOffsetTarget) && (i == MARK_NUM - 2))
						|| (mShifting && (mOffsetX < mOffsetTarget) && (i == MARK_NUM - 3))
						|| (mShifting && (mOffsetX > mOffsetTarget) && (i == MARK_NUM - 1))) {
					canvas.drawBitmap(mBmpTagSelected, cx - mTagWidth / 2, cy
							- mTagHeight, null);
					mPaintTagTime.setColor(0xFFFFFFFF);
					canvas.drawText(mMark[i].dateStr, cx, cy - mTagHeight
							- mTagTimeTextHeight, mPaintTagTime);
					// canvas.drawText(mMark[i].timeStr, cx, cy - mTagHeight,
					// mPaintTagTime);
				} else {
					canvas.drawBitmap(mBmpTag, cx - mTagWidth / 2, cy
							- mTagHeight, null);
					mPaintTagTime.setColor(0xFF888888);
					canvas.drawText(mMark[i].dateStr, cx, cy - mTagHeight
							- mTagTimeTextHeight, mPaintTagTime);
					// canvas.drawText(mMark[i].timeStr, cx, cy - mTagHeight,
					// mPaintTagTime);
				}
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
				mOriginalY = event.getY();
				// Log.e("touch", "down");

				break;
			case MotionEvent.ACTION_MOVE:
				if (scroll(event.getX() - mOriginalX)) {
					mOriginalX = event.getX();
				}
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				synchronized (this) {
					mTouching = false;
				}
				//				if (Math.abs(event.getX() - mOriginalX) < 10
				//						&& Math.abs(event.getY() - mOriginalY) < 10) {
				//					scrollControl(true);
				//				}
				if (Math.abs(event.getX() - mOriginalX) < 10
						&& Math.abs(event.getY() - mOriginalY) < 10) {
					for (int i = 0; i < 4; i++) {
						float cx = -mSegment / 2 + mSegment*(i+1) + mOffsetX;
						float cr = cx / mWidth;
						float cy = mSineWave.getWaveAt(cr) + mHeight / 2;
						regions.get(i).set(
								(int) (cx - mPointCenterMarkHighlight.x),
								(int) (cy - mPointCenterMarkHighlight.y),
								(int) (cx + mPointCenterMarkHighlight.x),
								(int) (cy + mPointCenterMarkHighlight.y));
					}
					if (region1.contains((int) mOriginalX, (int) mOriginalY)) {
						mOffsetTarget+=mSegment*3;
					}
					else if (region2.contains((int) mOriginalX, (int) mOriginalY)) {
						mOffsetTarget+=mSegment*2;
					}
					else if (region3.contains((int) mOriginalX, (int) mOriginalY)) {
						mOffsetTarget+=mSegment;
					}
					else if (region4.contains((int) mOriginalX, (int) mOriginalY)) {
						mOnWaveClickListener.onWaveClick();
					}

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

	public boolean scrollControl(boolean flag) {
		/* return if scroll happens. */
		boolean isMoved = false;
		if (flag) {
			// Move to previous
			synchronized (this) {
				if (mAdapter.getCurrentIndex() > -1) {
					mOffsetTarget += mSegment;
					isMoved = true;
				}
			}
			if (isMoved) {
				playSoundEffect();
			}
			return true;
		} else {
			// Move to next
			synchronized (this) {
				if (mAdapter.getCurrentIndex() < mAdapter.getCount() - 2) {
					mOffsetTarget -= mSegment;
					isMoved = true;
				}
			}
			if (isMoved) {
				playSoundEffect();
			}
			return true;
		}
	}

	public boolean scroll(float deltaX) {
		/* return if scroll happens. */
		boolean isMoved = false;
		if (deltaX > mSegment / 2) {
			// Move to previous
			synchronized (this) {
				if (mAdapter.getCurrentIndex() > -1) {// 0 //-1
					mOffsetTarget += mSegment;
					isMoved = true;
				}
			}
			if (isMoved) {
				playSoundEffect();
			}
			return true;
		} else if (deltaX < -mSegment / 2) {
			// Move to next
			synchronized (this) {
				if (mAdapter.getCurrentIndex() < mAdapter.getCount() - 2) {
					mOffsetTarget -= mSegment;
					isMoved = true;
				}
			}
			if (isMoved) {
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
			mMark[mMark.length - 1] = (MobiWaveMark) nextMark;
		}
	}

	public void pushPrevMark(WaveMark prevMark) {
		synchronized (this) {
			for (int i = mMark.length - 1; i > 0; i--) {
				mMark[i] = mMark[i - 1];
			}
			mMark[0] = (MobiWaveMark) prevMark;
		}
	}

	public void pushToNext() {
		mOffsetX += mSegment;
		mOffsetTarget += mSegment;
		if (mAdapter.getCurrentIndex() != mAdapter.getCount() - 1) {
			mAdapter.setCurrentIndex(mAdapter.getCurrentIndex() + 1);
			pushNextMark(mAdapter.getItemMark(mAdapter.getCurrentIndex() + 2));
		}
	}

	public void pushToFront() {
		mOffsetX -= mSegment;
		mOffsetTarget -= mSegment;
		if (mAdapter.getCurrentIndex() != 0) {
			mAdapter.setCurrentIndex(mAdapter.getCurrentIndex() - 1);
			pushPrevMark(mAdapter.getItemMark(mAdapter.getCurrentIndex() - 3));
		}
	}

	private void playSoundEffect() {
		if (Constants.isVoicePlay) {
			((Vibrator) this.getContext().getSystemService(
					Context.VIBRATOR_SERVICE)).vibrate(20);

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

	private class SineWave {
		public static final int WAVE_LENGTH = 51;
		float magnitude = 1f;
		float phase = 0f;
		float[] wave = new float[WAVE_LENGTH];

		public SineWave() {
		}

		void changePhase(float v0) {
			phase += v0;
		}

		void updateWave() {
			float interval = 1.0f / wave.length;
			float rx = 0;
			for (int i = 0; i < wave.length; i++) {
				wave[i] = getWaveAt(rx);
				rx += interval;
			}
		}

		float getWaveAt(float rx) {
			return (float) (magnitude * Math.sin(2 * Math.PI * (rx + phase))
					* mHeight / HEIGHT_DIVIDER);
		}

		public Path getPath() {
			mPath.reset();
			float x = 0;
			mPath.moveTo(x, wave[0]);
			for (int i = 1; i < wave.length; i++) {
				x += mInterval;
				mPath.lineTo(x, wave[i]);
			}
			mPath.offset(0, mHeight / 2);

			return mPath;
		}

		public Path getPath(float r0, float r1) {
			mPath.reset();
			int i0 = (int) (r0 * WAVE_LENGTH);
			int i1 = (int) (r1 * WAVE_LENGTH);
			if (i0 < 0) {
				i0 = 0;
			}
			if (i0 >= WAVE_LENGTH - 1) {
				i0 = WAVE_LENGTH - 2;
			}
			if (i1 < 0) {
				i1 = 0;
			}
			if (i1 >= WAVE_LENGTH) {
				i1 = WAVE_LENGTH - 1;
			}
			float x = i0 * mInterval;
			mPath.moveTo(x, wave[i0]);
			for (int i = i0 + 1; i <= i1; i++) {
				x += mInterval;
				mPath.lineTo(x, wave[i]);
			}
			mPath.offset(0, mHeight / 2);
			return mPath;
		}
	}

	public interface OnWaveClickListener {
		public void onWaveClick();
	}

	public void setOnWaveClickListener(OnWaveClickListener mOnWaveClickListener) {
		this.mOnWaveClickListener = mOnWaveClickListener;
	}

}
