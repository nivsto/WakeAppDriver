package com.wakeappdriver.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

import com.wakeappdriver.classes.CapturedFrame;
import com.wakeappdriver.classes.FrameAnalyzer;
import com.wakeappdriver.classes.FrameQueue;
import com.wakeappdriver.classes.FrameQueueManager;
import com.wakeappdriver.classes.NoIdentificationAlerter;
import com.wakeappdriver.classes.ResultQueue;
import com.wakeappdriver.classes.WindowAnalyzer;
import com.wakeappdriver.configuration.ConfigurationParameters;
import com.wakeappdriver.enums.FrameAnalyzerType;
import com.wakeappdriver.enums.FrameQueueType;
import com.wakeappdriver.enums.IndicatorType;
import com.wakeappdriver.implementations.BlinkDurationIndicator;
import com.wakeappdriver.implementations.PercentCoveredFrameAnalyzer;
import com.wakeappdriver.implementations.PerclosIndicator;
import com.wakeappdriver.implementations.SimpleAlerter;
import com.wakeappdriver.implementations.WakeAppPredictor;
import com.wakeappdriver.interfaces.Alerter;
import com.wakeappdriver.interfaces.Indicator;
import com.wakeappdriver.interfaces.Predictor;
import com.wakeappdriver.tasks.DetectorTask;
import com.wakeappdriver.R;

import android.os.Bundle;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.WindowManager;


public class GoActivity extends Activity implements CvCameraViewListener2 {
	private static final String TAG = "WAD";

	private CameraBridgeViewBase   mOpenCvCameraView;

	private CascadeClassifier mFaceDetector;
	private CascadeClassifier mRightEyeDetector;
	
	private FrameQueueManager queueManager;
	private List<FrameAnalyzer> frameAnalyzers;
	private DetectorTask detector;
	
	private Thread detectionTask;
	private List<Thread> frameAnalyzerTasks;

	private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				Log.d(TAG, Thread.currentThread().getName() + " :: OpenCV loaded successfully");
				try {
					// load face-classifier file from application resources
					InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
					File mFaceCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
					FileOutputStream os = new FileOutputStream(mFaceCascadeFile);

					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					is.close();
					os.close();

					// load right-eye-classifier file from application resources
					InputStream iser = getResources().openRawResource(R.raw.haarcascade_lefteye_2splits);
					File cascadeDirER = getDir("cascadeER", Context.MODE_PRIVATE);
					File cascadeFileER = new File(cascadeDirER, "haarcascade_eye_right.xml");
					FileOutputStream oser = new FileOutputStream(cascadeFileER);

					byte[] bufferER = new byte[4096];
					int bytesReadER;
					while ((bytesReadER = iser.read(bufferER)) != -1) {
						oser.write(bufferER, 0, bytesReadER);
					}
					iser.close();
					oser.close();

					// create face-classifier
					mFaceDetector = new CascadeClassifier(mFaceCascadeFile.getAbsolutePath());
					if (mFaceDetector.empty()) {
						Log.e(TAG, Thread.currentThread().getName() + " :: Failed to load cascade classifier");
						mFaceDetector = null;
					} else
						Log.d(TAG, Thread.currentThread().getName() + " :: Loaded cascade classifier from " + mFaceCascadeFile.getAbsolutePath());

					// create right-eye-classifier
					mRightEyeDetector = new CascadeClassifier(cascadeFileER.getAbsolutePath());
					if (mRightEyeDetector.empty()) {
						Log.e(TAG, Thread.currentThread().getName() + " :: Failed to load cascade classifier");
						mRightEyeDetector = null;
					} else
						Log.d(TAG, Thread.currentThread().getName() + " :: Loaded cascade classifier from " + cascadeFileER.getAbsolutePath());

					// set classifiers to frame-analyzers
					((PercentCoveredFrameAnalyzer)frameAnalyzers.get(0)).setmFaceDetector(mFaceDetector);
					((PercentCoveredFrameAnalyzer)frameAnalyzers.get(0)).setmRightEyeDetector(mRightEyeDetector);
					
					cascadeDir.delete();
					cascadeDirER.delete();
										
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, Thread.currentThread().getName() + " :: Failed to load cascade. Exception thrown: " + e);
				}

				mOpenCvCameraView.enableView();
			}
			break;
			default:
			{
				super.onManagerConnected(status);
			} break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_view_listener_activty);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Show the Up button in the action bar.

		Resources res = getResources();
		ConfigurationParameters params = new ConfigurationParameters(this);

		this.init(params); //initialize WAD data structures
		
		Thread.currentThread().setName("CameraThread");

		//java camera view works alot faster than native camera view
		if(params.getCameraMode()){
			mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.native_camera_surface_view);
		} else {
			mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_camera_surface_view);
		}
		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.setCameraIndex(1);
		mOpenCvCameraView.enableFpsMeter();
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera_view_listener_activty, menu);
		return true;
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		this.finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if ((keyCode == KeyEvent.KEYCODE_BACK))
	    {
	        finish();
	    }
	    return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, Thread.currentThread().getName() + " :: service has been closed");
		
		super.onDestroy();
		mOpenCvCameraView.disableView();
		this.queueManager.killManager();
		for (Thread t : frameAnalyzerTasks) {
			try {
				t.join(1000);
			} catch (InterruptedException e) {
				Log.e(TAG, Thread.currentThread().getName() + " :: couldn't kill thread " + t.getName());
			}
		}
			
		detector.killDetector();
		try {
			detectionTask.join(detector.getWindowSize());
		} catch (InterruptedException e) {
			Log.e(TAG, Thread.currentThread().getName() + " :: couldn't kill thread " + detectionTask.getName());
		}
	}

	@Override
	public void onCameraViewStarted(int width, int height) {		
	}

	@Override
	public void onCameraViewStopped() {
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		
		Mat gray = inputFrame.gray().clone();
		Mat rgba = inputFrame.rgba().clone();
		Long timestamp = System.currentTimeMillis();
		queueManager.putFrame(new CapturedFrame(timestamp, rgba, gray));
		return rgba;
	}


	//init data structures
	private void init(ConfigurationParameters params){
		
		FrameQueue frameQueue1 = new FrameQueue(params.getMaxFrameQueueSize(),FrameQueueType.PERCENT_COVERED_QUEUE);
//		FrameQueue frameQueue2 = new FrameQueue(10,FrameQueueType.HEAD_INCLINATION_QUEUE);
//		FrameQueue frameQueue3 = new FrameQueue(10,FrameQueueType.YAWN_SIZE_QUEUE);

		List<FrameQueue> frameQueueList = new ArrayList<FrameQueue>();
		frameQueueList.add(frameQueue1);
//		frameQueueList.add(frameQueue2);
//		frameQueueList.add(frameQueue3);

		ResultQueue resultQueue1 = new ResultQueue(FrameAnalyzerType.PERCENT_COVERED);
//		ResultQueue resultQueue2 = new ResultQueue(FrameAnalyzerType.HEAD_INCLINATION);
//		ResultQueue resultQueue3 = new ResultQueue(FrameAnalyzerType.YAWN_SIZE);

		List<ResultQueue> resultQueueList = new ArrayList<ResultQueue>();
		resultQueueList.add(resultQueue1);
//		resultQueueList.add(resultQueue2);
//		resultQueueList.add(resultQueue3);

		queueManager = new FrameQueueManager(frameQueueList);

		PercentCoveredFrameAnalyzer frameAnalyzer1 = new PercentCoveredFrameAnalyzer(queueManager,frameQueue1,resultQueue1);
//		FrameAnalyzer frameAnalyzer2 = new StubFrameAnalyzer(queueManager,frameQueue2,resultQueue2);
//		FrameAnalyzer frameAnalyzer3 = new StubFrameAnalyzer(queueManager,frameQueue3,resultQueue3);
		this.frameAnalyzers = new ArrayList<FrameAnalyzer>();
		this.frameAnalyzers.add(frameAnalyzer1);

		int minSamples = params.getMinSamples();
		Indicator indicator1 = new PerclosIndicator(minSamples);
		Indicator indicator2 = new BlinkDurationIndicator(5);
//		Indicator indicator3 = new StubIndicator(7);

		HashMap<IndicatorType,Indicator> indicatorList = new HashMap<IndicatorType,Indicator>();
		indicatorList.put(IndicatorType.PERCLOS, indicator1);
		indicatorList.put(IndicatorType.BLINK_DURATION, indicator2);
//		indicatorList.add(indicator3);

		this.frameAnalyzerTasks = new ArrayList<Thread>();
		for (FrameAnalyzer frameAnalyzer : this.frameAnalyzers) {
			Thread t = new Thread(frameAnalyzer);
			t.setName(frameAnalyzer.getClass().getSimpleName());
			frameAnalyzerTasks.add(t);
			t.start();
		}

		double alertThreshold = params.getAlertThreshold();
		int windowSize = params.getWindowSize();

		Alerter alerter;
		try {
		Class<?> clazz = Class.forName(params.getAlertType());
		Constructor<?> constructor = clazz.getConstructor(Context.class);
		alerter = (Alerter)(constructor.newInstance(this));
		} catch (Exception e) {
			alerter = new SimpleAlerter(this);
		}

		Alerter noIdenAlerter = new NoIdentificationAlerter(this);
		WindowAnalyzer windowAnalyzer = new WindowAnalyzer(resultQueueList, indicatorList);
		Predictor predictor = new WakeAppPredictor();
		
		int learningModeDuration = params.getLearningModeDuration();
		int durationBetweenAlerts = params.getDurationBetweenAlerts();
		boolean collectMode = params.getCollectMode();
		int numOfWindowsBetweenTwoQueries = params.getNumOfWindowsBetweenTwoQueries();
		String android_id = Secure.getString(getBaseContext().getContentResolver(),
                Secure.ANDROID_ID);
		
		this.detector = new DetectorTask(alerter, windowAnalyzer, predictor, alertThreshold, windowSize, noIdenAlerter,
				learningModeDuration, durationBetweenAlerts,collectMode,numOfWindowsBetweenTwoQueries, android_id);

		this.detectionTask = new Thread(this.detector);
		detectionTask.setName("DetectionTask");
		detectionTask.start();
	}

}
