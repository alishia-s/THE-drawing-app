#include <jni.h>
#include <android/bitmap.h>

/*for greyscale math:
 * hex-->rgb
 * greyscale for pixel = (r+g+b)/3
 */
static void greyscale(AndroidBitmapInfo* info, void* pixels){}

//return pointer to array [r,g,b]
static int* getRGB(uint32_t * pixel)
{
    int* RGB_arr = NULL;
    int red, blue, green;

    //get rgb
    red = (int) ((*pixel & 0x00FF0000) >> 16);
    green = (int)((*pixel & 0x0000FF00) >> 8);
    blue = (int) (*pixel & 0x00000FF );

    //add rgb to arr/ptr
    RGB_arr[0]=red;
    RGB_arr[1]=green;
    RGB_arr[2]=blue;

    return RGB_arr;
}

static void invert(AndroidBitmapInfo* info, void* pixels);

extern "C" {}