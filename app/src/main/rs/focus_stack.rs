#pragma version(1)
#pragma rs java_package_name(freed.utils)
#pragma rs_fp_relaxed
rs_allocation gCurrentFrame;
rs_allocation gLastFrame;
uchar4 __attribute__((kernel)) stack(uint32_t x, uint32_t y)
{
    uchar4 curPixel;
    curPixel = rsGetElementAt_uchar4(gCurrentFrame, x, y);
    //rsDebug("CurPixel", curPixel);

    int dx = x + ((x == 0) ? 1 : -1);
    //rsDebug("dx", dx);
    int sum = 0;
    uchar4 tmpPix = rsGetElementAt_uchar4(gCurrentFrame, dx, y);
    int tmp;
    tmp = tmpPix.r - curPixel.r;
    sum += tmp * tmp;
    tmp = tmpPix.g - curPixel.g;
    sum += tmp * tmp;
    tmp = tmpPix.b - curPixel.b;
    sum += tmp * tmp;

    int dy = y + ((y == 0) ? 1 : -1);
    tmpPix = rsGetElementAt_uchar4(gCurrentFrame, x, dy);
    tmp = tmpPix.r - curPixel.r;
    sum += tmp * tmp;
    tmp = tmpPix.g - curPixel.g;
    sum += tmp * tmp;
    tmp = tmpPix.b - curPixel.b;
    sum += tmp * tmp;

    sum >>= 9;
    sum *= sum * sum;
    if((curPixel.r+sum)>255 && (curPixel.g+sum)>255 && (curPixel.b+sum)>255)
        return curPixel;
    else
        return rsGetElementAt_uchar4(gLastFrame, x, y);
}