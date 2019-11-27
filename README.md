# ROAM G4x Plugin for Apache Cordova

This plugin supports Ingenico ROAM G4x credit card scanner. It provides a simple JavaScript API for scanning credit cards on iOS and Android.

## Supported Platforms

* iOS
* Android (4.3 or greater)

# Installing

### Cordova

    $ cordova plugin add https://github.com/salonsuitesolutions/cordova-plugin-roam-g4x

### PhoneGap

    $ phonegap plugin add https://github.com/salonsuitesolutions/cordova-plugin-roam-g4x

### PhoneGap Build

Edit config.xml to install the plugin for [PhoneGap Build](http://build.phonegap.com).

    <gap:plugin name="com.salonsuitesolutions.roam.g4x" source="pgb" />
    <preference name="phonegap-version" value="cli-6.1.0" />

### iOS 10

For iOS 10, apps will crash unless they include usage description keys for the types of data they access. For this plugin, NSMicrophoneUsageDescription must be defined.

This can be done when the plugin is installed using the MICROPHONE_USAGE_DESCRIPTION variable.

    $ cordova plugin add https://github.com/salonsuitesolutions/cordova-plugin-roam-g4x --variable MICROPHONE_USAGE_DESCRIPTION="Your description here"

# API Reference

## Functions

- [roam.waitForCardSwipe](#waitforcardswipe)
- [roam.stopWaitingForCardSwipe](#stopwaitingforcardswipe)
- [roam.onCardSwipeDetected](#oncardswipedetected)
- [roam.onConnected](#onconnected)
- [roam.onDisconnected](#ondisconnected)
- [roam.onError](#onerror)
- [roam.isConnected](#isconnected)
- [roam.checkPermissions](#checkpermissions)
- [roam.restart](#restart)
- [roam.start](#stop)
- [roam.stop](#start)

### waitForCardSwipe

Function `waitForCardSwipe` must be called before the user swipes a card.

The success callback is called one time if the reader is initialized and is ready to scan.

The failure callback is called if the reader is not available.

When a card is scanned the results are send to [roam.onCardSwipeDetected](#oncardswipedetected).

    roam.waitForCardSwipe(
        function() {
            // success update the UI
        },
        function(message) {
            console.log(message);
            navigator.notification.alert(message, {}, 'Error');
        }
    );

### stopWaitingForCardSwipe

Function `stopWaitingForCardSwipe` cancels the request initialized by `waitForCardSwipe`. This function only needs to be called to cancel. The plugin will stop waiting after a card is scanned.

This functional always calls the success callback.

### onConnected

Function `onConnected` is called when the credit card reader is connected to the phone. If the card reader is plugged in when the app launches, `onConnected` will be called.

    roam.onConnected = function(){
        // update UI to indicate reader is available
    }

### onDisconnected

Function `onDisconnected` is called when the credit card reader is removed from the phone.

    roam.onDisconnected = function() {
        // update UI to indicate reader is NOT available
    }

### onError

Function `onError` is called when the there is a plugin error.

If the user has denied the record audio permissions, this function will be called during plugin initialization with the message "Record audio permission denied".

This callback is optional. Your application does *not* need to implement this function.

    roam.onError = function(message) {
        // update UI to indicating the error
    }

### isConnected

Function `isConnected` is used to determine if the card reader is connected. It returns true when the reader is connected and false when the reader is not connected.

    if (roam.isConnected()) {
        message = "The card reader is connected."
    } else {
        message = "The card reader is NOT connected."
    }
    console.log(message);

### checkPermissions

Function `checkPermissions` is used to determine if the user granted permission for the app to record audio. The success callback is called when the permission is granted. The failure callback is called when the permissions is denied.

The roam libraries require the audio recording permission to interact with the reader hardware.

iOS users will be asked one time to grant audio permission. The system remembers the users choice. If the user has denied recording permission, they can reenable it in Settings > Privacy > Microphone.

Android 6 users will be prompted to grant audio recording permission. If the user denies the permission, they will be asked again each time unless they check "Never ask again." The user can re-enable permissions in Settings > Apps > *App Name* > Permissions > Microphone.

The record audio permission is always enabled for Android 4 and 5 users.

The plugin will check permissions when it starts. If permissions are denied it will call [onError](#onerror) with "Record audio permission denied". 

        roam.checkPermissions(
            function() {
                console.log("Record audio permission granted");
            },
            function() {
                console.log("Record audio permission denied");
            }
        );

### onCardSwipeDetected

Function `onCardSwipeDetected` is called when the reader detects a card swipe. A JSON object is passed to the registered function.

    roam.onCardSwipeDetected = function(cardDataAsJson)) {
        // update the UI with some card data
        // send encrypted card data to the server
    }

Ideally the callback receives a successful card scan. Ensure that Track1Status and Track2Status are both "0" before continuing. The read may fail for one track and work for the other. If there is an error, the JSON will contain the error message.

This plugin is wrapping the native libraries for the Ingenico ROAM reader, so the ROAM documentation will be the most definitive source on how to interpret the scan data.

### restart

Function `restart` is used to programatically restart ROAM reader. It should behave similar to the user unplugging the reader and then pluging the reader back in. **Normally this should not be needed.** The function has been added as a work around for card readers that aren't behaving.

The success callback is called one time if the reader is initialized and is ready to scan.

The failure callback is called if the reader can not be restarted.

NOTE that the [onDisconnected](#ondisconnected) and [onConnected](#onconnected) callbacks are called while the device is restarting.

    roam.restart(
        function() {
            console.log('Card reader restarted.');
            // success update the UI
        },
        function(message) {
            console.log(message);
            navigator.notification.alert(message, {}, 'Error');
        }
    );

### stop

Function `stop` is used to programatically restart ROAM reader.  The [start](#start) function must be called before scanning again. 

The success callback is called one time when the reader is disabled.

The [onDisconnected](#ondisconnected) callback is also be called when the reader stops.

*You probably don't want to call this function.* Try using [restart](#restart) instead.

### start

Function `start` is used to programatically start ROAM reader.  This function only needs to be called after calling the [stop](#stop) function. 

The success callback is called one time if the reader is initialized and is ready to scan.

The failure callback is called if the reader can not be restarted.

The [onConnected](#onconnected) callback is called when reader starts.

*You probably don't want to call this function.* Try using [restart](#restart) instead.

## Examples Scans

Successful Card Read

    {
        "CardExpDate": "1809",
        "CardHolderName": "COBB/JAYNE ",
        "Command": "WaitForMagneticCardSwipe",
        "PackedEncryptedTrack": "$32$zYHtvFD1oOQPdjFCuPgg5c5BFS9q8d97OOKvhfpVASCFkzGlnMM3wZg/Bm4ILAGKKe1vM99T4JmEfuL6ZTwlMp246UERBJIQAAAAAAAAAACt2F+TzUkEctC12onzm7NDYfDFG/RsSOvF1vLFOOSxa6x0cjfzWusp",
        "FormatID": "32",
        "KSN": "77760841400138E0005B",
        "PAN": "4492XXXXXXXX8893",
        "ResponseCode": "Success",
        "EncryptedTrack": "cd81edbc50f5a0e40f763142b8f820e5ce411b006af1df7b38e2af85fa550120859331a59d65b7c1983f066e082c018d1ded6f33df53e099847ee2fa653c25329db8e941110492100000000000000000add85f93cd490472d0b5da89f39bb34361f0c51bf46c48ebc5d6f00900f4b16bac747237f35aeb29",
        "Track1Status": "0",
        "Track2Status": "0"
    }

Track 1 Error

    {
        "CardExpDate": "1809",
        "CardHolderName": "",
        "Command": "WaitForMagneticCardSwipe",
        "PackedEncryptedTrack": "$32$AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAgGqiwa6R6DcTBNck9DWRwUWY98dM7w10pnqcyyiOQgX52p/7GpgUC",
        "FormatID": "32",
        "KSN": "77760841400138E0005A",
        "PAN": "4492XXXXXXXX8893",
        "ResponseCode": "Success",
        "EncryptedTrack": "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000201aa8b06ba47a0dc4c135c93d0d647053d616f1d33bc35d299ea732ca2089317e76a7fec6a60502",
        "Track1Status": "1",
        "Track2Status": "0"
    }

Card Read Error

    {
        "Command": "WaitForMagneticCardSwipe",
        "ErrorCode": "ReaderInterrupted",
        "ResponseCode": "Error"
    }

iOS will time out if the card swipe takes too long

    {
        "Command": "WaitForMagneticCardSwipe",
        "ResponseType": "Unknown",
        "ErrorCode": "TimeoutExpired",
        "ResponseCode": "Error"
    }
