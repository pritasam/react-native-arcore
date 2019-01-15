
package com.reactlibrary;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class RNReactNativeArcorePackage implements ReactPackage {

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {

        List<NativeModule> modules = new ArrayList<>();
        modules.add(new RNReactNativeArcoreModule(reactContext));
        return modules;

        //  return Arrays.<NativeModule>asList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
         return Arrays.<ViewManager>asList(new RNReactNativeArcoreViewManager(reactContext));

    }

    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Arrays.asList();
    }
}