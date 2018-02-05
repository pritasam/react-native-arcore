
package com.reactlibrary;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.ViewGroup;
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

    public RNReactNativeArcoreMainView(ThemedReactContext context, Activity activity)
    {
        super(context);
        Log.d("React:", "RNReactNativeArcoreMainView(Contructtor)");
      //  mOriginalOrientation = activity.getRequestedOrientation();
        mActivity = activity;
        this.setOrientation(LinearLayout.VERTICAL);
        // add the buttons and signature views
        this.setBackgroundColor(Color.BLUE);
        this.arCoreView = new RNReactNativeArCoreView(context,this);
        this.addView(arCoreView);
        setLayoutParams(new android.view.ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
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
