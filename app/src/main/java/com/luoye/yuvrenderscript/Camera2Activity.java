package com.luoye.yuvrenderscript;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.hardware.camera2.CameraDevice;
import android.media.Image;
import android.os.Bundle;
import android.widget.ImageView;

import com.luoye.bzcamera.BZCamera2View;


public class Camera2Activity extends AppCompatActivity {
    private byte[] yBuffer = null;
    private byte[] uBuffer = null;
    private byte[] vBuffer = null;
    private YUVConvertUtil yuvConvertUtil;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);
        yuvConvertUtil = new YUVConvertUtil(this);
        final ImageView image_view = findViewById(R.id.image_view);
        BZCamera2View bz_camera2_view = findViewById(R.id.bz_camera2_view);
        bz_camera2_view.setCheckCameraCapacity(false);
        bz_camera2_view.setPreviewTargetSize(480, 640);
        bz_camera2_view.setOnStatusChangeListener(new BZCamera2View.OnStatusChangeListener() {
            @Override
            public void onPreviewSuccess(CameraDevice mCameraDevice, int width, int height) {

            }

            @Override
            public void onImageAvailable(Image image, int displayOrientation, float fps) {
                int width = image.getWidth();
                int height = image.getHeight();
                Image.Plane[] planes = image.getPlanes();
                if (null == yBuffer) {
                    yBuffer = new byte[width * height];
                }
                if (null == uBuffer) {
                    uBuffer = new byte[planes[1].getBuffer().capacity()];
                }
                if (null == vBuffer) {
                    vBuffer = new byte[planes[2].getBuffer().capacity()];
                }
                planes[0].getBuffer().get(yBuffer);
                planes[1].getBuffer().get(vBuffer);
                planes[2].getBuffer().get(uBuffer);

//                int finalWidth = width;
//                int finalHeight = height;
//                if (displayOrientation == 90 || displayOrientation == 270) {
//                    finalWidth = height;
//                    finalHeight = width;
//                }
//                byte[] bytes = yuvConvertUtil.yuv420_2_RGBA(yBuffer, uBuffer, vBuffer, planes[1].getPixelStride(), width, height, displayOrientation, true);
//                if (null == bitmap) {
//                    bitmap = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888);
//                }
//                bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(bytes));

                final Bitmap bitmap = yuvConvertUtil.yuv420_2_Bitmap(yBuffer, uBuffer, vBuffer, planes[1].getPixelStride(), width, height, displayOrientation, true);
                image_view.post(new Runnable() {
                    @Override
                    public void run() {
                        image_view.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }
}
