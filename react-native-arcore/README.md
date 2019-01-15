
# react-native-react-native-arcore

## Getting started

`$ npm install react-native-react-native-arcore --save`

### Mostly automatic installation

`$ react-native link react-native-react-native-arcore`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-react-native-arcore` and add `RNReactNativeArcore.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNReactNativeArcore.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNReactNativeArcorePackage;` to the imports at the top of the file
  - Add `new RNReactNativeArcorePackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-react-native-arcore'
  	project(':react-native-react-native-arcore').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-react-native-arcore/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-react-native-arcore')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNReactNativeArcore.sln` in `node_modules/react-native-react-native-arcore/windows/RNReactNativeArcore.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using React.Native.Arcore.RNReactNativeArcore;` to the usings at the top of the file
  - Add `new RNReactNativeArcorePackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNReactNativeArcore from 'react-native-react-native-arcore';

// TODO: What to do with the module?
RNReactNativeArcore;
```
  