# Android ARCore in react native



# react-native-arcore

## Getting started

`$ npm install react-native-arcore --save`

### Mostly automatic installation

`$ react-native link react-native-arcore`

### Manual installation


#### Android

1. Open up `android  node_modules/react-native/android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNReactNativeArcorePackage;` to the imports at the top of the file
  - Add `new RNReactNativeArcorePackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-arcore'
  	project(':react-native-arcore').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-arcore/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-arcore')
  	```
4. Copy the  Folder Google from /libraries/m2repository/com  into the   node_modules/react-native/android/com/

5. Copy the  Folder include from /libraries/include  into the   node_modules/react-native/android/

6. Add following code in Mainactivity in your application to request for the Camera 

```java

 @Override
    protected void onResume() {
        super.onResume();
        // ARCore requires camera permissions to operate. If we did not yet obtain runtime
        // permission on Android M and above, now is a good time to ask the user for it.
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
               CameraPermissionHelper.requestCameraPermission(this);
        }
    }

  @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this,"Camera permission is needed to run this application", Toast.LENGTH_LONG).show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }
   
```

## Usage

```javascript
import ARCORE from 'react-native-arcore';

// TODO: What to do with the module?
 render() {
       return (
        <View style={styles.container}>
        <ARCORE
          style={{ flex: 1 , width: '100%'  , height: '100%' }}
          onPlaneHitDetected={this._onPlanTappedevent.bind(this)}
          onPlaneDetected={this._onPlaneDetectedEvent.bind(this)}
          >
        </ARCORE>
        
        </View>
       );
    }

```
  
