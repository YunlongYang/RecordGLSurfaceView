package com.play.android.gsv.render.ui.test;

import android.opengl.GLSurfaceView;

import com.play.android.gsv.virtual.render.RenderWatcher;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SaveVideoRenderWatcher extends RenderWatcher {
    public SaveVideoRenderWatcher(GLSurfaceView.Renderer proxyRender) {
        super(proxyRender);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
    }

    @Override
    protected void beforeOnDrawFrame() {

    }

    @Override
    protected void afterOnDrawFrame() {

    }
}
