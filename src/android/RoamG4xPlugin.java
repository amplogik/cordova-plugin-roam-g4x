package com.salonsuitesolutions.roam;

import android.Manifest;
import android.content.pm.PackageManager;

import com.roam.roamreaderunifiedapi.DeviceManager;
import com.roam.roamreaderunifiedapi.RoamReaderUnifiedAPI;
import com.roam.roamreaderunifiedapi.callback.DeviceResponseHandler;
import com.roam.roamreaderunifiedapi.callback.DeviceStatusHandler;
import com.roam.roamreaderunifiedapi.constants.DeviceStatus;
import com.roam.roamreaderunifiedapi.constants.DeviceType;
import com.roam.roamreaderunifiedapi.constants.Parameter;
import com.roam.roamreaderunifiedapi.constants.ProgressMessage;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.*;

public class RoamG4xPlugin extends CordovaPlugin {

    // actions
    private static final String CHECK_PERMISSIONS = "checkPermissions";
    private static final String WAIT_FOR_CARD_SWIPE = "waitForCardSwipe";
    private static final String STOP_WAITING_FOR_CARD_SWIPE = "stopWaitingForCardSwipe";

    private static final String REGISTER_CARD_SWIPE_CALLBACK = "registerCardSwipeCallback";
    private static final String REGISTER_CONNECTED_CALLBACK = "registerConnectedCallback";
    private static final String REGISTER_DISCONNECTED_CALLBACK = "registerDisconnectedCallback";
    private static final String REGISTER_ERROR_CALLBACK = "registerErrorCallback";

    private static final String STOP = "stop";
    private static final String START = "start";

    // callbacks
    private CallbackContext connectedCallback;
    private CallbackContext disconnectedCallback;
    private CallbackContext errorCallback;
    private CallbackContext cardSwipeDetectedCallback;

    private static final String TAG = "RoamG4xPlugin";

    // Android 23 requires user to explicitly grant permission for audio
    private static final String RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    private static final int CHECK_PERMISSIONS_REQ_CODE = 2;
    private static final int CARD_SWIPE_REQ_CODE = 3;
    private CallbackContext permissionCallback;

    private DeviceManager g4xDeviceManager;
    private DeviceStatusHandler g4xDeviceStatusHandler = new G4xDeviceStatusHandler();
    private DeviceResponseHandler g4xDeviceResponseHandler = new G4xDeviceResponseHandler();

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        LOG.i(TAG, "Cordova ROAM G4x Plugin");
        LOG.i(TAG, "(c)2016 Salon Suite Solutions");

        if (cordova.hasPermission(RECORD_AUDIO)) {
            initDeviceManager();
        }

    }

    @Override
    public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {

        LOG.d(TAG, "action = " + action);

        boolean validAction = true;

        if (action.equals(WAIT_FOR_CARD_SWIPE)) {

            if (cordova.hasPermission(RECORD_AUDIO)) {
                waitForCardSwipe(callbackContext);
            } else {
                permissionCallback = callbackContext;
                cordova.requestPermission(this, CARD_SWIPE_REQ_CODE, RECORD_AUDIO);
            }

        } else if (action.equals(CHECK_PERMISSIONS)) {

            if (cordova.hasPermission(RECORD_AUDIO)) {
                initDeviceManager();
                callbackContext.success();
            } else {
                permissionCallback = callbackContext;
                cordova.requestPermission(this, CHECK_PERMISSIONS_REQ_CODE, RECORD_AUDIO);
            }

        } else if (action.equals(STOP_WAITING_FOR_CARD_SWIPE)) {

            if (g4xDeviceManager != null) {
                g4xDeviceManager.getTransactionManager().stopWaitingForMagneticCardSwipe();
            }
            callbackContext.success();

        } else if (action.equals(REGISTER_CARD_SWIPE_CALLBACK)) {

            this.cardSwipeDetectedCallback = callbackContext;
            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);

        } else if (action.equals(REGISTER_CONNECTED_CALLBACK)) {

            this.connectedCallback = callbackContext;
            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);

        } else if (action.equals(REGISTER_DISCONNECTED_CALLBACK)) {

            this.disconnectedCallback = callbackContext;
            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);

        } else if (action.equals(REGISTER_ERROR_CALLBACK)) {

            this.errorCallback = callbackContext;
            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);

        } else if (action.equals(STOP)) {

            stop();
            callbackContext.success();

        } else if (action.equals(START)) {

            DeviceManager manager = getDeviceManager();
            if (manager != null) {
                callbackContext.success();
            } else {
                callbackContext.error("Card Reader failed to reinitialize.");
            }

        } else {

            validAction = false;

        }

        return validAction;
    }

    private void waitForCardSwipe(CallbackContext callbackContext) {

        DeviceStatus deviceStatus = getDeviceManager().getStatus();
        if (deviceStatus == null) {
            callbackContext.error("Card reader is not connected");
        } else {
            getDeviceManager().getTransactionManager().waitForMagneticCardSwipe(g4xDeviceResponseHandler);
            callbackContext.success();
        }
    }

    private void initDeviceManager() {
        if (g4xDeviceManager == null) {
            RoamReaderUnifiedAPI.enableDebugLogging(true);
            g4xDeviceManager = RoamReaderUnifiedAPI.getDeviceManager(DeviceType.G4x);
            g4xDeviceManager.initialize(this.cordova.getActivity(), g4xDeviceStatusHandler);
        }
    }

    private DeviceManager getDeviceManager() {
        initDeviceManager();
        return g4xDeviceManager;
    }

    private void stop() {
        // stop waiting for card swipe (avoids scan error send to callback)
        getDeviceManager().getTransactionManager().stopWaitingForMagneticCardSwipe();

        // release the device manager (causes disconnect to be called)
        boolean success = getDeviceManager().release();
        if (!success) {
            LOG.e(TAG, "Failed to release Device Manager");
        }

        // null out member variable so initialize() works
        g4xDeviceManager = null;
    }

    @Override
    public void onDestroy() {
        if (g4xDeviceManager != null) {
            g4xDeviceManager.release();
        }
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) /* throws JSONException */ {
        for(int result:grantResults) {
            if(result == PackageManager.PERMISSION_DENIED) {
                LOG.d(TAG, "User *rejected* Permissions");

                if (requestCode == CARD_SWIPE_REQ_CODE) {
                    this.permissionCallback.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Record audio permission denied"));
                } else {
                    this.permissionCallback.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
                }

                return;
            }
        }

        switch(requestCode) {
            case CHECK_PERMISSIONS_REQ_CODE:
                LOG.d(TAG, "User granted Record Audio Access");
                initDeviceManager();
                permissionCallback.success();
                break;
            case CARD_SWIPE_REQ_CODE:
                LOG.d(TAG, "User granted Record Audio Access");
                initDeviceManager();
                waitForCardSwipe(permissionCallback);
                break;
        }
    }

    private class G4xDeviceStatusHandler implements DeviceStatusHandler {

        @Override
        public void onConnected() {

            PluginResult result = new PluginResult(PluginResult.Status.OK);
            result.setKeepCallback(true);
            if (connectedCallback != null) {
                connectedCallback.sendPluginResult(result);
            } else {
                LOG.w(TAG, "onConnected: connectedCallback is null");
            }

        }

        @Override
        public void onDisconnected() {

            PluginResult result = new PluginResult(PluginResult.Status.OK);
            result.setKeepCallback(true);
            if (disconnectedCallback != null) {
                disconnectedCallback.sendPluginResult(result);
            } else {
                LOG.w(TAG, "onDisconnected: disconnectedCallback is null");
            }

        }

        @Override
        public void onError(String s) {

            // Intentionally calling OK even though this is an error since
            // the error callback is expecting errors. Note according to the docs
            // this is "called when the device returns an error while connecting."
            PluginResult result = new PluginResult(PluginResult.Status.OK, s);
            result.setKeepCallback(true);
            if (errorCallback != null) {
                errorCallback.sendPluginResult(result);
            } else {
                LOG.e(TAG, "onError: " + s);
            }

        }

    }

    private class G4xDeviceResponseHandler implements DeviceResponseHandler {

        @Override
        public void onResponse(Map<Parameter, Object> data) {

            JSONObject json = asJSON(data);

            PluginResult result = new PluginResult(PluginResult.Status.OK, json);
            result.setKeepCallback(true);
            cardSwipeDetectedCallback.sendPluginResult(result);

            g4xDeviceManager.getTransactionManager().stopWaitingForMagneticCardSwipe();
        }

        @Override
        public void onProgress(ProgressMessage progressMessage, String additionalMessage) {
            LOG.i(TAG, progressMessage.toString() + " : " + additionalMessage);
        }
    }

    private JSONObject asJSON(Map<Parameter, Object> map) {
        JSONObject json = new JSONObject();

        for (Parameter parameter : map.keySet()) {
            try {
                json.put(parameter.toString(), map.get(parameter).toString());
            } catch (JSONException e) {
                LOG.e(TAG, "Error adding " + parameter.getDebugString() + " to json");
            }
        }

        return json;
    }

}
