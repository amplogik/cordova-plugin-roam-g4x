//
//  CommunicationManagerBase.h
//  BLEBaseDriver
//
//  Created by Landi 联迪 - Robert on 13-8-27.
//  Copyright (c) 2013年 Landi 联迪. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DeviceSearchListener.h"
#import "CommunicationCallBack.h"
#import "CommDownloadCallback.h"
#import "RDeviceInfo.h"

@class TMSDownloadCtrl;

typedef enum _enumDeviceCommunicationMode{
    MASTERSLAVE,
    DUPLEX,
}DeviceCommunicationMode;

typedef enum{
    LD_PlatformFilterType_COMMON,  // Platfrom || subPlatform || FileType
    LD_PlatformFilterType_M3X,     // SubPlatform || HardwareConfig
}LD_PlatformFilterType;

@interface LDPlatformFilter : NSObject<NSCopying>

-(instancetype)initForCommon:(NSString*)platform subPlatform:(NSString*)subPlatform fileType:(NSString*)fileType;
-(instancetype)initForM3X:(NSString*)subPlatform hardwareConfig:(NSString*)hardwareConfig;

@property (assign, nonatomic) LD_PlatformFilterType FilterType;
@property (strong, nonatomic) NSString* Platform;
@property (strong, nonatomic) NSString* SubPlatform;
@property (strong, nonatomic) NSString* FileType;
@property (strong, nonatomic) NSString* HardwareConfig;

@end

@interface CommunicationManagerBase : NSObject

+(CommunicationManagerBase*)sharedInstance:(DeviceCommunicationChannel)channel;
+(int)searchDevices:(id<DeviceSearchListener>)dsl detectAudioDevice:(BOOL)detectAudioDevcie detectBluetooth:(BOOL)detectBluetoothDevice timeout:(long)timeout;
+(void)stopSearching;
+(NSString*)getLibVersion;
+(void)switchLog:(BOOL)bOpen;
-(int)openDevice:(NSString*)identifier;
-(int)openDevice:(NSString *)identifier cb:(id<CommunicationCallBack>) cb mode:(DeviceCommunicationMode)mode;
-(int)openDevice:(NSString *)identifier timeout:(long)timeout;
-(int)openDevice:(NSString *)identifier cb:(id<CommunicationCallBack>)cb mode:(DeviceCommunicationMode)mode timeout:(long)timeout;
-(int)exchangeData:(NSData*)data timeout:(long)timeout cb:(id<CommunicationCallBack>) cb;
-(int)exchangeData:(NSData *)data timeout:(long)timeout;
-(int)cancelExchange;
-(void)closeDevice;
-(void)closeResource;
-(BOOL)isConnected;
+(void)download:(RDeviceInfo*)di path:(NSString*)filePath callback:(id<CommDownloadCallback>)cb;
+(void)download:(RDeviceInfo*)di path:(NSString*)filePath filter:(LDPlatformFilter*)pf callback:(id<CommDownloadCallback>)cb;
//+(void)TMSDownload:(RDeviceInfo*)di path:(NSString*)filePath callback:(id<CommDownloadCallback>)cb;
+(TMSDownloadCtrl*)getTMSDownloadCtrl;

@end
