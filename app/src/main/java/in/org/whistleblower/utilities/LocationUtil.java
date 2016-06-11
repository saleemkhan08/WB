package in.org.whistleblower.utilities;

import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;

public class LocationUtil implements ResultCallback<LocationSettingsResult>,GoogleApiClient.ConnectionCallbacks
{
    static AppCompatActivity mActivity;
    private GoogleApiClient mGoogleApiClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private static SharedPreferences preferences;

    public static final String KEY_LOCATION_UPDATE_FREQ = "updateFreq";
    public static final int REQUEST_CODE_LOCATION_PERMISSION = 0x91;
    public static final int REQUEST_CODE_LOCATION_SETTINGS = 0x92;
    public static final String KEY_LOCATION_SETTINGS_DIALOG_SHOWN = "locationSettingsDialogShown";
    private static final String KEY_PERMISSION_ASKED = "permissionAsked";
    private LocationRequest mLocationRequest;

    public void turnOnLocationSettings(AppCompatActivity activity)
    {
        mActivity = activity;
        preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    protected void buildLocationSettingsRequest()
    {
        MiscUtil.log("buildLocationSettingsRequest");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }

    public boolean isLocationSettingsOn()
    {
        MiscUtil.log("isLocationSettingsOn");
        LocationManager locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        try
        {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            {
                MiscUtil.log("Location Settings On");
                return true;
            }
        }
        catch (Exception ex)
        {
            MiscUtil.log("Exception : " + ex.getMessage());
            MiscUtil.log("Location Settings OFF");
            return false;
        }
        MiscUtil.log("Location Settings OFF");
        return false;
    }
    protected void checkLocationSettings()
    {
        MiscUtil.log("checkLocationSettings");
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(mGoogleApiClient, mLocationSettingsRequest);
        result.setResultCallback(this);
    }

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult)
    {
        final Status status = locationSettingsResult.getStatus();
        MiscUtil.log("LocationSettingsStatusCodes.SUCCESS : " +
                LocationSettingsStatusCodes.SUCCESS + " = " + status.getStatusCode());

        switch (status.getStatusCode())
        {
            case LocationSettingsStatusCodes.SUCCESS:
                MiscUtil.log("All location settings are satisfied.");
                //updateCurrentLocationOnMap();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                MiscUtil.log("Location settings are not satisfied. Show the user a dialog to" +
                        " upgrade location settings ");
                try
                {
                    boolean isLocationSettingsDialogShown = preferences.getBoolean(KEY_LOCATION_SETTINGS_DIALOG_SHOWN, false);
                    MiscUtil.log("isLocationSettingsDialogShown : " + isLocationSettingsDialogShown);
                    if (!isLocationSettingsDialogShown)
                    {
                        MiscUtil.log("Showing Location Settings Dialog");
                        status.startResolutionForResult(mActivity, REQUEST_CODE_LOCATION_SETTINGS);
                        preferences.edit().putBoolean(KEY_LOCATION_SETTINGS_DIALOG_SHOWN, true).apply();
                    }
                }
                catch (IntentSender.SendIntentException e)
                {
                    MiscUtil.log("PendingIntent unable to execute request.");
                }

                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                MiscUtil.log("Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }

    protected synchronized void buildGoogleApiClient()
    {
        MiscUtil.log("Building GoogleApiClient");
        if (mGoogleApiClient == null)
        {
            MiscUtil.log("mGoogleApiClient == null");
            mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
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
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(Integer.parseInt(preferences.getString(KEY_LOCATION_UPDATE_FREQ, "30000")));
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle bundle)
    {

    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    public static LatLng getLatLng(String lat, String lng)
    {
        return new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
    }
}
