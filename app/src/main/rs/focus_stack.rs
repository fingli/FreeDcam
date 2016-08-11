#pragma version(1)
#pragma rs java_package_name(freed.utils)
#pragma rs_fp_relaxed
rs_allocation gOrginalFrame;
rs_allocation gFocusDataFrame;
rs_allocation gStackFrame;
uchar4 __attribute__((kernel)) stack(uint32_t x, uint32_t y)
{
    uchar4 curPixel;
    curPixel = rsGetElementAt_uchar4(gFocusDataFrame, x, y);
    if(curPixel.g < 254)
    {
        uchar4 rgbO = rsGetElementAt_uchar4(gOrginalFrame, x, y);
        return  rgbO;
    }
    else
        return rsGetElementAt_uchar4(gStackFrame, x, y);

}