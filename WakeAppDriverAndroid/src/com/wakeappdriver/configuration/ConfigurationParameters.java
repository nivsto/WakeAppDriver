package com.wakeappdriver.configuration;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.wakeappdriver.R;
import com.wakeappdriver.configuration.Enums.StartMode;

public class ConfigurationParameters {
	
	private static SharedPreferences sharedPref;	
			
	public static void init(Context context){
		Resources res = context.getResources();
		sharedPref = context.getSharedPreferences(res.getString(R.string.awd_config_fname), Activity.MODE_PRIVATE);
	
	}
	
	public static int getWindowSize() {
		return sharedPref.getInt("windowSize", 15000);
	}

	public static void setWindowSize(int windowSize) {
		Editor editor = sharedPref.edit();
		editor.putInt("windowSize", windowSize);
		editor.apply();
	}

	public static int getMaxFrameQueueSize() {
		return sharedPref.getInt("maxFrameQueueSize", 20);
	}

	public static void setMaxFrameQueueSize(int maxFrameQueueSize) {
		Editor editor = sharedPref.edit();
		editor.putInt("maxFrameQueueSize", maxFrameQueueSize);
		editor.apply();	
	}

	public static double getAlertThreshold() {
		return sharedPref.getFloat("alertThreshold", (float) 0.15);
	}

	public static void setAlertThreshold(double alertThreshold) {
		Editor editor = sharedPref.edit();
		editor.putFloat("alertThreshold", (float)alertThreshold);
		editor.apply();	
	}

	public static String getAlertType() {
		return sharedPref.getString("alertType", "SimpleAlerter");
	}
	
//	public static String getAlertType(Context context) {
//		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//		return sharedPreferences.getString("alert_type", "ship_bell");
//	}
	
	public static int getAlert(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String audioFileName = sharedPreferences.getString("alert_type", "ship_bell");
		return getAudioFile(audioFileName);
	}

	private static int getAudioFile(String audioFileName) {
		if(audioFileName.equals("ship_bell"))
			return R.raw.ship_bell;
		if(audioFileName.equals("speaking_voice_wake_up_call"))
			return R.raw.speaking_voice_wake_up_call;
		if(audioFileName.equals("car_horn"))
			return R.raw.car_horn;
		// Add more alerts here.
	return 0;
}

	public static void setAlertType(String alertType) {
		Editor editor = sharedPref.edit();
		editor.putString("alertType", alertType);
		editor.apply();
	}
	
	public static int getVolume(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getInt("volume", 1);
	}

	public static int getMinSamples() {
		return sharedPref.getInt("minSamples", 50);
	}

	public static void setMinSamples(int minSamples) {
		Editor editor = sharedPref.edit();
		editor.putInt("minSamples", minSamples);
		editor.apply();	
	}

	public static int getLearningModeDuration() {
		return sharedPref.getInt("learningModeDuration", 2);
	}

	public static void setLearningModeDuration(int learningModeDuration) {
		Editor editor = sharedPref.edit();
		editor.putInt("learningModeDuration", learningModeDuration);
		editor.apply();	
	}

	public static int getDurationBetweenAlerts() {
		return sharedPref.getInt("durationBetweenAlerts", 3);
	}

	public static void setDurationBetweenAlerts(int durationBetweenAlerts) {
		Editor editor = sharedPref.edit();
		editor.putInt("durationBetweenAlerts", durationBetweenAlerts);
		editor.apply();	
	}

	public static boolean getCameraMode() {
		return sharedPref.getBoolean("cameraMode", false);	// true = native, false = java
	}

	public static void setCameraMode(boolean cameraMode) {
		Editor editor = sharedPref.edit();
		editor.putBoolean("cameraMode", cameraMode);
		editor.apply();	
	}

	public static int getClosedAlertLimit() {
		return sharedPref.getInt("closedAlertLimit", 1000);
	}
	
	public static void setClosedAlertLimit(int closedAlertLimit) {
		Editor editor = sharedPref.edit();
		editor.putInt("closedAlertLimit", closedAlertLimit);
		editor.apply();		
	}
	
	public static double getBlinkLimit() {
		return sharedPref.getFloat("blinkLimit", (float) 0.4);
	}
	
	public static void setBlinkLimit(double blinkLimit) {
		Editor editor = sharedPref.edit();
		editor.putFloat("blinkLimit", (float)blinkLimit);
		editor.apply();	
	}
	public static long getEmergencyCooldown() {
		return sharedPref.getLong("emergencyCooldown", (long) 3000);
	}
	
	public static void setEmergencyCooldown(long emergencyCooldown) {
		Editor editor = sharedPref.edit();
		editor.putLong("emergencyCooldown", emergencyCooldown);
		editor.apply();	
	}
	public static StartMode getStartMode() {
		String modeName = sharedPref.getString("StartMode", StartMode.ACTIVITY.name());
		return StartMode.toStartMode(modeName);
	}
	
	public static void setStartMode(StartMode startMode) {
		Editor editor = sharedPref.edit();
		editor.putString("StartMode", startMode.name());
		editor.apply();		
	}

	public static boolean getCollectMode(){
		return sharedPref.getBoolean("collectMode", false);
	}
	
	public static void setCollectMode(boolean isInCollectingMode){
		Editor editor = sharedPref.edit();
		editor.putBoolean("collectMode", isInCollectingMode);
		editor.apply();
	}

	public static int getDrowsinessAssumption(){
		return sharedPref.getInt("drowsinessAssumption", -1);
	}
	
	public static boolean getDisplayBar(Context context){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getBoolean("display_drowsiness_bar", true);
	}
	
	public static void setDrosinessAssumption(int drowsinessAssumption){
		Editor editor = sharedPref.edit();
		editor.putInt("drowsinessAssumption", drowsinessAssumption);
		editor.apply();
	}
	
	public static int getNumOfWindowsBetweenTwoQueries(){
		//must be at least 2 to work properly
		return sharedPref.getInt("numOfWindowsBetweenTwoQueries", 2);
	}
	
	public static void setNumOfWindowsBetweenTwoQueries(int numOfWindowsBetweenTwoQueries){
		Editor editor = sharedPref.edit();
		editor.putInt("numOfWindowsBetweenTwoQueries", numOfWindowsBetweenTwoQueries);
		editor.apply();
	}
}