package com.the.drawingapp

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.core.graphics.get
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.navigation.testing.TestNavHostController
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

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
    fun isNewCanvasDisplayedOnClick() {
        val activityScenario: ActivityScenario<MainActivity> =
            ActivityScenario.launch(MainActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.newDrawingButton)).perform(click())
        onView(withId(R.id.canvas)).check(matches(isDisplayed()))
        activityScenario.moveToState(Lifecycle.State.DESTROYED)
    }

    private lateinit var dao : DrawingAppDao
    private lateinit var db : DrawingAppDatabase
    //private lateinit var vm: DrawingViewModel
    private lateinit var scope : CoroutineScope
    private lateinit var context : DrawingApplication

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, DrawingAppDatabase::class.java)
            .build()
        dao = db.drawingAppDao()
        scope = CoroutineScope(SupervisorJob())
    }
    @After
    @Throws(IOException::class)
    fun closedb(){
        db.close()
    }
    //ui tests
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

    @Test
    fun toggleToSquareShapeButton(){
        val activityScenario: ActivityScenario<MainActivity> =
            ActivityScenario.launch(MainActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.newDrawingButton)).perform(click())
        onView(withId(R.id.shape_button)).perform(click())
        runBlocking {
            val isRectangle = true
            val lifecycleOwner = TestLifecycleOwner()
            lifecycleOwner.run {
                withContext(Dispatchers.Main) {
                    val vm = DrawingViewModel(DrawingAppRepository(scope, dao, context))
                    val tool = vm.tool
                    tool.toggleShape(isRectangle)
                    assertEquals(Paint.Cap.SQUARE, tool.paint.strokeCap!!)
                }
            }
        }
        activityScenario.moveToState(Lifecycle.State.DESTROYED)
    }

    /*Testing ViewModel logic*/
    @Test
    fun testViewModel_updatingBitmap() {
        runBlocking {
            val lifecycleOwner = TestLifecycleOwner()
            lifecycleOwner.run {
                withContext(Dispatchers.Main) {
                    val vm = DrawingViewModel(DrawingAppRepository(scope, dao, context))
                    val newBitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
                    var updatedBitmap = false
                    //changing bitmap
                    vm.canvasBitmap.observe(lifecycleOwner)
                    {
                        updatedBitmap = true
                    }
                    //initialize bitMap
                    vm.initBitmap()
                    //update it
                    vm.updateBitmap(newBitmap)
                    //bitmap is originally 800 x 800, check to see if size also changed
                    //so old one is replaced
                    assertEquals(400, vm.canvasBitmap.value!!.height)
                    assertEquals(400, vm.canvasBitmap.value!!.width)
                    assertTrue(updatedBitmap)
                }
            }
        }
    }

    @Test
    fun testingViewModel_initBitmap()
    {
        runBlocking {
            val lifecycleOwner = TestLifecycleOwner()
            lifecycleOwner.run {
                withContext(Dispatchers.Main) {
                    val vm = DrawingViewModel(DrawingAppRepository(scope, dao, context))

                    //initialize bitMap
                    vm.initBitmap()

                    assertNotEquals(1920, vm.canvasBitmap.value!!.height)
                    assertNotEquals(1080, vm.canvasBitmap.value!!.width)
                }
            }
        }

    }

    /*Testing Tool Logic*/
    @Test
    fun testingTool_updateColor(){
        runBlocking {
            val lifecycleOwner = TestLifecycleOwner()
            lifecycleOwner.run {
                withContext(Dispatchers.Main) {

                    val vm = DrawingViewModel(DrawingAppRepository(scope, dao, context))
                    val tool = vm.tool
                    //change color to red (#ff0000 -> int)
                    val color = 0xFF000000.toInt()
                    var updatedColor = false

                    //color is set to an int, when starting, it's black
                    tool.activatePen()
                    //change color
                    tool.currentColor.observe(lifecycleOwner)
                    {
                        updatedColor = true
                    }
                    tool.updateColor(color)
                    assertEquals(color, tool.currentColor.value!!)
                    assertTrue(updatedColor)
                }
            }
        }
    }

   @Test
    fun testingTool_updateStrokeWidth(){
        runBlocking {
            val lifecycleOwner = TestLifecycleOwner()
            lifecycleOwner.run {
                withContext(Dispatchers.Main) {
                    val vm = DrawingViewModel(DrawingAppRepository(scope, dao, context))
                    val tool = vm.tool
                    val width = 13f
                    var updatedWidth = false

                    //set width to default value
                    tool.activatePen()
                    tool.strokeWidth.observe(lifecycleOwner)
                    {
                        updatedWidth = true
                    }
                    tool.updateStrokeWidth(width)
                    assertEquals(13f, tool.strokeWidth.value!!)
                    assertTrue(updatedWidth)
                }
            }
        }
    }

    @Test
    fun testingTool_toggleShape(){
        runBlocking {
            val lifecycleOwner = TestLifecycleOwner()
            lifecycleOwner.run {
                withContext(Dispatchers.Main) {
                    val vm = DrawingViewModel(DrawingAppRepository(scope, dao, context))
                    val tool = vm.tool
                    val isCircle = false

                    tool.toggleShape(isCircle)
                    assertEquals(Paint.Join.ROUND, tool.paint.strokeJoin!!)
                }
            }
        }
    }

//    testing db logic
//    https://stackoverflow.com/questions/58589300/test-methods-of-room-dao-with-kotlin-coroutines-and-flow
//    @Test
//    fun testingDB_SavingAndGettingDrawings() = runTest{
//        val drawing = DrawingAppData(Date(), "drawing1")
//        dao.saveDrawing(drawing)
//        val list = dao.getAllDrawings().take(1).toList()
//        assertEquals(1, list.size)
//        assertEquals("drawing1", list.get(0).get(0).name)
//    }
}

@RunWith(AndroidJUnit4::class)
class DrawingAppComposeTests {

    @get:Rule
    val composeTestRule = createComposeRule()
    private var drawingFlow = MutableStateFlow<List<Drawing>>(emptyList())

    @Before
    fun setup() {
        drawingFlow.value = listOf(
            Drawing(1, Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)),
            Drawing(2, Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)),
            Drawing(3, Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888))
        )

    }

    @Test
    fun savedCanvasList_displaysCorrectNumberOfItems() {
        composeTestRule.setContent {
            SavedCanvasList(savedCanvases = drawingFlow) {}
        }

        drawingFlow.value.forEachIndexed() { _, bitmap ->
            composeTestRule
                .onNodeWithContentDescription(bitmap.toString(), useUnmergedTree = true)
                .assertExists()
        }
    }

    @Test
    fun savedCanvas_clickTriggersOnClick() {
        var clicked = false
        val drawing = Drawing(1, Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888))
        drawingFlow.value = listOf(drawing)

        composeTestRule.setContent {
            SavedCanvasList(savedCanvases = drawingFlow) {
                clicked = true
            }
        }

        composeTestRule
            .onNodeWithContentDescription(drawing.toString(), useUnmergedTree = true)
            .performClick()

        assert(clicked)
    }

    @Test
    fun savedCanvasList_displaysBitmapImagesCorrectly() = runTest {
        val bitmap = Drawing(1, Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.RED) })
        val bitmapDescription = bitmap.toString()
        val bitmapListFlow = MutableStateFlow(listOf(bitmap))

        composeTestRule.setContent {
            SavedCanvasList(savedCanvases = bitmapListFlow) {
            }
        }

        composeTestRule.onNodeWithContentDescription(bitmapDescription, useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun savedCanvas_clickTriggersExpectedAction() = runTest {
        var clickedBitmap: Drawing? = null
        val target = drawingFlow.value.first()
        val targetDescription = target.toString()

        composeTestRule.setContent {
            SavedCanvasList(savedCanvases = drawingFlow) { selectedBitmap ->
                clickedBitmap = selectedBitmap
            }
        }

        composeTestRule
            .onNodeWithContentDescription(targetDescription, useUnmergedTree = true)
            .performClick()

        assertEquals(target, clickedBitmap)
    }

    @Test
    fun savedCanvasList_ScrollsHorizontally() {
        val bitmap = drawingFlow.value.last()

        composeTestRule.setContent {
            SavedCanvasList(savedCanvases = drawingFlow) {}
        }
        composeTestRule.onNodeWithContentDescription(bitmap.toString(), useUnmergedTree = true)
            .assertExists()
            .performTouchInput { swipeLeft() }

    }
    @Test
    fun canvasClick_TriggersNavigation() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val bitmap = Drawing(1, Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888))

        composeTestRule.setContent {

            val savedCanvasesFlow = MutableStateFlow(
                listOf(bitmap)
            )
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.mainScreenFragment)
            SavedCanvasList(savedCanvases = savedCanvasesFlow) {
                navController.navigate(R.id.drawableFragment) // Use the correct action ID
            }
        }

        composeTestRule.onNodeWithContentDescription(bitmap.toString(), useUnmergedTree = true)
            .performClick()

        assert(navController.currentDestination?.id == R.id.drawableFragment) // Use the correct destination ID
    }


}

@RunWith(AndroidJUnit4::class)
class DrawingAppDrawingTests {
    private lateinit var activityScenario: ActivityScenario<MainActivity>
    private var canvas: DrawingView? = null
    @Before
    fun setUp() {
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.newDrawingButton)).perform(click())
    }
    @Test
    fun currCanvas_CanDrawDot() {
        onView(withId(R.id.canvas)).perform(click())
        activityScenario.onActivity { activity ->
            canvas = activity.findViewById(R.id.canvas)
        }
        assertEquals(Color.BLACK.toInt(), canvas?.getBitmap()?.get(500, 500))
    }
    @Test
    fun currCanvas_CanDrawHorizontalLine() {
        onView(withId(R.id.canvas)).perform(swipeLeft())
        activityScenario.onActivity { activity ->
            canvas = activity.findViewById(R.id.canvas)
        }
        assertEquals(Color.BLACK.toInt(), canvas?.getBitmap()?.get(300,500)) // Checks pixel at the middle of the canvas
        assertEquals(Color.BLACK.toInt(), canvas?.getBitmap()?.get(400,500)) // Checks pixel at the middle of the canvas
        assertEquals(Color.BLACK.toInt(), canvas?.getBitmap()?.get(500,500)) // Checks pixel at the middle of the canvas
        assertEquals(Color.BLACK.toInt(), canvas?.getBitmap()?.get(600,500)) // Checks pixel at the middle of the canvas
        assertEquals(Color.BLACK.toInt(), canvas?.getBitmap()?.get(700,500)) // Checks pixel at the middle of the canvas
    }
    @Test
    fun currCanvas_CanDrawVerticalLine() {
        onView(withId(R.id.canvas)).perform(swipeUp())
        activityScenario.onActivity { activity ->
            canvas = activity.findViewById(R.id.canvas)
        }
        assertEquals(Color.BLACK.toInt(), canvas?.getBitmap()?.get(500,300)) // Checks pixel at the middle of the canvas
        assertEquals(Color.BLACK.toInt(), canvas?.getBitmap()?.get(500,400)) // Checks pixel at the middle of the canvas
        assertEquals(Color.BLACK.toInt(), canvas?.getBitmap()?.get(500,500)) // Checks pixel at the middle of the canvas
        assertEquals(Color.BLACK.toInt(), canvas?.getBitmap()?.get(500,600)) // Checks pixel at the middle of the canvas
        assertEquals(Color.BLACK.toInt(), canvas?.getBitmap()?.get(500,700)) // Checks pixel at the middle of the canvas
    }
}