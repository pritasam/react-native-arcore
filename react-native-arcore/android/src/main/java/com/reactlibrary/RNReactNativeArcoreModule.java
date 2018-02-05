
package com.reactlibrary;

import android.app.Activity;
import android.content.Intent;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

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



  @Override
  public String getName() {
    return "RNReactNativeArcore";
  }

  public Activity getActivity() {
    return this.getCurrentActivity();
  }
}