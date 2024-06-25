package com.apsolete.textscan;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class TextScanner {
    private final Activity activity;
    private SurfaceView camera;
    private TextScannerListener listener;

    private static final int REQUEST_CAMERA = 12;
    private static final String LOG_TEXT = "SCANNER";
    private static String state = "loading";
    private boolean showToasts = true;
    private static final boolean scanning = false;

    public TextScanner(Activity activity) {
        this.activity = activity;
    }

    public TextScanner(Activity activity, SurfaceView surfaceView) {
        this.activity = activity;
        setSurfaceView(surfaceView);
    }

    public TextScanner(Activity activity, SurfaceView surfaceView, TextScannerListener listener) {
        this.activity = activity;
        setSurfaceView(surfaceView);
        setListener(listener);
        scan();
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        this.camera = surfaceView;
    }

    public void setListener(TextScannerListener listener) {
        this.listener = listener;
    }

    public void scan() {
        prepareScanning();
    }

    private void prepareScanning() {
        if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            return;
        }
        TextRecognizer textRecognizer = new TextRecognizer.Builder(activity).build();
        if (!textRecognizer.isOperational()) {

            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean lowStorage = activity.registerReceiver(null, lowStorageFilter) != null;
            if (lowStorage) {
                state = "You have low storage";
                if (showToasts) Toast.makeText(activity, state, Toast.LENGTH_LONG).show();
                Log.e(LOG_TEXT,state);
                listener.onStateChanged(state, 2);
            } else {
                state = "OCR not ready";
                if (showToasts)Toast.makeText(activity, state, Toast.LENGTH_LONG).show();
                Log.e(LOG_TEXT,state);
                listener.onStateChanged(state, 3);
            }
            return;

        }
        cameraSource = new CameraSource.Builder(activity, textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setRequestedFps(2.0f)
                .setAutoFocusEnabled(true)
                .build();
        camera.getHolder().addCallback(surfaceHolderCallback);
        textRecognizer.setProcessor(textBlockProcessor);
    }

    public void showToasts(boolean show) {
        showToasts = show;
    }

    public String getState() {
        return state;
    }

    public void setScanning(boolean scanning) {
        if (scanning) {
            prepareScanning();
            scanning = true;
        } else {
            camera.destroyDrawingCache();
            scanning = false;
        }
    }

    public boolean isScanning() {
        return scanning;
    }

    private CameraSource cameraSource;
    private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
            try {
                if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    activity.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                    return;
                }
                cameraSource.start(camera.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) { }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
            cameraSource.stop();
        }
    };
    private Detector.Processor<TextBlock> textBlockProcessor = new Detector.Processor<TextBlock>() {
        @Override
        public void release() { }

        @Override
        public void receiveDetections(@NonNull Detector.Detections<TextBlock> detections) {
            state = "running";
            listener.onStateChanged(state, 1);

            final SparseArray<TextBlock> items = detections.getDetectedItems();
            if (items.size() != 0) {
                final StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < items.size(); ++i) {
                    TextBlock item = items.valueAt(i);
                    stringBuilder.append(item.getValue());
                    stringBuilder.append("\n");
                }
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        listener.onDetected(stringBuilder.toString());
                    }
                });
                Log.d(LOG_TEXT, stringBuilder.toString());
            }
        }
    };
}
