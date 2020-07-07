package com.play.android.gsv.virtual.egl.config;

import com.play.android.gsv.virtual.egl.version.EGLContextClientVersion;

public class SimpleEGLConfigChooser extends ComponentSizeChooser {
    public SimpleEGLConfigChooser(boolean withDepthBuffer, EGLContextClientVersion eglContextClientVersion) {
        super(8, 8, 8, 0, withDepthBuffer ? 16 : 0, 0,eglContextClientVersion);
    }
}
