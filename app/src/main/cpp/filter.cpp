#include <jni.h>
#include <android/bitmap.h>

/*for greyscale math:
 * hex-->rgb
 * greyscale for pixel = (r+g+b)/3
 */

//forward declarations
static uint8_t* getRGB(uint32_t );
static void greyscale(AndroidBitmapInfo*, void*);
static void invert(AndroidBitmapInfo*, void*);

static void greyscale(AndroidBitmapInfo* bitmap, void* pixels)
{
    int x, y;
//    int red = 0;
//    int green = 0;
//    int blue = 0;
    uint8_t* RGB;
    uint32_t * line;

//    int dim = (bitmap->height) * (bitmap->width);

    for (y = 0; y < bitmap->height; y++)
    {
        line = (uint32_t *)pixels;

        for (x = 0; x < bitmap->width; x++) {
            if (line[x] != 0xFF000000) {
            RGB = getRGB(line[x]);

            //math found at https://goodcalculators.com/rgb-to-grayscale-conversion-calculator/
            line[x] = ((255 - 0.299 * RGB[0]) + (255 - 0.587 * RGB[1]) + (255 - 0.114 * RGB[2])) / 3;

//            line[x]=(line[x] & 0xff) << 24              |
//                    ((int)(0.07 * RGB[2])) & 0xff << 16 |
//                    ((int)(0.72 * RGB[1])) & 0xff << 8  |
//                    ((int)(0.21 * RGB[0])) & 0xff;
            }
        }
        pixels = (char*)pixels + bitmap->stride;
    }
}

//return pointer to array [r,g,b]
static uint8_t * getRGB(uint32_t pixel)
{
    //get rgb
    uint8_t blue = (int)  ((pixel & 0x00FF0000) >> 16);
    uint8_t green = (int)((pixel & 0x0000FF00) >> 8);
    uint8_t red = (int) ((pixel & 0x000000FF));

    uint8_t RGB_arr[3] = {red, green, blue};

    return RGB_arr;
}

static void invert(AndroidBitmapInfo* info, void* pixels);

extern "C"
JNIEXPORT void JNICALL
Java_com_the_drawingapp_Greyscale_greyscale(JNIEnv *env, jobject thiz, jobject bmp) {
    AndroidBitmapInfo info;
    void* pixels;
    if((AndroidBitmap_getInfo(env, bmp, &info))<0)
    {
        return;
    }
    if(info.format != ANDROID_BITMAP_FORMAT_RGBA_8888)
    {
        return;
    }
    if((AndroidBitmap_lockPixels(env, bmp, &pixels)) < 0)
    {
        return;
    }

    greyscale(&info, pixels);
    AndroidBitmap_unlockPixels(env, bmp);
}