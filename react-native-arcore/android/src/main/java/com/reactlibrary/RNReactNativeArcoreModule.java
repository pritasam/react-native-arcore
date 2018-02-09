
package com.reactlibrary;

import android.app.Activity;
import android.content.Intent;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.uimanager.NativeViewHierarchyManager;
import com.facebook.react.uimanager.UIBlock;
import com.facebook.react.uimanager.UIManagerModule;

public class RNReactNativeArcoreModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public RNReactNativeArcoreModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;

  }
  private Promise mPickerPromise;

  private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {

    }
  };

  @ReactMethod
  public void show() {

  }

  @ReactMethod
  public void takePicture(final int viewTag, final Promise promise) {
    final ReactApplicationContext context = getReactApplicationContext();
    UIManagerModule uiManager = context.getNativeModule(UIManagerModule.class);
    uiManager.addUIBlock(new UIBlock() {
      @Override
      public void execute(NativeViewHierarchyManager nativeViewHierarchyManager) {
        RNReactNativeArcoreMainView coremainView = (RNReactNativeArcoreMainView) nativeViewHierarchyManager.resolveView(viewTag);
        try {
          //coremainView.resumeRendering();
        } catch (Exception e) {
          promise.reject("E_CAMERA_BAD_VIEWTAG", "takePictureAsync: Expected a Camera component");
        }
      }
    });
  }


  @Override
  public String getName() {
    return "RNReactNativeArcore";
  }

  public Activity getActivity() {
    return this.getCurrentActivity();
  }
}