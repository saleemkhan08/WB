package in.org.whistleblower.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.org.whistleblower.AlarmActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.dao.LocationAlarmDao;
import in.org.whistleblower.dao.NotifyLocationDao;
import in.org.whistleblower.dao.ShareLocationDao;
import in.org.whistleblower.fragments.MapFragment;
import in.org.whistleblower.fragments.NotifyLocationFragment;
import in.org.whistleblower.interfaces.ResultListener;
import in.org.whistleblower.interfaces.SettingsResultListener;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.LocationAlarm;
import in.org.whistleblower.models.NotificationData;
import in.org.whistleblower.models.NotifyLocation;
import in.org.whistleblower.models.ShareLocation;
import in.org.whistleblower.receivers.NotificationActionReceiver;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.NavigationUtil;
import in.org.whistleblower.utilities.NotificationsUtil;
import in.org.whistleblower.utilities.PermissionUtil;
import in.org.whistleblower.utilities.VolleyUtil;

public class LocationTrackingService extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks
{
    public static final String KEY_SHARE_LOCATION_IN_REAL_TIME = "shareLocationInRealTime";
    public static final String KEY_SHARE_LOCATION = "shareCurrentLocation";
    public static final String KEY_NOTIFY_LOCATION = "notifyLocation";
    public static final String STOP_SERVICE = "stopService";
    public static final String FORCE_STOP = "forceStop";
    public static final String TURN_ON_LOCATION_SETTINGS = "turnOnLocationSettings";
    private static final int SHARE_REAL_TIME_LOCATION_NOTIFICATION_ID = 9091;
    public static final String DELETE_SHARE_LOCATION_NOTIFICATION = "DELETE_SHARE_LOCATION_NOTIFICATION";
    public static final String DELETE_ALARM_NOTIFICATION = "DELETE_ALARM_NOTIFICATION";
    public static final String DELETE_NOTIFY_ARRIVAL_ALARM_NOTIFICATION = "DELETE_NOTIFY_ARRIVAL_ALARM_NOTIFICATION";
    private static final String TAG = "LocationTrackingService";

    boolean isAlarmSet;
    boolean isNotifyArrival;
    boolean isShareLocation;
    boolean isTravellingMode;

    protected LocationRequest mLocationRequest;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationSettingsRequest mLocationSettingsRequest;

    public static final int ALARM_NOTIFICATION = 1001;
    public static final String KEY_TRAVELLING_MODE = "KEY_TRAVELLING_MODE";


    public static final String KEY_ALARM_SET = "KEY_ALARM_SET";
    public static final String KEY_LOCATION_UPDATE_FREQ = "updateFreq";
    private boolean mStartLocationUpdates;
    LatLng mCurrentLatLng;
    private ShareLocation shareLocation;

    public LocationTrackingService()
    {
        Log.d(TAG, "Service : Constructor");
    }

    NotificationManager mNotificationManager;
    SharedPreferences preferences;

    @Override
    public IBinder onBind(Intent intent)
    {
        Log.d(TAG, "Service : onBind");
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Otto.register(this);
        preferences = WhistleBlower.getPreferences();
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
        Log.d(TAG, "Service : onCreate");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId)
    {
        if (intent == null)
        {
            Log.d(TAG, "onStartCommand");
            if (!isStopServiceConditionMet())
            {
                if (!isTravellingMode && PermissionUtil.isLocationPermissionAvailable() && !PermissionUtil.isLocationSettingsOn())
                {
                    String msg = null;
                    if (isShareLocation)
                    {
                        msg = "You were sharing your location";
                    }
                    if (isNotifyArrival)
                    {
                        if (msg == null)
                        {
                            msg = "You had set a notification to your friend for a destination";
                        }
                        else
                        {
                            msg += (isAlarmSet ? ", " : " and ") + " a notification to your friend was set for a destination";
                        }
                    }
                    if (isAlarmSet)
                    {
                        if (msg == null)
                        {
                            msg = "You had set a Location Alarm";
                        }
                        else
                        {
                            msg += " and a Location Alarm was also set";
                        }

                    }
                    msg += ".\nDo you wanna turn on Location settings?";
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
            }
        }
        else
        {
            if (intent.hasExtra(KEY_SHARE_LOCATION_IN_REAL_TIME))
            {
                Log.d(TAG, "KEY_SHARE_LOCATION_REAL_TIME");
                preferences.edit().putBoolean(KEY_SHARE_LOCATION_IN_REAL_TIME, true).commit();
                communicator(DELETE_SHARE_LOCATION_NOTIFICATION);
            }
            else if (intent.hasExtra(KEY_SHARE_LOCATION))
            {
                Log.d(TAG, "KEY_SHARE_LOCATION");
                shareLocation = intent.getParcelableExtra(ShareLocation.LOCATION);
                preferences.edit().putBoolean(KEY_SHARE_LOCATION, true).commit();
            }
            else if (intent.hasExtra(KEY_ALARM_SET))
            {
                Log.d(TAG, "KEY_ALARM_SET");
                preferences.edit().putBoolean(KEY_ALARM_SET, true).commit();
                communicator(DELETE_ALARM_NOTIFICATION);
            }
            else if (intent.hasExtra(KEY_NOTIFY_LOCATION))
            {
                Log.d(TAG, "KEY_NOTIFY_ARRIVAL");
                preferences.edit().putBoolean(KEY_NOTIFY_LOCATION, true).commit();
                communicator(DELETE_NOTIFY_ARRIVAL_ALARM_NOTIFICATION);
            }
        }

        if (PermissionUtil.isLocationPermissionAvailable() && PermissionUtil.isLocationSettingsOn())
        {
            startLocationUpdates();
        }
        else
        {
            turnOnLocationSettings();
        }
        return START_STICKY;
    }

    private void turnOnLocationSettings()
    {
        PermissionUtil.turnOnLocationSettings(mGoogleApiClient, mLocationSettingsRequest, new SettingsResultListener()
        {
            @Override
            public void onLocationSettingsTurnedOn()
            {
                startLocationUpdates();
            }

            @Override
            public void onLocationSettingsCancelled()
            {
            }
        });
    }

    private void startLocationUpdates()
    {
        Log.d(TAG, "startLocationUpdates");
        if (mGoogleApiClient.isConnected())
        {
            int interval = Integer.parseInt(preferences.getString(KEY_LOCATION_UPDATE_FREQ, "500"));
            mLocationRequest.setInterval(interval);
            mLocationRequest.setFastestInterval(interval);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else
        {
            mStartLocationUpdates = true;
        }
    }

    private void stopLocationUpdates()
    {
        Log.d(TAG, "stopLocationUpdates");
        if (mGoogleApiClient.isConnected())
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        mStartLocationUpdates = false;
    }

    @Subscribe
    public void communicator(String action)
    {
        Log.d("Communicator", action);
        switch (action)
        {
            case STOP_SERVICE:
                stopService();
                break;
            case FORCE_STOP:
                stopSelf();
                break;
            case TURN_ON_LOCATION_SETTINGS:
                turnOnLocationSettings();
                break;
            case DELETE_ALARM_NOTIFICATION:
                triggerAlarm(mCurrentLatLng);
                break;
            case DELETE_SHARE_LOCATION_NOTIFICATION:
                shareRealTimeLocation(mCurrentLatLng);
                break;
            case DELETE_NOTIFY_ARRIVAL_ALARM_NOTIFICATION:
                notifyLocation(mCurrentLatLng);
                break;
        }
    }

    private LatLng getLatLng(Location mCurrentLocation)
    {
        return new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "Service : onDestroy");
        Otto.unregister(this);
        stopLocationUpdates();
    }

    protected synchronized void buildGoogleApiClient()
    {
        MiscUtil.log("Building GoogleApiClient");
        if (mGoogleApiClient == null)
        {
            MiscUtil.log("mGoogleApiClient == null");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .build();
        }
        mGoogleApiClient.connect();
        MiscUtil.log("Building GoogleApiClient Exit");
    }

    protected void createLocationRequest()
    {
        MiscUtil.log("createLocationRequest");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Integer.parseInt(preferences.getString(KEY_LOCATION_UPDATE_FREQ, "500")));
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void buildLocationSettingsRequest()
    {
        MiscUtil.log("buildLocationSettingsRequest");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        Log.d(TAG, "Service  : onConnected : mStartLocationUpdates : " + mStartLocationUpdates);

        if (mStartLocationUpdates)
        {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Log.d(TAG, "Service  : onConnectionSuspended");
        stopLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location)
    {
        Log.d(TAG, "onLocationChanged");
        mCurrentLatLng = getLatLng(location);
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        Otto.post(location);
        if (preferences.getBoolean(KEY_ALARM_SET, false))
        {
            triggerAlarm(currentLatLng);
        }
        if (preferences.getBoolean(KEY_SHARE_LOCATION_IN_REAL_TIME, false))
        {
            shareRealTimeLocation(currentLatLng);
        }
        if (preferences.getBoolean(KEY_SHARE_LOCATION, false))
        {
            shareLocation(currentLatLng);
        }
        if (preferences.getBoolean(KEY_NOTIFY_LOCATION, false))
        {
            notifyLocation(currentLatLng);
        }
        stopService();
    }

    private void notifyLocation(LatLng latLng)
    {
        Log.d(TAG, "notifyLocation");
        ArrayList<NotifyLocation> notifyLocations = NotifyLocationDao.getList();
        int size = notifyLocations.size();
        if (size > 0)
        {
            if (latLng != null)
            {
                String notificationMessage = "";
                for (NotifyLocation location : notifyLocations)
                {
                    double notifyLat = Double.parseDouble(location.latitude);
                    double notifyLng = Double.parseDouble(location.longitude);

                    double distance = MapFragment.distFrom(latLng.latitude, latLng.longitude, notifyLat, notifyLng);
                    notificationMessage += location.receiverName + ", ";
                    if (distance < location.radius)
                    {
                        Map<String, String> data = new HashMap<>();
                        data.put(NotifyLocation.SENDER_NAME, location.senderEmail);
                        data.put(NotifyLocation.SENDER_NAME, location.senderName);
                        data.put(NotifyLocation.SENDER_PHOTO_URL, location.senderPhotoUrl);
                        data.put(NotifyLocation.RECEIVER_EMAIL, location.receiverEmail);
                        data.put(NotifyLocation.LONGITUDE, location.longitude);
                        data.put(NotifyLocation.LATITUDE, location.latitude);
                        data.put(NotifyLocation.MESSAGE, location.message);
                        data.put(NotifyLocation.RADIUS, "" + location.radius);
                        data.put(NotifyLocation.STATUS, "" + location.status);
                        data.put(VolleyUtil.KEY_ACTION, KEY_NOTIFY_LOCATION);

                        VolleyUtil.sendPostData(data, new ResultListener<String>()
                        {
                            @Override
                            public void onSuccess(String result)
                            {
                                Log.d("NotifyLocation", result);
                            }

                            @Override
                            public void onError(VolleyError error)
                            {
                                Log.d("NotifyLocation", error.getMessage());
                            }
                        });
                    }
                }
                Log.d(TAG, "notificationMessage : "+notificationMessage );
                if(notificationMessage.length() > 2)
                {
                    NotificationData notificationData = new NotificationData();
                    notificationData.contentIntentTag = NavigationUtil.FRAGMENT_TAG_NOTIFY_LOCATION_LIST;
                    notificationData.contentTitle = "Notifying Location To:";
                    notificationData.contentText = notificationMessage.substring(0, notificationMessage.length() - 2);
                    notificationData.onGoing = true;
                    notificationData.notificationId = NotificationActionReceiver.NOTIFICATION_ID_RECEIVING_NOTIFIED_LOCATION;
                    NotificationsUtil.showNotification(notificationData);
                }
            }
            else
            {
                NotificationsUtil.removeNotification(NotificationActionReceiver.NOTIFICATION_ID_RECEIVING_NOTIFIED_LOCATION);
            }
        }
        else
        {
            preferences.edit().putBoolean(KEY_NOTIFY_LOCATION, false).commit();
            NotificationsUtil.removeNotification(NotificationActionReceiver.NOTIFICATION_ID_RECEIVING_NOTIFIED_LOCATION);
            stopService();
        }
    }

    private void shareRealTimeLocation(LatLng latLng)
    {
        Log.d(TAG, "shareRealTimeLocation");
        ArrayList<ShareLocation> shareLocations = ShareLocationDao.getList();

        if (shareLocations.size() > 0)
        {
            if (latLng != null)
            {
                String message = "";
                for (ShareLocation location : shareLocations)
                {
                    Map<String, String> data = new HashMap<>();

                    data.put(ShareLocation.SENDER_EMAIL, preferences.getString(Accounts.EMAIL, "saleemkhan08@gmail.com"));
                    data.put(ShareLocation.SENDER_PHOTO_URL, preferences.getString(Accounts.PHOTO_URL, ""));
                    data.put(ShareLocation.SENDER_NAME, preferences.getString(Accounts.NAME, "Saleem"));

                    data.put(ShareLocation.RECEIVER_EMAIL, location.receiverEmail);

                    data.put(ShareLocation.SENDER_LONGITUDE, "" + latLng.longitude);
                    data.put(ShareLocation.SENDER_LATITUDE, "" + latLng.latitude);

                    data.put(VolleyUtil.KEY_ACTION, KEY_SHARE_LOCATION_IN_REAL_TIME);
                    message += location.senderName + ", ";
                    VolleyUtil.sendPostData(data, new ResultListener<String>()
                    {
                        @Override
                        public void onSuccess(String result)
                        {
                            Log.d("RealTime", result);
                        }

                        @Override
                        public void onError(VolleyError error)
                        {
                            Log.d("RealTime", "Error : " + error.getMessage());
                        }
                    });
                }
                message = message.substring(0, message.length() - 2);

                NotificationData data = new NotificationData();
                data.contentIntentTag = NavigationUtil.FRAGMENT_TAG_SHARING_REAL_TIME_LOCATION;
                data.contentText = message;
                data.contentTitle = "Sharing Location To:";
                data.onGoing = true;
                data.notificationId = NotificationActionReceiver.NOTIFICATION_ID_SHARING_REAL_TIME_LOCATION;
                NotificationsUtil.showNotification(data);
            }
            else
            {
                NotificationsUtil.removeNotification(NotificationActionReceiver.NOTIFICATION_ID_SHARING_REAL_TIME_LOCATION);
            }
        }
        else
        {
            preferences.edit().putBoolean(KEY_SHARE_LOCATION_IN_REAL_TIME, false).commit();
            NotificationsUtil.removeNotification(NotificationActionReceiver.NOTIFICATION_ID_SHARING_REAL_TIME_LOCATION);
            stopService();
        }
    }

    private void shareLocation(LatLng latLng)
    {
        Log.d(TAG, "shareLocation");
        if (shareLocation != null && latLng != null)
        {
            Map<String, String> data = new HashMap<>();
            data.put(ShareLocation.SENDER_EMAIL, shareLocation.senderEmail);
            data.put(ShareLocation.RECEIVER_EMAIL, shareLocation.receiverEmail);
            data.put(ShareLocation.SENDER_PHOTO_URL, shareLocation.senderPhotoUrl);
            data.put(ShareLocation.SENDER_NAME, shareLocation.senderName);
            data.put(ShareLocation.SENDER_LONGITUDE, "" + latLng.longitude);
            data.put(ShareLocation.SENDER_LATITUDE, "" + latLng.latitude);
            data.put(VolleyUtil.KEY_ACTION, KEY_SHARE_LOCATION);
            VolleyUtil.sendPostData(data, new ResultListener<String>()
            {
                @Override
                public void onSuccess(String result)
                {
                    Log.d("shareLocation", result);
                    shareLocation = null;
                    preferences.edit().putBoolean(KEY_SHARE_LOCATION, false).apply();
                }

                @Override
                public void onError(VolleyError error)
                {
                    Log.d("shareLocation", "" + error.getMessage());
                }
            });
        }
    }

    private void triggerAlarm(LatLng latLng)
    {
        Log.d(TAG, "triggerAlarm");
        ArrayList<LocationAlarm> alarms = LocationAlarmDao.getList();
        boolean allAlarmsStatus = false;
        String locations = "No Alarms Set...";
        int noOfLocations = 0;
        if (alarms.size() > 0)
        {
            for (LocationAlarm alarm : alarms)
            {
                if (alarm.status == LocationAlarm.ALARM_ON)
                {
                    double alarmLat = Double.parseDouble(alarm.latitude);
                    double alarmLng = Double.parseDouble(alarm.longitude);

                    double distance = (latLng != null) ? MapFragment.distFrom(latLng.latitude, latLng.longitude, alarmLat, alarmLng)
                            : 1000000;

                    Log.d("triggerAlarm", "triggerAlarm  distance : " + distance);
                    boolean currentAlarm = true;
                    if (distance < alarm.radius)
                    {
                        Intent intent = new Intent(this, AlarmActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(LocationAlarm.ALARM, alarm);
                        startActivity(intent);
                        currentAlarm = false;
                        LocationAlarmDao.update(alarm.address, LocationAlarm.ALARM_OFF);
                    }
                    else
                    {
                        noOfLocations++;
                        if (noOfLocations > 1)
                        {
                            locations = "Click here to view the list...";
                        }
                        else
                        {
                            locations = NotifyLocationFragment.getAddressLines(alarm.address, 3);
                        }
                    }
                    allAlarmsStatus = currentAlarm;
                }
            }
        }

        if (!allAlarmsStatus)
        {
            preferences.edit().putBoolean(KEY_ALARM_SET, false).apply();
            NotificationsUtil.removeNotification(NotificationActionReceiver.NOTIFICATION_ID_LOCATION_ALARMS);
            stopService();
        }
        else
        {
            NotificationData data = new NotificationData();

            data.action1IntentText = (noOfLocations > 1) ? "Turn Off All Alarms" : "Turn Off Alarm";
            data.action1IntentIcon = R.mipmap.bell_cross_accent;
            data.action1IntentTag = NotificationActionReceiver.CANCEL_ALL_ALARMS;

            data.contentIntentTag = NavigationUtil.FRAGMENT_TAG_LOCATION_ALARM;
            data.contentText = locations;
            data.contentTitle = "Location Alarm";
            data.onGoing = true;
            data.notificationId = NotificationActionReceiver.NOTIFICATION_ID_LOCATION_ALARMS;

            NotificationsUtil.showAlarmNotification(locations, noOfLocations);
        }
    }

    public void stopService()
    {
        Log.d(TAG, "stopService");
        if (isStopServiceConditionMet())
        {
            stopLocationUpdates();
            stopSelf();
            Log.d(TAG, "stopSelf");
        }
    }

    private boolean isStopServiceConditionMet()
    {
        isAlarmSet = preferences.getBoolean(KEY_ALARM_SET, false);
        isNotifyArrival = preferences.getBoolean(KEY_NOTIFY_LOCATION, false);
        isShareLocation = preferences.getBoolean(KEY_SHARE_LOCATION_IN_REAL_TIME, false);
        isTravellingMode = preferences.getBoolean(KEY_TRAVELLING_MODE, false);
        if (!isShareLocation)
        {
            if (mNotificationManager != null)
            {
                mNotificationManager.cancel(SHARE_REAL_TIME_LOCATION_NOTIFICATION_ID);
            }
        }
        Log.d(TAG, "Service  : isAlarmSet " + isAlarmSet + ", isNotifyArrival " + isNotifyArrival + ", shareLocation " + isShareLocation + ", isTravellingMode :" + isTravellingMode);
        if (!isAlarmSet && !isNotifyArrival && !isShareLocation && !isTravellingMode)
        {
            Log.d(TAG, "isStopServiceConditionMet  : true");
            return true;
        }
        else
        {
            Log.d(TAG, "isStopServiceConditionMet  : false");
            return false;
        }
    }
}