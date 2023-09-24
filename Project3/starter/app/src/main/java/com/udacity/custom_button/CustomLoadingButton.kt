package com.udacity.custom_button

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.udacity.R
import timber.log.Timber
import kotlin.math.min
import kotlin.properties.Delegates

class CustomLoadingButton @JvmOverloads constructor(
    ctx: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(ctx, attributeSet, defStyleAttr){

    //styling attributes
    private var loadingDefaultBackgroundColor = 0
    private var loadingBackgroundColor = 0
    private var loadingDefaultText: CharSequence = ""
    private var loadingText: CharSequence = ""
    private var loadingTextColor = 0
    private var progressCircleBackgroundColor = 0

    private var widthSize = 0
    private var heightSize = 0

    private var buttonText = ""

    //paint of button
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    //paint of text on button
    private val buttonTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55f
        typeface = Typeface.DEFAULT
    }

    private lateinit var buttonTextBounds: Rect

    private val progressCircleRect = RectF()
    private var progressCircleSize = 0f

    private val animatorSet: AnimatorSet = AnimatorSet().apply {
        duration = 3000
        disableViewDuringAnimation(this@CustomLoadingButton, this)
    }

    private fun disableViewDuringAnimation(view: View, animatorSet: AnimatorSet) {
        Timber.d("#disableViewDuringAnimation")
        animatorSet.doOnStart {
            view.isEnabled = false
        }
        animatorSet.doOnEnd {
            view.isEnabled = true
        }
    }

    private var currentProgressCircleAnimationValue = 0f
    private var progressCircleAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            currentProgressCircleAnimationValue = it.animatedValue as Float
            invalidate()
        }
    }

    private var currentButtonBackgroundAnimationValue = 0f
    private lateinit var buttonBackgroundAnimator: ValueAnimator

    private var buttonState: ButtonState by Delegates.observable(ButtonState.Completed){ _,_,newState ->
        when(newState){
            ButtonState.Loading -> {
                Timber.d("#buttonState = ButtonState.Loading()")
                buttonText = loadingText.toString()
                if (!::buttonTextBounds.isInitialized){
                    retrieveButtonTextBounds()
                    computeProgressCircleRect()
                }
                animatorSet.start()
            }
            else -> {
                Timber.d("#buttonState = else")
                buttonText = loadingDefaultText.toString()
                if(newState == ButtonState.Completed){
                    animatorSet.cancel()
                }
            }
        }
    }

    private fun retrieveButtonTextBounds(){
        Timber.d("#retrieveButtonTextBounds()")
        buttonTextBounds = Rect()
        buttonTextPaint.getTextBounds(buttonText, 0, buttonText.length, buttonTextBounds)
    }

    private fun computeProgressCircleRect(){
        Timber.d("#computeProgressCircleRect()")
        val horizontalCenter = (buttonTextBounds.right + buttonTextBounds.width() + 16f)
        val verticalCenter = heightSize / 2

        progressCircleRect.set(
            horizontalCenter - progressCircleSize,
            verticalCenter - progressCircleSize,
            horizontalCenter + progressCircleSize,
            verticalCenter + progressCircleSize
        )
    }

    init {
        Timber.d("#init()")
        isClickable = true
        context.withStyledAttributes(attributeSet, R.styleable.LoadingButton){
            loadingDefaultBackgroundColor = getColor(R.styleable.LoadingButton_loadingDefaultBackgroundColor, 0)
            loadingBackgroundColor = getColor(R.styleable.LoadingButton_loadingBackgroundColor, 0)
            loadingDefaultText = getText(R.styleable.LoadingButton_loadingDefaultText)
            loadingTextColor = getColor(R.styleable.LoadingButton_loadingTextColor, 0)
            loadingText = getText(R.styleable.LoadingButton_loadingText)
        }
        buttonText = loadingDefaultText.toString()
        progressCircleBackgroundColor = ContextCompat.getColor(context, R.color.colorAccent)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Timber.d("#onMeasure()")
        val minWidth = paddingLeft + paddingRight + suggestedMinimumWidth
        val w = resolveSizeAndState(minWidth, widthMeasureSpec, 1)
        val h = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0)
        widthSize = w
        heightSize = h
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Timber.d("#onSizeChanged()")
        progressCircleSize = (min(h,w)/2) * 0.4f
        createBackgroundAnimatorForButton()
    }

    private fun createBackgroundAnimatorForButton() {
        Timber.d("#createBackgroundAnimatorForButton()")
        ValueAnimator.ofFloat(0f, widthSize.toFloat()).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                currentButtonBackgroundAnimationValue = it.animatedValue as Float
                invalidate()
            }
        }.apply {
            buttonBackgroundAnimator = this
            animatorSet.playTogether(progressCircleAnimator, buttonBackgroundAnimator)
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        Timber.d("#performClick()")
        if (buttonState  == ButtonState.Completed){
            buttonState = ButtonState.Clicked
            invalidate()
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        Timber.d("#onDraw()")
        canvas?.let {
            drawBackgroundColorForButton(it)
            drawButtonText(it)
            drawProgressCircleIfLoading(it)
        }
    }

    private fun drawBackgroundColorForButton(canvas: Canvas) {
        Timber.d("#drawBackgroundColorForButton()")
        when(buttonState){
            ButtonState.Loading -> {
                //draw loading background color
                buttonPaint.color = loadingBackgroundColor
                canvas.drawRect(0f, 0f, currentButtonBackgroundAnimationValue, heightSize.toFloat(), buttonPaint)

                //draw default loading background color
                buttonPaint.color = loadingDefaultBackgroundColor
                canvas.drawRect(currentButtonBackgroundAnimationValue, 0f, widthSize.toFloat(), heightSize.toFloat(), buttonPaint)
            }
            else -> {
                //draw loading background color when it not is loading state
                canvas.drawColor(loadingDefaultBackgroundColor)
            }
        }
    }

    private fun drawButtonText(canvas: Canvas) {
        Timber.d("#drawButtonText()")
        buttonTextPaint.color = loadingTextColor
        canvas.drawText(
            buttonText, (widthSize / 2f),
            ((heightSize / 2) + ((buttonTextPaint.descent() - buttonTextPaint.ascent()) / 2) - buttonTextPaint.descent()),
            buttonTextPaint
        )
    }

    private fun drawProgressCircleIfLoading(canvas: Canvas) {
        Timber.d("#drawProgressCircleIfLoading()")
        if (buttonState == ButtonState.Loading){
            buttonPaint.color = progressCircleBackgroundColor
            canvas.drawArc(progressCircleRect, 0f, currentProgressCircleAnimationValue, true, buttonPaint)
        }
    }

    fun changeButtonState(currentState: ButtonState){
        Timber.d("#changeButtonState()")
        if (currentState != buttonState){
            buttonState = currentState
            invalidate()
        }
    }
}
