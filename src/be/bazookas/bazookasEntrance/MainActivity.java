package be.bazookas.bazookasEntrance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import android.support.v7.app.ActionBarActivity;
import android.app.ActionBar.LayoutParams;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import be.bazookas.bazookasEntrance.R;

public class MainActivity extends ActionBarActivity implements
		CvCameraViewListener2 {
	private CameraBridgeViewBase mOpenCvCameraView;
	private String TAG = "Entrance_Bazookas_Debug";
	private WelcomeFragment wf;
	private VideoFragment vf;
	private Mat mRgba;
	private Mat mGray;
	private File mCascadeFile;
	private CascadeClassifier mJavaDetector;
	private DetectionBasedTracker mNativeDetector;
	public static final int JAVA_DETECTOR = 0;
	public static final int NATIVE_DETECTOR = 1;
	private int mDetectorType = NATIVE_DETECTOR;
	private String[] mDetectorName;

	private float mRelativeFaceSize = 0.2f;
	private int mAbsoluteFaceSize = 0;

	ImageView imgCrown;
	ImageView imgMoustache;
	TextView txtWelcome;
	HandleXML obj;
	private ArrayList<Person> persons;
	ArrayList<ArrayList<Point>>TLs = new ArrayList<ArrayList<Point>>();
	ArrayList<ArrayList<Point>>BRs = new ArrayList<ArrayList<Point>>();
	Point tl = new Point(0, 0);
	Point br = new Point(0, 0);
	private boolean timerIsRunning;
	private Timer t2 = new Timer();
	int numberOfFaces = 0;
	private ArrayList<ImageView> moustaches = new ArrayList<ImageView>();
	private ArrayList<ImageView> crowns = new ArrayList<ImageView>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// open();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		View decorView = getWindow().getDecorView();
		// Hide the status bar.
		if (Build.VERSION.SDK_INT < 16) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		setContentView(R.layout.activity_main);
		// getSupportActionBar().hide();
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		vf = new VideoFragment();
		ft.add(R.id.layVideo, vf);
		ft.commit();
		videoScreen = true;

		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);

		

	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");

				System.loadLibrary("detection_based_tracker");

				try {
					// load cascade file from application resources
					InputStream is = getResources().openRawResource(
							R.raw.lbpcascade_frontalface);
					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
					mCascadeFile = new File(cascadeDir,
							"lbpcascade_frontalface.xml");
					FileOutputStream os = new FileOutputStream(mCascadeFile);

					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					is.close();
					os.close();

					mJavaDetector = new CascadeClassifier(
							mCascadeFile.getAbsolutePath());
					if (mJavaDetector.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetector = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from "
								+ mCascadeFile.getAbsolutePath());

					mNativeDetector = new DetectionBasedTracker(
							mCascadeFile.getAbsolutePath(), 0);

					cascadeDir.delete();

				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
				}

				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	protected void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this,
				mLoaderCallback);
	};

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	private Mat Current;
	private Mat Prev;
	private Mat Difference;
	private boolean first = true;
	private boolean videoScreen = true;
	private boolean timerRunning = false;
	private int movementFrames = 0;
	private int i;
	private Timer t;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub
		Current = new Mat();
		Prev = new Mat();
		Difference = new Mat();
		hierarchy = new Mat();
	}

	@Override
	public void onCameraViewStopped() {

		Current.release();
		Prev.release();
		Difference.release();
		hierarchy.release();
	}

	Mat hierarchy;
	ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		Current = inputFrame.gray();
		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();
		Core.flip(mRgba, mRgba, 1);
		Core.flip(mGray, mGray, 1);
		if (first) {
			Current.copyTo(Prev);
			first = false;
		}
		try {
			Core.absdiff(Current, Prev, Difference);
		} catch (Exception ex) {
			// Current.copyTo(Prev);
			// Core.absdiff(Current, Prev, Difference);
		}
		org.opencv.imgproc.Imgproc.threshold(Difference, Difference, 35, 255,
				org.opencv.imgproc.Imgproc.THRESH_BINARY);

		if (videoScreen) {
			if (Core.countNonZero(Difference) > 1) {
				movementFrames++;
				if (movementFrames == 10) {
					Log.i(TAG, "Go to next");
					// wf = new WelcomeFragment();
					// FragmentManager fm = getFragmentManager();
					// FragmentTransaction ft = fm.beginTransaction();
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							vf.getView().setVisibility(SurfaceView.INVISIBLE);
							LinearLayout ll = (LinearLayout) findViewById(R.id.layVideo);
							RelativeLayout ll1 = (RelativeLayout) findViewById(R.id.layWelcome);
							LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
									LinearLayout.LayoutParams.MATCH_PARENT, 0,
									0.999f);
							LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
									LinearLayout.LayoutParams.MATCH_PARENT, 0,
									0.001f);
							ll.setLayoutParams(lp1);
							ll1.setLayoutParams(lp);
							first = true;
						}
					});
					vf.pauseVideo();
					// ft.add(R.id.layVideo, wf);
					// ft.commit();
					videoScreen = false;
					timerRunning = false;
				} else {
					i = 10 - movementFrames;
					Log.i(TAG, "move for another " + i + " frames");
				}
			} else {
				Log.i(TAG, "No movement");
				movementFrames = 0;
			}
		} else {
			getPerson();
			if (Core.countNonZero(Difference) < 35) {
				if (!timerRunning) {
					timerRunning = true;
					t = new Timer();
					t.schedule(new TimerTask() {

						@Override
						public void run() {

							Log.i(TAG, "Timer Finished");
							// FragmentManager fm = getFragmentManager();
							// FragmentTransaction ft = fm.beginTransaction();
							// ft.remove(wf);
							// ft.add(R.id.layVideo, vf);
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									vf.getView().setVisibility(
											SurfaceView.VISIBLE);
									LinearLayout ll = (LinearLayout) findViewById(R.id.layVideo);
									RelativeLayout ll1 = (RelativeLayout) findViewById(R.id.layWelcome);
									LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
											LinearLayout.LayoutParams.MATCH_PARENT,
											0, 0.999f);
									LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
											LinearLayout.LayoutParams.MATCH_PARENT,
											0, 0.001f);
									ll.setLayoutParams(lp);
									ll1.setLayoutParams(lp1);
									first = true;
								}
							});

							// ft.commit();
							vf.resumeVideo();
							videoScreen = true;
						}
					}, 5000);
				}
			} else {
				if (timerRunning) {
					timerRunning = false;
					t.cancel();
				}
				if (mAbsoluteFaceSize == 0) {
					int height = mGray.rows();
					if (Math.round(height * mRelativeFaceSize) > 0) {
						mAbsoluteFaceSize = Math.round(height
								* mRelativeFaceSize);
					}
					mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
				}

				MatOfRect faces = new MatOfRect();

				if (mDetectorType == JAVA_DETECTOR) {
					if (mJavaDetector != null)
						mJavaDetector.detectMultiScale(mGray, faces, 1.1,
								30,
								30, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
								new Size(mAbsoluteFaceSize, mAbsoluteFaceSize),
								new Size());
				} else if (mDetectorType == NATIVE_DETECTOR) {
					if (mNativeDetector != null)
						mNativeDetector.detect(mGray, faces);

				} else {
					Log.e(TAG, "Detection method is not selected!");
				}

				Rect[] facesArray = faces.toArray();
				
				if (facesArray.length == 0) {
					numberOfFaces =0;
					if (!timerIsRunning) {
						timerIsRunning = true;
						t2 = new Timer();
						t2.schedule(new TimerTask() {

							@Override
							public void run() {
								runOnUiThread(new Runnable() {

									@Override
									public void run() {
										timerIsRunning = false;
										TLs.clear();
										BRs.clear();
										
										for (ImageView img : crowns) {
											img.setVisibility(SurfaceView.GONE);
										}
										for (ImageView img : moustaches) {
											img.setVisibility(SurfaceView.GONE);
										}
									}
								});

							}
						}, 1000);
					}
				}

				else {
					if (timerIsRunning) {
						timerIsRunning = false;
						t2.cancel();
					}
					if (facesArray.length != numberOfFaces) {
						TLs.clear();
						BRs.clear();
						numberOfFaces = facesArray.length;
						for (int i = 0; i < facesArray.length; i++) {
							ArrayList<Point> tempTl = new ArrayList<Point>();
							for (int j=0;j<3;j++){
								tempTl.add(facesArray[i].tl());
							}
							TLs.add(tempTl);
							ArrayList<Point> tempBr = new ArrayList<Point>();
							for (int j=0;j<3;j++){
								tempBr.add(facesArray[i].br());
							}
							BRs.add(tempBr);
						}
						final RelativeLayout ll = (RelativeLayout) findViewById(R.id.layWelcome);
						for (final ImageView img : moustaches) {
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									ll.removeView(img);
								}
							});
						}
						for (final ImageView img : crowns) {

							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									ll.removeView(img);
								}
							});
						}
						crowns.clear();
						moustaches.clear();
						for (int i = 0; i < facesArray.length; i++) {
							final ImageView moustache = new ImageView(
									getApplicationContext());
							final ImageView crown = new ImageView(
									getApplicationContext());
							crowns.add(crown);
							moustaches.add(moustache);
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									moustache.setImageDrawable(getResources()
											.getDrawable(R.drawable.moustache));
									crown.setImageDrawable(getResources()
											.getDrawable(R.drawable.sombrero));
									RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(200, 78);
									RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(550, 280);
									moustache.setLayoutParams(lp);
									crown.setLayoutParams(lp1);
									
									ll.addView(crown);
									ll.addView(moustache);

								}
							});

						}
					}
					
					for (int i = 0; i < facesArray.length; i++) {
						imgCrown = crowns.get(i);
						imgMoustache = moustaches.get(i);
						int totalTlX = 0;
						int totalTlY = 0;
						int totalBrX = 0;
						int totalBrY = 0;
						TLs.get(i).remove(0);
						TLs.get(i).add(facesArray[i].tl());
						BRs.get(i).remove(0);
						BRs.get(i).add(facesArray[i].br());
						
						/*
						 * for (Point p : Tls) { tl = new
						 * Point((tl.x+p.x)*0.5,(tl.y+p.y)*0.5); }
						 */
						for (Point p : TLs.get(i)) {
							totalTlX += p.x;
							totalTlY += p.y;
						}
						for (Point p : BRs.get(i)) {
							totalBrX += p.x;
							totalBrY += p.y;
						}
						tl = new Point(totalTlX / TLs.get(i).size(), totalTlY
								/ TLs.get(i).size());
						br = new Point(totalBrX / BRs.get(i).size(), totalBrY
								/ BRs.get(i).size());
						final Point tlFinal = tl;

						/*
						 * for (Point p : Brs) { br = new
						 * Point((br.x+p.x)*0.5,(br.y+p.y)*0.5); }
						 */
						final Point brFinal = br;
						// Core.rectangle(mRgba, tlFinal, br, FACE_RECT_COLOR,
						// 3);

						runOnUiThread(new Runnable() {

							@Override
							public void run() {

								int width = (int) ((brFinal.x - tlFinal.x) * 2);
								int height = (int) ((width / 196.0) * 100);
								ViewGroup.LayoutParams p1 = imgCrown
										.getLayoutParams();
								p1.width = width;
								p1.height = height;
								imgCrown.setLayoutParams(p1);
								
								ViewGroup.LayoutParams p2 = imgMoustache
										.getLayoutParams();
								p2.width = (int) (width * 0.25);
								p2.height = (int) (((p2.width) * 0.005) * 78);
								imgMoustache.setLayoutParams(p2);

								imgCrown.setX((float) (tlFinal.x
										+ (brFinal.x - tlFinal.x) * 0.5 - width * 0.5) + 10);

								imgCrown.setY((float) (tlFinal.y - height * 0.667));
								imgMoustache
										.setY((float) (brFinal.y - ((brFinal.y - tlFinal.y) * 0.35)));
								imgMoustache
										.setX((float) ((tlFinal.x)
												+ (brFinal.x - tlFinal.x) * 0.5 - width * 0.125));
								imgCrown.setVisibility(SurfaceView.VISIBLE);
								imgMoustache.setVisibility(SurfaceView.VISIBLE);
							
							}
						});

					}
				}

			}
		}

		Current.copyTo(Prev);
		return mRgba;
	}
	private void getPerson() {
		 obj = new HandleXML(getString(R.string.ContentURL));
	      obj.fetchXML();
	      while(obj.parsingComplete);
	      persons = obj.getPersons();
	      runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				 txtWelcome = (TextView) findViewById(R.id.txtWelcome);
			      if (persons.size()>0)
			      txtWelcome.setText("Welcome "+persons.get(0).get_naam() + " from " + persons.get(0).get_company()+ "\nat Bazookas Mobile Agency");
			      else
			    	  txtWelcome.setText("Welcome at Bazookas Mobile Agency");
				
			}
		});
	    
	}

}
