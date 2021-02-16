package com.example.base.PolyVanko

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.base.R
import java.lang.Math.pow
import kotlin.math.*


class MCustomView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    enum class CurValue{
        Save,
        NoBrizg,
        Kapaet,
        Brizzhet,
        Hlishet,
        Topit
    }
    private val valToTag = mapOf(
            Pair(0,CurValue.Kapaet),
            Pair(1,CurValue.Brizzhet),
            Pair(2,CurValue.Hlishet),
            Pair(3,CurValue.Topit))
    private val minimal = 0
    private val maximal = 3
    private var currentVal = minimal//in 0..3
    private var text: String = valToTag[currentVal]?.name ?: "Error"

    private var vkl: Boolean = true
    private lateinit var atributi : HashMap<String, Int?>

    private val minSliderRad = resources.getDimension(R.dimen.slider_round_min_radius)
    private var sliderRad = 26f
    private val sliderMax = 55f

    private var colorSl = Color.parseColor("#00747a")
    private var colorCr = Color.parseColor("#00107a")

    private var circleRad: Float? = null
    private val circleCenter = Point()
    private val sliderCenter = Point()

    private val frame = RectF()
    private val brush = Paint()
    private var isTouched: Boolean= false

    init {
        initPaint()
        val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.MCustomView)
        typedArray?.let { atributi = hashMapOf()}
        val res = typedArray?.getInt(R.styleable.MCustomView_current_value, -10)
        atributi["current_value"] = if (res != -10 && res in minimal..maximal) res else null
        typedArray?.recycle()
    }
    fun initAttrs() = setCurrentValue(
            valToTag[
                atributi["current_value"]
                        ?:let {  Log.d("Argument", "IS NULL"); 1}
            ]!!
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = resources.getDimensionPixelSize(R.dimen.slider_default_size)
        val desiredHeight = resources.getDimensionPixelSize(R.dimen.slider_default_size)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int = if (widthMode == MeasureSpec.EXACTLY) {
            widthSize
        } else if (widthMode == MeasureSpec.AT_MOST) {
            desiredWidth.coerceAtMost(widthSize)
        } else {
            desiredWidth
        }

        val height: Int = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else if (heightMode == MeasureSpec.AT_MOST) {
            desiredHeight.coerceAtMost(heightSize)
        } else {
            desiredHeight
        }

        setMeasuredDimension(width, height)
    }
    private fun initPaint() {
        brush.style = Paint.Style.FILL
        brush.strokeWidth = 6F
        brush.flags = Paint.ANTI_ALIAS_FLAG
        brush.textAlign = Paint.Align.CENTER
    }
    override fun onDraw(canvas: Canvas?) {
        canvas?.let{
            super.onDraw(canvas)
            initSize(canvas)
            drawCircle(canvas)
            drawSlider(canvas)
        }
    }
    private fun initSize(canvas: Canvas) {
        circleCenter.x = (canvas.width - sliderMax).toInt()
        circleCenter.y = (canvas.height - sliderMax).toInt()
        if (circleRad == null) circleRad = canvas.width - 2 * sliderMax
        frame.set(
                circleCenter.x - circleRad!!,
                circleCenter.y - circleRad!!,
                circleCenter.x + circleRad!!,
                circleCenter.y + circleRad!!
        )

    }
    private fun drawCircle(canvas: Canvas) {
        brush.style = Paint.Style.STROKE
        brush.color = colorCr
        val piaces = maximal - minimal
        val countOfArcs = (Const.End_Angle - Const.Start_Angle) / piaces
        for (i in minimal until maximal) {
            canvas.drawArc(
                    frame,
                    Const.Start_Angle + i * countOfArcs,
                    countOfArcs - 1f,
                    false, brush
            )
        }

        brush.style = Paint.Style.FILL
        brush.color = colorCr//Color of mini circles
        canvas.drawCircle(
                cos(Math.toRadians(Const.End_Angle.toDouble())).toFloat()
                        * circleRad!! + circleCenter.x,
                sin(Math.toRadians(Const.End_Angle.toDouble())).toFloat()
                        * circleRad!! + circleCenter.y,
                minSliderRad + 20, brush
        )
        canvas.drawCircle(
                cos(Math.toRadians(Const.Start_Angle.toDouble())).toFloat()
                        * circleRad!! + circleCenter.x,
                sin(Math.toRadians(Const.Start_Angle.toDouble())).toFloat()
                        * circleRad!! + circleCenter.y,
                minSliderRad, brush
        )


    }
    private fun drawSlider(canvas: Canvas) {
        val angle: Int = ((currentVal - minimal) * (Const.End_Angle - Const.Start_Angle) /
                (maximal - minimal) + Const.Start_Angle).toInt()
        sliderCenter.x = (cos(Math.toRadians(angle.toDouble())) * circleRad!! + circleCenter.x).toInt()
        sliderCenter.y = (sin(Math.toRadians(angle.toDouble())) * circleRad!! + circleCenter.y).toInt()

        brush.color = colorSl
        brush.textSize = resources.getDimensionPixelSize(R.dimen.slider_value_size).toFloat()

        canvas.drawText(
                text,
                circleCenter.x - circleRad!! * 3 / 5, circleCenter.y.toFloat(),
                brush
        )
        if (vkl) canvas.drawCircle(
                sliderCenter.x.toFloat(), sliderCenter.y.toFloat(), sliderRad, brush
        )
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(!vkl) return false
        event?.let{
            val touch = Point(
                    round(event.x).toInt(),
                    round(event.y).toInt()
            )
            when(event.action){
                MotionEvent.ACTION_UP -> return false
                MotionEvent.ACTION_DOWN -> {
                    isTouched = Util.isNearTouch(
                            touch.x, touch.y, 20, sliderCenter, sliderRad.toInt()
                    )
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isTouched) {
//                        Log.d("Touch", "Down//Move-(${event.x};${event.y})")
                        var angle =
                                Util.pointToAngle(touch.x, touch.y, circleCenter) -
                                        Const.Start_Angle;//error may be here
//                        Log.d("Touch",
//                                """touch - ${touch.x};${touch.y}^
//                                |..Center - ${circleCenter.x}, ${circleCenter.y},
//                                |..Angle - ${angle}""".trimMargin()
//                        )
                        if (angle.toInt() !in -20..110) return false//Alwawys angel in 0(+-20)..90(+-20)
                        if (angle < 0) angle = 0F
                        if (angle > 90) angle = 90f
                        val value =
                                (minimal + angle * (maximal - minimal) /
                                        (Const.End_Angle - Const.Start_Angle)).roundToInt()//in 0..3
                        if (value != currentVal
                        ) {
                            setCurrentValue(valToTag[value]!!)
                        }
                    }
                }
            }
        }
        return true
    }

    fun Vkl() = setCurrentValue(CurValue.Save)
    fun Vikl() = setCurrentValue(CurValue.NoBrizg)


    fun setCurrentValue(curVal: CurValue){
        if (curVal != CurValue.NoBrizg){
            vkl = true
            colorSl = Color.parseColor("#00747a")
            colorCr = Color.parseColor("#00107a")
            text = when (curVal) {
                CurValue.Save -> {

                    valToTag[currentVal]?.name?:"Error"
                }
                CurValue.Kapaet -> {
                    sliderRad = 26f
                    currentVal = 0
                    curVal.name
                }
                CurValue.Brizzhet -> {
                    sliderRad = 34f
                    currentVal = 1
                    curVal.name
                }
                CurValue.Hlishet -> {
                    sliderRad = 42f
                    currentVal = 2
                    curVal.name
                }
                CurValue.Topit -> {
                    sliderRad = sliderMax
                    currentVal = 3
                    curVal.name
                }
                else -> "Error"
            }
        } else {
            colorSl = Color.parseColor("#0f0f0f")
            colorCr = Color.parseColor("#616161")
            vkl = false
            text = "Ne brizgaet"
        }
        invalidate()
    }

    fun getVal(): CurValue = valToTag[currentVal]!!
}
object Const{
    const val Start_Angle = 180f
    const val End_Angle = 270f
}
object Util{
    fun isNearTouch(
            touchX: Int, touchY: Int, Epsilon: Int, sliderCenter: Point, sliderRad: Int
    ) = sliderRad + Epsilon >= (sqrt(
            ((touchX - sliderCenter.x).toDouble().pow(2.0) +
                    (touchY - sliderCenter.y).toDouble().pow(2.0))
    ))
    fun pointToAngle(x: Int, y: Int, center: Point): Int {
        if (x >= center.x && y < center.y) {
            val opp = (x - center.x).toDouble()
            val adj = (center.y - y).toDouble()
            return 270 + Math.toDegrees(atan(opp / adj)).toInt()
        } else if (x > center.x) {
            val opp = (y - center.y).toDouble()
            val adj = (x - center.x).toDouble()
            return Math.toDegrees(atan(opp / adj)).toInt()
        } else if (y > center.y) {
            val opp = (center.x - x).toDouble()
            val adj = (y - center.y).toDouble()
            return 90 + Math.toDegrees(atan(opp / adj)).toInt()
        } else if (x < center.x) {
            val opp = (center.y - y).toDouble()
            val adj = (center.x - x).toDouble()
//            Log.d("Pure Angel", "${Math.toDegrees(atan(opp / adj)).toInt()}")
            return 180 + Math.toDegrees(atan(opp / adj)).toInt()
        }
        throw IllegalArgumentException()
    }
}
