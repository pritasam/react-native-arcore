package com.reactlibrary;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.facebook.react.views.view.ReactViewGroup;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class StitchSurface extends ReactViewGroup implements GLSurfaceView.Renderer {

    private GLSurfaceView mSurfaceView;

    public StitchSurface(Context context) {
        super(context);
        mSurfaceView = new GLSurfaceView(context);
        init();
    }


    private void init(){
        Log.d("React:", "GLSurfaceView");

        mSurfaceView.setPreserveEGLContextOnPause(true);
        mSurfaceView.setEGLConfigChooser(false);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setRenderer(this);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        mSurfaceView.onResume();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d("React:", "onSurfaceCreated");

        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }


    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d("React:", "onDrawFrame");
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }
}

