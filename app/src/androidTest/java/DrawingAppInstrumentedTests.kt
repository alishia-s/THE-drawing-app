package com.the.drawingapp

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import androidx.activity.ComponentActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.geometry.isFinite
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.navigation.testing.TestNavHostController
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import com.the.drawingapp.MainScreenFragment
import junit.framework.TestCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.junit.After
import org.junit.runner.manipulation.Ordering.Context
import org.mockito.Mockito.`when`
import java.io.IOException
import java.util.Date

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

}

@RunWith(AndroidJUnit4::class)
class DrawingAppComposeTests {

    @get:Rule
    val composeTestRule = createComposeRule()
    private var bitmapFlow = MutableStateFlow<List<Bitmap>>(emptyList())

    @Before
    fun setup() {
        bitmapFlow.value = listOf(
            Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888),
            Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888),
            Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        )

    }

    @Test
    fun savedCanvasList_displaysCorrectNumberOfItems() {
        composeTestRule.setContent {
            SavedCanvasList(savedCanvases = bitmapFlow) {}
        }

        bitmapFlow.value.forEachIndexed() { _, bitmap ->
            composeTestRule
                .onNodeWithContentDescription(bitmap.toString(), useUnmergedTree = true)
                .assertExists()
        }
    }

    @Test
    fun savedCanvas_clickTriggersOnClick() {
        var clicked = false
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmapFlow.value = listOf(bitmap)

        composeTestRule.setContent {
            SavedCanvasList(savedCanvases = bitmapFlow) {
                clicked = true
            }
        }

        composeTestRule
            .onNodeWithContentDescription(bitmap.toString(), useUnmergedTree = true)
            .performClick()

        assert(clicked)
    }

    @Test
    fun savedCanvasList_displaysBitmapImagesCorrectly() = runTest {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.RED)
        }
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
        var clickedBitmap: Bitmap? = null
        val target = bitmapFlow.value.first()
        val targetDescription = target.toString()

        composeTestRule.setContent {
            SavedCanvasList(savedCanvases = bitmapFlow) { selectedBitmap ->
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
        val bitmap = bitmapFlow.value.last()

        composeTestRule.setContent {
            SavedCanvasList(savedCanvases = bitmapFlow) {}
        }
        composeTestRule.onNodeWithContentDescription(bitmap.toString(), useUnmergedTree = true)
            .assertExists()
            .performTouchInput { swipeLeft() }

    }
    @Test
    fun canvasClick_TriggersNavigation() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        composeTestRule.setContent {

            val savedCanvasesFlow = MutableStateFlow(
                listOf(bitmap)
            )
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.mainScreenFragment)
            SavedCanvasList(savedCanvases = savedCanvasesFlow) {
                navController.navigate(R.id.drawableFragment2) // Use the correct action ID
            }
        }

        composeTestRule.onNodeWithContentDescription(bitmap.toString(), useUnmergedTree = true)
            .performClick()

        assert(navController.currentDestination?.id == R.id.drawableFragment2) // Use the correct destination ID
    }
}