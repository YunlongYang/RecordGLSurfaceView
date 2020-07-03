package com.play.android.gsv.virtual.thread;

import android.util.Log;

public class VirtualGLThreadManager {

    private static String TAG = "VirtualGLThreadManager";

    public synchronized void threadExiting(VirtualGLThread thread) {
        if (VirtualGLThread.LOG_THREADS) {
            Log.i(TAG, "exiting tid=" +  thread.getId());
        }
        thread.mExited = true;
        notifyAll();
    }

    /*
     * Releases the EGL context. Requires that we are already in the
     * sGLThreadManager monitor when this is called.
     */
    public void releaseEglContextLocked(VirtualGLThread thread) {
        notifyAll();
    }
}
