package com.udacity

import android.animation.ValueAnimator
import android.annotation.SuppressLint
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

    private var buttonText = "Download"
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val valueAnimator = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->

    }



    init {

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = resources.getDimension(R.dimen.default_text_size)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingButton)
        buttonText = typedArray.getString(R.styleable.LoadingButton_buttonText) ?: context.getString(R.string.button_name)
        typedArray.recycle()

        //New
        setOnClickListener{
            buttonState = ButtonState.Clicked
        }

    }


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // Draw Button background
        paint.color = ContextCompat.getColor(context, R.color.colorPrimary)
        canvas?.drawRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(), paint)

        // Draw Text On Button Background
        val textBounds = Rect()
        paint.getTextBounds(buttonText,0, buttonText.length,textBounds)
        val textBaseline = (heightSize - textBounds.height())/2f - textBounds.top
        paint.color = Color.WHITE
        canvas?.drawText(buttonText, (widthSize/2).toFloat(), textBaseline, paint)
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

}