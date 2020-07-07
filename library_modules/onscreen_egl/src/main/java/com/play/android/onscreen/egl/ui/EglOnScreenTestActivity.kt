package com.play.android.onscreen.egl.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.play.android.onscreen.egl.R
import com.play.android.onscreen.egl.egl.EglRecordRender
import kotlinx.android.synthetic.main.activity_egl_on_screen_test.*

class EglOnScreenTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_egl_on_screen_test)
        content_gl_sv.setEGLContextClientVersion(2)
        content_gl_sv.setRenderer(EglRecordRender(this,TestRender()))
    }
}
