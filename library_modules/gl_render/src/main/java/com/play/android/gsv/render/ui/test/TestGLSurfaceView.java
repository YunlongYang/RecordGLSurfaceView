package com.play.android.gsv.render.ui.test;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class TestGLSurfaceView extends GLSurfaceView {
    public TestGLSurfaceView(Context context) {
        super(context);
        init();
    }

    public TestGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init(){
        setEGLContextClientVersion(2);
    }
}
