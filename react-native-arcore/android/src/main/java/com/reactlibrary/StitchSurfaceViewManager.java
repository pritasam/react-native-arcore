package com.reactlibrary;

import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;

public class StitchSurfaceViewManager extends ViewGroupManager<TestSurface> {
    private static final String REACT_CLASS="RCTStitchSurface";
    /**
     * Subclasses should return a new View instance of the proper type.
     *
     * @param reactContext
     */
    @Override
    protected TestSurface createViewInstance(ThemedReactContext reactContext) {
        return new TestSurface(reactContext);
    }

    /**
     * @return the name of this view manager. This will be the name used to reference this view
     * manager from JavaScript in createReactNativeComponentClass.
     */
    @Override
    public String getName() {
        return REACT_CLASS;
    }
}