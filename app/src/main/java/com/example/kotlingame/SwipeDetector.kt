package com.example.kotlingame

import android.view.MotionEvent
import kotlin.math.atan2
import kotlin.math.sqrt

abstract class SwipeDetector (minToachLen : Int){
    private var startX = 0f
    private var startY = 0f
    private var minToachLen = minToachLen * 5

    abstract fun onSwipeDetected(direction: Direction) : Unit

    private fun distance(x: Number, y : Number) : Double {
        return sqrt(x.toDouble()*x.toDouble() + y.toDouble() * y.toDouble());
    }
    private fun calcAngle(x: Number, y : Number) : Double {
        return ((atan2(y.toDouble(),x.toDouble()) + Math.PI) * 180 / Math.PI + 180) % 360
    }
    fun onTouchEvent(event: MotionEvent?) : Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN ->  {
                startX = event.x
                startY = event.y
                //println("$startX + $startY\n")
            }
            MotionEvent.ACTION_MOVE -> {}
            MotionEvent.ACTION_UP ->{
                val dx = event.x - startX
                val dy = event.y - startY
                if(distance(dx,dy) > minToachLen) {
                    onSwipeDetected(Direction[calcAngle(dx,dy).toInt()])
                }
                startX = 0f
                startY = 0f
            }
            else -> {
                startX = 0f
                startY = 0f
            }
        }
        return false
    }
    public final enum class Direction{
        UNEXPECTED,
        UP,
        RIGHT,
        DOWN,
        LEFT;
        companion object {
            operator fun get(angle: Int): Direction = with(angle) {
                if (this in 45..134) Direction.DOWN
                else if (this in 135..224) Direction.LEFT
                else if (this in 225..314) Direction.UP
                else if (this in 0..44 || this in 315..360) Direction.RIGHT
                else Direction.UNEXPECTED
            }
        }
    }


}
