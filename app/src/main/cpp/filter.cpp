#include <jni.h>
#include <android/bitmap.h>

/*for greyscale math:
 * hex-->rgb
 * greyscale for pixel = (r+g+b)/3
 */

//forward declarations
static int* getRGB(uint32_t );
static void greyscale(AndroidBitmapInfo*, void*);
static void invert(AndroidBitmapInfo*, void*);

static void greyscale(AndroidBitmapInfo* bitmap, void* pixels)
{
    int x, y, new_pixel_color;
    int* RGB;
    uint32_t * line;

    for (y = 0; y < bitmap->height; y++)
    {
        line = (uint32_t *) pixels;

        for (x = 0; x < bitmap->width; x++){
            //RGB = getRGB(line[x]);

            //math found at https://goodcalculators.com/rgb-to-grayscale-conversion-calculator/
            new_pixel_color = (255-0.3 * RGB[0]) + (255-0.6 * RGB[1]) + (255-0.1 * RGB[2]);

            line[x] = new_pixel_color;
        }
        pixels = (char*)pixels + bitmap -> stride;
    }
}

//return pointer to array [r,g,b]
static int* getRGB(uint32_t pixel)
{
    //get rgb
    int red = (int)  ((pixel & 0x00FF0000) >> 16);
    int green = (int)((pixel & 0x0000FF00) >> 8);
    int blue = (int) ((pixel & 0x000000FF));

    int RGB_arr[3] = {red, green, blue};

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