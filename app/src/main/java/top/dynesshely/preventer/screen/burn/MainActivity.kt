package top.dynesshely.preventer.screen.burn

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import java.util.*

class MainActivity : ComponentActivity() {

    private var windowManager: WindowManager? = null
    private var floatingView: FloatingView? = null
    private var process = 0.0f
    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createStopButton())

        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
        } else {
            showPreventer()
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        windowManager?.removeView(floatingView)
        super.onDestroy()
    }

    private fun createStopButton(): Button {
        return Button(this).apply {
            text = "Stop"
            setOnClickListener {
                stopPreventer()
                showToast("Stopped")
            }
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
    }

    private fun showPreventer() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        floatingView = FloatingView(this)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.NO_GRAVITY

        windowManager?.addView(floatingView, params)

        startTimer()
    }

    private fun stopPreventer() {
        timer?.cancel()
        windowManager?.removeView(floatingView)
    }

    private fun startTimer() {
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                process += 0.005f
                if (process >= 1.0f) {
                    process = 0.0f
                }
                runOnUiThread {
                    floatingView?.invalidate()
                }
            }
        }, 0, 5)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    inner class FloatingView(context: Context) : View(context) {
        private val paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            val verticalBegin = screenHeight.toFloat() * process

            canvas.drawRect(0f, verticalBegin, screenWidth.toFloat(), verticalBegin + 5, paint)
        }
    }

    companion object {
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 123
    }
}
