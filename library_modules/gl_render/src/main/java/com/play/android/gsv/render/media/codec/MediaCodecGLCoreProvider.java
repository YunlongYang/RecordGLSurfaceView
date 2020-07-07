package com.play.android.gsv.render.media.codec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGL14;
import android.util.Log;
import android.view.Surface;

import com.play.android.gsv.virtual.BaseVirtualGLSurfaceView;
import com.play.android.gsv.virtual.VirtualGLCoreProvider;
import com.play.android.gsv.virtual.egl.VirtualEglHelper;
import com.play.android.gsv.virtual.egl.version.EGLContextClientVersion;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

public class MediaCodecGLCoreProvider implements VirtualGLCoreProvider {

    private static final String TAG = "MediaCodecGCP";

    private static final Logger logger = Logger.getLogger(TAG);


    private int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

    public static class VideoInfo{
        private String mimeType;
        private int width;
        private int height;
        private int bitRate;
        private int fps;
        private int iFrameInterval;
        private String outputVideoFilePath;

        public VideoInfo(String mimeType, int width, int height, int bitRate, int fps, int iFrameInterval, String outputVideoFilePath) {
            this.mimeType = mimeType;
            this.width = width;
            this.height = height;
            this.bitRate = bitRate;
            this.fps = fps;
            this.iFrameInterval = iFrameInterval;
            this.outputVideoFilePath = outputVideoFilePath;
        }
    }

    private VideoInfo videoInfo;

    private EGLContextClientVersion contextClientVersion;
    private MediaMuxer muxer;
    private MediaCodec encoder;

    private boolean mMuxerStarted;

    public MediaCodecGLCoreProvider(EGLContextClientVersion contextClientVersion) {
        this.contextClientVersion = contextClientVersion;
    }

    public void setVideoInfo(VideoInfo videoInfo){
        this.videoInfo = videoInfo;
    }

    private int[] attrib_list = new int[0];
    @Override
    public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
        attrib_list = new int[]{EGL_CONTEXT_CLIENT_VERSION, contextClientVersion.getEGLContextClientVersion(),
                EGL10.EGL_NONE };
        return egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT,
                attrib_list);
    }

    @Override
    public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
        if (!egl.eglDestroyContext(display, context)) {
            Log.e("DefaultContextFactory", "display:" + display + " context: " + context);
            if (BaseVirtualGLSurfaceView.LOG_THREADS) {
                Log.i("DefaultContextFactory", "tid=" + Thread.currentThread().getId());
            }
            VirtualEglHelper.throwEglException("eglDestroyContex", egl.eglGetError());
        }
    }

    @Override
    public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow) {
        EGLSurface result = null;
        //-----------------MediaFormat-----------------------
        // mediaCodeC采用的是H.264编码
        MediaFormat format = MediaFormat.createVideoFormat(videoInfo.mimeType, videoInfo.width, videoInfo.height);
        // 数据来源自surface
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        // 视频码率
        format.setInteger(MediaFormat.KEY_BIT_RATE, videoInfo.bitRate);
        // fps
        format.setInteger(MediaFormat.KEY_FRAME_RATE, videoInfo.fps);
        //设置关键帧的时间
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, videoInfo.iFrameInterval);

        //-----------------Encoder-----------------------
        try {
            encoder = MediaCodec.createEncoderByType(videoInfo.mimeType);
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            // 创建一个surface
            Surface surface = encoder.createInputSurface();
            result = egl.eglCreateWindowSurface(display, config, surface, attrib_list);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;
    }

    @Override
    public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
        egl.eglDestroySurface(display, surface);
    }

    private boolean mEncodeEnable = false;
    private ExecutorService encodeService;

    public void start(){
        encoder.start();
        //-----------------输出文件路径-----------------------
        // 输出文件路径
        String outputPath = videoInfo.outputVideoFilePath;

        //-----------------MediaMuxer-----------------------
        try {
            // 输出为MP4
            muxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException ioe) {
            throw new RuntimeException("MediaMuxer creation failed", ioe);
        }
        encodeService = Executors.newSingleThreadExecutor();
        mEncodeEnable = true;
        encodeService.execute(new Runnable() {
            @Override
            public void run() {
                drainEncoder(false);
            }
        });
    }

    public void stop(){
        mEncodeEnable = false;
        encodeService.shutdown();
        try {
            encodeService.awaitTermination(5, TimeUnit.SECONDS);
            drainEncoder(true);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
    }


    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private int mTrackIndex;
    /**
     * mEncoder从缓冲区取数据，然后交给mMuxer编码
     *
     * @param endOfStream 是否停止录制
     */
    public void drainEncoder(boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;

        // 停止录制
        if (endOfStream) {
            encoder.signalEndOfInputStream();
            logger.info("drainEncoder end");
        }else{
            logger.info("drainEncoder start");
        }
        //拿到输出缓冲区,用于取到编码后的数据
        ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();
        while (mEncodeEnable) {
            logger.info("drainEncoder running");
            //拿到输出缓冲区的索引
            int encoderStatus = encoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                logger.info("drainEncoder INFO_TRY_AGAIN_LATER");
                // no output available yet
                if (!endOfStream) {
//                    break;      // out of while
                } else {

                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                //拿到输出缓冲区,用于取到编码后的数据
                logger.info("drainEncoder INFO_OUTPUT_BUFFERS_CHANGED");
                encoderOutputBuffers = encoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                logger.info("drainEncoder INFO_OUTPUT_FORMAT_CHANGED");
                // should happen before receiving buffers, and should only happen once
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                //
                MediaFormat newFormat = encoder.getOutputFormat();
                // now that we have the Magic Goodies, start the muxer
                mTrackIndex = muxer.addTrack(newFormat);
                //
                muxer.start();
                mMuxerStarted = true;
            } else if (encoderStatus < 0) {
                logger.info("drainEncoder encoderStatus < 0 "+encoderStatus);
            } else {
                //获取解码后的数据
                logger.info("drainEncoder got encode data");
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }
                //
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    mBufferInfo.size = 0;
                }
                //
                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }
                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                    // 编码
                    muxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
                }
                //释放资源
                encoder.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        logger.info("reached end of stream unexpectedly");
                    } else {

                    }
                    break;      // out of while
                }
            }
        }

        logger.info("drainEncoder exit loop");
    }

}
