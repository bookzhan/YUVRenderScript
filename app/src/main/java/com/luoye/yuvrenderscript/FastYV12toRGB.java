package com.luoye.yuvrenderscript;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.util.Log;

import com.luoye.bzyuv.ScriptC_bz_yuv_util;

/**
 * Created by zhandalin on 2019-09-04 11:44.
 * Function:
 */
public class FastYV12toRGB {
    private final static String TAG = "bz_FastYUVtoRGB";
    private RenderScript renderScript;
    private Type.Builder yType, rgbaType;
    private Allocation yIn, out, vIn, uIn;
    private long count = 0;
    private long spaceTime = 0;
    private Bitmap bmpout;
    private final ScriptC_bz_yuv_util scriptC_yuv;
    private byte[] yBuffer;
    private byte[] uBuffer;
    private byte[] vBuffer;
    private byte[] rgbaBuffer;

    public FastYV12toRGB(Context context) {
        renderScript = RenderScript.create(context);
        scriptC_yuv = new ScriptC_bz_yuv_util(renderScript);
    }

    public Bitmap convertYV12toBitmap(byte[] yuvData, int width, int height, int orientation) {
        int finalWidth = width;
        int finalHeight = height;
        if (orientation == 90 || orientation == 270) {
            finalWidth = height;
            finalHeight = width;
        }
        convert2RGBA(yuvData, ImageFormat.YV12, width, height, orientation);
        if (null == bmpout) {
            bmpout = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888);
        }
        if (null != out) {
            out.copyTo(bmpout);
        }
        return bmpout;
    }

    public byte[] convertYV12toRgba(byte[] yuvData, int width, int height, int orientation) {
        convert2RGBA(yuvData, ImageFormat.YV12, width, height, orientation);
        if (null == rgbaBuffer) {
            rgbaBuffer = new byte[width * height * 4];
        }
        if (null != out) {
            out.copyTo(rgbaBuffer);
        }
        return rgbaBuffer;
    }

    private void convert2RGBA(byte[] yuvData, int imageFormat, int width, int height, int orientation) {
        long startTime = System.currentTimeMillis();
        int finalWidth = width;
        int finalHeight = height;
        if (orientation == 90 || orientation == 270) {
            finalWidth = height;
            finalHeight = width;
        }
        if (yType == null) {
            yType = new Type.Builder(renderScript, Element.U8(renderScript)).setX(width).setY(height);
            yIn = Allocation.createTyped(renderScript, yType.create(), Allocation.USAGE_SCRIPT);

            Type.Builder uvType = new Type.Builder(renderScript, Element.U8(renderScript)).setX(width / 2).setY(height / 2);
            uIn = Allocation.createTyped(renderScript, uvType.create(), Allocation.USAGE_SCRIPT);
            vIn = Allocation.createTyped(renderScript, uvType.create(), Allocation.USAGE_SCRIPT);


            rgbaType = new Type.Builder(renderScript, Element.RGBA_8888(renderScript)).setX(finalWidth).setY(finalHeight);
            out = Allocation.createTyped(renderScript, rgbaType.create(), Allocation.USAGE_SCRIPT);
        }
        if (null == yBuffer) {
            yBuffer = new byte[width * height];
        }
        if (null == uBuffer) {
            uBuffer = new byte[width * height / 4];
        }
        if (null == vBuffer) {
            vBuffer = new byte[width * height / 4];
        }
        System.arraycopy(yuvData, 0, yBuffer, 0, yBuffer.length);
        System.arraycopy(yuvData, yBuffer.length, uBuffer, 0, uBuffer.length);
        System.arraycopy(yuvData, yBuffer.length + uBuffer.length, vBuffer, 0, vBuffer.length);


        yIn.copyFrom(yBuffer);
        uIn.copyFrom(uBuffer);
        vIn.copyFrom(vBuffer);
        scriptC_yuv.set_mInY(yIn);
        scriptC_yuv.set_mInU(vIn);
        scriptC_yuv.set_mInV(uIn);
        scriptC_yuv.set_inWidth(width);
        scriptC_yuv.set_inHeight(height);
        scriptC_yuv.set_orientation(orientation);
        scriptC_yuv.set_flipY(true);

        scriptC_yuv.forEach_yuv_420_888_2_rgba(out);
        count++;
        spaceTime += (System.currentTimeMillis() - startTime);
        long time = spaceTime / count;
        Log.d("convertYV12toBitmap", "Bitmap time consuming=" + time);
    }
}
