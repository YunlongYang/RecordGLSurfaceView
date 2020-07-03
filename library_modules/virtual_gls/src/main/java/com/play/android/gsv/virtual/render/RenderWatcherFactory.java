package com.play.android.gsv.virtual.render;

import android.opengl.GLSurfaceView;

public interface RenderWatcherFactory {
    GLSurfaceView.Renderer createWatcher(GLSurfaceView.Renderer proxyRender);
}
