package com.play.android.onscreen.egl.egl

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Environment
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.logging.Logger
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class EglRecordRender (val context: Context, val proxyRender:GLSurfaceView.Renderer):GLSurfaceView.Renderer{

    companion object{
        val logger = Logger.getLogger(EglRecordRender::class.simpleName)
    }

    val BYTES_PER_FLOAT = 4

    private var width = 0
    private var height = 0

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        proxyRender.onSurfaceChanged(gl, width, height)
        this.width = width
        this.height = height
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        proxyRender.onSurfaceCreated(gl, config)
    }

    override fun onDrawFrame(gl: GL10?) {
        proxyRender.onDrawFrame(gl)
        //        GLES20.glBlendColor();
//        testRender.onDrawFrame(EMPTY_GL10);
        // 5. 读取当前画面上的像素信息
        val byteBuffer: ByteBuffer = readImageData()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(byteBuffer)
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "test_offscreen_render.jpg")
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))
            logger.info("图片已生成")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

    }

    private fun readImageData(): ByteBuffer {
        val buffer = ByteBuffer.allocate(width * height * BYTES_PER_FLOAT)
        GLES20.glReadPixels(0,
                0,
                width,
                height,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, buffer)
        return buffer
    }

}