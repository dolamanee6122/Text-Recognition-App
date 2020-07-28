package com.example.textrecognition;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class mainPage extends AppCompatActivity {
    private static final String TAG = "mainPage";
    private static final int requestPermissionID = 101;
    CameraSource cameraSource;
    SurfaceView surfaceView;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        surfaceView = findViewById(R.id.surfaceView);
        textView = findViewById(R.id.textView);
        startCameraSource();
    }

    private void startCameraSource(){

        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if(!textRecognizer.isOperational()){
            Log.w(TAG,"Detector dependencies missing.");
            return;
        }
        else{
                cameraSource = new CameraSource.Builder(getApplicationContext(),textRecognizer)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(1280,1024)
                        .setAutoFocusEnabled(true)
                        .setRequestedFps(2.0f)
                        .build();

                surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceCreated(SurfaceHolder holder) {
                        if(ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(mainPage.this,
                                    new String[] {Manifest.permission.CAMERA},requestPermissionID);
                            return;
                        }
                        try {
                            cameraSource.start(surfaceView.getHolder());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder holder) {
                        cameraSource.stop();
                    }
                });

                textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                    @Override
                    public void release() {

                    }

                    @Override
                    public void receiveDetections(Detector.Detections<TextBlock> detections) {
                        final SparseArray<TextBlock> items = detections.getDetectedItems();
                        if(items.size()==0)return;

                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for(int i=0;i<items.size();++i){
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue()+"\n");
                                }
                                textView.setText(stringBuilder.toString());
                            }
                        });
                    }
                });


        }
    }
}
