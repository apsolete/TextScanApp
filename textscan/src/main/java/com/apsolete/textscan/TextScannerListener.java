package com.apsolete.textscan;

public interface TextScannerListener {
    void onDetected(String detections);
    void onStateChanged(String state, int i);
}
