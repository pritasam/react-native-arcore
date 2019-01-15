
package com.reactlibrary;

import android.Manifest;
import android.content.pm.PackageManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
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
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class RNReactNativeArCoreView extends LinearLayout implements GLSurfaceView.Renderer , LifecycleEventListener {

    private static final String TAG = RNReactNativeArCoreView.class.getSimpleName();


    private ThemedReactContext mContextModule;
    private boolean installRequested;


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


    private  boolean firstTimePlaneDetected = false;
    private  float xPlaneDetected = 0;
    private  float yPlaneDetected = 0;
    private  float zPlaneDetected = 0;
    float[] projmatrix = new float[16];
    float[] viewmatrix = new float[16];
    float[] anchorMatrix = new float[16];
    Pose anchorPose;


    private  int heightOFScreen = 0;
    private  int weightOFScreen = 0;

    private final List<Float> mTranslationX = new ArrayList<Float>();
    private final List<Float> mTranslationZ = new ArrayList<Float>();
    private final float[] mOriginCameraMatrix = new float[16];
    private final float[] mCurrentCameraMatrix = new float[16];


    // Tap handling and UI.
    private final ArrayBlockingQueue<MotionEvent> mQueuedSingleTaps = new ArrayBlockingQueue<>(16);
    private final ArrayList<Anchor> mAnchors = new ArrayList<>();

    private CoreViewCallback callback;

    public interface CoreViewCallback {
        void planeDetected(WritableMap event);
        void planeHitDetected(WritableMap event);
    }

    @Override
    protected void onAttachedToWindow(){

        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow(){

        super.onDetachedFromWindow();
    }

    @Override
    public void onHostResume() {
        // Activity `onResume`
        Exception exception = null;
        String message = null;

        Log.e(TAG, "Called onHostResume");

        try {
           switch (ArCoreApk.getInstance().requestInstall(mContextModule.getCurrentActivity(), !installRequested)) {
                case INSTALL_REQUESTED:
                    installRequested = true;
                    Log.e(TAG, "Called installRequested onHostResume");

                    return;
                case INSTALLED:
                    break;
            }
            mSession = new Session(/* context= */ mContextModule);
        } catch (UnavailableArcoreNotInstalledException e) {
            message = "Please install ARCore";
            exception = e;
        } catch (UnavailableApkTooOldException e) {
            message = "Please update ARCore";
            exception = e;
        } catch (UnavailableSdkTooOldException e) {
            message = "Please update this app ARCore";
            exception = e;
        } catch (Exception e) {
            message = "This device does not support ARCore";
            exception = e;
        }
        if (message != null) {
            showSnackbarMessage(message, true);
            //Log.e(TAG, "Exception creating session", exception);
            return;
        }
        // Create default config and check if supported.
        Config config = new Config(mSession);
        if (!mSession.isSupported(config)) {
            showSnackbarMessage("This device does not support AR", true);
        }
        mSession.configure(config);
        hasCameraPermissions();
        try {
            if (mSession != null) {
                showLoadingMessage();
                // Note that order matters - see the note in onPause(), the reverse applies here.
                mSession.resume();
            }
        }catch (CameraNotAvailableException e) {
            // In some cases (such as another camera app launching) the camera may be given to
            // a different app instead. Handle this properly by showing a message and recreate the
            // session at the next iteration.
            showLoadingMessage();
            mSession = null;
            return;
        }
        mSurfaceView.onResume();
        mDisplayRotationHelper.onResume();
    }

    @Override
    public void onHostPause() {
        // Activity `onPause`
        mDisplayRotationHelper.onPause();
        mSurfaceView.onPause();
        if (mSession != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            mSession.pause();
        }
    }

    @Override
    public void onHostDestroy() {
        // Activity `onDestroy`
        mSession = null;
        mDisplayRotationHelper = null;
        mSurfaceView = null;
    }

    public float[] calculateWorld2CameraMatrix(float[] modelmtx, float[] viewmtx, float[] prjmtx) {
        float scaleFactor = 1.0f;
        float[] scaleMatrix = new float[16];
        float[] modelXscale = new float[16];
        float[] viewXmodelXscale = new float[16];
        float[] world2screenMatrix = new float[16];
        Matrix.setIdentityM(scaleMatrix, 0);
        scaleMatrix[0] = scaleFactor;
        scaleMatrix[5] = scaleFactor;
        scaleMatrix[10] = scaleFactor;
        Matrix.multiplyMM(modelXscale, 0, modelmtx, 0, scaleMatrix, 0);
        Matrix.multiplyMM(viewXmodelXscale, 0, viewmtx, 0, modelXscale, 0);
        Matrix.multiplyMM(world2screenMatrix, 0, prjmtx, 0, viewXmodelXscale, 0);
        return world2screenMatrix;
    }

    public double[] world2Screen(int screenWidth, int screenHeight, float[] world2cameraMatrix)
    {
        float[] origin = {0f, 0f, 0f, 1f};
        float[] ndcCoord = new float[4];
        Matrix.multiplyMV(ndcCoord, 0,  world2cameraMatrix, 0,  origin, 0);
        ndcCoord[0] = ndcCoord[0]/ndcCoord[3];
        ndcCoord[1] = ndcCoord[1]/ndcCoord[3];
        double[] pos_2d = new double[]{0,0};
        pos_2d[0] = screenWidth  * ((ndcCoord[0] + 1.0)/2.0);
        pos_2d[1] = screenHeight * (( 1.0 - ndcCoord[1])/2.0);
        return pos_2d;
    }


    private double getDegree(double value1, double value2)
    {
        double firstAngle = value1 * 90;
        double secondAngle = value2 * 90;
        if (secondAngle >= 0 && firstAngle >= 0)
        {
            return firstAngle; // first quadrant
        }
        else if (secondAngle < 0 && firstAngle >= 0)
        {
            return 90 + (90 - firstAngle); //second quadrant
        }
        else if (secondAngle < 0 && firstAngle < 0)
        {
            return 180 - firstAngle; //third quadrant
        }
        else
        {
            return 270 + (90 + firstAngle); //fourth quadrant
        }
    }

    /**
     * Calculates the current rotation and subtracts the original camera orientation angle to get
     * the accurate change in angle by the camera. Ensure values will always be between 0 and
     * 359.999->
     *
     * @return
     */
    private double findCameraAngleFromOrigin()
    {
        double angle = getDegree(mCurrentCameraMatrix[2], mCurrentCameraMatrix[0]) -
                getDegree(mOriginCameraMatrix[2], mOriginCameraMatrix[0]);
        if (angle < 0)
            return angle + 360;
        return angle;
    }


    public RNReactNativeArCoreView(ThemedReactContext context, CoreViewCallback callback) {
        super(context);
        this.callback = callback;
        mContextModule = context;
        mContextModule.addLifecycleEventListener(this);
        //  this.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        mSurfaceView = new GLSurfaceView(context);
        this.addView(mSurfaceView);
        mDisplayRotationHelper = new DisplayRotationHelper(/*context=*/ context);
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
        mSurfaceView.onPause();
        installRequested = false;


        DisplayMetrics displayMetrics = new DisplayMetrics();
        (mContextModule.getCurrentActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        heightOFScreen = displayMetrics.heightPixels;
        weightOFScreen = displayMetrics.widthPixels;
        Log.d("receiver", "Got onHostResume: " + heightOFScreen);
        //onHostResume();
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
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        // Create the texture and pass it to ARCore session to be filled during update().
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



/*
    public void passPlaneHitDetectedData( float[] projectionMatrix, float[] viewMatrix) {
        WritableMap projection =  JsonUtils.createMapFromFloat(projectionMatrix);
        WritableMap viewMap =  JsonUtils.createMapFromFloat(viewMatrix);
        if (firstTimePlaneDetected == false) {
            firstTimePlaneDetected = true;
            for (Anchor anchor : mAnchors) {
                if (anchor.getTrackingState() != TrackingState.TRACKING) {
                    continue;
                }
                xPlaneDetected = anchor.getPose().tx();
                yPlaneDetected = anchor.getPose().ty();
                zPlaneDetected = anchor.getPose().tz();
               // planeDetectedHashCode = anchor.hashCode();
            }
        }
        WritableMap map = new WritableNativeMap();
        map.putBoolean("planeHitDetected", true);
        map.putDouble("x",xPlaneDetected);
        map.putDouble("y",yPlaneDetected);
        map.putDouble("z",zPlaneDetected);
       // map.putDouble("id",planeDetectedHashCode);
        map.putMap("projection",projection);
        map.putMap("viewMap",viewMap);
        if (callback != null) {
            callback.planeHitDetected(map);
        }
    }*/

    public void passPlaneHitDetectedData( float[] projectionMatrix, float[] viewMatrix) {
        WritableMap projection =  JsonUtils.createMapFromFloat(projectionMatrix);
        WritableMap viewMap =  JsonUtils.createMapFromFloat(viewMatrix);
        WritableMap map = new WritableNativeMap();
            for (Anchor anchor : mAnchors) {
                if (anchor.getTrackingState() != TrackingState.TRACKING) {
                    continue;
                }
              /*  if (firstTimePlaneDetected == false) {
                    xPlaneDetected = anchor.getPose().tx();
                    yPlaneDetected = anchor.getPose().ty();
                    zPlaneDetected = anchor.getPose().tz();
                    anchor.getPose().toMatrix(mAnchorMatrix, 0);
                    anchorPose = anchor.getPose();
                    mTranslationX.add(viewMatrix[3]);
                    mTranslationZ.add(viewMatrix[11]);
                    firstTimePlaneDetected = true;
                }*/
                anchor.getPose().toMatrix(mAnchorMatrix, 0);
                if (mTranslationX.size() <= 0)
                {
                    mTranslationX.add(viewMatrix[3]);
                }
                if (mTranslationZ.size() <= 0)
                {
                    mTranslationZ.add(viewMatrix[11]);
                }
                translateMatrix(mTranslationX.get(0), -mTranslationZ.get(0), 0);
                map.putBoolean("planeHitDetected", true);
                map.putDouble("x",mAnchorMatrix[12]);
                map.putDouble("y",mAnchorMatrix[13]);
                map.putDouble("z",mAnchorMatrix[14]);
                //  map.putDouble("id",anchor.hashCode());
                map.putMap("projection",projection);
                map.putMap("viewMap",viewMap);
                if (callback != null) {
                    callback.planeHitDetected(map);
                }
            }
    }

    private void translateMatrix(float xDistance, float yDistance, float zDistance)
    {
        Matrix.translateM(mAnchorMatrix, 0, xDistance, yDistance, zDistance);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mDisplayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

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

                        camera.getDisplayOrientedPose().toMatrix(mOriginCameraMatrix, 0);

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
            camera.getProjectionMatrix(projmatrix, 0, 0.1f, 100.0f);
            // Get camera matrix and draw.
            camera.getViewMatrix(viewmatrix, 0);

            // Compute lighting from average intensity of the image.
            final float lightIntensity = frame.getLightEstimate().getPixelIntensity();
          /*  // Visualize tracked points.
            PointCloud pointCloud = frame.acquirePointCloud();
            mPointCloud.update(pointCloud);
            mPointCloud.draw(viewmtx, projmtx);
            // Application is responsible for releasing the point cloud resources after
            // using it.
            pointCloud.release();*/
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
            camera.getDisplayOrientedPose().toMatrix(mCurrentCameraMatrix, 0);
            // Visualize planes.
            if (mAnchors.size() <= 0) {
                mPlaneRenderer.drawPlanes(mSession.getAllTrackables(Plane.class), camera.getDisplayOrientedPose(), projmatrix);
            }
            // Visualize anchors created by touch.
            float scaleFactor = 1.0f;
            passPlaneHitDetectedData(projmatrix,viewmatrix);
             /*
            for (Anchor anchor : mAnchors) {
                if (anchor.getTrackingState() != TrackingState.TRACKING) {
                    continue;
                }
                // Get the current pose of an Anchor in world space. The Anchor pose is updated
                // during calls to session.update() as ARCore refines its estimate of the world.
                anchor.getPose().toMatrix(mAnchorMatrix, 0);
                // Update and draw the model and its shadow.
                mVirtualObject.updateModelMatrix(mAnchorMatrix, scaleFactor);
                mVirtualObjectShadow.updateModelMatrix(mAnchorMatrix, scaleFactor);
                mVirtualObject.draw(viewmtx, projmtx, lightIntensity);
                mVirtualObjectShadow.draw(viewmtx, projmtx, lightIntensity);
            }*/
        } catch (Throwable t) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t);
        }
    }

    private void showSnackbarMessage(String message, boolean finishOnDismiss) {
    /*    mMessageSnackbar = Snackbar.make(
                this.findViewById(android.R.id.content),
                message, Snackbar.LENGTH_INDEFINITE);
        mMessageSnackbar.getView().setBackgroundColor(0xbf323232);
        if (finishOnDismiss) {
            mMessageSnackbar.setAction(
                    "Dismiss",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mMessageSnackbar.dismiss();
                        }
                    });
            mMessageSnackbar.addCallback(
                    new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                           // finish();
                        }
                    });
        }
        mMessageSnackbar.show();*/
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