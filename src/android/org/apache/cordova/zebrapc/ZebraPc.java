package org.apache.cordova.zebrapc;
import org.apache.cordova.*;
import org.json.*;
import android.content.*;
import java.util.*;
import java.io.*;
import android.os.*;

public class ZebraPc extends CordovaPlugin {

	static final String lock = "ZebraLinkLock";
	static final String PRINT = "print";
	static final String STATUS = "status";

	@Override
	public boolean execute(String action, JSONArray inargs, final CallbackContext callbackContext) throws JSONException
	{
		if(action.equals(PRINT))
		{
			final String templateName = inargs.getString(0);
			JSONObject data = inargs.getJSONObject(1);
			final HashMap<String,String> variableData = toMap(data);
			
			cordova.getThreadPool().execute(new Runnable() {
            	public void run() {
                	print(templateName,variableData,callbackContext);
            	}
        	});
		}
		else if (action.equals(STATUS))
		{
				cordova.getThreadPool().execute(new Runnable() {
            	public void run() {
                	getStatus(callbackContext);
            	}
        	});
		}
		return true;
	}

	private void getStatus(final CallbackContext callbackContext){
		Intent intent = new Intent();
		intent.setComponent(new ComponentName("com.zebra.printconnect",
		"com.zebra.printconnect.print.GetPrinterStatusService"));
		intent.putExtra("com.zebra.printconnect.PrintService.RESULT_RECEIVER", buildIPCSafeReceiver(new
			ResultReceiver(null) {
			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				if (resultCode == 0) { // Result code 0 indicates success
				// Handle successful printer status retrieval
				// Hash map of printer status conditions (null when no printer selected)
					HashMap<String, String> printerStatusMap = (HashMap<String, String>)
					resultData.getSerializable("PrinterStatusMap");
					PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, new JSONObject(printerStatusMap));
					pluginResult.setKeepCallback(true);
					callbackContext.sendPluginResult(pluginResult);
				} else {
			// Handle unsuccessful printer status retrieval
			// Error message (null on successful printer status retrieval)
					String errorMessage = resultData.getString("com.zebra.printconnect.PrintService.ERROR_MESSAGE");
					callbackContext.error(errorMessage);
				}
			}
		})
		);
		cordova.getActivity().getApplicationContext().startService(intent);
	}

	private void print(final String template, final HashMap<String,String> variableData, final CallbackContext callbackContext){
		
		Intent intent = new Intent();
 		intent.setComponent(new ComponentName("com.zebra.printconnect", "com.zebra.printconnect.print.TemplatePrintService"));
 		intent.putExtra("com.zebra.printconnect.PrintService.TEMPLATE_FILE_NAME",  template); 
 		intent.putExtra("com.zebra.printconnect.PrintService.VARIABLE_DATA", variableData);

		intent.putExtra("com.zebra.printconnect.PrintService.RESULT_RECEIVER", buildIPCSafeReceiver(new
			ResultReceiver(null) {
 				@Override
 				protected void onReceiveResult(int resultCode, Bundle resultData) {
					if (resultCode == 0) { // Result code 0 indicates success
						callbackContext.success();
					} else {
						// Handle unsuccessful print
						// Error message (null on successful print)
						String errorMessage = resultData.getString("com.zebra.printconnect.PrintService.ERROR_MESSAGE");
						callbackContext.error(errorMessage);
					}
 				}
 			}
		));
		cordova.getActivity().getApplicationContext().startService(intent);
	}

	private ResultReceiver buildIPCSafeReceiver(ResultReceiver actualReceiver) {
		Parcel parcel = Parcel.obtain();
		actualReceiver.writeToParcel(parcel, 0);
		parcel.setDataPosition(0);
		ResultReceiver receiverForSending = ResultReceiver.CREATOR.createFromParcel(parcel);
		parcel.recycle();
		return receiverForSending;
	}


	public static HashMap<String, String> toMap(JSONObject object) throws JSONException {
		HashMap<String, String> map = new HashMap<String, String>();

		Iterator<String> keysItr = object.keys();
		
		while(keysItr.hasNext()) {
			String key = keysItr.next();
			String value = object.getString(key);
			map.put(key, value);
		}
		return map;
	}
}
