package com.example.waterintaketracker

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.max

/**
 * A simple bar chart drawn with Canvas (no external library).
 * Shows one bar per day plus a dashed line for the daily goal.
 */
class WeeklyChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var values: List<Float> = emptyList()
    private var labels: List<String> = emptyList()
    var goal: Float = 0f
        set(value) { field = value; invalidate() }

    private val density = resources.displayMetrics.density
    private val scaled = resources.displayMetrics.scaledDensity

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary)
    }
    private val goalMetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.accent)
    }
    private val emptyBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.card_stroke)
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.text_secondary)
        textSize = 12 * scaled
        textAlign = Paint.Align.CENTER
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary_dark)
        textSize = 11 * scaled
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val goalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.text_secondary)
        strokeWidth = 1.5f * density
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(8 * density, 6 * density), 0f)
    }

    private val rect = RectF()

    fun setData(values: List<Float>, labels: List<String>) {
        this.values = values
        this.labels = labels
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = (200 * density).toInt()
        setMeasuredDimension(
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (values.isEmpty()) return

        val labelSpace = 22 * density
        val valueSpace = 18 * density
        val chartTop = paddingTop + valueSpace
        val chartBottom = height - paddingBottom - labelSpace
        val chartHeight = chartBottom - chartTop
        val left = paddingLeft.toFloat()
        val slot = (width - paddingLeft - paddingRight) / values.size.toFloat()
        val barWidth = slot * 0.55f
        val maxValue = max(values.maxOrNull() ?: 0f, goal).coerceAtLeast(1f)
        val corner = 6 * density

        values.forEachIndexed { i, v ->
            val cx = left + slot * i + slot / 2
            val barHeight = (v / maxValue) * chartHeight
            val minVisible = 3 * density
            rect.set(
                cx - barWidth / 2,
                chartBottom - max(barHeight, minVisible),
                cx + barWidth / 2,
                chartBottom
            )
            val paint = when {
                v <= 0f -> emptyBarPaint
                goal > 0f && v >= goal -> goalMetPaint
                else -> barPaint
            }
            canvas.drawRoundRect(rect, corner, corner, paint)

            if (v > 0f) {
                canvas.drawText(WaterAdapter.formatOz(v.toDouble()), cx, rect.top - 5 * density, valuePaint)
            }
            labels.getOrNull(i)?.let {
                canvas.drawText(it, cx, chartBottom + labelSpace - 6 * density, labelPaint)
            }
        }

        if (goal > 0f) {
            val y = chartBottom - (goal / maxValue) * chartHeight
            canvas.drawLine(left, y, (width - paddingRight).toFloat(), y, goalPaint)
        }
    }
}
