
package com.reactlibrary;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

public class RNReactNativeArcoreMainView extends LinearLayout implements RNReactNativeArCoreView.CoreViewCallback {

    RNReactNativeArCoreView arCoreView;
    Activity mActivity;
    int mOriginalOrientation;
    String viewMode = "portrait";
    LinearLayout buttonsLayout;

    public void show()
    {
        Log.d("React:", "show method called");
    }

    public RNReactNativeArcoreMainView(Context context)
    {
        super(context);
        Log.d("React:", "RNReactNativeArcoreMainView single");
        this.setBackgroundColor(Color.BLUE);
        //this.buttonsLayout = this.buttonsLayout();
        //this.addView(buttonsLayout);
         this.arCoreView = new RNReactNativeArCoreView(this.getContext(),this);
         this.addView(arCoreView);
         setLayoutParams(new android.view.ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                 ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public RNReactNativeArcoreMainView(ThemedReactContext context, Activity activity)
    {
        super(context);
        Log.d("React:", "RNReactNativeArcoreMainView(Contructtor)");
      //  mOriginalOrientation = activity.getRequestedOrientation();
     //   mActivity = activity;
      //  this.setOrientation(LinearLayout.VERTICAL);
        // add the buttons and signature views
        this.setBackgroundColor(Color.BLUE);
         //this.buttonsLayout = this.buttonsLayout();
        this.arCoreView = new RNReactNativeArCoreView(this.getContext(),this);
        this.addView(arCoreView);
        setLayoutParams(new android.view.ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }


    private void layoutViewFinder(int left, int top, int right, int bottom) {
        if (null == arCoreView) {
            return;
        }
       // this.buttonsLayout.layout(left, top, right , bottom);
        float width = right - left;
        float height = bottom - top;
        int viewfinderWidth;
        int viewfinderHeight;
        viewfinderWidth = (int) width;
        viewfinderHeight = (int) height;
        int viewFinderPaddingX = (int) ((width - viewfinderWidth) / 2);
        int viewFinderPaddingY = (int) ((height - viewfinderHeight) / 2);
        this.arCoreView.layout(viewFinderPaddingX, viewFinderPaddingY, viewFinderPaddingX + viewfinderWidth, viewFinderPaddingY + viewfinderHeight);
        this.postInvalidate(this.getLeft(), this.getTop(), this.getRight(), this.getBottom());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
       this.layoutViewFinder( l,t,r,b);
    }

    @Override
    public void onViewAdded(View child) {
        if (this.arCoreView == child) return;
        Log.d("React:", "RNReactNativeArcoreMainVie");
        // remove and readd view to make sure it is in the back.
        // @TODO figure out why there was a z order issue in the first place and fix accordingly.
        this.removeView(this.arCoreView);
        this.addView(this.arCoreView,0);
    }



    private LinearLayout buttonsLayout() {

        // create the UI programatically
        LinearLayout linearLayout = new LinearLayout(this.getContext());
        Button saveBtn = new Button(this.getContext());
        Button clearBtn = new Button(this.getContext());


        // set texts, tags and OnClickListener
        saveBtn.setText("Save");
        saveBtn.setTag("Save");

        clearBtn.setText("Reset");
        clearBtn.setTag("Reset");

        linearLayout.addView(saveBtn);
        linearLayout.addView(clearBtn);

        // return the whoe layout
        return linearLayout;
    }

    public void setViewMode(String viewMode) {
        this.viewMode = viewMode;

        if (viewMode.equalsIgnoreCase("portrait")) {
          //  mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (viewMode.equalsIgnoreCase("landscape")) {
           // mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    public void planeDetected(WritableMap event) {
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "topChange", event);
    }

    @Override
    public void planeHitDetected(WritableMap event) {
        ReactContext reactContext = (ReactContext) getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "topChange", event);
    }


}
