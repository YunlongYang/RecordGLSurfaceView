package com.play.android.gsv.virtual.egl.config;

import com.play.android.gsv.virtual.BaseVirtualGLSurfaceView;

public class SimpleEGLConfigChooser extends ComponentSizeChooser {
    public SimpleEGLConfigChooser(boolean withDepthBuffer, BaseVirtualGLSurfaceView baseVirtualGLSurfaceView) {
        super(8, 8, 8, 0, withDepthBuffer ? 16 : 0, 0,baseVirtualGLSurfaceView);
    }
}
