package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class WheelView(context: Context, attrs: AttributeSet? = null): View(context, attrs) {

    private var images = Array<Bitmap?>(7) { ResourcesCompat.getDrawable(resources, R.drawable.ic_launcher_background, null)?.toBitmap()}
    private var imageToDraw = 0

    private var started = false
    private var isSpinning = false
    private val startBtnText = "ROLL"
    private val resetBtnText = "RESET"
    private var textToDraw = ""

    private var rotationAngle = 0f
    private var startAngle = 0f
    private val spinHandler = Handler(Looper.getMainLooper())

    private val leftEdgeFactor = 0.066f
    private val rightEdgeFactor = 0.934f
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var sliderScale = 0.5f
    private var circleScale = 0.5f

    private val imageTopBottom = Pair(0.03f,0.3f)
    private val imageRect = RectF()
    private val circleRect = RectF()
    private val pointerPath = Path()
    private val textTopBottom = Pair(0.33f,0.39f)
    private val sliderTopBottom = Pair(0.85f,0.895f)
    private val btnTopBottom = Pair(0.91f,0.97f)
    private val wheelTopBottom = Pair(0.42f,0.82f)

    private val buttonPaint = Paint().apply {
        this.color = Color.MAGENTA
        this.isAntiAlias = true
        this.style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        this.isAntiAlias = true
        this.color = Color.BLACK
    }

    private val framePaint = Paint().apply {
        this.isAntiAlias = true
        this.style = Paint.Style.STROKE
        this.strokeWidth = 4f
        this.color = Color.BLACK
    }

    private val sliderPaint = Paint().apply {
        this.isAntiAlias = true
        this.style = Paint.Style.FILL
        this.color = Color.DKGRAY
    }

    private val pointerPaint = Paint().apply {
        this.isAntiAlias = true
        this.style = Paint.Style.FILL
        this.color = Color.BLACK
    }

    private val circlePaint = Paint().apply {
        this.style = Paint.Style.FILL
        this.isAntiAlias = true
    }

    private val sectorColors = intArrayOf(
        Color.RED, Color.rgb(255,165,0),Color.YELLOW, Color.GREEN,
        Color.rgb(173,216,230), Color.BLUE, Color.rgb(128,0,128)
    )

    private val colorMap = listOf(
        Pair(resources.getString(R.string.car), "https://dummyimage.com/640x360/fff/aaa"),
        Pair(resources.getString(R.string.prize), "https://dummyimage.com/640x360/fff/aaa"),
        Pair(resources.getString(R.string.bankrupt), "https://dummyimage.com/640x360/fff/aaa"),
        Pair(resources.getString(R.string.plus), "https://dummyimage.com/640x360/fff/aaa"),
        Pair(resources.getString(R.string.chance), "https://dummyimage.com/640x360/fff/aaa"),
        Pair(resources.getString(R.string.hundred), "https://dummyimage.com/640x360/fff/aaa"),
        Pair(resources.getString(R.string.doubling), "https://dummyimage.com/640x360/fff/aaa"),
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // drawing wheel
        circleRect.set(
            leftEdgeFactor * width,
            wheelTopBottom.first * height,
            rightEdgeFactor * width,
            wheelTopBottom.second * height
        )
        val radius = (circleRect.bottom - circleRect.top)/2 * circleScale
        circleRect.inset(radius,radius)
        for (i in sectorColors.indices) {
            circlePaint.color = sectorColors[i]
            canvas.drawArc(
                circleRect,
                rotationAngle + i * (360f / sectorColors.size),
                360f / sectorColors.size,
                true,
                circlePaint
            )
        }

        // drawing pointer
        pointerPath.reset()
        pointerPath.moveTo(width/2f, (circleRect.bottom + circleRect.top)/2) // Top point
        pointerPath.lineTo(width/2 - 0.003f*width, circleRect.bottom + 0.006f*width) // Bottom-left point
        pointerPath.lineTo(width/2 + 0.003f*width, circleRect.bottom + 0.006f*width) // Bottom-right point
        pointerPath.close()
        canvas.drawPath(pointerPath,pointerPaint)

        // draw image and image frame
        canvas.drawRoundRect(
            leftEdgeFactor * width,
            imageTopBottom.first * height,
            rightEdgeFactor * width,
            imageTopBottom.second * height,
            10f,
            10f,
            framePaint
        )
        if (started) {
            imageRect.set(
                leftEdgeFactor * width,
                imageTopBottom.first * height,
                rightEdgeFactor * width,
                imageTopBottom.second * height
            )
            canvas.drawBitmap(images[imageToDraw]!!, null, imageRect, null)
        }

        // draw text and text frame
        textPaint.textSize = 0.09f * width
        canvas.drawRoundRect(
            leftEdgeFactor * width,
            textTopBottom.first * height,
            rightEdgeFactor * width,
            textTopBottom.second * height,
            10f,
            10f,
            framePaint
        )
        canvas.drawText(
            textToDraw,
            (width - textPaint.measureText(textToDraw)) / 2,
            (textTopBottom.second - 0.01f) * height,
            textPaint
        )

        // draw slider and its frame
        canvas.drawRoundRect(
            leftEdgeFactor * width,
            sliderTopBottom.first * height,
            rightEdgeFactor * width,
            sliderTopBottom.second * height,
            10f,
            10f,
            framePaint
        )

        canvas.drawRoundRect(
            leftEdgeFactor * width,
            sliderTopBottom.first * height,
            rightEdgeFactor * width * sliderScale,
            sliderTopBottom.second * height,
            10f,
            10f,
            sliderPaint
        )

        // draw buttons text and frame
        canvas.drawRoundRect(
            leftEdgeFactor * width,
            btnTopBottom.first * height,
            width / 2 - width * leftEdgeFactor / 2,
            btnTopBottom.second * height,
            10f,
            10f,
            buttonPaint
        )
        canvas.drawRoundRect(
            width / 2 + width * leftEdgeFactor / 2,
            btnTopBottom.first * height,
            rightEdgeFactor * width,
            btnTopBottom.second * height,
            10f,
            10f,
            buttonPaint
        )

        canvas.drawText(
            resetBtnText,
            (width / 2 - textPaint.measureText(resetBtnText)) / 2,
            (btnTopBottom.second - 0.01f) * height,
            textPaint
        )
        canvas.drawText(
            startBtnText,
            width / 2 + (width / 2 - textPaint.measureText(resetBtnText)) / 2,
            (btnTopBottom.second - 0.01f) * height,
            textPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val touchX = event.x
        val touchY = event.y

        // calculating circle center
        val centerX = width / 2f
        val touchRadius = centerX - circleRect.left
        val centerY = circleRect.bottom - touchRadius

        val distance = sqrt(
            (touchX - centerX).toDouble().pow(2.0) + (touchY - centerY).toDouble().pow(2.0)
        ).toFloat()

        when (event.action) {
            // touch validation
            MotionEvent.ACTION_DOWN -> {

                touchStartX = touchX
                touchStartY = touchY

                if (touchY in btnTopBottom.first * height..btnTopBottom.second * height &&
                    touchX in (leftEdgeFactor * width / 2 + width / 2)..rightEdgeFactor * width ||
                    distance <= touchRadius
                ) {
                    started = true
                    if (!isSpinning) {
                        startSpinning()
                    }
                } else if (touchY in btnTopBottom.first * height..btnTopBottom.second * height &&
                    touchX in (leftEdgeFactor * width)..width / 2 - leftEdgeFactor * width / 2
                ) {
                    reset()
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchStartX in (leftEdgeFactor * width)..rightEdgeFactor * width &&
                    touchStartY in sliderTopBottom.first * height..sliderTopBottom.second * height) {
                    sliderScale = (touchX/width).coerceIn(0.075f,1f)
                    circleScale = 1f - (touchX/width).coerceIn(0.05f,1f)
                    invalidate()
                }

            }
        }
        return super.onTouchEvent(event)
    }

    private fun startSpinning() {
        isSpinning = true
        spinHandler.postDelayed({
            // Randomly stop the wheel after a delay
            stopSpinning()
        }, 3000) // Adjust the delay as needed
        spinWheel()
    }

    private fun stopSpinning() {
        isSpinning = false
        spinHandler.removeCallbacksAndMessages(null)
    }

    private fun spinWheel() {
        // Rotate the wheel by a random angle
        // В течение нескольких секунд (устанавливаем в startSpinning) ставит колесо в новое
        // положение (задается строкой ниже рандомно). Новое положение выбирается каждые 500 милисекунд (можно изменить ниже)
        rotationAngle = Random.nextFloat() * 360
        startAngle = (359.9f - (rotationAngle-90f))%360f    /*(abs(startAngle +360 - rotationAngle)).toInt() %360*/
        val ind = if ((startAngle / 51.42f).toInt() > 6f) 6 else (startAngle / 51.42f).toInt()
        changeColor(ind)
        imageToDraw = ind
        textToDraw = colorMap[ind].first

        invalidate()

        // Continue spinning until stopped
        if (isSpinning) {
            spinHandler.postDelayed({
                spinWheel()
            }, 100) // Насколько часто будем менять положение колеса
        }
    }

    private fun changeColor(ind: Int) {
        val color = if (ind == -1) Color.DKGRAY else sectorColors[ind]
        framePaint.color = color
        buttonPaint.color = color
        sliderPaint.color = color
    }

    private fun reset() {
        rotationAngle = 0f
        started = false
        textToDraw = ""
        changeColor(-1)
        sliderScale = 0.5f
        circleScale = 0.5f
        invalidate()
    }

    fun setImage(ind:Int, bitmap:Bitmap) {
        images[ind] = bitmap
    }

}