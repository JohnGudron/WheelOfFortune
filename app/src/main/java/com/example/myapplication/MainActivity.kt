package com.example.myapplication

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val wheelView = findViewById<WheelView>(R.id.wheel_view)
        val urisArr = arrayOf(
            "https://cdn-icons-png.freepik.com/256/3774/3774278.png?ga=GA1.1.458017803.1706870122&semt=ais",
            "https://cdn-icons-png.freepik.com/256/1139/1139982.png?ga=GA1.1.458017803.1706870122&semt=ais",
            "https://cdn-icons-png.freepik.com/256/7847/7847712.png?ga=GA1.1.458017803.1706870122&semt=ais",
            "https://cdn-icons-png.freepik.com/256/32/32339.png?ga=GA1.1.458017803.1706870122&semt=ais",
            "https://cdn-icons-png.freepik.com/256/4603/4603134.png?ga=GA1.1.458017803.1706870122&semt=ais",
            "https://cdn-icons-png.freepik.com/256/10034/10034463.png?ga=GA1.1.458017803.1706870122&semt=ais",
            "https://cdn-icons-png.freepik.com/256/14242/14242272.png?ga=GA1.1.458017803.1706870122&semt=ais"
        )

        for (i in 0..6) {
            Picasso.get().load(urisArr[i]).into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom?) {
                    wheelView.setImage(i, bitmap)
                }

                override fun onBitmapFailed(
                    e: Exception?,
                    errorDrawable: android.graphics.drawable.Drawable?
                ) {
                    println("Image loading failed")
                }

                override fun onPrepareLoad(placeHolderDrawable: android.graphics.drawable.Drawable?) {
                    println("onPrepareLoad")
                }
            })
        }
    }
}