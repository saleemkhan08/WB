package in.org.whistleblower.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.model.LatLng;

import in.org.whistleblower.AlarmActivity;
import in.org.whistleblower.fragments.MapFragment;
import in.org.whistleblower.interfaces.LocationChangeListener;
import in.org.whistleblower.interfaces.SettingsResultListener;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.PermissionUtil;

public class LocationTrackingService extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks
{
    public static final String KEY_LATLNG = "KEY_LATLNG";
    public static final String KEY_PLACE_NAME = "KEY_PLACE_NAME";
    protected LocationRequest mLocationRequest;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationSettingsRequest mLocationSettingsRequest;

    public static final int ALARM_NOTIFICATION = 1001;
    public static final String KEY_TRAVELLING_MODE = "KEY_TRAVELLING_MODE";
    public static final String KEY_NOTIFY_ARRIVAL = "KEY_NOTIFY_ARRIVAL";
    public static final String KEY_SHARE_LOCATION = "KEY_SHARE_LOCATION";
    public static final String KEY_ALARM_SET = "KEY_ALARM_SET";
    public static final String KEY_LOCATION_UPDATE_FREQ = "updateFreq";
    private LatLng alarmLatlng;
    private String alarmAddress;
    private static final float OFFSET_LAT = 0.008983f;
    private static final float OFFSET_LNG = 0.015060f;
    private boolean mStartLocationUpdates;
    private boolean isLocationListenerRegistered;
    private LocationChangeListener mListener;
    Location mCurrentLocation;
    private final IBinder iBinder = new LocalBinder();

    public LocationTrackingService()
    {
        Log.d("FlowLogs", "Service : Constructor");
    }

    public void registerLocationChangedListener(LocationChangeListener listener)
    {
        mListener = listener;
        isLocationListenerRegistered = true;
        Log.d("FlowLogs", "Service : registerLocationChangedListener");

    }

    public void unRegisterLocationChangedListener()
    {
        Log.d("FlowLogs", "Service : unRegisterLocationChangedListener");
        isLocationListenerRegistered = false;
    }


    public class LocalBinder extends Binder
    {
        public LocationTrackingService getService()
        {
            Log.d("FlowLogs", "Service : getService");
            return LocationTrackingService.this;
        }
    }

    NotificationManager notificationManager;
    SharedPreferences preferences;

    @Override
    public IBinder onBind(Intent intent)
    {
        Log.d("FlowLogs", "Service : onBind");
        return iBinder;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
        Log.d("FlowLogs", "Service : onCreate");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId)
    {
        Log.d("FlowLogs", "Service : onStartCommand");
        if (PermissionUtil.isLocationPermissionAvailable() && PermissionUtil.isLocationSettingsOn())
        {
            startLocationUpdates();
        }
        else
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

        if (intent != null && intent.hasExtra(KEY_ALARM_SET))
        {
            alarmAddress = intent.getStringExtra(KEY_PLACE_NAME);
            alarmLatlng = intent.getParcelableExtra(KEY_LATLNG);
        }

        return START_STICKY;
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

    private void stopLocationUpdates()
    {
        Log.d("FlowLogs", "Service : stopLocationUpdates");

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mStartLocationUpdates = false;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d("FlowLogs", "Service : onDestroy");

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
        Log.d("FlowLogs", "Service  : onLocationChanged : isLocationListenerRegistered : " + isLocationListenerRegistered);
        mCurrentLocation = location;
        if (isLocationListenerRegistered)
        {
            mListener.onLocationChanged(location);
        }
        if(preferences.getBoolean(KEY_ALARM_SET, false))
        {
            triggerAlarm(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        stopService();
    }

    private void triggerAlarm(LatLng latLng)
    {
        double distance = MapFragment.distFrom(latLng.latitude, latLng.longitude, alarmLatlng.latitude, alarmLatlng.longitude);
        Log.d("triggerAlarm", "triggerAlarm  distance : "+distance);
        if (distance < 1000)
        {
            Intent intent = new Intent(this, AlarmActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);// Make it Notification with audio and vibration.
            /*stopSelf();
            notificationManager.cancel(ALARM_NOTIFICATION);*/

            preferences.edit().putBoolean(KEY_ALARM_SET, false).commit();
            stopService();
        }
    }

    public void stopService()
    {
        Log.d("FlowLogs", "Service  : onLocationChanged : isLocationListenerRegistered : " + isLocationListenerRegistered);

        boolean alarmSet = preferences.getBoolean(KEY_ALARM_SET, false);
        boolean notifyArrival = preferences.getBoolean(KEY_NOTIFY_ARRIVAL, false);
        boolean shareLocation = preferences.getBoolean(KEY_SHARE_LOCATION, false);
        boolean travellingMode = preferences.getBoolean(KEY_TRAVELLING_MODE, false);
        Log.d("FlowLogs", "Service  : alarmSet " + alarmSet + ", notifyArrival " + notifyArrival + ", shareLocation " + shareLocation + ", travellingMode :" + travellingMode);
        if (!alarmSet && !notifyArrival && !shareLocation && !travellingMode)
        {

            stopLocationUpdates();
            stopSelf();
            Log.d("FlowLogs", "Service  : stopSelf");

        }
    }
}