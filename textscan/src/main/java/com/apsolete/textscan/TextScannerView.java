package com.apsolete.textscan;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.util.Log;

public class TextScannerView extends SurfaceView {

    private final String state = "";
    private boolean scanning = false;
    private TextScannerListener listener;

    public TextScannerView(Context context) {
        super(context);
    }

    public TextScannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextScannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnDetectedListener(Activity activity, TextScannerListener listener){
        this.listener = listener;
        scanning = true;
        new TextScanner(activity, this, listener);
    }

    protected void onDetected(String detections){
        Log.d("detections", detections);
    }

    protected void onStateChanged(String state, int i) {
        Log.d("state", state + " # " + i);
    }

    public String getState() {
        return state;
    }

    public boolean isScanning() {
        return scanning;
    }
}
