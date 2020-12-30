package com.luoye.yuvrenderscript;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.widget.ImageView;

import com.luoye.bzcamera.BZCameraView;
import com.luoye.bzcamera.listener.OnCameraStateListener;

public class CameraActivity extends AppCompatActivity {

    private ImageView image_view;
    private BZCameraView bz_camera_view;
    private YUVConvertUtil yuvConvertUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        yuvConvertUtil = new YUVConvertUtil(getApplicationContext());

        image_view = findViewById(R.id.image_view);
        bz_camera_view = findViewById(R.id.bz_camera_view);
        bz_camera_view.setNeedCallBackData(true);
        bz_camera_view.setPreviewTargetSize(480, 640);
        bz_camera_view.setPreviewFormat(ImageFormat.NV21);
        bz_camera_view.setOnCameraStateListener(new OnCameraStateListener() {
            @Override
            public void onPreviewSuccess(Camera camera, int width, int height) {
            }

            @Override
            public void onPreviewFail(String message) {

            }

            @Override
            public void onPreviewDataUpdate(byte[] data, int width, int height, int displayOrientation, int cameraId) {
//                final Bitmap bitmap = yuvConvertUtil.yuv_yv12_2_Bitmap(data, width, height, displayOrientation, true);
                final Bitmap bitmap = yuvConvertUtil.yuv_nv21_2_Bitmap(data, width, height, displayOrientation, true);
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
