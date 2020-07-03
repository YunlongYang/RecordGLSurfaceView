package com.play.android.gsv.render.ui.test;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.play.android.gsv.render.R;
import com.play.android.gsv.render.core.RenderDelegate;
import com.play.android.gsv.virtual.VirtualGLSurfaceView;
import com.play.android.gsv.virtual.render.RenderWatcherFactory;

public class TestGsvActivity extends AppCompatActivity {

    private TestGLSurfaceView gLSurfaceView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_gsv);
//        gLSurfaceView = findViewById(R.id.gl_surfaceview);
        RenderDelegate delegate = new RenderDelegate();
//        gLSurfaceView.setRenderer(delegate);
        VirtualGLSurfaceView virtualGLSurfaceView = new VirtualGLSurfaceView(this);
        virtualGLSurfaceView.setRenderer(delegate);
        virtualGLSurfaceView.setRenderWatcherFactory(new RenderWatcherFactory() {
            @Override
            public GLSurfaceView.Renderer createWatcher(GLSurfaceView.Renderer proxyRender) {
                return new SaveVideoRenderWatcher(proxyRender);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
