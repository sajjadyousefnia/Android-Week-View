package com.alamkanak.weekview

import android.graphics.Canvas
import android.text.Layout
import android.text.StaticLayout
import android.util.SparseArray

internal class TimeColumnRenderer(
    private val viewState: ViewState
) : Renderer, TimeFormatterDependent {

    private val timeLabelLayouts = SparseArray<StaticLayout>()
    private val quarterTimeLabelLayouts = SparseArray<StaticLayout>()
    private val halfTimeLabelLayouts = SparseArray<StaticLayout>()


    init {
        updateTimeLabels()
    }

    override fun onSizeChanged(width: Int, height: Int) {
        updateTimeLabels()
    }

    override fun onTimeFormatterChanged(formatter: TimeFormatter) {
        updateTimeLabels()
    }

    override fun render(canvas: Canvas) = with(viewState) {
        val bottom = viewState.viewHeight.toFloat()
        val bounds = viewState.timeColumnBounds
        val extraInitialPadding = viewWidth


        // Draw background
        canvas.drawRect(bounds, timeColumnBackgroundPaint)

        val hourLines = FloatArray(hoursPerDay * 4)

        for (hour in displayedHours) {
            val heightOfHour = hourHeight * (hour - minHour)
            val topMargin = headerHeight + currentOrigin.y + heightOfHour

            val isOutsideVisibleArea = topMargin > bottom
            if (isOutsideVisibleArea) {
                continue
            }

            var y = topMargin - timeColumnTextHeight / 2

            // If the hour separator is shown in the time column, move the time label below it
            if (showTimeColumnHourSeparators) {
                y += timeColumnTextHeight / 2 + hourSeparatorPaint.strokeWidth + timeColumnPadding
            }

            val label = timeLabelLayouts[hour]
            var x =
                if (viewState.isLtr) {
                    bounds.right - viewState.timeColumnPadding - viewState.timeColumnMarginRight + viewState.timeColumnMarginLeft
                } else {
                    bounds.left + viewState.timeColumnPadding - viewState.timeColumnMarginRightRtl + viewState.timeColumnMarginLeftRtl
                }
            val minimumX = canvas.clipBounds.left
            val maximumX = canvas.clipBounds.right

            x = x.coerceAtMost(maximumX.toFloat())
            x = x.coerceAtLeast(minimumX.toFloat())

            canvas.withTranslation(x, y) {
                label.draw(this)
            }
            if (showTimeColumnHourSeparators && hour > 0) {
                val j = hour - 1
                hourLines[j * 4] = x
                hourLines[j * 4 + 1] = topMargin
                hourLines[j * 4 + 2] = x + timeColumnWidth
                hourLines[j * 4 + 3] = topMargin
            }
        }

        // Draw the vertical time column separator
        if (showTimeColumnSeparator) {
            val lineX = if (isLtr) {
                timeColumnWidth - timeColumnSeparatorPaint.strokeWidth / 2
            } else {
                viewWidth - timeColumnWidth
            }
            canvas.drawLine(lineX, headerHeight, lineX, bottom, timeColumnSeparatorPaint)
        }

        // Draw the hour separator inside the time column
        if (showTimeColumnHourSeparators) {
            canvas.drawLines(hourLines, hourSeparatorPaint)
        }
    }

    private fun updateTimeLabels() = with(viewState) {
        timeLabelLayouts.clear()

        val textLayouts = mutableListOf<StaticLayout>()

        val labelTextAlignment = when (viewState.isLtr) {
            true -> {
                Layout.Alignment.ALIGN_NORMAL
            }

            false -> {
                Layout.Alignment.ALIGN_OPPOSITE
            }
        }

        for (hour in displayedHours) {
            val textLayout =
                timeFormatter(hour).toTextLayout(
                    timeColumnTextPaint,
                    width = Int.MAX_VALUE,
                    alignment = labelTextAlignment
                )
            textLayouts += textLayout
            timeLabelLayouts.put(hour, textLayout)

            if (viewState.showQuarterHourSeparator) {
                // 15 min


            } else if (viewState.showHalfHourSeparator) {
                //  timeLabelLayouts.put()
            }
        }
        val maxLineLength = textLayouts.maxOfOrNull { it.maxLineLength } ?: 0f
        val maxLineHeight = textLayouts.maxOfOrNull { it.height } ?: 0

        updateTimeColumnBounds(
            lineLength = maxLineLength,
            lineHeight = maxLineHeight.toFloat()
        )
    }


}
