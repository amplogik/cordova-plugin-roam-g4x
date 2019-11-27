#import <Cordova/CDVPlugin.h>

#import <MediaPlayer/MediaPlayer.h>
#import <RUA/RUAEnumerationHelper.h>
#import <RUA/RUADeviceManager.h>
#import <RUA/RUADevice.h>
#import <RUA/RUA.h>
#import <RUA/RUADeviceStatusHandler.h>
#import <RUA/RUADeviceResponseHandler.h>
#import <RUA/RUATransactionManager.h>

#import <AVFoundation/AVFoundation.h>


@interface SSSRoamG4xPlugin : CDVPlugin <RUADeviceStatusHandler>

- (void)waitForCardSwipe:(CDVInvokedUrlCommand*)command;
- (void)stopWaitingForCardSwipe:(CDVInvokedUrlCommand*)command;
- (void)checkPermissions:(CDVInvokedUrlCommand*)command;

- (void)stop:(CDVInvokedUrlCommand*)command;
- (void)start:(CDVInvokedUrlCommand*)command;

- (void)registerCardSwipeCallback:(CDVInvokedUrlCommand*)command;
- (void)registerConnectedCallback:(CDVInvokedUrlCommand*)command;
- (void)registerDisconnectedCallback:(CDVInvokedUrlCommand*)command;
- (void)registerErrorCallback:(CDVInvokedUrlCommand*)command;

@end
