# Android ARCore in react native



# react-native-arcore

## Getting started

`$ npm install react-native-arcore --save`

### Mostly automatic installation

`$ react-native link react-native-arcore`

### Manual installation


#### Android

1. Open up `android /app/src/main/java/[...]/MainActivity.java`
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
    
## Usage



In Android Manifest file


    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
    <uses-permission android:name="android.permission.CAMERA" />

Add above permissions 
     
     
    <application
        <meta-data android:name="com.google.ar.core" android:value="optional" />
    </application>
   
Add above metadata under the application tag


```javascript
import ARCORE from 'react-native-arcore';



 async requestCameraPermission() {
    try {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.CAMERA,
        {
          'title': 'Cool Photo App Camera Permission',
          'message': 'Cool Photo App needs access to your camera ' +
                     'so you can take awesome pictures.',
        },
      );
      if (granted === PermissionsAndroid.RESULTS.GRANTED) {
      } else {
        console.log(currentConsoleTag, "Camera permission denied");
      }
    } catch (err) {
      console.log(currentConsoleTag, "METHOD: requestCameraPermission", err);
    }
  }
  
  // Also request the Camera permission before showing the ARCORE 

  _onPlaneDetectedEvent(result) {
    console.log(result);
  }

  _onPlanTappedevent(result) {
    console.log(result);
  }
  
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
  
