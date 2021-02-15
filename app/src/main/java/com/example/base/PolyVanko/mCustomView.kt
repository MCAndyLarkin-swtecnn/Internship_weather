package com.example.base.PolyVanko

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.base.R
import java.lang.Math.pow
import kotlin.math.*


class mCustomView : View {
    var text: String = "Ne brizgaet"

    val minimal = 0//Value
    val maximal = 3
    var currentVal = minimal//in 0..4

    val minSliderRad = resources.getDimension(R.dimen.slider_round_min_radius)//or 20
    //    val maxSliderRad = 60f
    var sliderRad = 26f
    val sliderMax = 55f

    var circleRad: Float? = null
    val circleCenter = Point()//!!

    val sliderCenter = Point()

    val frame = RectF()
    val brush = Paint()

    var isTouched: Boolean= false
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        initPaint();
    }

    private fun initAttrs(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.mCustomView)
        currentVal = typedArray.getInt(R.styleable.mCustomView_current_value, 50)
        typedArray.recycle()
    }
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
            Math.min(desiredWidth, widthSize)
        } else {
            desiredWidth
        }

        val height: Int = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else if (heightMode == MeasureSpec.AT_MOST) {
            Math.min(desiredHeight, heightSize)
        } else {
            desiredHeight
        }

        setMeasuredDimension(width, height)
    }
    fun initPaint() {
        brush.color = Color.parseColor("#00107a")//Color of line
        brush.style = Paint.Style.FILL
        brush.strokeWidth = 6F
        brush.flags = Paint.ANTI_ALIAS_FLAG
        brush.textAlign = Paint.Align.CENTER
    }

    protected override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        initSize(canvas)
        drawCircle(canvas)
        drawSlider(canvas)
    }

    private fun initSize(canvas: Canvas?) {
        canvas?.let{
            circleCenter.x = canvas.width / 2
            circleCenter.y = canvas.height.div(2)
            if (circleRad == null) circleRad = canvas.width / 2 - sliderMax
            frame.set(
                    circleCenter.x - circleRad!!,
                    circleCenter.y - circleRad!!,
                    circleCenter.x + circleRad!!,
                    circleCenter.y + circleRad!!
            )
        }
        Log.d("circleRadius", "$circleRad")
    }

    private fun drawCircle(canvas: Canvas?){
        brush.style = Paint.Style.STROKE
        canvas?.let {
            it.drawArc(
                    frame,
                    Const.Start_Angle,
                    Const.End_Angle - Const.Start_Angle,
                    false, brush
            )
            brush.style = Paint.Style.FILL
            brush.color = Color.parseColor("#00107a")//Color of mini circles
            it.drawCircle(
                    cos(Math.toRadians(Const.End_Angle.toDouble())).toFloat()
                            * circleRad!! + circleCenter.x,
                    sin(Math.toRadians(Const.End_Angle.toDouble())).toFloat()
                            * circleRad!! + circleCenter.y,
                    minSliderRad+20, brush
            )
            it.drawCircle(
                    cos(Math.toRadians(Const.Start_Angle.toDouble())).toFloat()
                            * circleRad!! + circleCenter.x,
                    sin(Math.toRadians(Const.Start_Angle.toDouble())).toFloat()
                            * circleRad!! + circleCenter.y,
                    minSliderRad, brush
            )
        }

    }
    private fun drawSlider(canvas: Canvas?) {
        val angle: Int = ((currentVal - minimal) * (Const.End_Angle - Const.Start_Angle) /
                (maximal - minimal) + Const.Start_Angle).toInt()
        sliderCenter.x = (cos(Math.toRadians(angle.toDouble()))*circleRad!! + circleCenter.x).toInt()
        sliderCenter.y = (sin(Math.toRadians(angle.toDouble()))*circleRad!! + circleCenter.y).toInt()
        brush.color = Color.parseColor("#00747a") // Color of slider
        canvas?.let {
            it.drawCircle(
                    sliderCenter.x.toFloat(), sliderCenter.y.toFloat(), sliderRad, brush
            )
            brush.textSize = resources.getDimensionPixelSize(R.dimen.slider_value_size).toFloat()
            it.drawText(
                    text,
                    circleCenter.x.toFloat(), circleCenter.y + sliderRad * 3 / 2,
                    brush
            )
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let{
            val touch = Point(
                    round(event.getX()).toInt(),
                    round(event.getY()).toInt()
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
                                Util.pointToAngle(touch.x, touch.y, circleCenter) - Const.Start_Angle;//error may be here
                        Log.d("Touch",
                                """touch - ${touch.x};${touch.y}^
                                |..Center - ${circleCenter.x}, ${circleCenter.y},
                                |..Angle - ${angle}""".trimMargin()
                        )
                        if (angle.toInt() !in -20..110) return false//Alwawys angel in 0(+-20)..90(+-20)
                        if(angle < 0) angle = 0F
                        if(angle > 90) angle = 90f
                        val value =
                                (minimal + angle * (maximal - minimal) /
                                        (Const.End_Angle - Const.Start_Angle)).roundToInt()//in 0..3
                        if (value != currentVal
//                                &&
//                            abs(currentVal - value) < (maximal - minimal) / 10 + 1//??
                        ) {
                            setCurrentValue(value)
                        }
                    }
                }

            }
            it
        }
        return true
    }
    fun setCurrentValue(curVal: Int) {
        currentVal = curVal
        text = when(currentVal){
            0 -> {
                sliderRad = 26f
                "Ne brizgaet"
            }
            1 -> {
                sliderRad = 34f
                "Slabo brizgaet"
            }
            2 -> {
                sliderRad = 42f
                "Silno brizgaet"
            }
            3 -> {
                sliderRad = sliderMax
                "Topit"
            }
            else -> "Error"
        }
        invalidate()
    }
    fun setLine(text: String) {
        this.text = text
    }
    fun getVal(): Int = currentVal
}
object Const{
    const val Start_Angle = 180f
    const val End_Angle = 270f
}
object Util{
    fun isNearTouch(
            touchX: Int, touchY: Int, Epsilon: Int, sliderCenter: Point, sliderRad: Int
    ) = sliderRad + Epsilon >= (sqrt(
            (pow((touchX - sliderCenter.x).toDouble(), 2.0) +
                    pow((touchY - sliderCenter.y).toDouble(), 2.0))
    ))
    fun pointToAngle(x: Int, y: Int, center: Point): Int {
        if (x >= center.x && y < center.y) {
            val opp = (x - center.x).toDouble()
            val adj = (center.y - y).toDouble()
            Log.d("Dirty Angel", "1")
            return 270 + Math.toDegrees(atan(opp / adj)).toInt()
        } else if (x > center.x) {
            val opp = (y - center.y).toDouble()
            val adj = (x - center.x).toDouble()
            Log.d("Dirty Angel", "2")
            return Math.toDegrees(atan(opp / adj)).toInt()
        } else if (y > center.y) {
            val opp = (center.x - x).toDouble()
            val adj = (y - center.y).toDouble()
            Log.d("Dirty Angel", "3")
            return 90 + Math.toDegrees(atan(opp / adj)).toInt()
        } else if (x < center.x) {
            val opp = (center.y - y).toDouble()
            val adj = (center.x - x).toDouble()
            Log.d("Pure Angel", "${Math.toDegrees(atan(opp / adj)).toInt()}")
            return 180 + Math.toDegrees(atan(opp / adj)).toInt()
        }
        throw IllegalArgumentException()
    }
}

/*Problems:
    * Incorrect angle
    * Static pozition of slider
 */