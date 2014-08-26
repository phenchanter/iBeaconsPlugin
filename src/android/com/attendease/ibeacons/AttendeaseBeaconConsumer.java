package com.attendease.ibeacons;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import java.lang.Boolean;
import java.lang.String;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Date;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;


//https://github.com/RadiusNetworks/android-ibeacon-reference/blob/master/src/com/radiusnetworks/ibeaconreference/MonitoringActivity.java
import android.util.Log;
import android.os.Bundle;
import android.os.RemoteException;
import android.app.Service;
import android.os.Binder;
import android.os.IBinder;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.support.v4.content.LocalBroadcastManager;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class AttendeaseBeaconConsumer extends Service implements IBeaconConsumer
{
    public static final String TAG = "AttendeaseBeaconConsumer";
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);

    private JSONArray beaconUUIDs = new JSONArray();


    private static Hashtable beacons = new Hashtable();

    private static int NOTIFICATION_ID = 424242;

    private static Hashtable beaconNotifications = new Hashtable();

    private static boolean notifyZero = false;

    private static String notificationServer = "";
    private static Integer notificationInterval = 3600;
    private static String authToken = "";
    private static String attendeeName = "";
    private static String attendeeId = "";

    public static Hashtable getBeacons() {
      return beacons;
    }

    public static void setNotifyServer(String server, Integer interval) {
      Log.i(TAG, "AttendeaseBeaconConsumer.setNotifyServer");
      notificationServer = server;
      notificationInterval = interval;
    }

    public static void setNotifyServerAuthToken(String theAuthToken) {
      Log.i(TAG, "AttendeaseBeaconConsumer.setNotifyServerAuthToken");
      authToken = theAuthToken;
    }

    private void startNotification(Context context) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(getIconValue(context.getPackageName(), "icon"))
                        .setContentTitle("You found a beacon!")
                        .setContentText("Have a nice day.");

        Intent targetIntent = new Intent(context, AttendeaseBeacons.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(NOTIFICATION_ID, builder.build());
    }
    private void runNotification(Context context, String title, String message){
        Intent intent = new Intent(context, AttendeaseBeaconAlertActivity.class); //this, "com.attendease.ibeacons.AttendeaseBeaconAlertService");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // You can also include some extra data.
        intent.putExtra("package", context.getPackageName());
        intent.putExtra("title", title );
        intent.putExtra("message", message);
        startActivity(intent);
        startNotification(context);
        if (notificationServer != "" && authToken != "") {
            // TODO: notify the server about the beacon.
        }
    }
//    public void postData(Hashtable beacons) {
//        // Create a new HttpClient and Post Header
//        HttpClient httpclient = new DefaultHttpClient();
//        HttpPost httppost = new HttpPost("http://www.yoursite.com/script.php");
//
//        try {
//            // Add your data
//            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
//            for(Beacon beacon: beacons){
//
//            }
//            nameValuePairs.add(new BasicNameValuePair("id", "12345"));
//            nameValuePairs.add(new BasicNameValuePair("stringdata", "AndDev is Cool!"));
//            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//
//            // Execute HTTP Post Request
//            HttpResponse response = httpclient.execute(httppost);
//
//        } catch (ClientProtocolException e) {
//            // TODO Auto-generated catch block
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//        }
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, intent == null ? "Ya null" : "Ya zver!");
        Log.i(TAG, intent.getStringExtra("beaconUUIDs"));
        if(intent != null) {
            try {
                beaconUUIDs = new JSONArray(intent.getStringExtra("beaconUUIDs"));
            } catch (JSONException e) {
                Log.e(TAG, "onStartCommand: Got JSON Exception " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG, "onStartCommand: Got an Exception " + e.getMessage());
                e.printStackTrace();
            }
        }

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "AttendeaseBeaconConsumer.onCreate");
        iBeaconManager.bind(this);
    }
    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "AttendeaseBeaconConsumer.onDestroy");
        iBeaconManager.unBind(this);
    }
    @Override
    public void onIBeaconServiceConnect() {
        final Context thus = this;

        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
          @Override
          public void didEnterRegion(Region region) {
            Log.i(TAG, "I just saw an iBeacon: " + region.toString());
          }

          @Override
          public void didExitRegion(Region region) {
            Log.i(TAG, "I no longer see an iBeacon: " + region.toString() );
          }

          @Override
          public void didDetermineStateForRegion(int state, Region region) {
            if (state == 1)
            {
              Log.i(TAG, "I have just switched to SEEING iBeacons: " + region.toString());
            }
            else
            {
              Log.i(TAG, "I have just switched to NOT SEEING iBeacons: " + region.toString());
            }
          }
        });

        iBeaconManager.setRangeNotifier(new RangeNotifier() {
          @Override
          public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
              Log.i(TAG, "iBeacons size: "+iBeacons.size());
              beacons.clear();
              if (iBeacons.size() > 0) {
                  notifyZero = true;
                  Vector data = new Vector();
                  // Iterate through and clean if no such beacon.
                  Set<String> keys = beaconNotifications.keySet();
                  Log.i(TAG, "keys size: "+keys.size());
                  if(keys.size() == 0){
                      Iterator<IBeacon> iterator = iBeacons.iterator();
                      Log.i(TAG, "no beaconNotifications keySet");
                      while (iterator.hasNext()) {
                          IBeacon beacon = iterator.next();
                          Log.i(TAG, region.getProximityUuid() + ": The iBeacon I see is about " + beacon.getAccuracy() + " meters away.");
                          data.addElement(beacon);
                          String identifier = beacon.getProximityUuid() + "," + beacon.getMajor() + "," + beacon.getMinor();
                          // Only notify the server/app if the beacon is near or in yo' face!
                          // Added CLProximityFar because walking into a room with the phone in your pocket seems to trigger this one first... and doesn't retrigger as you get closer.
                          if (beacon.getProximity() == IBeacon.PROXIMITY_FAR || beacon.getProximity() == IBeacon.PROXIMITY_NEAR || beacon.getProximity() == IBeacon.PROXIMITY_IMMEDIATE) {
                              Date previousTime = (Date) beaconNotifications.get(identifier);
                              Boolean notify = true;
                              if (previousTime != null) {
                                  notify = false;
                              }

                              if (notify) {
                                  Log.v(TAG, "NOTIFY about this beacon: " + identifier);
                                  beaconNotifications.put(identifier, new Date());
                                  runNotification(thus,"Find!","cool");
                              }
                          }
                      }
                  }
                  else {
                      ArrayList<String> deleteList = new ArrayList<String>();
                      int ind = 0;
                      for(String key: keys) {
                        Iterator<IBeacon> iterator = iBeacons.iterator();
//                      System.out.println("Value of " + key + " is: " + beaconNotifications.get(key));
                          Log.i(TAG, "iterate through keys");
                          Boolean toClean = true;
                          Boolean isChecked = false;
                          Log.i(TAG,"Checking key " + key);
                          while (iterator.hasNext()) {
                              IBeacon beacon = iterator.next();
                              isChecked = true;
                              Log.i(TAG, region.getProximityUuid() + ": The iBeacon I see is about " + beacon.getAccuracy() + " meters away.");
                              if(ind == 0){
                                  data.addElement(beacon);
                              }
                              String identifier = beacon.getProximityUuid() + "," + beacon.getMajor() + "," + beacon.getMinor();
                              Log.i(TAG,"check if equal "+ (key.equals(identifier))+"   identifier " + identifier +"  key "+key);
                              if (key.equals(identifier)) {
                                  toClean = false;
                                  Log.i(TAG,"must not delete " + key);
                              }
                              if (beacon.getProximity() == IBeacon.PROXIMITY_FAR || beacon.getProximity() == IBeacon.PROXIMITY_NEAR || beacon.getProximity() == IBeacon.PROXIMITY_IMMEDIATE) {
                                  Date previousTime = (Date) beaconNotifications.get(identifier);
                                  Boolean notify = true;
                                  if (previousTime != null) {
                                      notify = false;
                                  }
                                  if (notify) {
                                      Log.v(TAG, "NOTIFY about this beacon: " + identifier);
                                      beaconNotifications.put(identifier, new Date());
                                      runNotification(thus,"Find!","cool");
                                  }
                              }
                          }
                          ind = ind + 1;
                          Log.i(TAG, "Pre delete "+toClean +"   " + key);
                          if(toClean) {
                              deleteList.add(key);
                          }
                      }
                      Boolean cleaned  = false;
                      for (String dk: deleteList){
                          Log.i(TAG, "delete "+ dk);
                          beaconNotifications.remove(dk);
                          Log.i(TAG, "size "+beaconNotifications.size());
                          cleaned = true;
                      }
                      if(cleaned){
                          runNotification(thus,"Lost!","Not so cool");
                      }
                  }
                  beacons.put(region.getProximityUuid(), data);
              }
              else
              {
                beacons.put(region.getProximityUuid(), new Vector());
                if(notifyZero) {
                    notifyZero = false;
                    runNotification(thus,"Lost!","Not so cool");
                    beaconNotifications.clear();
                }
              }
          }
        });

        try {
          Log.i(TAG,"beaconUUIDs "+ beaconUUIDs.length());
          for (int i = 0; i < beaconUUIDs.length(); ++i) {
              try {
                String beaconUUID = beaconUUIDs.getString(i);

                Log.v(TAG, "iBeaconManager.startMonitoringBeaconsInRegion: " + beaconUUID);

                iBeaconManager.startMonitoringBeaconsInRegion(new Region(beaconUUID, beaconUUID, null, null));
                iBeaconManager.startRangingBeaconsInRegion(new Region(beaconUUID, beaconUUID, null, null));
              } catch (JSONException e) {
                Log.e(TAG, "onIBeaconServiceConnect: Got JSON Exception " + e.getMessage());
              }
          }



        } catch (RemoteException e) {   }
    }

    /**
     * https://github.com/katzer/cordova-plugin-local-notifications/blob/master/src/android/Options.java
     * Returns numerical icon Value
     *
     * @param {String} className
     * @param {String} iconName
     */
    private int getIconValue (String className, String iconName) {
        int icon = 0;

        try {
            Class<?> klass  = Class.forName(className + ".R$drawable");

            icon = (Integer) klass.getDeclaredField(iconName).get(Integer.class);
        } catch (Exception e) {}

        return icon;
    }
}