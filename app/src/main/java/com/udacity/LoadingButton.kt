package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var widthSize = 0
    private var heightSize = 0
    private var textWidth = 0f

    private var progressCircle = 0f
    private var progressWidth = 0f
    private val textSize = resources.getDimension(R.dimen.default_text_size)

    private var buttonText: String = context.getString(R.string.button_name)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        textSize = resources.getDimension(R.dimen.default_text_size)
    }

    private val buttonColor = ContextCompat.getColor(context, R.color.colorPrimary)
    private val loadingColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
    private val circleColor = ContextCompat.getColor(context, R.color.colorAccent)

    private var valueAnimator = ValueAnimator()

    /**
     * handles different button states (Clicked, Loading, Completed)
     * and updates the view accordingly
     */
    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, new ->
        when (new) {
            ButtonState.Loading -> {
                buttonText = resources.getString(R.string.button_loading)
                valueAnimator = ValueAnimator.ofFloat(0f, widthSize.toFloat()).apply {
                    duration = 6000
                    addUpdateListener { anim ->
                        progressWidth = anim.animatedValue as Float
                        progressCircle = (widthSize.toFloat() / 365) * progressWidth
                        invalidate()
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            if (buttonState == ButtonState.Loading) {
                                buttonState = ButtonState.Completed
                            }
                        }
                    })
                    start()
                }
            }
            ButtonState.Completed -> {
                valueAnimator.cancel()
                progressWidth = 0f
                progressCircle = 0f
                buttonText = context.getString(R.string.button_name)
                invalidate()

            }
            else -> {}
        }
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingButton)
        buttonText = typedArray.getString(R.styleable.LoadingButton_buttonText) ?: context.getString(R.string.button_name)
        typedArray.recycle()

        setOnClickListener {
            if (buttonState != ButtonState.Loading) {
                buttonState = ButtonState.Loading
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        drawButtonBackground(canvas)
        drawButtonText(canvas)
        drawProgressCircle(canvas)
    }

    private fun drawButtonBackground(canvas: Canvas?) {
        paint.color = if (buttonState == ButtonState.Completed) buttonColor else loadingColor
        canvas?.drawRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(), paint)
    }

    private fun drawButtonText(canvas: Canvas?) {
        val textBounds = Rect()
        paint.getTextBounds(buttonText, 0, buttonText.length, textBounds)
        val textBaseline = (heightSize - textBounds.height()) / 2f - textBounds.top
        paint.color = Color.WHITE
        textWidth = paint.measureText(buttonText)
        canvas?.drawText(buttonText, (widthSize / 2).toFloat(), textBaseline, paint)
    }

    private fun drawProgressCircle(canvas: Canvas?) {
        canvas?.save()
        canvas?.translate(widthSize / 2 + textWidth / 2 + textSize / 2, heightSize / 2 - textSize / 2)
        paint.color = circleColor
        canvas?.drawArc(RectF(0f, 0f, textSize, textSize), 0f, progressCircle * 0.365f, true, paint)
        canvas?.restore()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw = paddingLeft + paddingRight + suggestedMinimumWidth
        val w = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0)
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }
}
