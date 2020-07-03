package com.play.android.gsv.virtual.render;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public abstract class RenderWatcher implements GLSurfaceView.Renderer {

    private GLSurfaceView.Renderer proxyRender;

    public RenderWatcher(GLSurfaceView.Renderer proxyRender) {
        this.proxyRender = proxyRender;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        proxyRender.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        proxyRender.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        beforeOnDrawFrame();
        proxyRender.onDrawFrame(gl);
        afterOnDrawFrame();
    }

    protected abstract void beforeOnDrawFrame();
    protected abstract void afterOnDrawFrame();
}
