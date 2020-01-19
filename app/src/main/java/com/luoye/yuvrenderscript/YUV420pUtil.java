package com.luoye.yuvrenderscript;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;

import com.luoye.bzyuv.ScriptC_bz_yuv_util;

/**
 * Created by zhandalin on 2020-01-15 14:00.
 * description:
 */
public class YUV420pUtil {
    private static final String TAG = "bz_YUV420pUtil";
    private static final int RGBA = 0;
    private static final int BGRA = 1;
    private final RenderScript renderScript;
    private final ScriptC_bz_yuv_util scriptC_yuv;

    private Allocation yIn;
    private Allocation uIn;
    private Allocation vIn;
    private Allocation rgbaOut, bgrOut;
    private Bitmap bitmap;
    private byte[] rgbaBuffer, bgraBuffer;

    public YUV420pUtil(Context context) {
        renderScript = RenderScript.create(context);
        scriptC_yuv = new ScriptC_bz_yuv_util(renderScript);
    }

    public Bitmap yuv2Bitmap(byte[] yData, byte[] uData, byte[] vData, int uvPixelStride, int width, int height, int orientation, boolean flipY) {
        int finalWidth = width;
        int finalHeight = height;
        if (orientation == 90 || orientation == 270) {
            finalWidth = height;
            finalHeight = width;
        }
        convert(yData, uData, vData, uvPixelStride, width, height, orientation, flipY, RGBA);
        if (null == bitmap) {
            bitmap = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888);
        }
        if (null != rgbaOut) {
            rgbaOut.copyTo(bitmap);
        }
        return bitmap;
    }

    public byte[] yuv2RGBA(byte[] yData, byte[] uData, byte[] vData, int uvPixelStride, int width, int height, int orientation, boolean flipY) {
        convert(yData, uData, vData, uvPixelStride, width, height, orientation, flipY, RGBA);
        if (null == rgbaBuffer) {
            rgbaBuffer = new byte[width * height * 4];
        }
        if (null != rgbaOut) {
            rgbaOut.copyTo(rgbaBuffer);
        }
        return rgbaBuffer;
    }

    public byte[] yuv2BGRA(byte[] yData, byte[] uData, byte[] vData, int uvPixelStride, int width, int height, int orientation, boolean flipY) {
        convert(yData, uData, vData, uvPixelStride, width, height, orientation, flipY, BGRA);
        if (null == bgraBuffer) {
            bgraBuffer = new byte[width * height * 4];
        }
        if (null != bgrOut) {
            bgrOut.copyTo(bgraBuffer);
        }
        return bgraBuffer;
    }


    private void convert(byte[] yData, byte[] uData, byte[] vData, int uvPixelStride, int width, int height, int orientation, boolean flipY, int format) {
        if (null == yData || null == uData || null == vData || uvPixelStride <= 0 || width <= 0 || height <= 0) {
            return;
        }
        int finalWidth = width;
        int finalHeight = height;
        if (orientation == 90 || orientation == 270) {
            finalWidth = height;
            finalHeight = width;
        }
        if (yIn == null) {
            Type.Builder yType = new Type.Builder(renderScript, Element.U8(renderScript)).setX(width).setY(height);
            yIn = Allocation.createTyped(renderScript, yType.create(), Allocation.USAGE_SCRIPT);

            Type.Builder uvType = new Type.Builder(renderScript, Element.U8(renderScript)).setX(uData.length);
            uIn = Allocation.createTyped(renderScript, uvType.create(), Allocation.USAGE_SCRIPT);
            vIn = Allocation.createTyped(renderScript, uvType.create(), Allocation.USAGE_SCRIPT);

            if (format == BGRA) {
//                Type.Builder outType = new Type.Builder(renderScript, Element.RGB_888(renderScript)).setX(finalWidth).setY(finalHeight);
                Type.Builder outType = new Type.Builder(renderScript, Element.RGBA_8888(renderScript)).setX(finalWidth).setY(finalHeight);
                bgrOut = Allocation.createTyped(renderScript, outType.create(), Allocation.USAGE_SCRIPT);
            } else {
                Type.Builder outType = new Type.Builder(renderScript, Element.RGBA_8888(renderScript)).setX(finalWidth).setY(finalHeight);
                rgbaOut = Allocation.createTyped(renderScript, outType.create(), Allocation.USAGE_SCRIPT);
            }
        }

        yIn.copyFrom(yData);
        uIn.copyFrom(uData);
        vIn.copyFrom(vData);
        scriptC_yuv.set_mInY(yIn);
        scriptC_yuv.set_mInU(vIn);
        scriptC_yuv.set_mInV(uIn);
        scriptC_yuv.set_inWidth(width);
        scriptC_yuv.set_inHeight(height);
        scriptC_yuv.set_orientation(orientation);
        scriptC_yuv.set_flipY(flipY);
        scriptC_yuv.set_uvPixelStride(uvPixelStride);

        if (format == BGRA) {
            scriptC_yuv.forEach_yuv_420_888_2_bgra(bgrOut);
        } else {
            scriptC_yuv.forEach_yuv_420_888_2_rgba(rgbaOut);
        }
    }
}
