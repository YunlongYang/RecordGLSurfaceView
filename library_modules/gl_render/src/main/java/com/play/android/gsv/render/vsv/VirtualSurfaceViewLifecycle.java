package com.play.android.gsv.render.vsv;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.play.android.gsv.virtual.BaseVirtualGLSurfaceView;

public class VirtualSurfaceViewLifecycle {

    private BaseVirtualGLSurfaceView baseVirtualGLSurfaceView;

    private int width;
    private int height;

    public VirtualSurfaceViewLifecycle(BaseVirtualGLSurfaceView baseVirtualGLSurfaceView) {
        this.baseVirtualGLSurfaceView = baseVirtualGLSurfaceView;
    }

    public void setDisplaySize(int width,int height){
        this.width = width;
        this.height = height;
    }

    public void onActivityCreate(){
        baseVirtualGLSurfaceView.surfaceCreated(surfaceHolder);
    }

    public void onActivityResume(){
        baseVirtualGLSurfaceView.onResume();
        baseVirtualGLSurfaceView.surfaceChanged(surfaceHolder,0,width,height);
    }

    public void onActivityPause(){
        baseVirtualGLSurfaceView.onPause();
    }

    public void onActivityDestroy(){
        baseVirtualGLSurfaceView.surfaceDestroyed(surfaceHolder);
    }

    private final SurfaceHolder surfaceHolder = new SurfaceHolder() {
        @Override
        public void addCallback(Callback callback) {

        }

        @Override
        public void removeCallback(Callback callback) {

        }

        @Override
        public boolean isCreating() {
            return false;
        }

        @Override
        public void setType(int type) {

        }

        @Override
        public void setFixedSize(int width, int height) {

        }

        @Override
        public void setSizeFromLayout() {

        }

        @Override
        public void setFormat(int format) {

        }

        @Override
        public void setKeepScreenOn(boolean screenOn) {

        }

        @Override
        public Canvas lockCanvas() {
            return null;
        }

        @Override
        public Canvas lockCanvas(Rect dirty) {
            return null;
        }

        @Override
        public void unlockCanvasAndPost(Canvas canvas) {

        }

        @Override
        public Rect getSurfaceFrame() {
            return null;
        }

        @Override
        public Surface getSurface() {
            return null;
        }
    };

}
