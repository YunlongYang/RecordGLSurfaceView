package com.play.android.gsv.virtual.render;

import android.opengl.GLSurfaceView;

public class EmptyRenderWatcherFactory implements RenderWatcherFactory {
    @Override
    public GLSurfaceView.Renderer createWatcher(GLSurfaceView.Renderer proxyRender) {
        return new EmptyRenderWatcher(proxyRender);
    }
}
