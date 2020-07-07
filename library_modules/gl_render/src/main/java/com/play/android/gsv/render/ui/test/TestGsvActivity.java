package com.play.android.gsv.render.ui.test;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

import com.play.android.gsv.render.R;
import com.play.android.gsv.render.core.RenderDelegate;
import com.play.android.gsv.render.media.codec.MediaCodecGLCoreProvider;
import com.play.android.gsv.render.vsv.VirtualSurfaceViewLifecycle;
import com.play.android.gsv.virtual.VirtualGLSurfaceView;
import com.play.android.gsv.virtual.render.RenderWatcherFactory;

public class TestGsvActivity extends AppCompatActivity {

    private TestGLSurfaceView gLSurfaceView;
    private VirtualSurfaceViewLifecycle lifecycle;
    private MediaCodecGLCoreProvider mediaCodecGLCoreProvider;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        setContentView(R.layout.activity_test_gsv);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//        gLSurfaceView = findViewById(R.id.gl_surfaceview);
        RenderDelegate delegate = new RenderDelegate();
//        gLSurfaceView.setRenderer(delegate);
        VirtualGLSurfaceView virtualGLSurfaceView = new VirtualGLSurfaceView(this);
        virtualGLSurfaceView.setEGLContextClientVersion(2);
        mediaCodecGLCoreProvider = new MediaCodecGLCoreProvider(virtualGLSurfaceView);
        mediaCodecGLCoreProvider.setVideoInfo(new MediaCodecGLCoreProvider.VideoInfo(
                "video/avc",
                displayMetrics.widthPixels,
                displayMetrics.heightPixels,
                800000,
                15,
                10,
                getExternalFilesDir(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/create.mp4"
        ));
        virtualGLSurfaceView.setVirtualGLCoreProvider(mediaCodecGLCoreProvider);
        virtualGLSurfaceView.setRenderer(delegate);
        virtualGLSurfaceView.setRenderWatcherFactory(new RenderWatcherFactory() {
            @Override
            public GLSurfaceView.Renderer createWatcher(GLSurfaceView.Renderer proxyRender) {
                return new SaveVideoRenderWatcher(proxyRender);
            }
        });
        lifecycle = new VirtualSurfaceViewLifecycle(virtualGLSurfaceView);
        lifecycle.setDisplaySize(displayMetrics.widthPixels,displayMetrics.heightPixels);
        lifecycle.onActivityCreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        lifecycle.onActivityResume();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mediaCodecGLCoreProvider.start();
            }
        },3000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        lifecycle.onActivityPause();
        mediaCodecGLCoreProvider.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lifecycle.onActivityDestroy();
    }
}
