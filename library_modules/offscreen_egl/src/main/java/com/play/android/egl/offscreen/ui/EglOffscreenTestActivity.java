package com.play.android.egl.offscreen.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.play.android.egl.offscreen.R;
import com.play.android.egl.offscreen.egl.test.EglEnvTest;
import com.play.android.egl.offscreen.egl.test.TestRender;

public class EglOffscreenTestActivity extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egl_offscreen_test);
//        glSurfaceView = findViewById(R.id.content_gl_sv);

//        glSurfaceView.setEGLContextClientVersion(2);
//        glSurfaceView.setRenderer(new TestRender());

        EglEnvTest.main(getApplicationContext());
    }
}
