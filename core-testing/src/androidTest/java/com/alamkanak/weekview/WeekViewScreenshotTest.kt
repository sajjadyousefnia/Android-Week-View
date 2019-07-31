package com.alamkanak.weekview

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.test.platform.app.InstrumentationRegistry
import com.facebook.testing.screenshot.Screenshot
import com.facebook.testing.screenshot.ViewHelpers
import org.junit.BeforeClass
import org.junit.Test
import java.util.Calendar

/**
 * Test that generates screenshots of a [WeekView].
 * Run via ./gradlew runDebugAndroidTestScreenshotTest
 */
class WeekViewScreenshotTest {

    private val db: EventsDatabase = FakeEventsDatabase()

    @Test
    fun basicWeekView() {
        val view = createWeekView { startDate, endDate -> db.getEventsInRange(startDate, endDate) }
        onEventsLoaded {
            view.snapScreenshot("Basic WeekView")
        }
    }

    @Test
    fun weekViewWithoutAllDayEvents() {
        val view = createWeekView { startDate, endDate ->
            db.getEventsInRange(startDate, endDate, includeAllDay = false)
        }
        onEventsLoaded {
            view.snapScreenshot("WeekView without all-day events")
        }
    }

    private fun View.snapScreenshot(
        name: String,
        width: Int = 1080,
        height: Int = 1600,
        description: String? = null,
        group: String? = null
    ) {
        ViewHelpers
            .setupView(this)
            .setExactWidthPx(width)
            .setExactHeightPx(height)
            .layout()
            .draw()

        Screenshot
            .snap(this)
            .setName(name)
            .apply {
                description?.let { setDescription(it) }
                group?.let { setGroup(it) }
            }
            .record()
    }

    private fun createWeekView(
        onMonthChange: (Calendar, Calendar) -> List<WeekViewDisplayable<Event>>
    ): WeekView<Event> {
        return WeekView<Event>(InstrumentationRegistry.getInstrumentation().context).apply {
            isAdaptiveEventTextSize = true
            allDayEventTextSize = 40
            columnGap = 3
            eventCornerRadius = 12
            defaultEventTextColor = Color.WHITE

            eventPaddingHorizontal = 12
            eventPaddingVertical = 6

            isShowHeaderRowBottomLine = true
            headerRowBottomLineWidth = 2
            headerRowBottomLineColor = Color.LTGRAY

            headerRowTextColor = Color.DKGRAY
            timeColumnTextColor = Color.DKGRAY

            headerRowPadding = 36
            hourHeight = 180f

            overlappingEventGap = 6

            isShowTimeColumnSeparator = true
            timeColumnSeparatorColor = Color.LTGRAY
            timeColumnSeparatorWidth = 2

            timeColumnPadding = 24

            setBackgroundColor(Color.WHITE)
            setOnMonthChangeListener(onMonthChange)

            goToHour(2)
        }
    }

    private fun onEventsLoaded(block: () -> Unit) {
        Handler(Looper.getMainLooper()).post {
            Thread.sleep(1_000)
            block()
        }
        Thread.sleep(3_000)
    }

    companion object {

        @BeforeClass
        @JvmStatic
        fun setup() {
            // TODO Explain
            Looper.prepare()
        }
    }
}
