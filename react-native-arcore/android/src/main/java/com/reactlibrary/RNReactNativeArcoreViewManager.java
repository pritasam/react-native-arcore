
package com.reactlibrary;
import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import javax.annotation.Nullable;

public class RNReactNativeArcoreViewManager extends ViewGroupManager<RNReactNativeArcoreMainView> {

    public static final String PROPS_VIEW_MODE = "viewMode";


    private RNReactNativeArcoreModule mContextModule;


    public RNReactNativeArcoreViewManager(ReactApplicationContext reactContext) {
        mContextModule = new RNReactNativeArcoreModule(reactContext);
    }

    @Override
    public String getName() {
        return "RNArcoreView";
    }

    @ReactProp(name = PROPS_VIEW_MODE)
    public void setViewMode(RNReactNativeArcoreMainView view, @Nullable String viewMode) {
        if (view != null) {
            // view.setViewMode(viewMode);
        }
    }


    @Override
    protected RNReactNativeArcoreMainView createViewInstance(ThemedReactContext reactContext) {
        return new RNReactNativeArcoreMainView(reactContext, mContextModule.getActivity());
    }

    @Override
    public void receiveCommand(
            RNReactNativeArcoreMainView view,
            int commandType,
            @Nullable ReadableArray args) {
        Assertions.assertNotNull(view);
        Assertions.assertNotNull(args);
    }
}