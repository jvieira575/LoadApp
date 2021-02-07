package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.properties.Delegates

/**
 * Custom view class that represents the "Download" button in the [MainActivity]. This view will
 * animate once clicked to show that we are downloading the specified repo the user selected.
 */
class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Width and height of view
    private var widthSize = 0
    private var heightSize = 0

    // View attributes
    private var downloadBackgroundColor = 0
    private var downloadTextColor = 0
    private var loadingBackgroundColor = 0
    private var loadingTextColor = 0
    private var loadingCircleColor = 0

    // Animators
    private var loadingButtonAnimator = ValueAnimator()
    private var loadingCircleAnimator = ValueAnimator()

    // Animation properties
    private var loadingButtonProgressWidth = 0
    private var loadingCircleAngle = 0

    // Paint
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Observable which observes the ButtonState to start or stop animations
    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->

        Log.d("LoadingButton", "ButtonState Old: $old New: $new")

        // Prevents animations from hanging around even when the download is completed...
        if (new != old) {
            when (new) {
                ButtonState.Loading -> {
                    // Animate the loading button
                    invalidate()
                    loadingButtonAnimator = ValueAnimator.ofInt(0, widthSize).apply {
                        duration = 1000
                        addUpdateListener { valueAnimator ->
                            loadingButtonProgressWidth = animatedValue as Int
                            valueAnimator.repeatCount = ValueAnimator.INFINITE
                            valueAnimator.repeatMode = ValueAnimator.REVERSE
                            invalidate()
                        }
                        start()
                    }

                    // Animate the loading button circle
                    invalidate()
                    loadingCircleAnimator = ValueAnimator.ofInt(0, 360).apply {
                        duration = 2000
                        addUpdateListener { valueAnimator ->
                            loadingCircleAngle = valueAnimator.animatedValue as Int
                            valueAnimator.repeatCount = ValueAnimator.INFINITE
                            invalidate()
                        }
                        start()
                    }
                }
                ButtonState.Completed -> {

                    // Cancel all animations
                    loadingButtonAnimator.end()
                    loadingCircleAnimator.end()
                    loadingButtonAnimator.removeAllUpdateListeners()
                    loadingCircleAnimator.removeAllUpdateListeners()
                    loadingButtonProgressWidth = 0
                    loadingCircleAngle = 0
                    invalidate()
                }
                else -> Log.i("LoadingButton", "Clicked. Nothing to do.")
            }
        }
    }

    init {
        isClickable = true
        context.theme.obtainStyledAttributes(attrs, R.styleable.LoadingButton, 0, 0).apply {
            try {
                downloadBackgroundColor = getColor(
                    R.styleable.LoadingButton_downloadBackgroundColor,
                    context.getColor(R.color.colorPrimary)
                )
                downloadTextColor =
                    getColor(R.styleable.LoadingButton_downloadTextColor, Color.WHITE)
                loadingBackgroundColor = getColor(
                    R.styleable.LoadingButton_loadingBackgroundColor,
                    context.getColor(R.color.colorPrimaryDark)
                )
                loadingTextColor = getColor(R.styleable.LoadingButton_loadingTextColor, Color.WHITE)
                loadingCircleColor = getColor(
                    R.styleable.LoadingButton_loadingCircleColor,
                    context.getColor(R.color.colorAccent)
                )
            } finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // The Download button
        paint.color = downloadBackgroundColor
        canvas?.drawRect(
            0f,
            0f,
            widthSize.toFloat(),
            heightSize.toFloat(), paint
        )

        // The Loading button
        paint.color = loadingBackgroundColor
        canvas?.drawRect(
            0f,
            0f,
            widthSize.toFloat() * loadingButtonProgressWidth / 100,
            heightSize.toFloat(), paint
        )

        // Button text
        paint.color = loadingTextColor
        paint.textSize = context.resources.getDimension(R.dimen.default_text_size)
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        // Determine offset value to center the text properly
        val textHeight: Float = paint.descent() - paint.ascent()
        val textOffset: Float = textHeight / 2 - paint.descent()

        canvas?.drawText(
            getButtonText(),
            (widthSize / 2).toFloat(),
            (heightSize / 2).toFloat() + textOffset, paint
        )

        // The loading circle
        paint.color = loadingCircleColor
        canvas?.drawArc(
            (widthSize - 250f), (heightSize / 2) - 50f, (widthSize - 150f),
            (heightSize / 2) + 50f, 0F, loadingCircleAngle.toFloat(), true, paint
        )
    }

    /**
     * Retrieves the correct text for button depending on button state.
     */
    private fun getButtonText(): String {
        return when (buttonState) {
            ButtonState.Loading -> context.getString(R.string.button_loading)
            ButtonState.Completed -> context.getString(R.string.button_download)
            else -> context.getString(R.string.button_loading)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    /**
     * Sets the state of the LoadingButton.
     */
    fun setState(buttonState: ButtonState) {
        this.buttonState = buttonState
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true
        invalidate()
        return true
    }
}