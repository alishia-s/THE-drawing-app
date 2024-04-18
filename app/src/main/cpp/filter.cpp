#include <jni.h>
#include <android/bitmap.h>

/*for greyscale math:
 * hex-->rgb
 * greyscale for pixel = (r+g+b)/3
 */

//forward declarations
struct argb{
    uint8_t red;
    uint8_t green;
    uint8_t blue;
    uint8_t alpha;
};

static uint32_t * getRGB(uint32_t );
static void greyscale(AndroidBitmapInfo*, void*);
static void invert(AndroidBitmapInfo*, void*);

static void greyscale(AndroidBitmapInfo* bitmap, void* pixels)
{
    int x, y;
    argb * line;

    for (y = 0; y < bitmap->height; y++)
    {
        line = (argb *)pixels;

        for (x = 0; x < bitmap->width; x++) {
                //math found at https://goodcalculators.com/rgb-to-grayscale-conversion-calculator/

//                int color = ((0.299 * line[x].red) + (0.587 * line[x].green) + (0.114 * line[x].blue)) / 3;
                int color = ((line[x].red) + (line[x].green) + (line[x].blue)) / 3;

                line[x].red = color;
                line[x].green = color;
                line[x].blue = color;
                line[x].alpha = 0xff;
        }
        pixels = (char*)pixels + bitmap->stride;
    }
}

//return pointer to array [r,g,b]
static uint32_t * getRGB(uint32_t pixel)
{
    //get rgb
    uint32_t blue = (int)  ((pixel & 0x00FF0000) >> 16);
    uint32_t green = (int)((pixel & 0x0000FF00) >> 8);
    uint32_t red = (int) ((pixel & 0x000000FF));

    uint32_t RGB_arr[3] = {red, green, blue};

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