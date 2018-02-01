
package com.reactlibrary;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class RNReactNativeArcoreMainView extends LinearLayout {

    RNReactNativeArCoreView arCoreView;
    Activity mActivity;
    int mOriginalOrientation;
    String viewMode = "portrait";

    public RNReactNativeArcoreMainView(Context context, Activity activity)
    {
        super(context);
        mOriginalOrientation = activity.getRequestedOrientation();
        mActivity = activity;

        this.setOrientation(LinearLayout.VERTICAL);
        this.arCoreView = new RNReactNativeArCoreView(context);
        // add the buttons and signature views
        this.addView(arCoreView);
        setLayoutParams(new android.view.ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setViewMode(String viewMode) {
        this.viewMode = viewMode;

        if (viewMode.equalsIgnoreCase("portrait")) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (viewMode.equalsIgnoreCase("landscape")) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

}