//package com.the.drawingapp
//
//import android.graphics.Bitmap
//import android.graphics.Paint
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.testing.TestLifecycleOwner
//import androidx.test.core.app.ActivityScenario
//import androidx.test.espresso.Espresso.onView
//import androidx.test.espresso.action.ViewActions.click
//import androidx.test.espresso.assertion.ViewAssertions.matches
//import androidx.test.espresso.matcher.ViewMatchers.isClickable
//import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
//import androidx.test.espresso.matcher.ViewMatchers.isEnabled
//import androidx.test.espresso.matcher.ViewMatchers.withId
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.runBlocking
//import kotlinx.coroutines.withContext
//import org.junit.Assert.*
//import org.junit.Test
//import org.junit.runner.RunWith
//
///**
// * Instrumented test, which will execute on an Android device.
// *
// * See [testing documentation](http://d.android.com/tools/testing).
// *
// * Before testing, turn off certain developer options
// * (follow instructions on here to turn on developer options: https://developer.android.com/studio/debug/dev-options)
// * (then, look up the following developer options and turn animation off:
// *      Window animation scale
// *      Transition animation scale
// *      Animator duration scale)
// */
//@RunWith(AndroidJUnit4::class)
//class DrawingAppInstrumentedTests {
//
//    //starting activity: https://stackoverflow.com/questions/30191715/start-activity-for-testing
//    /*UI TESTING*/
//    //testing MainActivity
//    @Test
//    fun isNewCanvasDisplayedOnClick() {
//        val activityScenario: ActivityScenario<MainActivity> =
//            ActivityScenario.launch(MainActivity::class.java)
//        activityScenario.moveToState(Lifecycle.State.RESUMED)
//        onView(withId(R.id.newDrawingButton)).perform(click())
//        onView(withId(R.id.canvas)).check(matches(isDisplayed()))
//        activityScenario.moveToState(Lifecycle.State.DESTROYED)
//    }
//
//    @Test
//    fun doesGoBackButtonGoBackToMain() {
//        val activityScenario: ActivityScenario<MainActivity> =
//            ActivityScenario.launch(MainActivity::class.java)
//        activityScenario.moveToState(Lifecycle.State.RESUMED)
//        onView(withId(R.id.newDrawingButton)).perform(click())
//        onView(withId(R.id.back_button)).perform(click())
//
//        //recycler only exists in main
//        onView(withId(R.id.recycler)).check(matches(isDisplayed()))
//        activityScenario.moveToState(Lifecycle.State.DESTROYED)
//    }
//
//    @Test
//    //testing DrawingView
//    fun isPenButtonClickable() {
//        val activityScenario: ActivityScenario<MainActivity> =
//            ActivityScenario.launch(MainActivity::class.java)
//        activityScenario.moveToState(Lifecycle.State.RESUMED)
//        onView(withId(R.id.newDrawingButton)).perform(click())
//        onView(withId(R.id.pen_button)).check(matches(isClickable()))
//        activityScenario.moveToState(Lifecycle.State.DESTROYED)
//    }
//
//    @Test
//    //testing DrawingView
//    fun isEraserButtonClickable() {
//        val activityScenario: ActivityScenario<MainActivity> =
//            ActivityScenario.launch(MainActivity::class.java)
//        activityScenario.moveToState(Lifecycle.State.RESUMED)
//        onView(withId(R.id.newDrawingButton)).perform(click())
//        onView(withId(R.id.eraser_button)).check(matches(isClickable()))
//        activityScenario.moveToState(Lifecycle.State.DESTROYED)
//    }
//
//    @Test
//    //testing DrawingView
//    fun isPenSizeBarClickable() {
//        val activityScenario: ActivityScenario<MainActivity> =
//            ActivityScenario.launch(MainActivity::class.java)
//        activityScenario.moveToState(Lifecycle.State.RESUMED)
//        onView(withId(R.id.newDrawingButton)).perform(click())
//        onView(withId(R.id.penSizeBar)).check(matches(isEnabled()))
//        activityScenario.moveToState(Lifecycle.State.DESTROYED)
//    }
//
//    @Test
//    //testing DrawingView
//    fun isColorButtonClickable() {
//        val activityScenario: ActivityScenario<MainActivity> =
//            ActivityScenario.launch(MainActivity::class.java)
//        activityScenario.moveToState(Lifecycle.State.RESUMED)
//        onView(withId(R.id.newDrawingButton)).perform(click())
//        onView(withId(R.id.color_button)).check(matches(isClickable()))
//        activityScenario.moveToState(Lifecycle.State.DESTROYED)
//    }
//
//    @Test
//    fun toggleToSquareShapeButton(){
//        val activityScenario: ActivityScenario<MainActivity> =
//            ActivityScenario.launch(MainActivity::class.java)
//        activityScenario.moveToState(Lifecycle.State.RESUMED)
//        onView(withId(R.id.newDrawingButton)).perform(click())
//        onView(withId(R.id.shape_button)).perform(click())
//        runBlocking {
//            val tool = Tool()
//            val isRectangle = true
//            val lifecycleOwner = TestLifecycleOwner()
//            lifecycleOwner.run {
//                withContext(Dispatchers.Main) {
//                    tool.toggleShape(isRectangle)
//                }
//                assertEquals(Paint.Cap.SQUARE, tool.paint.strokeCap!!)
//            }
//        }
//        activityScenario.moveToState(Lifecycle.State.DESTROYED)
//    }
//
//    /*Testing ViewModel logic*/
//    @Test
//    fun testViewModel_updatingBitmap() {
//        runBlocking {
//            val vm = DrawingViewModel()
//            val newBitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
//            var updatedBitmap = false
//            val lifecycleOwner = TestLifecycleOwner()
//            lifecycleOwner.run {
//                withContext(Dispatchers.Main) {
//                    //changing bitmap
//                    vm.canvasBitmap.observe(lifecycleOwner)
//                    {
//                        updatedBitmap = true
//                    }
//                    //initialize bitMap
//                    vm.initBitmap()
//                    //update it
//                    vm.updateBitmap(newBitmap)
//
//                    //bitmap is originally 800 x 800, check to see if size also changed
//                    //so old one is replaced
//                    assertEquals(400, vm.canvasBitmap.value!!.height)
//                    assertEquals(400, vm.canvasBitmap.value!!.width)
//                    assertTrue(updatedBitmap)
//                }
//            }
//        }
//    }
//
//    @Test
//    fun testingViewModel_initBitmap()
//    {
//        runBlocking {
//            val vm = DrawingViewModel()
//            val lifecycleOwner = TestLifecycleOwner()
//            lifecycleOwner.run {
//                withContext(Dispatchers.Main) {
//                    //initialize bitMap
//                    vm.initBitmap()
//
//                    //bitmap is originally 800 x 800, check to see if size also changed
//                    //so old one is replaced
//                    assertEquals(1920, vm.canvasBitmap.value!!.height)
//                    assertEquals(1080, vm.canvasBitmap.value!!.width)
//                }
//            }
//        }
//
//    }
//
//    /*Testing Tool Logic*/
//    @Test
//    fun testingTool_updateColor(){
//        runBlocking {
//            val tool = Tool()
//            //change color to red (#ff0000 -> int)
//            val color = 0xFF000000.toInt()
//            var updatedColor = false
//            val lifecycleOwner = TestLifecycleOwner()
//            lifecycleOwner.run {
//                withContext(Dispatchers.Main) {
//                    //color is set to an int, when starting, it's black
//                    tool.activatePen()
//                    //change color
//                    tool.currentColor.observe(lifecycleOwner)
//                    {
//                        updatedColor = true
//                    }
//                    tool.updateColor(color)
//                }
//                assertEquals(color, tool.currentColor.value!!)
//                assertTrue(updatedColor)
//            }
//        }
//    }
//    @Test
//    fun testingTool_updateStrokeWidth(){
//        runBlocking {
//            val tool = Tool()
//            val width = 13f
//            var updatedWidth = false
//            val lifecycleOwner = TestLifecycleOwner()
//            lifecycleOwner.run {
//                withContext(Dispatchers.Main) {
//                    //set width to default value
//                    tool.activatePen()
//                    tool.strokeWidth.observe(lifecycleOwner)
//                    {
//                        updatedWidth = true
//                    }
//                    tool.updateStrokeWidth(width)
//                }
//                assertEquals(13f, tool.strokeWidth.value!!)
//                assertTrue(updatedWidth)
//            }
//        }
//    }
//    @Test
//    fun testingTool_toggleShape(){
//        runBlocking {
//            val tool = Tool()
//            val isCircle = false
//            val lifecycleOwner = TestLifecycleOwner()
//            lifecycleOwner.run {
//                withContext(Dispatchers.Main) {
//                    tool.toggleShape(isCircle)
//                }
//                assertEquals(Paint.Join.ROUND, tool.paint.strokeJoin!!)
//            }
//        }
//    }
//}