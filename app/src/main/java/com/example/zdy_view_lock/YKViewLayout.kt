package com.example.zdy_view_lock

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.drawToBitmap
import kotlin.math.sqrt

class YKViewLayout(context: Context,attributeSet: AttributeSet?,def:Int) :View(context,attributeSet,def) {
//    代码创建
    constructor(context: Context ) : this(context,null,0)
//    xml创建
    constructor(context: Context,attributeSet: AttributeSet?): this(context,attributeSet,0)

    private var mContainerWidth:Int = 0
    private var mContainerHeight = 0
    private var wcircleRound = 52
    private var NcircleRound = 8
    private var ykcanvas:Canvas? = Canvas()

//    记录点
    private var dotsArray = ArrayList<Point>()
    private var selectedpotin = ArrayList<Point>()
    var firstPoint = Point()
    var secondPoitn = Point()
    var cPoint = Point()

//    密码
private var password = StringBuffer()

//    线de画笔
    private val linePaint by lazy {
        Paint().also {
            it.style = Paint.Style.STROKE
            it.strokeWidth = 5.5f
            it.color = Color.parseColor("#FF5151")
        }
    }
//    画头像
    private val TPaint by lazy {
        Paint().also {
            var a = BitmapFactory.decodeResource(resources,R.drawable.a)
            it.shader = BitmapShader(a,Shader.TileMode.REPEAT,Shader.TileMode.REPEAT)
        }
}

//    记录当前手指的位置
    private var currentPoint = Point()
    private var lastPoint = Point()
    //按下的时候是否是按在一个点上面
    private var isTouchInPoint: Boolean = false

//    正常时的画笔 外圈
    private val wpaint by lazy {
        Paint().also {
            it.style = Paint.Style.STROKE
            it.strokeWidth = 5.5f
            it.color = Color.parseColor("#d0d0d0")
        }
    }
    private val wpaint2 by lazy {
        Paint().also {
            it.style = Paint.Style.STROKE
            it.strokeWidth = 5.5f
            it.color = Color.parseColor("#FF5151")
        }
    }
//    内圈
    private val Npaint by lazy {
    Paint().also {
        it.style = Paint.Style.STROKE
        it.strokeWidth = 5.5f
        it.color = Color.parseColor("#8E8E8E")
    }
}
    private val Npaint2 by lazy {
        Paint().also {
            it.style = Paint.Style.FILL
            it.strokeWidth = 5.5f
            it.color = Color.parseColor("#CE0000")
        }
    }



    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        ykcanvas = canvas

//        画九个点
        var padding = countPadding(mContainerWidth)
        drawNineNormalDot(canvas,padding,wcircleRound,NcircleRound,wpaint,Npaint)
        Log.v("yk","onDraw被调用了")
//    改变被选中的点的颜色
        changeSelectedDotColor(selectedpotin,canvas,wcircleRound,NcircleRound,wpaint2,Npaint2)

//        画线(都在圈内）
        drawlines(canvas)

//        画一个圆头像
        canvas?.drawCircle(measuredWidth/2.toFloat(),150f,120f,TPaint)

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when(event?.action){
            MotionEvent.ACTION_DOWN ->{
                for (point in dotsArray){
                    if (judgeDistance(event.x,event.y,point,wcircleRound)){
                        firstPoint = point
                        selectedpotin.add(firstPoint)
                    }
                }
                Log.v("yk","dotsArray: $dotsArray")
            }
            MotionEvent.ACTION_MOVE ->{
                currentPoint = Point(event.x.toInt(),event.y.toInt())

                for (point in dotsArray){
                    if (judgeDistance(event.x,event.y,point,wcircleRound)){
                        if (firstPoint == cPoint){
                            firstPoint = point
                            selectedpotin.add(firstPoint)
                        }else{
                            if (point != firstPoint){
                                secondPoitn = point
                                selectedpotin.add(secondPoitn)
                                firstPoint = secondPoitn
                            }
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP ->{
                firstPoint = secondPoitn
//                firstLinePoint = null
                Log.v("yk","selectedpotin:$selectedpotin")
//                selectedpotin在被清空之前将其点的颜色改了,并显示密码
                for (item in selectedpotin){
                    for ((i,a) in dotsArray.withIndex()){
                        if (a == item){
                            (i+1).toString().also {
                                password?.append(it)
                                Log.v("yk","密码: $it")
                            }
                        }
                    }
                }
                Log.v("yk","密码: $password")
                Toast.makeText(context,"密码是：$password",Toast.LENGTH_SHORT).show()
                selectedpotin.clear()
                password.delete(0,password.length)
            }
        }
        invalidate()
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mContainerHeight = measuredHeight
        mContainerWidth = measuredWidth
    }

//    计算点之间的间距
    fun countPadding(mWidth:Int):Int{
        return (mWidth - (wcircleRound * 6) ) / 4
    }
//    画九个正常点
    fun drawNineNormalDot(convas:Canvas?,padding:Int,wRound:Int,NRound:Int,wpaint:Paint,Npaint:Paint){

    var i = 0
    var k = 0
    while (i < 3){
        while (k < 3){
            var a = Point(0,0)
            a.x = (padding*(k+1)+wRound*(k*2+1))
            a.y = (450+(padding*i)+wRound*(i*2))
            convas?.drawCircle(a.x.toFloat(),a.y.toFloat(),wRound.toFloat(),wpaint)
            k++
            if (dotsArray.size < 9){
                dotsArray.add(a)
            }
//            Log.v("yk","aaaaaaaaa $dotsArray")
        }
        k = 0
        i ++
    }

    i = 0
    k = 0
    while (i < 3){
        while (k < 3){
            convas?.drawCircle((padding*(k+1)+wRound*(k*2+1)).toFloat(),(450+(padding*i)+wRound*(i*2)).toFloat(),NRound.toFloat(),Npaint)
            k++
        }
        k = 0
        i ++
    }
}

//    画线
    fun drawlines(canvas: Canvas?){
    if (selectedpotin.size > 0){
        lastPoint = selectedpotin[0]
        for (item in selectedpotin){
            drawLine(lastPoint,item,canvas,linePaint)
            lastPoint = item
        }
    }
//        点不在圆内
    for (item in selectedpotin){
        if (currentPoint != item){
            drawLine(selectedpotin[selectedpotin.size - 1],currentPoint,canvas,linePaint)
        }
    }
 }

//    判断安下的点距离最近的圆的距离是否大于外援半径
    fun judgeDistance(x:Float,y:Float,circleDot:Point,R: Int): Boolean{
    return sqrt(((x - circleDot.x)*(x - circleDot.x) + (y - circleDot.y)*(y - circleDot.y)).toDouble()) < R
 }

//    改变被选中的点的颜色
    fun changeSelectedDotColor(array: ArrayList<Point>,canvas: Canvas?,wRound: Int,NRound: Int,wpaint: Paint,Npaint: Paint){
     for (itme in array){
        canvas?.drawCircle(itme.x.toFloat(),itme.y.toFloat(),wRound.toFloat(),wpaint)
        canvas?.drawCircle(itme.x.toFloat(),itme.y.toFloat(),NRound.toFloat(),Npaint)
//        Log.v("yk","被调用了")
     }
  }

    fun drawLine(first:Point, second: Point, canvas: Canvas?, linePaint:Paint){
        canvas?.drawLine(first.x.toFloat(),first.y.toFloat(),second.x.toFloat(),second.y.toFloat(),linePaint)
    }

}







