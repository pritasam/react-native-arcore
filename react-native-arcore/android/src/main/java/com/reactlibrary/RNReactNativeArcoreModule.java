
package com.reactlibrary;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.google.ar.core.ArCoreApk;

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
  public void checkIfDeviceSupportAR(Promise promise) {
    ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(reactContext);
    if (availability.isTransient()) {
      // Re-query at 5Hz while compatibility is checked in the background.
      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {

         // checkIfDeviceSupportAR(promise);
        }
      }, 200);
    }
      if (availability != ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE) {
          promise.resolve(true);
      }
      else {
          promise.resolve(false);
      }
    /*  if (availability.isSupported()) {
          promise.resolve("Pritam ");
          // indicator on the button.
      } else { // Unsupported or unknown.
          promise.resolve("NON supported Pritam ");
      }*/
  }


  @Override
  public String getName() {
    return "RNReactNativeArcoreModule";
  }


  public Activity getActivity() {
    return this.getCurrentActivity();
  }
}