package com.play.android.gsv.virtual;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.SurfaceHolder;

import com.play.android.gsv.virtual.egl.config.EGLConfigChooser;
import com.play.android.gsv.virtual.egl.config.SimpleEGLConfigChooser;
import com.play.android.gsv.virtual.egl.context.DefaultContextFactory;
import com.play.android.gsv.virtual.egl.context.EGLContextFactory;
import com.play.android.gsv.virtual.egl.surface.DefaultWindowSurfaceFactory;
import com.play.android.gsv.virtual.egl.surface.EGLWindowSurfaceFactory;
import com.play.android.gsv.virtual.egl.version.EGLContextClientVersion;
import com.play.android.gsv.virtual.egl.wrapper.GLWrapper;
import com.play.android.gsv.virtual.thread.VirtualGLThread;
import com.play.android.gsv.virtual.thread.VirtualGLThreadManager;

import java.lang.ref.WeakReference;

public class BaseVirtualGLSurfaceView implements SurfaceHolder.Callback2, EGLContextClientVersion {
    private static final String TAG = "BVGLSView";

    private Context context;
    private static final VirtualGLThreadManager sGLThreadManager = new VirtualGLThreadManager();

    private final WeakReference<BaseVirtualGLSurfaceView> mThisWeakRef =
            new WeakReference<>(this);

    private VirtualGLThread mGLThread;
    private GLSurfaceView.Renderer mRenderer;
    private boolean mDetached;
    private EGLConfigChooser mEGLConfigChooser;
    private EGLContextFactory mEGLContextFactory;
    private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
    private GLWrapper mGLWrapper;

    public int mDebugFlags;
    private int mEGLContextClientVersion;
    boolean mPreserveEGLContextOnPause;

    public BaseVirtualGLSurfaceView(Context context) {
        this.context = context;
        init();
    }
    private void init() {
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed
        // TODO Lifecycle Call
//        SurfaceHolder holder = getHolder();
//        holder.addCallback(this);
        // setFormat is done by SurfaceView in SDK 2.3 and newer. Uncomment
        // this statement if back-porting to 2.2 or older:
        // holder.setFormat(PixelFormat.RGB_565);
        //
        // setType is not needed for SDK 2.0 or newer. Uncomment this
        // statement if back-porting this code to older SDKs.
        // holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
    }

    protected GLSurfaceView.Renderer render;

    public void setRenderer(GLSurfaceView.Renderer renderer) {
        checkRenderThreadState();
        if (mEGLConfigChooser == null) {
            mEGLConfigChooser = new SimpleEGLConfigChooser(true,this);
        }
        if (mEGLContextFactory == null) {
            mEGLContextFactory = new DefaultContextFactory(this);
        }
        if (mEGLWindowSurfaceFactory == null) {
            mEGLWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
        }
        mRenderer = renderer;
        mGLThread = new VirtualGLThread(mThisWeakRef);
        mGLThread.start();
    }


    public void onAttachedToWindow() {
        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "onAttachedToWindow reattach =" + mDetached);
        }
        if (mDetached && (mRenderer != null)) {
            int renderMode = RENDERMODE_CONTINUOUSLY;
            if (mGLThread != null) {
                renderMode = mGLThread.getRenderMode();
            }
            mGLThread = new VirtualGLThread(mThisWeakRef);
            if (renderMode != RENDERMODE_CONTINUOUSLY) {
                mGLThread.setRenderMode(renderMode);
            }
            mGLThread.start();
        }
        mDetached = false;
    }

    public void onDetachedFromWindow() {
        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "onDetachedFromWindow");
        }
        if (mGLThread != null) {
            mGLThread.requestExitAndWait();
        }
        mDetached = true;
    }

    /**
     * Queue a runnable to be run on the GL rendering thread. This can be used
     * to communicate with the Renderer on the rendering thread.
     * Must not be called before a renderer has been set.
     * @param r the runnable to be run on the GL rendering thread.
     */
    public void queueEvent(Runnable r) {
        mGLThread.queueEvent(r);
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of GLSurfaceView.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        mGLThread.surfaceCreated();
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of GLSurfaceView.
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return
        mGLThread.surfaceDestroyed();
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of GLSurfaceView.
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        mGLThread.onWindowResize(w, h);
    }

    /**
     * This method is part of the SurfaceHolder.Callback2 interface, and is
     * not normally called or subclassed by clients of GLSurfaceView.
     */
    @Override
    public void surfaceRedrawNeededAsync(SurfaceHolder holder, Runnable finishDrawing) {
        if (mGLThread != null) {
            mGLThread.requestRenderAndNotify(finishDrawing);
        }
    }

    /**
     * This method is part of the SurfaceHolder.Callback2 interface, and is
     * not normally called or subclassed by clients of GLSurfaceView.
     */
    @Deprecated
    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {
        // Since we are part of the framework we know only surfaceRedrawNeededAsync
        // will be called.
    }




    protected void finalize() throws Throwable {
        try {
            if (mGLThread != null) {
                // GLThread may still be running if this view was never
                // attached to a window.
                mGLThread.requestExitAndWait();
            }
        } finally {
            super.finalize();
        }
    }
    public void setRenderMode(int renderMode) {
        mGLThread.setRenderMode(renderMode);
    }

    public int getRenderMode() {
        return mGLThread.getRenderMode();
    }

    public void onPause() {
        mGLThread.onPause();
    }

    /**
     * Resumes the rendering thread, re-creating the OpenGL context if necessary. It
     * is the counterpart to {@link #onPause()}.
     *
     * This method should typically be called in
     * {android.app.Activity#onStart Activity.onStart}.
     *
     * Must not be called before a renderer has been set.
     */
    public void onResume() {
        mGLThread.onResume();
    }

    private void checkRenderThreadState() {
        if (mGLThread != null) {
            throw new IllegalStateException(
                    "setRenderer has already been called for this instance.");
        }
    }



    private VirtualGLCoreProvider virtualGLCoreProvider;

    public EGLWindowSurfaceFactory getEGLWindowSurfaceFactory() {
        return mEGLWindowSurfaceFactory;
    }

    public VirtualGLCoreProvider getVirtualGLCoreProvider() {
        return virtualGLCoreProvider;
    }

    public void setVirtualGLCoreProvider(VirtualGLCoreProvider virtualGLCoreProvider) {
        this.virtualGLCoreProvider = virtualGLCoreProvider;
        mEGLWindowSurfaceFactory = virtualGLCoreProvider;
        mEGLContextFactory = virtualGLCoreProvider;
    }

    public GLWrapper getGLWrapper() {
        return mGLWrapper;
    }

    public void setGLWrapper(GLWrapper mGLWrapper) {
        this.mGLWrapper = mGLWrapper;
    }

    public static VirtualGLThreadManager getGLThreadManager() {
        return sGLThreadManager;
    }

    public boolean isPreserveEGLContextOnPause() {
        return mPreserveEGLContextOnPause;
    }

    public void setPreserveEGLContextOnPause(boolean mPreserveEGLContextOnPause) {
        this.mPreserveEGLContextOnPause = mPreserveEGLContextOnPause;
    }

    public GLSurfaceView.Renderer getRender() {
        return render;
    }

    public EGLContextFactory getEGLContextFactory() {
        return mEGLContextFactory;
    }

    public EGLConfigChooser getEGLConfigChooser() {
        return mEGLConfigChooser;
    }

    // getEGLWindowSurfaceFactory().createWindowSurface(mEgl,
    //                    mEglDisplay, mEglConfig, view.getHolder());
    public Object getHolder() {
        return null;
    }

    public int getEGLContextClientVersion() {
        return mEGLContextClientVersion;
    }

    public void setEGLContextClientVersion(int mEGLContextClientVersion) {
        this.mEGLContextClientVersion = mEGLContextClientVersion;
    }

   public final static boolean LOG_ATTACH_DETACH = false;
   public final static boolean LOG_THREADS = false;
   public final static boolean LOG_PAUSE_RESUME = false;
   public final static boolean LOG_SURFACE = false;
   public final static boolean LOG_RENDERER = false;
   public final static boolean LOG_RENDERER_DRAW_FRAME = false;
   public final static boolean LOG_EGL = false;
    /**
     * The renderer only renders
     * when the surface is created, or when {requestRender} is called.
     *
     * @see #setRenderMode(int)
     */
    public final static int RENDERMODE_WHEN_DIRTY = 0;
    /**
     * The renderer is called
     * continuously to re-render the scene.
     *
     * @see #setRenderMode(int)
     */
    public final static int RENDERMODE_CONTINUOUSLY = 1;

    /**
     * Check glError() after every GL call and throw an exception if glError indicates
     * that an error has occurred. This can be used to help track down which OpenGL ES call
     * is causing an error.
     *
     */
    public final static int DEBUG_CHECK_GL_ERROR = 1;

    /**
     * Log GL calls to the system log at "verbose" level with tag "GLSurfaceView".
     *
     */
    public final static int DEBUG_LOG_GL_CALLS = 2;


}
