#include <jni.h>
#include <android/bitmap.h>

/*for greyscale math:
 * hex-->rgb
 * greyscale for pixel = (r+g+b)/3
 */

//forward declarations
static int* getRGB(uint32_t);
static void greyscale(AndroidBitmapInfo*, void*);
static void invert(AndroidBitmapInfo*, void*);

static void greyscale(AndroidBitmapInfo* bitmap, void* pixels)
{
    int x, y, new_pixel_color;
    int* RGB;
    uint32_t* line;

    for (y = 0; y < bitmap->height; y++)
    {
        line = (uint32_t*)pixels;

        for (x = 0; x < bitmap->width; x++){
            RGB = getRGB(line[x]);

            new_pixel_color = (RGB[0] + RGB[1] + RGB[2])/ 3;

            line[x] = new_pixel_color;
        }
        pixels = (char*)pixels + bitmap -> stride;
    }

    //loop unrolling version (needs tweaking)
    /*
     *  newi = dim - ii - 1;
        for (jj = j; jj < j + N; jj +=2)
        {
          nj = jj + 1;
          kj = dim - jj - 1;
          mj = dim - jj - 2;
          value1 = (src[RIDX(ii, jj, dim)].red + src[RIDX(ii, jj, dim)].green + src[RIDX(ii, jj, dim)].blue) / 3;
          value2 = (src[RIDX(ii, nj, dim)].red + src[RIDX(ii, nj, dim)].green + src[RIDX(ii, nj, dim)].blue) / 3;

          dest[RIDX(kj, newi, dim)].red = dest[RIDX(kj, newi, dim)].green = dest[RIDX(kj, newi, dim)].blue = value1;
          dest[RIDX(mj, newi, dim)].red = dest[RIDX(mj, newi, dim)].green = dest[RIDX(mj, newi, dim)].blue = value2;
        }*/
}

//return pointer to array [r,g,b]
static int* getRGB(uint32_t pixel)
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