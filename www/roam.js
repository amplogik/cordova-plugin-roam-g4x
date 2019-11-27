/*global cordova*/

var connected = false;

// Functions to wire native callbacks to JavaScript
var connectedCallback = function() {
    connected = true;
    if (module.exports.onConnected) {
        module.exports.onConnected();
    } else {
        console.log('onConnected');
    }
};

var disconnectedCallback = function() {
    connected = false;
    if (module.exports.onDisconnected) {
        module.exports.onDisconnected();
    } else {
        console.log('onDisconnected');
    }
};

var errorCallback = function(error) {
    if (module.exports.onError) {
        module.exports.onError(error);
    } else {
        console.log('onError ' + error);
    }
};

var cardSwipeDetectedCallback = function(json) {
    if (module.exports.onCardSwipeDetected) {
        module.exports.onCardSwipeDetected(json);
    } else {
        console.log('onCardSwipeDetected ' + JSON.stringify(data, null, 4));
    }
};

// Error callback generator for register functions
var _failure = function(name) {
    return function() {
        console.log("Failed to add " + name);
    };
};

// Code to wire Cordova callbacks to JavaScript callbacks
cordova.exec(
    connectedCallback,
    _failure('registerConnectedCallback'),
    'Roam', 'registerConnectedCallback', []);

cordova.exec(
    disconnectedCallback,
    _failure('registerDisconnectedCallback'),
    'Roam', 'registerDisconnectedCallback', []);

cordova.exec(
    errorCallback,
    _failure('registerErrorCallback'),
    'Roam', 'registerErrorCallback', []);

cordova.exec(
    cardSwipeDetectedCallback,
    _failure('registerCardSwipeCallback'),
    'Roam', 'registerCardSwipeCallback', []
);

// check permissions is required for Android to initialize the plugin
// also calling on iOS for consistent behavior when permission denied
cordova.exec(
    function() { console.log("Plugin initialized"); },
    function() { errorCallback("Record audio permission denied"); },
    "Roam", "checkPermissions", []
);

module.exports = {

    waitForCardSwipe: function (success, failure) {
        cordova.exec(success, failure, "Roam", "waitForCardSwipe", []);
    },

    stopWaitingForCardSwipe: function (success, failure) {
        cordova.exec(success, failure, "Roam", "stopWaitingForCardSwipe", []);
    },

    checkPermissions: function (success, failure) {
        cordova.exec(success, failure, "Roam", "checkPermissions", []);
    },

    isConnected: function() {
        return connected;
    },

    // device management functions - stop, start, and restart 
    // these are hacks to work around scan errors

    stop: function (success, failure) {
        cordova.exec(success, failure, "Roam", "stop", []);
    }, 

    // only call start after calling stop
    start: function (success, failure) {
        cordova.exec(success, failure, "Roam", "start", []);
    },

    restart: function (success, failure) {

        // setTimeout on start is to avoid "Connected Device is not G4x" on Android    
        roam.stop(
            function() {
                setTimeout(roam.start, 300, success, failure);
            },
            failure
        );
    },

    // end device management functions

    // users should override these functions
    onCardSwipeDetected: function(data) {},
    onConnected: function() {},
    onDisconnected: function() {},
    onError: function(error) {}

};
