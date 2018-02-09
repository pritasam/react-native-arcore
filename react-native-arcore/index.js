
'use strict';

var ReactNative = require('react-native');
var React = require('react');
var PropTypes = require('prop-types');
var {
    findNodeHandle, 
    Platform, 
    NativeModules, 
    ViewPropTypes, 
    requireNativeComponent, 
    View,
    UIManager,
    DeviceEventEmitter,
    NativeAppEventEmitter,
    Text,
} = ReactNative;


const ArCoreManager = NativeModules.RNReactNativeArcore;

class ARCORE extends React.Component {

    constructor() {
        super();
        this.onChange = this.onChange.bind(this);
        this.subscriptions = [];
        ArCoreManager.show();
    }

    onChange(event) {

        if(event.nativeEvent.planeDetected){
            if (!this.props.onPlaneDetected) {
                return;
            }
            this.props.onPlaneDetected({
                planeDetected: event.nativeEvent
            });
        }


        if(event.nativeEvent.planeHitDetected){
            if (!this.props.onPlaneHitDetected) {
                return;
            }
            this.props.onPlaneHitDetected({
                onPlaneHitDetected: event.nativeEvent
            });
        }

    }

    _getHandle() {
        return findNodeHandle(this.map);
    }

    componentDidMount() {

        console.log("changes");
        if (this.props.onPlaneDetected) {
            let sub = DeviceEventEmitter.addListener(
                'onPlaneDetected',
                this.props.onPlaneDetected
            );
            this.subscriptions.push(sub);
        }

        if (this.props.onPlaneHitDetected) {
            let sub = DeviceEventEmitter.addListener(
                'onPlaneHitDetected',
                this.props.onPlaneHitDetected
            );
            this.subscriptions.push(sub);
        }
    }

    componentWillUnmount() {
        this.subscriptions.forEach(sub => sub.remove());
        this.subscriptions = [];
    }

    render() {
        return (
            <RNArcoreView {...this.props}
             onChange={this.onChange}
               ref={ref => { this.map = ref; }}
             />
        );
    }
}

ARCORE.propTypes = {
  ...View.propTypes,
   viewMode: PropTypes.string
};

var RNArcoreView = requireNativeComponent('RNArcoreView', ARCORE, {
    nativeOnly: { onChange: true,
    nativeBackgroundAndroid: true,
    nativeForegroundAndroid: true }
});

module.exports = ARCORE;