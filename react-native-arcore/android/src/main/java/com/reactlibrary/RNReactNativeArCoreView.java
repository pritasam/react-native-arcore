
package com.reactlibrary;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.Trackable.TrackingState;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.reactlibrary.arutil.rendering.DisplayRotationHelper;
import com.reactlibrary.arutil.rendering.ObjectRenderer;
import com.reactlibrary.arutil.rendering.ObjectRenderer.BlendMode;
import com.reactlibrary.arutil.rendering.PlaneRenderer;
import com.reactlibrary.arutil.rendering.PointCloudRenderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class RNReactNativeArCoreView extends LinearLayout implements GLSurfaceView.Renderer {

    private static final String TAG = RNReactNativeArCoreView.class.getSimpleName();

    private Context mContextModule;

    private GLSurfaceView mSurfaceView;
    private Session mSession;
    private GestureDetector mGestureDetector;
    private Snackbar mMessageSnackbar;
    private DisplayRotationHelper mDisplayRotationHelper;

    private final com.reactlibrary.arutil.rendering.BackgroundRenderer mBackgroundRenderer = new com.reactlibrary.arutil.rendering.BackgroundRenderer();
    private final ObjectRenderer mVirtualObject = new ObjectRenderer();
    private final ObjectRenderer mVirtualObjectShadow = new ObjectRenderer();
    private final PlaneRenderer mPlaneRenderer = new PlaneRenderer();
    private final PointCloudRenderer mPointCloud = new PointCloudRenderer();

    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private final float[] mAnchorMatrix = new float[16];

    // Tap handling and UI.
    private final ArrayBlockingQueue<MotionEvent> mQueuedSingleTaps = new ArrayBlockingQueue<>(16);
    private final ArrayList<Anchor> mAnchors = new ArrayList<>();

    private CoreViewCallback callback;

    public interface CoreViewCallback {
        void planeDetected(WritableMap event);
        void planeHitDetected(WritableMap event);
    }

    public RNReactNativeArCoreView(Context context, CoreViewCallback callback) {
        super(context);
        this.setOrientation(LinearLayout.HORIZONTAL);
        this.setBackgroundColor(Color.WHITE);
        this.callback = callback;
        mContextModule = context;
      //  this.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        mSurfaceView =  new GLSurfaceView(mContextModule);
        this.addView(mSurfaceView);
        mDisplayRotationHelper = new DisplayRotationHelper(/*context=*/ mContextModule);
        // Set up tap listener.
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                onSingleTap(e);
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });

        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });

        // Set up renderer.
        mSurfaceView.setPreserveEGLContextOnPause(true);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        mSurfaceView.setRenderer(this);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        Exception exception = null;
        String message = null;
        try {
            mSession = new Session(/* context= */ mContextModule);
        } catch (UnavailableArcoreNotInstalledException e) {
            message = "Please install ARCore";
            exception = e;
        } catch (UnavailableApkTooOldException e) {
            message = "Please update ARCore";
            exception = e;
        } catch (UnavailableSdkTooOldException e) {
            message = "Please update this app";
            exception = e;
        } catch (Exception e) {
            message = "This device does not support AR";
            exception = e;
        }
        if (message != null) {
            showSnackbarMessage(message, true);
            return;
        }
        Config config = new Config(mSession);
        if (!mSession.isSupported(config)) {
            showSnackbarMessage("This device does not support AR", true);
        }
        mSession.configure(config);
        hasCameraPermissions();
        if (mSession != null) {
            showLoadingMessage();
            mSession.resume();
        }
        mSurfaceView.onResume();
        mDisplayRotationHelper.onResume();
    }

    private boolean hasCameraPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
            return result == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.e(TAG, "onAttachedToWindow");
    }

    public void passPlaneDetectedData() {
        WritableMap event = Arguments.createMap();
        event.putBoolean("planeDetected", true);
        WritableArray array = new WritableNativeArray();
        int i = 0;
        for (Plane plane : mSession.getAllTrackables(Plane.class)) {
            if (plane.getTrackingState() == TrackingState.TRACKING) {
                i = 1;
                WritableMap map = new WritableNativeMap();
                map.putDouble("x",plane.getCenterPose().tx());
                map.putDouble("y",plane.getCenterPose().ty());
                map.putDouble("z",plane.getCenterPose().tz());
                map.putDouble("id",plane.hashCode());
                array.pushMap(map);
            }
        }
        if (callback != null && i > 0) {
            event.putArray("planes",array);
            callback.planeDetected(event);
        }
    }

    public void passPlaneHitDetectedData( float[] projectionMatrix, float[] viewMatrix) {
        Log.e(TAG, "Failed to read plane texture");
        WritableMap projection =  JsonUtils.createMapFromFloat(projectionMatrix);
        WritableMap viewMap =  JsonUtils.createMapFromFloat(viewMatrix);
        for (Anchor anchor : mAnchors) {
            if (anchor.getTrackingState() != TrackingState.TRACKING) {
                continue;
            }
            WritableMap map = new WritableNativeMap();
            map.putBoolean("planeHitDetected", true);
            map.putDouble("x",anchor.getPose().tx());
            map.putDouble("y",anchor.getPose().ty());
            map.putDouble("z",anchor.getPose().tz());
            map.putDouble("id",anchor.hashCode());
            map.putMap("projection",projection);
            map.putMap("viewMap",viewMap);
            if (callback != null) {
                callback.planeHitDetected(map);
            }
        }
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        Log.e(TAG, "onSurfaceCreated");
        mBackgroundRenderer.createOnGlThread(/*context=*/ mContextModule);
        if (mSession != null) {
            mSession.setCameraTextureName(mBackgroundRenderer.getTextureId());
        }
        // Prepare the other rendering objects.
        try {
            mVirtualObject.createOnGlThread(/*context=*/mContextModule, "andy.obj", "andy.png");
            mVirtualObject.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f);

            mVirtualObjectShadow.createOnGlThread(/*context=*/mContextModule,
                    "andy_shadow.obj", "andy_shadow.png");
            mVirtualObjectShadow.setBlendMode(BlendMode.Shadow);
            mVirtualObjectShadow.setMaterialProperties(1.0f, 0.0f, 0.0f, 1.0f);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read obj file");
        }
        try {
            mPlaneRenderer.createOnGlThread(/*context=*/mContextModule, "trigrid.png");
        } catch (IOException e) {
            Log.e(TAG, "Failed to read plane texture");
        }
        mPointCloud.createOnGlThread(/*context=*/mContextModule);
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.e(TAG, "onSurfaceChanged");
        mDisplayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.e(TAG, "onDrawFrame");
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        if (mSession == null) {
            return;
        }
        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        mDisplayRotationHelper.updateSessionIfNeeded(mSession);
        try {
            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            Frame frame = mSession.update();
            Camera camera = frame.getCamera();
            // Handle taps. Handling only one tap per frame, as taps are usually low frequency
            // compared to frame rate.
            MotionEvent tap = mQueuedSingleTaps.poll();
            if (tap != null && camera.getTrackingState() == TrackingState.TRACKING) {
                for (HitResult hit : frame.hitTest(tap)) {
                    // Check if any plane was hit, and if it was hit inside the plane polygon
                    Trackable trackable = hit.getTrackable();
                    if (trackable instanceof Plane
                            && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                        // Cap the number of objects created. This avoids overloading both the
                        // rendering system and ARCore.
                        if (mAnchors.size() >= 1) {
                            mAnchors.get(0).detach();
                            mAnchors.remove(0);
                        }
                        // Adding an Anchor tells ARCore that it should track this position in
                        // space. This anchor is created on the Plane to place the 3d model
                        // in the correct position relative both to the world and to the plane.
                        mAnchors.add(hit.createAnchor());
                        // Hits are sorted by depth. Consider only closest hit on a plane.
                        break;
                    }
                }
            }
            // Draw background.
            mBackgroundRenderer.draw(frame);
            // If not tracking, don't draw 3d objects.
            if (camera.getTrackingState() == TrackingState.PAUSED) {
                return;
            }
            // Get projection matrix.
            float[] projmtx = new float[16];
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);
            // Get camera matrix and draw.
            float[] viewmtx = new float[16];
            camera.getViewMatrix(viewmtx, 0);
            final float lightIntensity = frame.getLightEstimate().getPixelIntensity();
            passPlaneDetectedData();
            // Check if we detected at least one plane. If so, hide the loading message.
            if (mMessageSnackbar != null) {
                for (Plane plane : mSession.getAllTrackables(Plane.class)) {
                    if (plane.getType() == Plane.Type.HORIZONTAL_UPWARD_FACING
                            && plane.getTrackingState() == TrackingState.TRACKING) {
                        hideLoadingMessage();
                        break;
                    }
                }
            }
            // Visualize planes.
            mPlaneRenderer.drawPlanes(
                    mSession.getAllTrackables(Plane.class), camera.getDisplayOrientedPose(), projmtx);
            float scaleFactor = 1.0f;
            passPlaneHitDetectedData(projmtx,viewmtx);
        } catch (Throwable t) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t);
        }
    }

    private void showSnackbarMessage(String message, boolean finishOnDismiss) {
    }

    private void showLoadingMessage() {
        showSnackbarMessage("Searching for surfaces...", false);
    }

    private void hideLoadingMessage() {
        if (mMessageSnackbar != null) {
            mMessageSnackbar.dismiss();
        }
        mMessageSnackbar = null;
    }

    private void onSingleTap(MotionEvent e) {
        // Queue tap if there is space. Tap is lost if queue is full.
        mQueuedSingleTaps.offer(e);
    }
}