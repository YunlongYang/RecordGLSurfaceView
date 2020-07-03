package com.play.android.gsv.virtual;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.play.android.gsv.virtual.render.EmptyRenderWatcherFactory;
import com.play.android.gsv.virtual.render.RenderWatcherFactory;

public class VirtualGLSurfaceView extends BaseVirtualGLSurfaceView{

    private RenderWatcherFactory renderWatcherFactory;

    public VirtualGLSurfaceView(Context context) {
        super(context);
        renderWatcherFactory = new EmptyRenderWatcherFactory();
    }

    public void setRenderWatcherFactory(RenderWatcherFactory renderWatcherFactory) {
        this.renderWatcherFactory = renderWatcherFactory;
    }

    @Override
    public void setRenderer(GLSurfaceView.Renderer renderer) {
        super.setRenderer(renderWatcherFactory.createWatcher(renderer));
    }
}
