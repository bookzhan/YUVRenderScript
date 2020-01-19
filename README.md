> The strongest YUV conversion RenderScript in history, supports conversion to RGBA, BGRA, and supports rotation and flipping simultaneously. The YUV data format supports NV21, YV12, YUV420_888, corresponding to the YUV processing of Android Camera1API and Camera2 API.



​	During this time, I was doing some Android Camera data processing related work, involving YUV to RGBA. Due to the extremely high performance requirements, I have adopted a variety of solutions. At first, I tried the methods in OpenCV and finally found the performance. Not very ideal. Finally, libyuv was used. The performance of this library is better than libyuv. The performance requirements are not particularly high. You can use this library. This library has certain defects. The degree of image restoration after YUV conversion is very bad, and there is obviously a color difference And, on Android phones running for a long time, the performance drops significantly. Finally, there is no way to start using RenderScript. I have to talk about Google. He only provides a ScriptIntrinsicYuvToRGB, and does not support YUV420_888, nor does it support rotation, flipping. Nausea!  Only wrote it myself！



The supported methods are as follows：

1. yuv_yv12_2_Bitmap
2. yuv_yv12_2_RGBA
3. yuv_yv12_2_BGRA
4. yuv_nv21_2_Bitmap
5. yuv_nv21_2_RGBA
6. yuv_nv21_2_BGRA
7. yuv420_2_Bitmap
8. yuv420_2_RGBA
9. yuv420_2_BGRA



Give me a star if you like，Thanks！



中文文档地址（Chinese document address）：http://www.bzblog.online/wordpress/index.php/2020/01/19/yuvrenderscript/