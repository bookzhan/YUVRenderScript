package com.luoye.yuvrenderscript;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.widget.ImageView;

import com.luoye.bzcamera.BZCameraView;
import com.luoye.bzcamera.listener.CameraStateListener;
import com.luoye.yuvrenderscript.R;

public class CameraActivity extends AppCompatActivity {

    private ImageView image_view;
    private BZCameraView bz_camera_view;
    private FastYV12toRGB fastYV12toRGB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        fastYV12toRGB = new FastYV12toRGB(getApplicationContext());

        image_view = findViewById(R.id.image_view);
        bz_camera_view = findViewById(R.id.bz_camera_view);
        bz_camera_view.setNeedCallBackData(true);
        bz_camera_view.setPreviewTargetSize(480, 640);
        bz_camera_view.setPreviewFormat(ImageFormat.YV12);
        bz_camera_view.setCameraStateListener(new CameraStateListener() {
            @Override
            public void onPreviewSuccess(Camera camera, int width, int height) {
            }

            @Override
            public void onPreviewFail(String message) {

            }

            @Override
            public void onPreviewDataUpdate(byte[] data, int width, int height, int displayOrientation, int cameraId) {
                final Bitmap bitmap = fastYV12toRGB.convertYV12toBitmap(data, width, height, displayOrientation);
                image_view.post(new Runnable() {
                    @Override
                    public void run() {
                        image_view.setImageBitmap(bitmap);
                    }
                });
            }

            @Override
            public void onCameraClose() {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bz_camera_view.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bz_camera_view.onPause();
    }
}
