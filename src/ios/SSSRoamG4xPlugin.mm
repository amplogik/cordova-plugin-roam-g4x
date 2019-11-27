#import "SSSRoamG4xPlugin.h"
#import <Cordova/CDV.h>

@interface SSSRoamG4xPlugin() {
    id <RUADeviceManager> manager;
    NSString *cardSwipeCallback;
    NSString *connectedCallback;
    NSString *disconnectedCallback;
    NSString *errorCallback;
}
@end

@implementation SSSRoamG4xPlugin

- (void)pluginInitialize {

    NSLog(@"Cordova ROAM G4x Plugin");
    NSLog(@"(c)2016 Salon Suite Solutions");

    [super pluginInitialize];

    manager = [RUA getDeviceManager:RUADeviceTypeG4x];

    BOOL init = [manager initializeDevice:self];
    if (init) {
        NSLog(@"Device Manager initialized");
        //[[manager getConfigurationManager] setCommandTimeout:60];
    }

}

- (void)waitForCardSwipe:(CDVInvokedUrlCommand*)command {

    if (![manager isReady]) {
        NSString *message = @"Card reader is not ready."; // TODO Android says: Card reader is not connnected
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:message];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    id <RUATransactionManager> tmgr = [manager getTransactionManager];

    [tmgr
            waitForMagneticCardSwipe: ^(RUAProgressMessage messageType, NSString* additionalMessage) {
                NSLog(@"%@", @"progress");
            }

            response: ^(RUAResponse *ruaResponse) {
                NSLog(@"%@", @"Received ruaResponse from card swipe");
                NSDictionary *data = [self RUAResponse_toDictionary:ruaResponse];
                CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:data];
                [pluginResult setKeepCallbackAsBool:TRUE];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:cardSwipeCallback];
            }
     ];

    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}

- (void)stopWaitingForCardSwipe:(CDVInvokedUrlCommand*)command {

    id <RUATransactionManager> tmgr = [manager getTransactionManager];
    [tmgr stopWaitingForMagneticCardSwipe];
    // always return success
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}

- (void)checkPermissions:(CDVInvokedUrlCommand*)command {
    
    [[AVAudioSession sharedInstance] requestRecordPermission:^(BOOL granted) {
        
        CDVPluginResult *pluginResult;

        if (granted) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        }
                            
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                            
    }];
    
}

- (void)stop:(CDVInvokedUrlCommand *)command {
    
    id <RUATransactionManager> tmgr = [manager getTransactionManager];
    [tmgr stopWaitingForMagneticCardSwipe];
    
    [manager releaseDevice];
    
    // always return success
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}

- (void)start:(CDVInvokedUrlCommand *)command {
    
    CDVPluginResult *pluginResult;
    manager = [RUA getDeviceManager:RUADeviceTypeG4x];
    BOOL init = [manager initializeDevice:self];
    
    if (init) {
        NSLog(@"Device Manager reinitialized");
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Card Reader failed to reinitialize."];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}

- (void)registerCardSwipeCallback:(CDVInvokedUrlCommand*)command {

    cardSwipeCallback = [command.callbackId copy];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
    [pluginResult setKeepCallbackAsBool: TRUE];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}

- (void)registerConnectedCallback:(CDVInvokedUrlCommand*)command {

    connectedCallback = [command.callbackId copy];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
    [pluginResult setKeepCallbackAsBool: TRUE];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}

- (void)registerDisconnectedCallback:(CDVInvokedUrlCommand*)command {

    disconnectedCallback = [command.callbackId copy];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
    [pluginResult setKeepCallbackAsBool: TRUE];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}

- (void)registerErrorCallback:(CDVInvokedUrlCommand*)command {

    errorCallback = [command.callbackId copy];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
    [pluginResult setKeepCallbackAsBool: TRUE];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}

#pragma mark RUADeviceStatusHandler

- (void)onConnected {

    NSLog(@"onConnected");
    if (connectedCallback) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [pluginResult setKeepCallbackAsBool: TRUE];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:connectedCallback];
    }

}

- (void)onDisconnected {

    NSLog(@"onDisconnected");
    if (disconnectedCallback) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [pluginResult setKeepCallbackAsBool: TRUE];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:disconnectedCallback];
    }

}

- (void)onError:(NSString *)message {

    NSLog(@"onError");
    [self sendError:message];

}

// onPlugged, onDetectionStarted, onDetectionStopped are optional

- (void)onPlugged {
    NSLog(@"onPlugged");
}

- (void)onDetectionStarted {
    NSLog(@"onDetectionStarted");
}

- (void)onDetectionStopped {
    NSLog(@"onDetectionStopped");
}


# pragma mark util

- (void)sendError:(NSString *)message {

    if (errorCallback) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: message];
        [pluginResult setKeepCallbackAsBool: TRUE];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:errorCallback];
    } else {
        NSLog(@"errorCallback is missing %@", message);
    }

}

- (NSDictionary *)RUAResponse_toDictionary:(RUAResponse *)response {

    NSMutableDictionary *data = [NSMutableDictionary dictionary];
    NSDictionary *responseData = [response responseData];

    [data setObject: [RUAEnumerationHelper RUACommand_toString:[response command]]
             forKey: [RUAEnumerationHelper RUAParameter_toString:RUAParameterCommand]];

    [data setObject: [RUAEnumerationHelper RUAResponseCode_toString:[response responseCode]]
             forKey: [RUAEnumerationHelper RUAParameter_toString:RUAParameterResponseCode]];

    [data setObject: [RUAEnumerationHelper RUAResponseType_toString:[response responseType]]
             forKey: [RUAEnumerationHelper RUAParameter_toString:RUAParameterResponseType]];

    if ([response responseCode] == RUAResponseCodeError) {

        [data setObject: [RUAEnumerationHelper RUAErrorCode_toString:[response errorCode]]
                 forKey: [RUAEnumerationHelper RUAParameter_toString:RUAParameterErrorCode]];

        if ([response additionalErrorDetails] != nil) {

            [data setObject: [response additionalErrorDetails]
                     forKey: [RUAEnumerationHelper RUAParameter_toString:RUAParameterErrorDetails]];
        }
    }

    if (responseData != nil) {
        NSArray *keyArray =  [[response responseData] allKeys];
        long count = [keyArray count];
        RUAParameter parameter;
        for (int i = 0; i < count; i++) {
            parameter = (RUAParameter)[[keyArray objectAtIndex:i] intValue];
            NSString *value = [responseData objectForKey:[keyArray objectAtIndex:i]];
            NSString *key = [RUAEnumerationHelper RUAParameter_toString:parameter];
            [data setObject: value forKey: key];
        }
    }

    return data;
}

@end
