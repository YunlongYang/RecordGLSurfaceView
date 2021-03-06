package com.play.android.gsv.virtual.os;


import android.util.Log;

public class Trace {
    /*
     * Writes trace events to the kernel trace buffer.  These trace events can be
     * collected using the "atrace" program for offline analysis.
     */

    private static final String TAG = "Trace";

    // These tags must be kept in sync with system/core/include/cutils/trace.h.
    // They should also be added to frameworks/native/cmds/atrace/atrace.cpp.
    /** @hide */
    public static final long TRACE_TAG_NEVER = 0;
    /** @hide */
    public static final long TRACE_TAG_ALWAYS = 1L << 0;
    /** @hide */
    public static final long TRACE_TAG_GRAPHICS = 1L << 1;
    /** @hide */
    public static final long TRACE_TAG_INPUT = 1L << 2;
    /** @hide */
    public static final long TRACE_TAG_VIEW = 1L << 3;
    /** @hide */
    public static final long TRACE_TAG_WEBVIEW = 1L << 4;
    /** @hide */
    public static final long TRACE_TAG_WINDOW_MANAGER = 1L << 5;
    /** @hide */
    public static final long TRACE_TAG_ACTIVITY_MANAGER = 1L << 6;
    /** @hide */
    public static final long TRACE_TAG_SYNC_MANAGER = 1L << 7;
    /** @hide */
    public static final long TRACE_TAG_AUDIO = 1L << 8;
    /** @hide */
    public static final long TRACE_TAG_VIDEO = 1L << 9;
    /** @hide */
    public static final long TRACE_TAG_CAMERA = 1L << 10;
    /** @hide */
    public static final long TRACE_TAG_HAL = 1L << 11;
    /** @hide */
    public static final long TRACE_TAG_APP = 1L << 12;
    /** @hide */
    public static final long TRACE_TAG_RESOURCES = 1L << 13;
    /** @hide */
    public static final long TRACE_TAG_DALVIK = 1L << 14;
    /** @hide */
    public static final long TRACE_TAG_RS = 1L << 15;
    /** @hide */
    public static final long TRACE_TAG_BIONIC = 1L << 16;
    /** @hide */
    public static final long TRACE_TAG_POWER = 1L << 17;
    /** @hide */
    public static final long TRACE_TAG_PACKAGE_MANAGER = 1L << 18;
    /** @hide */
    public static final long TRACE_TAG_SYSTEM_SERVER = 1L << 19;
    /** @hide */
    public static final long TRACE_TAG_DATABASE = 1L << 20;
    /** @hide */
    public static final long TRACE_TAG_NETWORK = 1L << 21;
    /** @hide */
    public static final long TRACE_TAG_ADB = 1L << 22;
    /** @hide */
    public static final long TRACE_TAG_VIBRATOR = 1L << 23;
    /** @hide */
    public static final long TRACE_TAG_AIDL = 1L << 24;
    /** @hide */
    public static final long TRACE_TAG_NNAPI = 1L << 25;
    /** @hide */
    public static final long TRACE_TAG_RRO = 1L << 26;

    private static final long TRACE_TAG_NOT_READY = 1L << 63;
    private static final int MAX_SECTION_NAME_LEN = 127;

    // Must be volatile to avoid word tearing.
    private static volatile long sEnabledTags = TRACE_TAG_NOT_READY;

    private static int sZygoteDebugFlags = 0;

    private static native long nativeGetEnabledTags();
    private static native void nativeSetAppTracingAllowed(boolean allowed);
    private static native void nativeSetTracingEnabled(boolean allowed);


    private Trace() {
    }

    /**
     * Caches a copy of the enabled-tag bits.  The "master" copy is held by the native code,
     * and comes from the PROPERTY_TRACE_TAG_ENABLEFLAGS property.
     * <p>
     * If the native code hasn't yet read the property, we will cause it to do one-time
     * initialization.  We don't want to do this during class init, because this class is
     * preloaded, so all apps would be stuck with whatever the zygote saw.  (The zygote
     * doesn't see the system-property update broadcasts.)
     * <p>
     * We want to defer initialization until the first use by an app, post-zygote.
     * <p>
     * We're okay if multiple threads call here simultaneously -- the native state is
     * synchronized, and sEnabledTags is volatile (prevents word tearing).
     */
    private static long cacheEnabledTags() {
        long tags = nativeGetEnabledTags();
        sEnabledTags = tags;
        return tags;
    }

    /**
     * Returns true if a trace tag is enabled.
     *
     * @param traceTag The trace tag to check.
     * @return True if the trace tag is valid.
     *
     * @hide
     */
    public static boolean isTagEnabled(long traceTag) {
        long tags = sEnabledTags;
        if (tags == TRACE_TAG_NOT_READY) {
            tags = cacheEnabledTags();
        }
        return (tags & traceTag) != 0;
    }

    /**
     * Writes trace message to indicate the value of a given counter.
     *
     * @param traceTag The trace tag.
     * @param counterName The counter name to appear in the trace.
     * @param counterValue The counter value.
     *
     * @hide
     */
    public static void traceCounter(long traceTag, String counterName, int counterValue) {
        if (isTagEnabled(traceTag)) {
            Log.i(TAG,traceTag + counterName + counterValue);
        }
    }

    /**
     * Set whether application tracing is allowed for this process.  This is intended to be set
     * once at application start-up time based on whether the application is debuggable.
     *
     * @hide
     */
    public static void setAppTracingAllowed(boolean allowed) {
        nativeSetAppTracingAllowed(allowed);

        // Setting whether app tracing is allowed may change the tags, so we update the cached
        // tags here.
        cacheEnabledTags();
    }

    /**
     * Set whether tracing is enabled in this process.  Tracing is disabled shortly after Zygote
     * initializes and re-enabled after processes fork from Zygote.  This is done because Zygote
     * has no way to be notified about changes to the tracing tags, and if Zygote ever reads and
     * caches the tracing tags, forked processes will inherit those stale tags.
     *
     * @hide
     */
    public static void setTracingEnabled(boolean enabled, int debugFlags) {
        nativeSetTracingEnabled(enabled);
        sZygoteDebugFlags = debugFlags;

        // Setting whether tracing is enabled may change the tags, so we update the cached tags
        // here.
        cacheEnabledTags();
    }

    /**
     * Writes a trace message to indicate that a given section of code has
     * begun. Must be followed by a call to {@link #traceEnd} using the same
     * tag.
     *
     * @param traceTag The trace tag.
     * @param methodName The method name to appear in the trace.
     *
     * @hide
     */
    public static void traceBegin(long traceTag, String methodName) {
        if (isTagEnabled(traceTag)) {
            Log.i(TAG,traceTag + methodName);
        }
    }

    /**
     * Writes a trace message to indicate that the current method has ended.
     * Must be called exactly once for each call to {@link #traceBegin} using the same tag.
     *
     * @param traceTag The trace tag.
     *
     * @hide
     */
    public static void traceEnd(long traceTag) {
        if (isTagEnabled(traceTag)) {
            Log.i(TAG,traceTag+"");
        }
    }

    /**
     * Writes a trace message to indicate that a given section of code has
     * begun. Must be followed by a call to {@link #asyncTraceEnd} using the same
     * tag. Unlike {@link #traceBegin(long, String)} and {@link #traceEnd(long)},
     * asynchronous events do not need to be nested. The name and cookie used to
     * begin an event must be used to end it.
     *
     * @param traceTag The trace tag.
     * @param methodName The method name to appear in the trace.
     * @param cookie Unique identifier for distinguishing simultaneous events
     *
     * @hide
     */
    public static void asyncTraceBegin(long traceTag, String methodName, int cookie) {
        if (isTagEnabled(traceTag)) {
            Log.i(TAG, traceTag+methodName+cookie);
        }
    }

    /**
     * Writes a trace message to indicate that the current method has ended.
     * Must be called exactly once for each call to {@link #asyncTraceBegin(long, String, int)}
     * using the same tag, name and cookie.
     *
     * @param traceTag The trace tag.
     * @param methodName The method name to appear in the trace.
     * @param cookie Unique identifier for distinguishing simultaneous events
     *
     * @hide
     */
    public static void asyncTraceEnd(long traceTag, String methodName, int cookie) {
        if (isTagEnabled(traceTag)) {
            Log.i(TAG, (traceTag+ methodName+ cookie));
        }
    }

    /**
     * Checks whether or not tracing is currently enabled. This is useful to avoid intermediate
     * string creation for trace sections that require formatting. It is not necessary
     * to guard all Trace method calls as they internally already check this. However it is
     * recommended to use this to prevent creating any temporary objects that would then be
     * passed to those methods to reduce runtime cost when tracing isn't enabled.
     *
     * @return true if tracing is currently enabled, false otherwise
     */
    public static boolean isEnabled() {
        return isTagEnabled(TRACE_TAG_APP);
    }

    /**
     * Writes a trace message to indicate that a given section of code has begun. This call must
     * be followed by a corresponding call to {@link #endSection()} on the same thread.
     *
     * <p class="note"> At this time the vertical bar character '|', newline character '\n', and
     * null character '\0' are used internally by the tracing mechanism.  If sectionName contains
     * these characters they will be replaced with a space character in the trace.
     *
     * @param sectionName The name of the code section to appear in the trace.  This may be at
     * most 127 Unicode code units long.
     */
    public static void beginSection( String sectionName) {
        if (isTagEnabled(TRACE_TAG_APP)) {
            if (sectionName.length() > MAX_SECTION_NAME_LEN) {
                throw new IllegalArgumentException("sectionName is too long");
            }
            Log.i(TAG, ("beginSection"+ sectionName));
        }
    }

    /**
     * Writes a trace message to indicate that a given section of code has ended. This call must
     * be preceeded by a corresponding call to {@link #beginSection(String)}. Calling this method
     * will mark the end of the most recently begun section of code, so care must be taken to
     * ensure that beginSection / endSection pairs are properly nested and called from the same
     * thread.
     */
    public static void endSection() {
        if (isTagEnabled(TRACE_TAG_APP)) {
            Log.i(TAG, ("endSection"));
        }
    }

    /**
     * Writes a trace message to indicate that a given section of code has
     * begun. Must be followed by a call to {@link #endAsyncSection(String, int)} with the same
     * methodName and cookie. Unlike {@link #beginSection(String)} and {@link #endSection()},
     * asynchronous events do not need to be nested. The name and cookie used to
     * begin an event must be used to end it.
     *
     * @param methodName The method name to appear in the trace.
     * @param cookie Unique identifier for distinguishing simultaneous events
     */
    public static void beginAsyncSection( String methodName, int cookie) {
        asyncTraceBegin(TRACE_TAG_APP, methodName, cookie);
    }

    /**
     * Writes a trace message to indicate that the current method has ended.
     * Must be called exactly once for each call to {@link #beginAsyncSection(String, int)}
     * using the same name and cookie.
     *
     * @param methodName The method name to appear in the trace.
     * @param cookie Unique identifier for distinguishing simultaneous events
     */
    public static void endAsyncSection( String methodName, int cookie) {
        asyncTraceEnd(TRACE_TAG_APP, methodName, cookie);
    }

    /**
     * Writes trace message to indicate the value of a given counter.
     *
     * @param counterName The counter name to appear in the trace.
     * @param counterValue The counter value.
     */
    public static void setCounter( String counterName, long counterValue) {
        if (isTagEnabled(TRACE_TAG_APP)) {
//            nativeTraceCounter(TRACE_TAG_APP, counterName, counterValue);
            Log.i(TAG, (TRACE_TAG_APP+ counterName+ counterValue));
        }
    }
}
