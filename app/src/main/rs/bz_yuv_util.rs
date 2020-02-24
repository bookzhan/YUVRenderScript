#pragma version(1)
#pragma rs java_package_name(com.luoye.bzyuv)
#pragma rs_fp_relaxed

rs_allocation mInput;

rs_allocation mInY;
rs_allocation mInU;
rs_allocation mInV;
rs_allocation mInNV21;
rs_allocation mInYV12;
int inWidth=0,inHeight=0;
int orientation=0;
bool flipY=false;
int uvPixelStride=1;

static uchar4 yuvToRGBA4(uchar y, uchar u, uchar v) {
    short Y = ((short)y) - 16;
    short U = ((short)u) - 128;
    short V = ((short)v) - 128;

    short4 p;
    p.x = (Y * 298 + V * 409 + 128) >> 8;
    p.y = (Y * 298 - U * 100 - V * 208 + 128) >> 8;
    p.z = (Y * 298 + U * 516 + 128) >> 8;
    p.w = 255;
    if(p.x < 0) {
        p.x = 0;
    }
    if(p.x > 255) {
        p.x = 255;
    }
    if(p.y < 0) {
        p.y = 0;
    }
    if(p.y > 255) {
        p.y = 255;
    }
    if(p.z < 0) {
        p.z = 0;
    }
    if(p.z > 255) {
        p.z = 255;
    }
    return (uchar4){p.x, p.y, p.z, p.w};
}

static float4 yuvToRGBA_f4(uchar y, uchar u, uchar v) {
    float4 yuv_U_values = {0.f, -0.392f * 0.003921569f, +2.02 * 0.003921569f, 0.f};
    float4 yuv_V_values = {1.603f * 0.003921569f, -0.815f * 0.003921569f, 0.f, 0.f};

    float4 color = (float)y * 0.003921569f;
    float4 fU = ((float)u) - 128.f;
    float4 fV = ((float)v) - 128.f;

    color += fU * yuv_U_values;
    color += fV * yuv_V_values;
    color = clamp(color, 0.f, 1.f);
    return color;
}

void makeRef(rs_allocation ay, rs_allocation au, rs_allocation av, rs_allocation aout) {
    uint32_t w = rsAllocationGetDimX(ay);
    uint32_t h = rsAllocationGetDimY(ay);

    for (int y = 0; y < h; y++) {
        //rsDebug("y", y);
        for (int x = 0; x < w; x++) {

            int py = rsGetElementAt_uchar(ay, x, y);
            int pu = rsGetElementAt_uchar(au, x >> 1, y >> 1);
            int pv = rsGetElementAt_uchar(av, x >> 1, y >> 1);

            //rsDebug("py", py);
            //rsDebug(" u", pu);
            //rsDebug(" v", pv);

            uchar4 rgb = yuvToRGBA4(py, pu, pv);
            //rsDebug("  ", rgb);

            rsSetElementAt_uchar4(aout, rgb, x, y);
        }
    }
}

void makeRef_f4(rs_allocation ay, rs_allocation au, rs_allocation av, rs_allocation aout) {
    uint32_t w = rsAllocationGetDimX(ay);
    uint32_t h = rsAllocationGetDimY(ay);

    for (int y = 0; y < h; y++) {
        //rsDebug("y", y);
        for (int x = 0; x < w; x++) {

            uchar py = rsGetElementAt_uchar(ay, x, y);
            uchar pu = rsGetElementAt_uchar(au, x >> 1, y >> 1);
            uchar pv = rsGetElementAt_uchar(av, x >> 1, y >> 1);

            //rsDebug("py", py);
            //rsDebug(" u", pu);
            //rsDebug(" v", pv);

            float4 rgb = yuvToRGBA_f4(py, pu, pv);
            //rsDebug("  ", rgb);

            rsSetElementAt_float4(aout, rgb, x, y);
        }
    }
}

static void setLocation(int x,int y, int*targetX,int *targetY){
    int finalX=x;
    int finalY=y;
    if(orientation!=0){
        if(orientation==90){
            finalX=y;
            finalY=inHeight-1-x;
            if(flipY){
               finalX=inWidth-1-finalX;
            }
        }else if(orientation==180){
             finalX=inWidth - 1 - x;
             finalY=inHeight - 1 - y;
             if(flipY){
                finalY=inHeight-1-finalY;
             }
        }else if(orientation==270){
             finalX=inWidth - 1 - y;
             finalY=x;
             if(flipY){
                finalY=inHeight-1-finalY;
             }
        }
    }else{
        if(flipY){
           finalY=inHeight-1-finalY;
        }
    }
    *targetX=finalX;
    *targetY=finalY;
}

uchar4 __attribute__((kernel)) yuv_420_888_2_rgba(uint32_t x, uint32_t y) {
    if(inWidth<=0||inHeight<=0){
        rsDebug("bz_inWidth<=0||inHeight<=0", inWidth,inHeight);
        return '0';
    }
    int targetX=x;
    int targetY=y;
    setLocation(x,y,&targetX,&targetY);
    uint uvIndex= uvPixelStride*(targetX/2)+ uvPixelStride*(inWidth/2)*(targetY/2);
    uchar yps= rsGetElementAt_uchar(mInY,targetX,targetY);
    uchar u= rsGetElementAt_uchar(mInU,uvIndex);
    uchar v= rsGetElementAt_uchar(mInV,uvIndex);
    return yuvToRGBA4(yps, u, v);
}

uchar4 __attribute__((kernel)) yuv_420_888_2_bgra(uint32_t x, uint32_t y) {
    if(inWidth<=0||inHeight<=0){
        rsDebug("bz_inWidth<=0||inHeight<=0", inWidth,inHeight);
        return '0';
    }
    int targetX=x;
    int targetY=y;
    setLocation(x,y,&targetX,&targetY);
    uint uvIndex= uvPixelStride*(targetX/2)+ uvPixelStride*(inWidth/2)*(targetY/2);
    uchar yps= rsGetElementAt_uchar(mInY,targetX,targetY);
    uchar u= rsGetElementAt_uchar(mInU,uvIndex);
    uchar v= rsGetElementAt_uchar(mInV,uvIndex);

    uchar4 rgba= yuvToRGBA4(yps, u, v);
    return (uchar4){rgba.z, rgba.y, rgba.x,rgba.w};
}

uchar4 __attribute__((kernel)) yuv_yv12_2_rgba(uint32_t x, uint32_t y) {
    if(inWidth<=0||inHeight<=0){
        rsDebug("bz_inWidth<=0||inHeight<=0", inWidth,inHeight);
        return '0';
    }
    int targetX=x;
    int targetY=y;
    setLocation(x,y,&targetX,&targetY);
    uint yIndex=targetX+inWidth*targetY;
    uint uIndex=inWidth*inHeight+ (targetX/2) + inWidth/2*(targetY/2);
    uint vIndex=inWidth*inHeight+inWidth*inHeight/4+ (targetX/2) + inWidth/2*(targetY/2);
    uchar yps= rsGetElementAt_uchar(mInYV12,yIndex);
    uchar u= rsGetElementAt_uchar(mInYV12,uIndex);
    uchar v= rsGetElementAt_uchar(mInYV12,vIndex);

    return yuvToRGBA4(yps, v, u);
}

uchar4 __attribute__((kernel)) yuv_yv12_2_bgra(uint32_t x, uint32_t y) {
    if(inWidth<=0||inHeight<=0){
        rsDebug("bz_inWidth<=0||inHeight<=0", inWidth,inHeight);
        return '0';
    }
    int targetX=x;
    int targetY=y;
    setLocation(x,y,&targetX,&targetY);
    uint yIndex=targetX+inWidth*targetY;
    uint uIndex=inWidth*inHeight+ (targetX/2) + inWidth/2*(targetY/2);
    uint vIndex=inWidth*inHeight+inWidth*inHeight/4+ (targetX/2) + inWidth/2*(targetY/2);
    uchar yps= rsGetElementAt_uchar(mInYV12,yIndex);
    uchar u= rsGetElementAt_uchar(mInYV12,uIndex);
    uchar v= rsGetElementAt_uchar(mInYV12,vIndex);

    uchar4 rgba= yuvToRGBA4(yps, v, u);
    return (uchar4){rgba.z, rgba.y, rgba.x,rgba.w};
}

uchar4 __attribute__((kernel)) yuv_nv21_2_rgba(uint32_t x, uint32_t y) {
    if(inWidth<=0||inHeight<=0){
        rsDebug("bz_inWidth<=0||inHeight<=0", inWidth,inHeight);
        return '0';
    }
    int targetX=x;
    int targetY=y;
    setLocation(x,y,&targetX,&targetY);
    uint yIndex=targetX+inWidth*targetY;
    uint uIndex=inWidth*inHeight+ 2*(targetX/2) + inWidth*(targetY/2);
    uint vIndex=inWidth*inHeight+ 2*(targetX/2) + inWidth*(targetY/2)+1;
    uchar yps= rsGetElementAt_uchar(mInNV21,yIndex);
    uchar u= rsGetElementAt_uchar(mInNV21,uIndex);
    uchar v= rsGetElementAt_uchar(mInNV21,vIndex);

    return yuvToRGBA4(yps, v, u);
}

uchar4 __attribute__((kernel)) yuv_nv21_2_bgra(uint32_t x, uint32_t y) {
    if(inWidth<=0||inHeight<=0){
        rsDebug("bz_inWidth<=0||inHeight<=0", inWidth,inHeight);
        return '0';
    }
    int targetX=x;
    int targetY=y;
    setLocation(x,y,&targetX,&targetY);
    uint yIndex=targetX+inWidth*targetY;
    uint uIndex=inWidth*inHeight+ 2*(targetX/2) + inWidth*(targetY/2);
    uint vIndex=inWidth*inHeight+ 2*(targetX/2) + inWidth*(targetY/2)+1;
    uchar yps= rsGetElementAt_uchar(mInNV21,yIndex);
    uchar u= rsGetElementAt_uchar(mInNV21,uIndex);
    uchar v= rsGetElementAt_uchar(mInNV21,vIndex);

    uchar4 rgba= yuvToRGBA4(yps, v, u);
    return (uchar4){rgba.z, rgba.y, rgba.x,rgba.w};
}


float4 __attribute__((kernel)) cvt_f4(uint32_t x, uint32_t y) {

    uchar py = rsGetElementAtYuv_uchar_Y(mInput, x, y);
    uchar pu = rsGetElementAtYuv_uchar_U(mInput, x, y);
    uchar pv = rsGetElementAtYuv_uchar_V(mInput, x, y);

    //rsDebug("py2", py);
    //rsDebug(" u2", pu);
    //rsDebug(" v2", pv);

    return rsYuvToRGBA_float4(py, pu, pv);
}

