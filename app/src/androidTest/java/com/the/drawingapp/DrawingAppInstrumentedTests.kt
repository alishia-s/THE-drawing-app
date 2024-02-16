package com.the.drawingapp

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 *
 * Before testing, turn off certain developer options
 * (follow instructions on here to turn on developer options: https://developer.android.com/studio/debug/dev-options)
 * (then, look up the following developer options and turn animation off:
 *      Window animation scale
 *      Transition animation scale
 *      Animator duration scale)
 */
@RunWith(AndroidJUnit4::class)
class DrawingAppInstrumentedTests {

    //starting activity: https://stackoverflow.com/questions/30191715/start-activity-for-testing
    /*UI TESTING*/
    //testing MainActivity
    @Test
    fun createAndDisplayNewCanvas() {
        val activityScenario: ActivityScenario<MainActivity> =
            ActivityScenario.launch(MainActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.newDrawingButton)).perform(click())
        onView(withId(R.id.canvas)).check(matches(isDisplayed()))
        activityScenario.moveToState(Lifecycle.State.DESTROYED)

    }

    @Test
    fun goBackButton() {
        val activityScenario: ActivityScenario<MainActivity> =
            ActivityScenario.launch(MainActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.newDrawingButton)).perform(click())
        onView(withId(R.id.back_button)).perform(click())
        onView(withId(R.id.recycler)).check(matches(isDisplayed()))
        activityScenario.moveToState(Lifecycle.State.DESTROYED)
    }

    @Test
    //testing DrawingView
    fun isPenButtonClickable() {
        val activityScenario: ActivityScenario<MainActivity> =
            ActivityScenario.launch(MainActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.newDrawingButton)).perform(click())
        onView(withId(R.id.pen_button)).check(matches(isClickable()))
        activityScenario.moveToState(Lifecycle.State.DESTROYED)
    }

    @Test
    //testing DrawingView
    fun isEraserButtonClickable() {
        val activityScenario: ActivityScenario<MainActivity> =
            ActivityScenario.launch(MainActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.newDrawingButton)).perform(click())
        onView(withId(R.id.eraser_button)).check(matches(isClickable()))
        activityScenario.moveToState(Lifecycle.State.DESTROYED)
    }

    @Test
    //testing DrawingView
    fun isPenSizeBarClickable() {
        val activityScenario: ActivityScenario<MainActivity> =
            ActivityScenario.launch(MainActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.newDrawingButton)).perform(click())
        onView(withId(R.id.penSizeBar)).check(matches(isEnabled()))
        activityScenario.moveToState(Lifecycle.State.DESTROYED)
    }

    @Test
    //testing DrawingView
    fun isColorButtonClickable() {
        val activityScenario: ActivityScenario<MainActivity> =
            ActivityScenario.launch(MainActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.newDrawingButton)).perform(click())
        onView(withId(R.id.color_button)).check(matches(isClickable()))
        activityScenario.moveToState(Lifecycle.State.DESTROYED)
    }

    /*Testing ViewModel logic*/

    private val vm = DrawingViewModel()
    private val lifecycleOwner = TestLifecycleOwner()

    @Test
    fun testViewModel_updatingBitmap() = runTest {
        val activityScenario: ActivityScenario<MainActivity> =
            ActivityScenario.launch(MainActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.newDrawingButton)).perform(click())
        val newBitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        var updatedBitmap = false
        vm.viewModelScope.launch {
                //changing bitmap
                vm.canvasBitmap.observe(lifecycleOwner)
                {
                    updatedBitmap = true
                    Log.e("TESTING", "${updatedBitmap!!}")
                }
            vm.updateBitmap(newBitmap)
        }
        advanceUntilIdle()
        delay(1000)

        //bitmap is originally 800 x 800, check to see if size also changed
        //so old one is replaced
        assertEquals(400, vm.canvasBitmap.value!!.height)
        assertEquals(400, vm.canvasBitmap.value!!.width)
        assertTrue(updatedBitmap)
    }

    @Test
    fun testingViewModel_updatingColor() = runTest{
        //color is set to an int, should be null if activity hasn't started
        val color = 1;
        var updatedColor = false
        vm.viewModelScope.launch {
            //changing color
            vm.color.observe(lifecycleOwner)
            {
                updatedColor = true
                Log.e("TESTING", "${vm.color.value!!}")
                Log.e("TESTING", "${updatedColor}")
            }
            vm.updateColor(color)
        }
        advanceUntilIdle()
        delay(1000)

        assertEquals(1, vm.color.value!!)
        assertTrue(updatedColor)
    }
}