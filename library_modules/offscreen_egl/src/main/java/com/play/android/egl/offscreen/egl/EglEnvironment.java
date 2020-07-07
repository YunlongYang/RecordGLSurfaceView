package com.play.android.egl.offscreen.egl;

import android.opengl.GLES20;

import java.nio.ByteBuffer;

public class EglEnvironment {

    public static final int BYTES_PER_FLOAT = 4;
    public static final int BYTES_PER_SHORT = 2;


    private int width;
    private int height;

    public void setDisplaySize(int width,int height){
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void prepareEnv(int[] frameBuffers, int[] textures){

        // 1. 创建FrameBuffer
        GLES20.glGenFramebuffers(frameBuffers.length, frameBuffers, 0);

        // 2.1 生成纹理对象
        GLES20.glGenTextures(textures.length, textures, 0);

    }

    public void initEnv(int texture){
        // 2.2 绑定纹理对象
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        // 2.3 设置纹理对象的相关信息：颜色模式、大小
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                width, height,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        // 2.4 纹理过滤参数设置
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        // 2.5 解绑当前纹理，避免后续无关的操作影响了纹理内容
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    public void bindFrameBufferInfo(int frameBuffer,int texture) {
        // 1. 绑定FrameBuffer到当前的绘制环境上
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer);
        // 2. 将纹理对象挂载到FrameBuffer上，存储颜色信息
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, texture, 0);
    }

    public void unbindFrameBufferInfo() {
        // 解绑FrameBuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public void finish(int[] frameBuffers,int[] textures){
        GLES20.glDeleteFramebuffers(frameBuffers.length, frameBuffers, 0);
        GLES20.glDeleteTextures(textures.length, textures, 0);
    }

    public ByteBuffer readImageData(){
        ByteBuffer buffer = ByteBuffer.allocate(width * height * BYTES_PER_FLOAT);
        GLES20.glReadPixels(0,
                0,
                width,
                height,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, buffer);
        return buffer;
    }
}
