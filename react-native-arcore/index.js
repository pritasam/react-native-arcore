
'use strict';

var ReactNative = require('react-native');
var React = require('react');
var PropTypes = require('prop-types');
var {
    requireNativeComponent,
    View,
    UIManager,
    DeviceEventEmitter,
    Text,
} = ReactNative;

class ARCORE extends React.Component {

    constructor() {
        super();
        this.onChange = this.onChange.bind(this);
        this.subscriptions = [];
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

    componentDidMount() {

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
        //    /*
            <RNReactNativeArcoreModule {...this.props} onChange={this.onChange}
            >
            </ RNReactNativeArcoreModule>
         //   */

                     //         <Text style={{color: 'red'}}>  and red  </Text>
        );
    }
}

ARCORE.propTypes = {
  ...View.propTypes,
   viewMode: PropTypes.string
};


var RNReactNativeArcoreModule = requireNativeComponent('RNReactNativeArcoreModule', ARCORE, {
    nativeOnly: { onChange: true }
});

module.exports = ARCORE;