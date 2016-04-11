package in.org.whistleblower;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

import in.org.whistleblower.fragments.MapFragment;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.PermissionUtil;
import in.org.whistleblower.utilities.SettingsResultListener;

public class LocationTrackingService extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks
{
    public static final String GET_LOCATION_ACTION = "GET_LOCATION_ACTION";
    public static final String REQUEST_TYPE = "REQUEST_TYPE";
    protected LocationRequest mLocationRequest;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationSettingsRequest mLocationSettingsRequest;

    public static final int ALARM_NOTIFICATION = 1001;
    public static final String KEY_TRAVELLING_MODE = "KEY_TRAVELLING_MODE";
    public static final String KEY_NOTIFY_ARRIVAL = "KEY_NOTIFY_ARRIVAL";
    public static final String KEY_SHARE_LOCATION = "KEY_SHARE_LOCATION";
    public static final String KEY_ALARM_SET = "KEY_ALARM_SET";
    public static final String KEY_GET_LOCATION = "KEY_GET_LOCATION";
    public static final String KEY_LOCATION_UPDATE_FREQ = "updateFreq";
    private static final float OFFSET_LAT = 0.008983f;
    private static final float OFFSET_LNG = 0.015060f;
    private boolean mStartLocationUpdates;

    public LocationTrackingService()
    {
    }

    NotificationManager notificationManager;
    SharedPreferences preferences;

    @Override
    public IBinder onBind(Intent intent)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId)
    {
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(LocationTrackingService.this, "Turned On", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLocationSettingsCancelled()
                {
                    Toast.makeText(LocationTrackingService.this, "Cancelled", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (intent.hasExtra(REQUEST_TYPE))
        {
            switch (intent.getStringExtra(REQUEST_TYPE))
            {
                case KEY_GET_LOCATION:
                    return START_NOT_STICKY;
            }
        }
        return START_STICKY;
    }

    private void startLocationUpdates()
    {
        if (mGoogleApiClient.isConnected())
        {
            mLocationRequest.setFastestInterval(Integer.parseInt(preferences.getString(KEY_LOCATION_UPDATE_FREQ, "30000")));
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else
        {
            mStartLocationUpdates = true;
        }
    }

    private void stopLocationUpdates()
    {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
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
        if (mStartLocationUpdates)
        {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        stopLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location)
    {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent();
        intent.setAction(GET_LOCATION_ACTION);
        intent.putExtra(MapFragment.LOCATION, location);
        manager.sendBroadcast(new Intent());

        /*float expLat = preferences.getFloat(MapFragment.LATITUDE, 0),
                expLng = preferences.getFloat(MapFragment.LONGITUDE, 0),

                actLat = (float) location.getLatitude(),
                actLng = (float) location.getLongitude();

        if (expLng < (actLng + OFFSET_LNG) && expLng > (actLng - OFFSET_LNG)
                &&
                expLat < (actLat + OFFSET_LAT) && expLat < (actLat - OFFSET_LAT))
        {
            startActivity(new Intent(this, AlarmActivity.class));// Make it Notification with audio and vibration.
            stopSelf();
            notificationManager.cancel(ALARM_NOTIFICATION);
        }*/

        boolean getLocation = preferences.getBoolean(KEY_GET_LOCATION, true);
        boolean alarmSet = preferences.getBoolean(KEY_ALARM_SET, false);
        boolean notifyArrival = preferences.getBoolean(KEY_NOTIFY_ARRIVAL, false);
        boolean shareLocation = preferences.getBoolean(KEY_SHARE_LOCATION, false);
        boolean travellingMode = preferences.getBoolean(KEY_TRAVELLING_MODE, false);

        if (getLocation && !alarmSet && !notifyArrival && !shareLocation && !travellingMode)
        {
            preferences.edit().putBoolean(KEY_GET_LOCATION, false).apply();
            stopLocationUpdates();
            stopSelf();
        }
        else
        {

        }
    }
}
