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
 * description: Element.RGB_888 and Element.RGBA_8888 allocate the same memory, 4 bytes aligned?
 * So theoretically does not support conversion to RGB
 */
public class YUVConvertUtil {
    private static final String TAG = "bz_YUV420pUtil";
    private static final int RGBA = 0;
    private static final int BGRA = 1;
    private final RenderScript renderScript;
    private final ScriptC_bz_yuv_util scriptC_yuv;

    private Allocation yIn;
    private Allocation uIn;
    private Allocation vIn;
    private Allocation yv12In;
    private Allocation nv21In;
    private Allocation finalOut;
    private Bitmap bitmap;
    private byte[] outBuffer;

    public YUVConvertUtil(Context context) {
        renderScript = RenderScript.create(context);
        scriptC_yuv = new ScriptC_bz_yuv_util(renderScript);
    }

    public Bitmap yuv_yv12_2_Bitmap(byte[] yv12Data, int width, int height, int orientation, boolean flipY) {
        int finalWidth = width;
        int finalHeight = height;
        if (orientation == 90 || orientation == 270) {
            finalWidth = height;
            finalHeight = width;
        }
        convertYV12(yv12Data, width, height, orientation, flipY, RGBA);
        if (null == bitmap) {
            bitmap = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888);
        }
        if (null != finalOut) {
            finalOut.copyTo(bitmap);
        }
        return bitmap;
    }

    public byte[] yuv_yv12_2_RGBA(byte[] yv12Data, int width, int height, int orientation, boolean flipY) {
        convertYV12(yv12Data, width, height, orientation, flipY, RGBA);
        if (null == outBuffer) {
            outBuffer = new byte[width * height * 4];
        }
        if (null != finalOut) {
            finalOut.copyTo(outBuffer);
        }
        return outBuffer;
    }

    public byte[] yuv_yv12_2_BGRA(byte[] yv12Data, int width, int height, int orientation, boolean flipY) {
        convertYV12(yv12Data, width, height, orientation, flipY, BGRA);
        if (null == outBuffer) {
            outBuffer = new byte[width * height * 4];
        }
        if (null != finalOut) {
            finalOut.copyTo(outBuffer);
        }
        return outBuffer;
    }

    public Bitmap yuv_nv21_2_Bitmap(byte[] nv21Data, int width, int height, int orientation, boolean flipY) {
        int finalWidth = width;
        int finalHeight = height;
        if (orientation == 90 || orientation == 270) {
            finalWidth = height;
            finalHeight = width;
        }
        convertNV21(nv21Data, width, height, orientation, flipY, RGBA);
        if (null == bitmap) {
            bitmap = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888);
        }
        if (null != finalOut) {
            finalOut.copyTo(bitmap);
        }
        return bitmap;
    }

    public byte[] yuv_nv21_2_RGBA(byte[] yv12Data, int width, int height, int orientation, boolean flipY) {
        convertNV21(yv12Data, width, height, orientation, flipY, RGBA);
        if (null == outBuffer) {
            outBuffer = new byte[width * height * 4];
        }
        if (null != finalOut) {
            finalOut.copyTo(outBuffer);
        }
        return outBuffer;
    }

    public byte[] yuv_nv21_2_BGRA(byte[] yv12Data, int width, int height, int orientation, boolean flipY) {
        convertNV21(yv12Data, width, height, orientation, flipY, BGRA);
        if (null == outBuffer) {
            outBuffer = new byte[width * height * 4];
        }
        if (null != finalOut) {
            finalOut.copyTo(outBuffer);
        }
        return outBuffer;
    }

    public Bitmap yuv420_2_Bitmap(byte[] yData, byte[] uData, byte[] vData, int uvPixelStride, int width, int height, int orientation, boolean flipY) {
        int finalWidth = width;
        int finalHeight = height;
        if (orientation == 90 || orientation == 270) {
            finalWidth = height;
            finalHeight = width;
        }
        convertYuv420(yData, uData, vData, uvPixelStride, width, height, orientation, flipY, RGBA);
        if (null == bitmap) {
            bitmap = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888);
        }
        if (null != finalOut) {
            finalOut.copyTo(bitmap);
        }
        return bitmap;
    }

    public byte[] yuv420_2_RGBA(byte[] yData, byte[] uData, byte[] vData, int uvPixelStride, int width, int height, int orientation, boolean flipY) {
        convertYuv420(yData, uData, vData, uvPixelStride, width, height, orientation, flipY, RGBA);
        if (null == outBuffer) {
            outBuffer = new byte[width * height * 4];
        }
        if (null != finalOut) {
            finalOut.copyTo(outBuffer);
        }
        return outBuffer;
    }

    public byte[] yuv420_2_BGRA(byte[] yData, byte[] uData, byte[] vData, int uvPixelStride, int width, int height, int orientation, boolean flipY) {
        convertYuv420(yData, uData, vData, uvPixelStride, width, height, orientation, flipY, BGRA);
        if (null == outBuffer) {
            outBuffer = new byte[width * height * 4];
        }
        if (null != finalOut) {
            finalOut.copyTo(outBuffer);
        }
        return outBuffer;
    }

    private void convertYV12(byte[] yv12Data, int width, int height, int orientation, boolean flipY, int format) {
        if (null == yv12Data || width <= 0 || height <= 0) {
            return;
        }
        int finalWidth = width;
        int finalHeight = height;
        if (orientation == 90 || orientation == 270) {
            finalWidth = height;
            finalHeight = width;
        }
        if (yv12In == null) {
            Type.Builder yType = new Type.Builder(renderScript, Element.U8(renderScript)).setX(width * height * 3 / 2);
            yv12In = Allocation.createTyped(renderScript, yType.create(), Allocation.USAGE_SCRIPT);

            Type.Builder outType = new Type.Builder(renderScript, Element.RGBA_8888(renderScript)).setX(finalWidth).setY(finalHeight);
            finalOut = Allocation.createTyped(renderScript, outType.create(), Allocation.USAGE_SCRIPT);
        }
        yv12In.copyFrom(yv12Data);
        scriptC_yuv.set_mInYV12(yv12In);
        scriptC_yuv.set_inWidth(width);
        scriptC_yuv.set_inHeight(height);
        scriptC_yuv.set_orientation(orientation);
        scriptC_yuv.set_flipY(flipY);
        if (format == BGRA) {
            scriptC_yuv.forEach_yuv_yv12_2_bgra(finalOut);
        } else {
            scriptC_yuv.forEach_yuv_yv12_2_rgba(finalOut);
        }
    }

    private void convertNV21(byte[] nv21Data, int width, int height, int orientation, boolean flipY, int format) {
        if (null == nv21Data || width <= 0 || height <= 0) {
            return;
        }
        int finalWidth = width;
        int finalHeight = height;
        if (orientation == 90 || orientation == 270) {
            finalWidth = height;
            finalHeight = width;
        }
        if (nv21In == null) {
            Type.Builder yType = new Type.Builder(renderScript, Element.U8(renderScript)).setX(width * height * 3 / 2);
            nv21In = Allocation.createTyped(renderScript, yType.create(), Allocation.USAGE_SCRIPT);
            Type.Builder outType = new Type.Builder(renderScript, Element.RGBA_8888(renderScript)).setX(finalWidth).setY(finalHeight);
            finalOut = Allocation.createTyped(renderScript, outType.create(), Allocation.USAGE_SCRIPT);
        }
        nv21In.copyFrom(nv21Data);
        scriptC_yuv.set_mInNV21(nv21In);
        scriptC_yuv.set_inWidth(width);
        scriptC_yuv.set_inHeight(height);
        scriptC_yuv.set_orientation(orientation);
        scriptC_yuv.set_flipY(flipY);
        if (format == BGRA) {
            scriptC_yuv.forEach_yuv_nv21_2_bgra(finalOut);
        } else {
            scriptC_yuv.forEach_yuv_nv21_2_rgba(finalOut);
        }
    }


    private void convertYuv420(byte[] yData, byte[] uData, byte[] vData, int uvPixelStride, int width, int height, int orientation, boolean flipY, int format) {
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

            Type.Builder outType = new Type.Builder(renderScript, Element.RGBA_8888(renderScript)).setX(finalWidth).setY(finalHeight);
            finalOut = Allocation.createTyped(renderScript, outType.create(), Allocation.USAGE_SCRIPT);
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
            scriptC_yuv.forEach_yuv_420_888_2_bgra(finalOut);
        } else {
            scriptC_yuv.forEach_yuv_420_888_2_rgba(finalOut);
        }
    }
}
