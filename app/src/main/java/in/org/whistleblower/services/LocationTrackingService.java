package in.org.whistleblower.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
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
import in.org.whistleblower.MainActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.fragments.MapFragment;
import in.org.whistleblower.fragments.NotifyLocationFragment;
import in.org.whistleblower.interfaces.ResultListener;
import in.org.whistleblower.interfaces.SettingsResultListener;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.LocationAlarm;
import in.org.whistleblower.models.LocationAlarmDao;
import in.org.whistleblower.models.NotifyLocation;
import in.org.whistleblower.models.NotifyLocationDao;
import in.org.whistleblower.models.ShareLocation;
import in.org.whistleblower.models.ShareLocationDao;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.NavigationUtil;
import in.org.whistleblower.utilities.NotificationsUtil;
import in.org.whistleblower.utilities.PermissionUtil;
import in.org.whistleblower.utilities.VolleyUtil;

public class LocationTrackingService extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks
{
    public static final String KEY_LATLNG = "KEY_LATLNG";
    public static final String KEY_PLACE_NAME = "KEY_PLACE_NAME";
    public static final String KEY_SHARE_LOCATION_REAL_TIME = "shareInRealTime";
    public static final String KEY_SHARE_LOCATION = "shareCurrentLocation";
    public static final String KEY_NOTIFY_ARRIVAL = "isNotifyArrival";
    public static final String STOP_LOCATION_SHARE = "stopLocationShare";
    private static final int NOTIFICATION_ID = 9090;
    public static final String STOP_SERVICE = "stopService";
    public static final String FORCE_STOP = "forceStop";
    public static final String TURN_ON_LOCATION_SETTINGS = "turnOnLocationSettings";
    private static final int SHARE_REAL_TIME_LOCATION_NOTIFICATION_ID = 9091;
    public static final String UPDATE_NOTIFICATION = "UPDATE_NOTIFICATION";
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
    Location mCurrentLocation;
    private NotifyLocation notifyLocation;
    private ShareLocation shareLocation;

    public LocationTrackingService()
    {
        Log.d("FlowLogs", "Service : Constructor");
    }

    NotificationManager mNotificationManager;
    SharedPreferences preferences;

    @Override
    public IBinder onBind(Intent intent)
    {
        Log.d("FlowLogs", "Service : onBind");
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
        Log.d("FlowLogs", "Service : onCreate");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId)
    {
        if (intent == null)
        {
            Log.d("FlowLogs", "Service : onStartCommand");
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
            if (intent.hasExtra(KEY_SHARE_LOCATION_REAL_TIME))
            {
                preferences.edit().putBoolean(KEY_SHARE_LOCATION_REAL_TIME, true).commit();
                updateNotification();
            }
            else if (intent.hasExtra(KEY_SHARE_LOCATION))
            {
                shareLocation = intent.getParcelableExtra(ShareLocation.LOCATION);
                preferences.edit().putBoolean(KEY_SHARE_LOCATION, true).commit();
            }
            else if (intent.hasExtra(KEY_ALARM_SET))
            {
                preferences.edit().putBoolean(KEY_ALARM_SET, true).commit();
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
        Log.d("FlowLogs", "Service : startLocationUpdates");
        if (mGoogleApiClient.isConnected())
        {
            int interval = Integer.parseInt(preferences.getString(KEY_LOCATION_UPDATE_FREQ, "30000"));
            mLocationRequest.setInterval(interval);
            mLocationRequest.setFastestInterval(interval);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else
        {
            mStartLocationUpdates = true;
        }
    }

    public void shareNotifyLocation(Map<String, String> data)
    {
        switch (data.get(VolleyUtil.KEY_ACTION))
        {
            case KEY_SHARE_LOCATION_REAL_TIME:
                break;
            case KEY_SHARE_LOCATION:
                break;
            case KEY_NOTIFY_ARRIVAL:
                break;
        }
    }

    void sendDataToServer(Map<String, String> data)
    {
        VolleyUtil.sendPostData(data, new ResultListener<String>()
        {
            @Override
            public void onSuccess(String result)
            {

            }

            @Override
            public void onError(VolleyError error)
            {

            }
        });
    }

    private void stopLocationUpdates()
    {
        Log.d("FlowLogs", "Service : stopLocationUpdates");

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
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
            case UPDATE_NOTIFICATION:
                updateNotification();
                break;
        }
    }

    private void updateNotification()
    {
        WhistleBlower.toast("updateNotification : UPDATE_NOTIFICATION");
        if (mCurrentLocation != null)
        {
            shareRealTimeLocation(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d("FlowLogs", "Service : onDestroy");
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
        mLocationRequest.setInterval(Integer.parseInt(preferences.getString(KEY_LOCATION_UPDATE_FREQ, "30000")));
        mLocationRequest.setFastestInterval(5000);
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
        Log.d("FlowLogs", "Service  : onConnected : mStartLocationUpdates : " + mStartLocationUpdates);

        if (mStartLocationUpdates)
        {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Log.d("FlowLogs", "Service  : onConnectionSuspended");

        stopLocationUpdates();
    }

    public Location getCurrentLocation()
    {
        Log.d("FlowLogs", "Service  : getCurrentLocation");
        return mCurrentLocation;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        mCurrentLocation = location;
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        Log.d("showMyLocOnMap", "");
        Otto.post(location);

        if (preferences.getBoolean(KEY_ALARM_SET, false))
        {
            triggerAlarm(currentLatLng);
        }
        if (preferences.getBoolean(KEY_SHARE_LOCATION_REAL_TIME, false))
        {
            shareRealTimeLocation(currentLatLng);
        }
        if (preferences.getBoolean(KEY_SHARE_LOCATION, false))
        {
            shareLocation(currentLatLng);
        }
        if (preferences.getBoolean(KEY_NOTIFY_ARRIVAL, false))
        {
            notifyLocation(currentLatLng);
        }
        stopService();
    }

    private void notifyLocation(LatLng latLng)
    {
        NotifyLocationDao dao = new NotifyLocationDao();
        ArrayList<NotifyLocation> notifyLocations = dao.getList();

        for (NotifyLocation location : notifyLocations)
        {
            double notifyLat = Double.parseDouble(location.latitude);
            double notifyLng = Double.parseDouble(location.longitude);

            double distance = MapFragment.distFrom(latLng.latitude, latLng.longitude, notifyLat, notifyLng);

            if (distance < location.radius)
            {
                Map<String, String> data = new HashMap<>();
                data.put(NotifyLocation.EMAIL, location.email);
                data.put(NotifyLocation.USER_EMAIL, location.userEmail);
                data.put(NotifyLocation.PHOTO_URL, location.photoUrl);
                data.put(NotifyLocation.NAME, location.name);
                data.put(NotifyLocation.LONGITUDE, location.longitude);
                data.put(NotifyLocation.LATITUDE, location.latitude);
                data.put(NotifyLocation.MESSAGE, location.message);
                data.put(NotifyLocation.RADIUS, "" + location.radius);
                data.put(VolleyUtil.KEY_ACTION, KEY_NOTIFY_ARRIVAL);

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
    }

    private void shareRealTimeLocation(LatLng latLng)
    {
        ShareLocationDao dao = new ShareLocationDao();
        ArrayList<ShareLocation> shareLocations = dao.getList();

        if (shareLocations.size() > 0)
        {
            String message = "";
            for (ShareLocation location : shareLocations)
            {
                Map<String, String> data = new HashMap<>();

                data.put(ShareLocation.EMAIL, preferences.getString(Accounts.EMAIL, "saleemkhan08@gmail.com"));
                data.put(ShareLocation.PHOTO_URL, preferences.getString(Accounts.PHOTO_URL, ""));
                data.put(ShareLocation.NAME, preferences.getString(ShareLocation.NAME, "Saleem"));

                data.put(ShareLocation.USER_EMAIL, location.userEmail);

                data.put(ShareLocation.LONGITUDE, "" + latLng.longitude);
                data.put(ShareLocation.LATITUDE, "" + latLng.latitude);

                data.put(VolleyUtil.KEY_ACTION, KEY_SHARE_LOCATION_REAL_TIME);
                message += location.name + ", ";
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

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(NavigationUtil.DIALOG_FRAGMENT_TAG, NavigationUtil.SHARE_LOCATION_LIST_FRAGMENT_TAG);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setContentTitle("Sharing Location To:")
                    .setSmallIcon(R.drawable.bullhorn_white)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setContentText(message)
                    .setContentIntent(pendingIntent);

            mNotificationManager.notify(SHARE_REAL_TIME_LOCATION_NOTIFICATION_ID, mBuilder.build());
        }
        else
        {
            preferences.edit().putBoolean(KEY_SHARE_LOCATION_REAL_TIME, false).commit();
        }
    }

    private void shareLocation(LatLng latLng)
    {
        if (shareLocation != null)
        {
            Map<String, String> data = new HashMap<>();
            data.put(ShareLocation.EMAIL, shareLocation.email);
            data.put(ShareLocation.USER_EMAIL, shareLocation.userEmail);
            data.put(ShareLocation.PHOTO_URL, shareLocation.photoUrl);
            data.put(ShareLocation.NAME, shareLocation.name);
            data.put(ShareLocation.LONGITUDE, "" + latLng.longitude);
            data.put(ShareLocation.LATITUDE, "" + latLng.latitude);
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
        LocationAlarmDao dao = new LocationAlarmDao();
        ArrayList<LocationAlarm> alarms = dao.getList();
        boolean allAlarmsStatus = false;
        String locations = "No Alarms Set...";
        int noOfLocations = 0;
        for (LocationAlarm alarm : alarms)
        {
            if (alarm.status == LocationAlarm.ALARM_ON)
            {
                double alarmLat = Double.parseDouble(alarm.latitude);
                double alarmLng = Double.parseDouble(alarm.longitude);

                double distance = MapFragment.distFrom(latLng.latitude, latLng.longitude, alarmLat, alarmLng);

                Log.d("triggerAlarm", "triggerAlarm  distance : " + distance);
                boolean currentAlarm = true;
                if (distance < alarm.radius)
                {
                    Intent intent = new Intent(this, AlarmActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(LocationAlarm.ALARM, alarm);
                    startActivity(intent);
                    currentAlarm = false;
                    dao.update(alarm.address, LocationAlarm.ALARM_OFF);
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
        if (!allAlarmsStatus)
        {
            preferences.edit().putBoolean(KEY_ALARM_SET, false).apply();
        }
        else
        {
            NotificationsUtil.showAlarmNotification(this, locations, noOfLocations);
        }
    }

    public void stopService()
    {
        if (isStopServiceConditionMet())
        {
            stopLocationUpdates();
            stopSelf();
            Log.d("FlowLogs", "Service  : stopSelf");
        }
    }

    private boolean isStopServiceConditionMet()
    {
        isAlarmSet = preferences.getBoolean(KEY_ALARM_SET, false);
        isNotifyArrival = preferences.getBoolean(KEY_NOTIFY_ARRIVAL, false);
        isShareLocation = preferences.getBoolean(KEY_SHARE_LOCATION_REAL_TIME, false);
        isTravellingMode = preferences.getBoolean(KEY_TRAVELLING_MODE, false);
        if (!isShareLocation)
        {
            if (mNotificationManager != null)
            {
                mNotificationManager.cancel(SHARE_REAL_TIME_LOCATION_NOTIFICATION_ID);
            }
        }
        Log.d("FlowLogs", "Service  : isAlarmSet " + isAlarmSet + ", isNotifyArrival " + isNotifyArrival + ", shareLocation " + isShareLocation + ", isTravellingMode :" + isTravellingMode);
        if (!isAlarmSet && !isNotifyArrival && !isShareLocation && !isTravellingMode)
        {
            Log.d("FlowLogs", "isStopServiceConditionMet  : true");
            return true;
        }
        else
        {
            Log.d("FlowLogs", "isStopServiceConditionMet  : false");
            return false;
        }
    }
}