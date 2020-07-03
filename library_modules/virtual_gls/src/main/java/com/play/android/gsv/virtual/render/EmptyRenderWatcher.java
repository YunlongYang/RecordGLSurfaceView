package com.play.android.gsv.virtual.render;

import android.opengl.GLSurfaceView;

public class EmptyRenderWatcher extends RenderWatcher {
    public EmptyRenderWatcher(GLSurfaceView.Renderer proxyRender) {
        super(proxyRender);
    }

    @Override
    protected void beforeOnDrawFrame() {

    }

    @Override
    protected void afterOnDrawFrame() {

    }
}
