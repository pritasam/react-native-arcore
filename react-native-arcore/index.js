
'use strict';

var ReactNative = require('react-native');
var React = require('react');
var PropTypes = require('prop-types');
var {
    requireNativeComponent,
    View,
    UIManager,
    DeviceEventEmitter
} = ReactNative;

class ARCORE extends React.Component {

    constructor() {
        super();
        this.onChange = this.onChange.bind(this);
    }

    onChange(event) {
       
    }

    componentDidMount() {
    }

    componentWillUnmount() {
    }

    render() {
        return (
            <RNArcoreView {...this.props} onChange={this.onChange}/>
        );
    }
}

ARCORE.propTypes = {
  ...View.propTypes,
   viewMode: PropTypes.string
};

var RNArcoreView = requireNativeComponent('RNArcoreView', ARCORE, {
    nativeOnly: { onChange: true }
});

module.exports = ARCORE;