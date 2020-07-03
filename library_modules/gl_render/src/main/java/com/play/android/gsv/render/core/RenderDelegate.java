package com.play.android.gsv.render.core;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class RenderDelegate implements GLSurfaceView.Renderer {
    int width;
    int height;
    int frameCount = 0;
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glViewport(0, 0, width, height);
        GLES20.glClearColor(frameCount%250, 0, (frameCount*7)%250, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);

        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
        frameCount ++;
        if(frameCount>=1000){
            frameCount = 0;
        }
    }
}
