package com.example.demigod.activity;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.demigod.R;
import com.example.demigod.drawing.Sprite;

public class HomeTown extends Activity implements View.OnTouchListener {

	private static int tileDimen;
	private int xAnchor, yAnchor;
	private Sprite hero;
	private Bitmap spriteHero, wallHoriz, wallVert, doorWood, roofVert,
			roofHoriz, grass1;
	private int[][] coordsWallHoriz, coordsWallVert, coordsDoor;
	private Bitmap bLeft, bUp, bRight, bDown, aButton, bButton;
	private Rect heroRect, perimeterRect; // Collision Rects
	private Rect bLeftRect, bUpRect, bRightRect, bDownRect, aButtonRect,
			bButtonRect; // Buttons
	private int screenWidth, screenHeight;
	private HomeTownCanvas homeTownCanvas;
	private SurfaceHolder townHolder;
	private Canvas c;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		homeTownCanvas = new HomeTownCanvas(this);
		homeTownCanvas.setOnTouchListener(this);

		new BitmapLoader().execute();

	}

	@Override
	protected void onPause() {
		super.onPause();
		homeTownCanvas.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		homeTownCanvas.resume();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		int x = (int) event.getX();
		int y = (int) event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:

			// Left Key Touched
			if (x <= bLeftRect.right && x >= bLeftRect.left
					&& y >= bLeftRect.top && y <= bLeftRect.bottom) {

				hero.setX(hero.getX() - tileDimen);
				heroRect.set(hero.getX(), hero.getY(), hero.getX() + tileDimen,
						hero.getY() + tileDimen);

				if (hero.getX() - tileDimen <= 0) {
					hero.setX(hero.getX() + tileDimen);
					heroRect.set(hero.getX(), hero.getY(), hero.getX()
							+ tileDimen, hero.getY() + tileDimen);
					xAnchor += tileDimen;
					updateCoords();
				}

				// Up Key Touched
			} else if (x <= bUpRect.right && x >= bUpRect.left
					&& y >= bUpRect.top && y <= bUpRect.bottom) {

				hero.setY(hero.getY() - tileDimen);
				heroRect.set(hero.getX(), hero.getY(), hero.getX() + tileDimen,
						hero.getY() + tileDimen);

				// Right Key Touched
			} else if (x <= bRightRect.right && x >= bRightRect.left
					&& y >= bRightRect.top && y <= bRightRect.bottom) {

				hero.setX(hero.getX() + tileDimen);
				heroRect.set(hero.getX(), hero.getY(), hero.getX() + tileDimen,
						hero.getY() + tileDimen);

				// Down Key Touched
			} else if (x <= bDownRect.right && x >= bDownRect.left
					&& y >= bDownRect.top && y <= bDownRect.bottom) {

				hero.setY(hero.getY() + tileDimen);
				heroRect.set(hero.getX(), hero.getY(), hero.getX() + tileDimen,
						hero.getY() + tileDimen);

			} else {
				// button was not pressed, do nothing
			}

			break;
		}

		return true;
	}

	public Bitmap getBitmapFromAssets(String filePath,
			BitmapFactory.Options opts) {
		AssetManager manager = getAssets();
		InputStream inStream = null;
		try {
			inStream = manager.open(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Bitmap bitmap = BitmapFactory.decodeStream(inStream, null, opts);
		return bitmap;
	}

	public void updateCoords() {
		coordsWallVert = new int[][] {
				{ xAnchor + (tileDimen * 2), yAnchor + (tileDimen * 3) },
				{ xAnchor + (tileDimen * 4), yAnchor + (tileDimen * 3) },
				{ xAnchor + (tileDimen * 5), yAnchor + (tileDimen * 3) } };
		coordsDoor = new int[][] { { xAnchor + (tileDimen * 3),
				yAnchor + (tileDimen * 3) } };
	}

	private class HomeTownCanvas extends SurfaceView implements
			SurfaceHolder.Callback, Runnable {

		volatile boolean running = false;
		private Thread t = null;
		private Context context;
		private Paint paint;

		public HomeTownCanvas(Context context) {
			super(context);
			this.context = context;
			Init();
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			townHolder = holder;
			c = townHolder.lockCanvas();
			draw(c);
			townHolder.unlockCanvasAndPost(c);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			running = true;
			t = new Thread(this);
			t.start();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {

		}

		@Override
		public void run() {

			while (running) {

				if (!townHolder.getSurface().isValid()) {
					continue;
				}

				c = townHolder.lockCanvas();

				c.drawColor(getResources().getColor(R.color.dark_grey));

				c.drawBitmap(grass1, xAnchor + (tileDimen * 3), yAnchor
						+ (tileDimen * 4), null);

				int x = 0;
				for (x = 0; x < coordsWallVert.length; x++) {
					c.drawBitmap(wallVert, coordsWallVert[x][0],
							coordsWallVert[x][1], null);
				}

				for (x = 0; x < coordsDoor.length; x++) {
					c.drawBitmap(doorWood, coordsDoor[x][0], coordsDoor[x][1],
							null);
				}

				hero.draw(c);

				updateHero();

				// Buttons
				c.drawBitmap(bLeft, null, bLeftRect, null);
				c.drawBitmap(bUp, null, bUpRect, null);
				c.drawBitmap(bRight, null, bRightRect, null);
				c.drawBitmap(bDown, null, bDownRect, null);
				c.drawBitmap(aButton, null, aButtonRect, null);
				c.drawBitmap(bButton, null, bButtonRect, null);

				townHolder.unlockCanvasAndPost(c);

			}

		}

		public void Init() {
			townHolder = getHolder();
			townHolder.addCallback(this);

			paint = new Paint();
			paint.setDither(false);
			paint.setColor(getResources().getColor(R.color.dark_grey));

			setWillNotDraw(false);
		}

		private void updateHero() {
			hero.update(System.currentTimeMillis());
		}

		public void pause() {
			running = false;

			while (running) {
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				break;
			}
			t = null;
		}

		public void resume() {
			running = true;
			t = new Thread(this);
			t.start();

		}
	}

	private class BitmapLoader extends AsyncTask<Void, Integer, Void> {

		private TextView tvLoadDescription;
		private ProgressBar progBarLoad;

		// Before running code in separate thread
		@Override
		protected void onPreExecute() {

			setContentView(R.layout.loading_screen);

			AnimationDrawable anim = new AnimationDrawable();
			anim.addFrame(
					getResources().getDrawable(R.drawable.shadow_knight_front1),
					150);
			anim.addFrame(
					getResources().getDrawable(R.drawable.shadow_knight_front2),
					150);
			anim.addFrame(
					getResources().getDrawable(R.drawable.shadow_knight_front1),
					150);
			anim.addFrame(
					getResources().getDrawable(R.drawable.shadow_knight_front3),
					150);

			anim.setOneShot(false);
			anim.start();

			ImageView ivLoadingSprite = (ImageView) findViewById(R.id.ivLoadingSprite);
			ivLoadingSprite.setImageDrawable(anim);

			tvLoadDescription = (TextView) findViewById(R.id.tvLoadingDescription);

			progBarLoad = (ProgressBar) findViewById(R.id.progBarLoadingScreen);
			progBarLoad.setIndeterminate(false);
			progBarLoad.setProgress(0);
			progBarLoad.setMax(100);

		}

		// The code to be executed in a background thread.
		@Override
		protected Void doInBackground(Void... params) {
			synchronized (this) {
				// Initialize an integer (that will act as a counter) to
				// zero
				int progressCounter = 0;

				while (progressCounter < 100) {

					tileDimen = (int) getResources().getDimension(
							R.dimen.tile_dimen);

					xAnchor = tileDimen;
					yAnchor = tileDimen;

					int buttonDimen = (int) getResources().getDimension(
							R.dimen.button_size);

					Point size = new Point();
					WindowManager w;
					w = getWindowManager();

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
						w.getDefaultDisplay().getSize(size);

						screenWidth = size.x;
						screenHeight = size.y;
					} else {
						Display d = w.getDefaultDisplay();
						screenWidth = d.getWidth();
						screenHeight = d.getHeight();
					}

					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inDither = true;
					opts.inPreferQualityOverSpeed = true;

					Bitmap bLeftTemp = getBitmapFromAssets(
							"drawables/views/left_key.png", opts);
					bLeft = Bitmap.createScaledBitmap(bLeftTemp, buttonDimen,
							buttonDimen, true);
					bLeftTemp.recycle();
					bLeftTemp = null;

					bLeftRect = new Rect(0, screenHeight - (buttonDimen * 2),
							buttonDimen, screenHeight - buttonDimen);

					Bitmap bUpTemp = getBitmapFromAssets(
							"drawables/views/up_key.png", opts);
					bUp = Bitmap.createScaledBitmap(bUpTemp, buttonDimen,
							buttonDimen, true);
					bUpTemp.recycle();
					bUpTemp = null;

					bUpRect = new Rect(buttonDimen, screenHeight
							- (buttonDimen * 3), buttonDimen * 2, screenHeight
							- (buttonDimen * 2));

					Bitmap bRightTemp = getBitmapFromAssets(
							"drawables/views/right_key.png", opts);
					bRight = Bitmap.createScaledBitmap(bRightTemp, buttonDimen,
							buttonDimen, true);
					bRightTemp.recycle();
					bRightTemp = null;

					bRightRect = new Rect(buttonDimen * 2, screenHeight
							- (buttonDimen * 2), buttonDimen * 3, screenHeight
							- buttonDimen);

					Bitmap bDownTemp = getBitmapFromAssets(
							"drawables/views/down_key.png", opts);
					bDown = Bitmap.createScaledBitmap(bDownTemp, buttonDimen,
							buttonDimen, true);
					bDownTemp.recycle();
					bDownTemp = null;

					bDownRect = new Rect(buttonDimen, screenHeight
							- buttonDimen, buttonDimen * 2, screenHeight);

					Bitmap aButtonTemp = getBitmapFromAssets(
							"drawables/views/a_button.png", opts);
					aButton = Bitmap.createScaledBitmap(aButtonTemp,
							buttonDimen, buttonDimen, true);
					aButtonTemp.recycle();
					aButtonTemp = null;

					aButtonRect = new Rect(screenWidth - (buttonDimen * 2),
							screenHeight - (buttonDimen * 2), screenWidth
									- buttonDimen, screenHeight - buttonDimen);

					Bitmap bButtonTemp = getBitmapFromAssets(
							"drawables/views/b_button.png", opts);
					bButton = Bitmap.createScaledBitmap(bButtonTemp,
							buttonDimen, buttonDimen, true);
					bButtonTemp.recycle();
					bButtonTemp = null;

					bButtonRect = new Rect(screenWidth - (buttonDimen * 4),
							screenHeight - (buttonDimen * 2), screenWidth
									- (buttonDimen * 3), screenHeight
									- (buttonDimen));

					// Load memory-efficient Bitmaps
					// grass

					Bitmap tempGrass1 = getBitmapFromAssets(
							"drawables/tiles/grass1.png", opts);
					grass1 = Bitmap.createScaledBitmap(tempGrass1, tileDimen,
							tileDimen, true);
					tempGrass1.recycle();
					tempGrass1 = null;

					Bitmap tempWallVert = getBitmapFromAssets(
							"drawables/objects/floor_wood_vertical.png", opts);
					wallVert = Bitmap.createScaledBitmap(tempWallVert,
							tileDimen, tileDimen, true);
					tempWallVert.recycle();
					tempWallVert = null;
					progressCounter = 10;
					publishProgress(progressCounter);

					Bitmap tempDoorWood = getBitmapFromAssets(
							"drawables/objects/door_wood.png", opts);
					doorWood = Bitmap.createScaledBitmap(tempDoorWood,
							tileDimen, tileDimen, true);
					tempDoorWood.recycle();
					tempDoorWood = null;

					Bitmap tempHeroSheet = getBitmapFromAssets(
							"drawables/characters/knight/knight_male_front_spritesheet.png",
							opts);
					spriteHero = Bitmap.createScaledBitmap(tempHeroSheet,
							tileDimen * 4, tileDimen, true);
					tempHeroSheet.recycle();
					tempHeroSheet = null;

					coordsWallVert = new int[][] {
							{ xAnchor + (tileDimen * 2),
									yAnchor + (tileDimen * 3) },
							{ xAnchor + (tileDimen * 4),
									yAnchor + (tileDimen * 3) },
							{ xAnchor + (tileDimen * 5),
									yAnchor + (tileDimen * 3) } };
					coordsDoor = new int[][] { { xAnchor + (tileDimen * 3),
							yAnchor + (tileDimen * 3) } };

					hero = new Sprite(getBaseContext(), spriteHero,
							tileDimen * 5, tileDimen * 5, 4, 4);
					heroRect = hero.getCollisionRect();

					progressCounter = 100;
					publishProgress(progressCounter);

				}
			}

			return null;
		}

		// Update the progress
		@Override
		protected void onProgressUpdate(Integer... values) {
			// set the current progress of the progress dialog
			progBarLoad.setProgress(values[0]);

			switch (progBarLoad.getProgress()) {

			case 5:
				tvLoadDescription.setText("Skipping water across rocks...");
				break;

			case 10:
				tvLoadDescription.setText("Calling in sick...");
				break;

			case 15:
				tvLoadDescription.setText("Chasing Chickens...");
				break;

			case 20:
				tvLoadDescription.setText("Smelling the coffee...");
				break;

			case 25:
				tvLoadDescription.setText("Calling mom...");
				break;

			case 30:
				tvLoadDescription.setText("Walking on sunshine...");
				break;

			case 100:
				tvLoadDescription.setText("Finally...");
				break;

			}
		}

		// after executing the code in the thread
		@Override
		protected void onPostExecute(Void result) {
			// initialize the View
			setContentView(homeTownCanvas);
		}
	}
}
