package com.play.android.gsv.virtual.egl.context;

import android.util.Log;

import com.play.android.gsv.virtual.BaseVirtualGLSurfaceView;
import com.play.android.gsv.virtual.egl.VirtualEglHelper;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public class DefaultContextFactory implements EGLContextFactory {
    private BaseVirtualGLSurfaceView baseVirtualGLSurfaceView;
    public DefaultContextFactory(BaseVirtualGLSurfaceView baseVirtualGLSurfaceView) {
        this.baseVirtualGLSurfaceView = baseVirtualGLSurfaceView;
    }

    private int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

    public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
        int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, baseVirtualGLSurfaceView.getEGLContextClientVersion(),
                EGL10.EGL_NONE };

        return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT,
                baseVirtualGLSurfaceView.getEGLContextClientVersion() != 0 ? attrib_list : null);
    }

    public void destroyContext(EGL10 egl, EGLDisplay display,
                               EGLContext context) {
        if (!egl.eglDestroyContext(display, context)) {
            Log.e("DefaultContextFactory", "display:" + display + " context: " + context);
            if (BaseVirtualGLSurfaceView.LOG_THREADS) {
                Log.i("DefaultContextFactory", "tid=" + Thread.currentThread().getId());
            }
            VirtualEglHelper.throwEglException("eglDestroyContex", egl.eglGetError());
        }
    }
}