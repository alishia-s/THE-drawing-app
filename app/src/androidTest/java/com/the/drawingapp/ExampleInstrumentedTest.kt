package com.the.drawingapp

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

import androidx.test.espresso.Espresso.onView;
import androidx.test.espresso.action.ViewActions.click;
import androidx.test.espresso.assertion.ViewAssertions.matches;
import androidx.test.espresso.matcher.ViewMatchers.isClickable;
import androidx.test.espresso.matcher.ViewMatchers.withId;
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import androidx.test.espresso.matcher.ViewMatchers.isEnabled

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
class ExampleInstrumentedTest {
//    @Test
//    fun useAppContext() {
//        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        assertEquals("com.the.drawingapp", appContext.packageName)
//    }

    //starting activity: https://stackoverflow.com/questions/30191715/start-activity-for-testing
    /*UI TESTING*/
    //testing MainActivity
    @Test
    fun createAndDisplayNewCanvas() {
        val activityScenario : ActivityScenario<MainActivity> = ActivityScenario.launch(MainActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.newDrawingButton)).perform(click())
        onView(withId(R.id.canvas)).check(matches(isDisplayed()))
        activityScenario.moveToState(Lifecycle.State.DESTROYED)

    }
    @Test
    fun goBackButton(){
        val activityScenario : ActivityScenario<MainActivity> = ActivityScenario.launch(MainActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.newDrawingButton)).perform(click())
        onView(withId(R.id.back_button)).perform(click())
        onView(withId(R.id.recycler)).check(matches(isDisplayed()))
        activityScenario.moveToState(Lifecycle.State.DESTROYED)
    }
    @Test
    //testing DrawingView
    fun areToolsClickable(){
        val activityScenario : ActivityScenario<MainActivity> = ActivityScenario.launch(MainActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.newDrawingButton)).perform(click())
        onView(withId(R.id.pen_button)).check(matches(isClickable()))
        onView(withId(R.id.eraser_button)).check(matches(isClickable()))
        onView(withId(R.id.penSizeBar)).check(matches(isEnabled()))
        onView(withId(R.id.color_button)).check(matches(isClickable()))
        activityScenario.moveToState(Lifecycle.State.DESTROYED)
    }

    //types of tests to write:
    /*
    checking if drawing displays properly
    clicking on pen
    clicking on eraser
    drawing with pen with different sizes
    erasing drawing
    choosing size of pen
    creating new drawing
     */
}