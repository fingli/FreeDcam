#pragma version(1)
#pragma rs java_package_name(freed.utils)
#pragma rs_fp_relaxed
rs_allocation gOrginalFrame;
rs_allocation gFocusDataFrame;
rs_allocation gStackFrame;
int width;
int height;
uchar4 __attribute__((kernel)) stack(uint32_t x, uint32_t y)
{
    uchar4 curPixel;
    //rsDebug("x/y", x + y);
    curPixel = rsGetElementAt_uchar4(gFocusDataFrame, x, y);
    if(curPixel.g < 254)
        return rsGetElementAt_uchar4(gOrginalFrame, x, y);
    if(x+1 < width)
    {
        curPixel = rsGetElementAt_uchar4(gFocusDataFrame, x+1, y);
        if(curPixel.g < 254)
            return rsGetElementAt_uchar4(gOrginalFrame, x, y);
    }

    if(y+1 < height)
    {
        curPixel = rsGetElementAt_uchar4(gFocusDataFrame, x, y +1);
        if(curPixel.g < 254)
            return rsGetElementAt_uchar4(gOrginalFrame, x, y);
    }
    if(y >= 1)
    {
        curPixel = rsGetElementAt_uchar4(gFocusDataFrame, x, y -1);
        if(curPixel.g < 254)
            return rsGetElementAt_uchar4(gOrginalFrame, x, y);
    }
    if(x >= 1)
    {
        curPixel = rsGetElementAt_uchar4(gFocusDataFrame, x -1, y);
        if(curPixel.g < 254)
            return rsGetElementAt_uchar4(gOrginalFrame, x, y);
    }
    if(x+1 < width && y+1 < height)
    {
        curPixel = rsGetElementAt_uchar4(gFocusDataFrame, x +1, y+1);
        if(curPixel.g < 254)
            return rsGetElementAt_uchar4(gOrginalFrame, x, y);
    }
    if(y >= 1 && x >= 1)
    {
        curPixel = rsGetElementAt_uchar4(gFocusDataFrame, x -1, y-1);
        if(curPixel.g < 254)
            return rsGetElementAt_uchar4(gOrginalFrame, x, y);
    }
    if(x+1 < width && y >= 1)
    {
        curPixel = rsGetElementAt_uchar4(gFocusDataFrame, x +1, y-1);
        if(curPixel.g < 254)
            return rsGetElementAt_uchar4(gOrginalFrame, x, y);
    }
    if(y+1 < height && x >= 1)
    {
        curPixel = rsGetElementAt_uchar4(gFocusDataFrame, x -1, y+1);
        if(curPixel.g < 254)
            return rsGetElementAt_uchar4(gOrginalFrame, x, y);
    }
    return rsGetElementAt_uchar4(gStackFrame, x, y);

}