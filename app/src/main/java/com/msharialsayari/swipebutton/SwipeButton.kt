package com.msharialsayari.swipebutton

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class SwipeButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = -1, defStyleRes: Int = -1
) : RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {


    private var initialX: Float = 0.toFloat()


    var enable: Boolean = false
        set(isEnables) {
            if (isEnables) {
                setOnTouchListener(getButtonTouchListener())
                textBefore.setTextColor(ContextCompat.getColor(context, R.color.Black))
                swipeButton.background = slidingButtonBackground
            } else {
                setOnTouchListener(getCancelTouchListener())
                textBefore.setTextColor(ContextCompat.getColor(context, R.color.silver))
                swipeButton.background = unEnabledSlidingBackgroundColor
            }
            field = isEnables
        }

    private var touchedEnd: Boolean = false
    private var fingerPositionInRange: Boolean = false


    private var holdSlidingButton: Float = 0f


    private var mainContainer: RelativeLayout

    //The texts in the center of the button
    private var textBefore: TextView //  SLIDE TO PAY
    private var textAfter: TextView //  $$$


    //The container of all text in the center
    private var textBeforeContainer: LinearLayout // SLIDE TO PAY $$$ SR
    private var textAfterContainer: LinearLayout // PAY $$$ SR

    //The container og horizontal ProgressBar
    private var loadingProgressBarContainer: LinearLayout

    //this progressBar will be shown when a user is sliding
    private var progressBar: RelativeLayout

    //This is the moving part of the view. It contains an icon.
    private var swipeButton: ImageView


    private var textBeforeColor: Int
    private var textAfterColor: Int

    private var progressBackgroundColor: Drawable
    private var slidingButtonBackground: Drawable
    private var unEnabledSlidingBackgroundColor: Drawable
    private var swipeButtonIcon: Drawable
    private var swipeButtonMainContainer: Drawable
    private var mainContainerTransitionDrawable: TransitionDrawable


    //default values
    private val defaultMargin: Int = 16
    private val defaultPadding: Int = 30
    private val defaultTextSize: Float = 16f
    private val defaultDuration: Int = 250
    private var isFirstCheck = true
    private var isStartSwiping = true
    private var isTouchEndEdge= true
    private var mainContainerWidth: Int = 0
    private var mainContainerHeight: Int = 0

    private var callBack: SwipeButtonListener? = null


    init {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwipeButton, 0, 0)


        this.mainContainer = RelativeLayout(context)
        this.swipeButton = ImageView(context)


        this.loadingProgressBarContainer = LinearLayout(context)
        this.progressBar = RelativeLayout(context)



        this.textBeforeContainer = LinearLayout(context)
        this.textBefore = TextView(context)



        this.textAfterContainer = LinearLayout(context)
        this.textAfter = TextView(context)






        mainContainerTransitionDrawable = ContextCompat.getDrawable(
            context,
            R.drawable.tr_swipe_button_transition
        ) as TransitionDrawable
        swipeButtonMainContainer =
            typedArray.getDrawable(R.styleable.SwipeButton_mainContainerBackground)
                ?: ContextCompat.getDrawable(
                    context,
                    R.drawable.bg_swipe_button_main_container_before_swipe
                )!!




        textBefore.text = typedArray.getString(R.styleable.SwipeButton_textBefore)
        textAfter.text = typedArray.getString(R.styleable.SwipeButton_textAfter)
        textBeforeColor = typedArray?.getColor(
            R.styleable.SwipeButton_textBeforeColor,
            ContextCompat.getColor(context, R.color.Black)
        ) ?: ContextCompat.getColor(context, R.color.Black)
        textAfterColor = typedArray?.getColor(
            R.styleable.SwipeButton_textAfterColor,
            ContextCompat.getColor(context, R.color.White)
        ) ?: ContextCompat.getColor(context, R.color.White)
        textBefore.textSize = typedArray?.getInteger(
            R.styleable.SwipeButton_textBeforeSize,
            defaultTextSize.toInt()
        )?.toFloat() ?: 16f
        textAfter.textSize = typedArray?.getInteger(
            R.styleable.SwipeButton_textAfterSize,
            defaultTextSize.toInt()
        )?.toFloat() ?: 16f


        progressBackgroundColor =
            typedArray.getDrawable(R.styleable.SwipeButton_progressBackgroundColor)
                ?: ContextCompat.getDrawable(
                    context,
                    R.drawable.bg_swipe_button_main_container_after_swipe
                )!!
        swipeButtonIcon = typedArray.getDrawable(R.styleable.SwipeButton_swipeButtonImageDrawable)
            ?: ContextCompat.getDrawable(context, R.drawable.ic_arrow_forward_white_24dp)!!
        slidingButtonBackground =
            typedArray.getDrawable(R.styleable.SwipeButton_swipeButtonBackgroundColor)
                ?: ContextCompat.getDrawable(context, R.drawable.bg_swipe_button_icon)!!
        unEnabledSlidingBackgroundColor =
            typedArray.getDrawable(R.styleable.SwipeButton_unEnabledSwipeButtonColor)
                ?: ContextCompat.getDrawable(context, R.drawable.bg_unenabled_swipe_button)!!
        swipeButton.setImageDrawable(swipeButtonIcon)
        swipeButton.background = slidingButtonBackground

        enable = typedArray.getBoolean(R.styleable.SwipeButton_enabled, true)


        layoutDirection = View.LAYOUT_DIRECTION_LTR
        buildTheView()
        if (enable) setOnTouchListener(getButtonTouchListener()) else setOnTouchListener(
            getCancelTouchListener()
        )
        typedArray.recycle()


    }

    private fun buildTheView() {
        // the order of method is matter
        initializeMainContainer(mainContainer)
        initializeTextBefore(textBeforeContainer)
        initializeTextBeforeContainer(mainContainer)
        initializeProgressBar(mainContainer)
        initializeTextAfter(textAfterContainer)
        initializeTextsAfterContainer(mainContainer)
        initializeSlidingButton(mainContainer)
    }

    /**
     * this is the main container of the view
     * @param background is the mainContainer of the view
     */
    private fun initializeMainContainer(background: RelativeLayout) {
        val layoutParamsView =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParamsView.addRule(CENTER_IN_PARENT, TRUE)
        background.background = swipeButtonMainContainer
        background.viewTreeObserver.addOnGlobalLayoutListener(
            GettingWidthAndHeightOfTheView(background)
        ) // to set width and height
        addView(background, layoutParamsView)
    }


    /**
     * this method is to initialize a text SR (2)
     *  @param background is the background of the texts container and here it is the textAfterContainer variable
     */
    private fun initializeTextAfter(background: LinearLayout) {
        val layoutParamsView =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        textAfter.setTextColor(textAfterColor)
        textAfter.textSize = defaultTextSize
        textAfter.alpha = 0f
        background.addView(textAfter, layoutParamsView)

    }

    /**
     * this method is to add a progressBar view to The main container of the view
     *  @param background is the main container of the view and here it is the mainContainer variable
     */
    private fun initializeProgressBar(background: RelativeLayout) {
        val layoutParamsView = LayoutParams(0, mainContainerHeight)
        layoutParamsView.addRule(ALIGN_PARENT_LEFT, TRUE)
        layoutParamsView.addRule(CENTER_VERTICAL, TRUE)
        progressBar.background = progressBackgroundColor
        background.addView(progressBar, layoutParamsView)
    }

    /**
     * this method is to initialize a container for texts that will be shown when a user is sliding
     *  @param background is the main container of the view and here it is the mainContainer variable
     */
    private fun initializeTextsAfterContainer(background: RelativeLayout) {

        val layoutParamsView =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParamsView.addRule(CENTER_IN_PARENT, TRUE)
        textAfterContainer.orientation = LinearLayout.HORIZONTAL
        textAfterContainer.setPadding(
            defaultPadding,
            defaultPadding,
            defaultPadding,
            defaultPadding
        )

        background.addView(textAfterContainer, layoutParamsView)

    }


    /**
     * this method is to initialize a container for texts that will be shown before sliding the button
     *  @param background is the main container of the view and here it is the mainContainer variable
     */
    private fun initializeTextBeforeContainer(background: RelativeLayout) {
        val layoutParamsView =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParamsView.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        textBeforeContainer.orientation = LinearLayout.HORIZONTAL
        textBeforeContainer.setPadding(
            defaultPadding,
            defaultPadding,
            defaultPadding,
            defaultPadding
        )
        background.addView(textBeforeContainer, layoutParamsView)

    }

    /**
     * this method is to initialize a text $$$
     *  @param background is the background of the texts container and here it is the textBeforeContainer variable
     */
    private fun initializeTextBefore(background: LinearLayout) {
        val layoutParamsView =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        textBefore.setTextColor(textBeforeColor)
        textBefore.textSize = defaultTextSize
        background.addView(textBefore, layoutParamsView)

    }


    /**
     * this method is to initialize a sliding button
     *  @param background is the main container of the view and here it is the mainContainer variable
     */
    private fun initializeSlidingButton(background: RelativeLayout) {
        swipeButton.setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding)
        val layoutParamsButton =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParamsButton.addRule(ALIGN_PARENT_LEFT, TRUE)
        layoutParamsButton.addRule(CENTER_VERTICAL, TRUE)
        background.addView(swipeButton, layoutParamsButton)
    }


    private fun getButtonTouchListener(): View.OnTouchListener {
        return OnTouchListener { v, event ->
            isTouchInTheSlidingButtonRange(event)
            when (event.action) {
                MotionEvent.ACTION_DOWN ->
                    return@OnTouchListener true
                MotionEvent.ACTION_MOVE ->
                    //if fingerPositionInRange true navigate to The method
                    if (fingerPositionInRange)
                        slideButton(event)
                MotionEvent.ACTION_UP -> {
                    //set isWorkin false
                    fingerPositionInRange = false
                    releaseButton(event)
                }


            }

            false
        }
    }

    /**
     * this method is to check if the fingerPosition is on SlidingButton or not
     */
    private fun isTouchInTheSlidingButtonRange(fingerPosition: MotionEvent) {


        if (isFirstCheck) {
            holdSlidingButton = swipeButton.right.toFloat()
            fingerPositionInRange = fingerPosition.x <= holdSlidingButton
            if (fingerPositionInRange) {
                holdSlidingButton = fingerPosition.x
                isFirstCheck = false
            }

        } else {
            fingerPositionInRange =
                fingerPosition.x >= holdSlidingButton || fingerPosition.x <= holdSlidingButton
            if (fingerPositionInRange)
                holdSlidingButton = fingerPosition.x
        }


    }

    private fun getCancelTouchListener(): View.OnTouchListener {
        return OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN ->
                    return@OnTouchListener true
                MotionEvent.ACTION_MOVE ->
                    return@OnTouchListener true
                MotionEvent.ACTION_UP ->
                    return@OnTouchListener true


            }

            false
        }
    }


    private fun slideButton(fingerPosition: MotionEvent): Boolean {
        if (initialX == 0f)
            initialX = swipeButton.x

        val startEdge = mainContainer.left
        val endEdge = mainContainer.width - (mainContainer.width / 20)
        val halfSlidingButton = swipeButton.width / 2



        // this IF statement to make sure that the button will not pass the end of the mainContainer
        if (fingerPosition.x + halfSlidingButton >= endEdge) { // the moving button touched the end
            swipeButton.x = mainContainer.width.toFloat() - swipeButton.width
            touchedEnd = true
            if (isTouchEndEdge) {
                isTouchEndEdge = false
                callBack?.onTouchEndEdge(this)
            }
            expandProgress(swipeButton.x + halfSlidingButton)


        } else { //  did not, keep moving
            touchedEnd = false
            isTouchEndEdge = true
            if (isStartSwiping) {
                isStartSwiping = false
                callBack?.onStartSwiping(this)
            }
            swipeButton.x = fingerPosition.x - halfSlidingButton
            touchedEnd = false
            if (swipeButton.x > 0) {
                expandProgress(swipeButton.x + halfSlidingButton)
            }


        }

        // this IF statement to make sure that the button will not pass the start of the mainContainer
        if (fingerPosition.x - halfSlidingButton <= startEdge) {
            swipeButton.x = 0f
            touchedEnd = false
            expandProgress(swipeButton.x)
        }

        changeAlphaOfTexts(fingerPosition.x)



        return true
    }


    private fun expandProgress(x: Float) {
        progressBar.layoutParams = LayoutParams(x.toInt(), mainContainer.height)

    }


    private fun transitionBackground() {
        mainContainer.background = mainContainerTransitionDrawable
        mainContainerTransitionDrawable.startTransition(defaultDuration)

    }

    @Throws(IllegalArgumentException::class)
    private fun releaseButton(event: MotionEvent): Boolean {
        if (!touchedEnd)
            moveButtonBack()
        else
            onCompletelySlidingButton()



        return true
    }

    private fun onCompletelySlidingButton() {

        requireNotNull(callBack) { "callBack object is null you must pass the target by using setSwipeButtonListener method" }
        doAction()
        callBack!!.onCompletelySlidingButton(this)
    }

    fun doAction() {
        transitionBackground()
        progressBar.layoutParams = LayoutParams(0, mainContainer.height)
        swipeButton.visibility = View.GONE
        textBeforeContainer.visibility = View.GONE
        textAfterContainer.visibility = View.VISIBLE
        setOnTouchListener(getCancelTouchListener())

    }

    private fun moveButtonBack() {

        val positionAnimator = ValueAnimator.ofFloat(swipeButton.x, 0f)
        positionAnimator.interpolator = AccelerateDecelerateInterpolator()
        positionAnimator.addUpdateListener {
            val x = positionAnimator.animatedValue as Float
            swipeButton.x = x
            expandProgress(swipeButton.x + swipeButton.width / 2)
            changeAlphaOfTexts(0f)
            holdSlidingButton = swipeButton.right.toFloat()
            isFirstCheck = true
            isStartSwiping = true
            touchedEnd = false
            isTouchEndEdge = true

        }


        positionAnimator.duration = defaultDuration.toLong()

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(positionAnimator)
        animatorSet.start()
    }


    private fun changeAlphaOfTexts(fingerPosition: Float) {

        val halfTheMainContainer = (mainContainer.width / 3)
        val beforePayTextAlpha = Math.max(
            0f,
            (((2 * halfTheMainContainer) - fingerPosition)) / (2 * halfTheMainContainer)
        )
        val afterPayTextAlpha = Math.min(1f, (fingerPosition * 4) / (mainContainer.width * 3))

        textBefore.alpha = beforePayTextAlpha
        if (beforePayTextAlpha <= 0.25f) {
            textAfter.alpha = (0.25f - beforePayTextAlpha) / 0.25f
            textAfter.alpha = (0.25f - beforePayTextAlpha) / 0.25f
        } else {
            textAfter.alpha = 0f
            textAfter.alpha = 0f

        }

    }


    fun resetView() {
        mainContainer.background = swipeButtonMainContainer
        textBefore.alpha = 1f
        textBeforeContainer.visibility = View.VISIBLE

        textAfter.alpha = 0f
        textAfterContainer.visibility = View.VISIBLE

        loadingProgressBarContainer.visibility = View.GONE

        progressBar.layoutParams = LayoutParams(0, mainContainerHeight)
        swipeButton.visibility = View.VISIBLE
        swipeButton.x = 0f
        isFirstCheck = true
        isStartSwiping = true
        touchedEnd = false
        isTouchEndEdge = true
        setOnTouchListener(getButtonTouchListener())
    }


    fun setTextAfterStyle(style: Typeface) {
        this.textAfter.typeface = style

    }

    fun setTextAfterSize(size: Float) {
        this.textAfter.textSize = size
    }

    fun setTextAfterColor(color: Int) {
        this.textAfter.setTextColor(color)
    }


    fun setTextBeforeStyle(style: Typeface) {
        this.textBefore.typeface = style

    }

    fun setTextBeforeSize(size: Float) {
        this.textBefore.textSize = size
    }

    fun setTextBeforeColor(color: Int) {
        this.textBefore.setTextColor(color)
    }


    fun setLinearProgressBarBackground(drawable: Drawable) {
        this.progressBar.background = drawable
    }

    fun setLinearProgressBarBackgroundColor(color: Int) {
        this.progressBar.setBackgroundColor(color)
    }


    fun setSlidingButtonBackground(background: Drawable) {
        this.swipeButton.background = background
    }

    fun setSlidingButtonBackgroundColor(color: Int) {
        this.swipeButton.setBackgroundColor(color)
    }

    fun setSlidingButtonImageDrawable(imageDrawable: Drawable) {
        this.swipeButton.setImageDrawable(imageDrawable)
    }


    fun setMainContainerBackground(background: Drawable) {
        this.mainContainer.background = background
    }

    fun setMainContainerBackgroundColor(color: Int) {
        this.mainContainer.setBackgroundColor(color)
    }

    fun setMainContainerTransitionDrawable(transitionDrawable: TransitionDrawable) {
        this.mainContainerTransitionDrawable = transitionDrawable
    }


    fun setSwipeButtonListener(listener: SwipeButtonListener) {
        this.callBack = listener
    }



    interface SwipeButtonListener {
        fun onCompletelySlidingButton(view: SwipeButton)
        fun onStartSwiping(view:SwipeButton)
        fun onTouchEndEdge(view:SwipeButton)
    }


    internal inner class GettingWidthAndHeightOfTheView(val view: View) :
        ViewTreeObserver.OnGlobalLayoutListener {

        override fun onGlobalLayout() {

            val width = view.width
            val height = view.height

            mainContainerWidth = width
            mainContainerHeight = mainContainerWidth / 7
            mainContainer.layoutParams = LayoutParams(mainContainerWidth, mainContainerHeight)
            swipeButton.layoutParams = LayoutParams(mainContainerHeight, mainContainerHeight)


        }
    }
}


