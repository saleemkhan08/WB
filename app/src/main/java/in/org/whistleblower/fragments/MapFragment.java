package in.org.whistleblower.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;

import in.org.whistleblower.MainActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.icon.FontAwesomeIcon;
import in.org.whistleblower.utilities.MiscUtil;

public class MapFragment extends SupportMapFragment implements OnLocationChangedListener,
        ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<LocationSettingsResult>
{
    public static final String LOCATION_UPDATE_FREQ = "updateFreq";
    public static final String MAP_TYPE = "mapType";
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String ZOOM = "ZOOM";
    public static final String TILT = "TILT";
    public static final String BEARING = "BEARING";
    LocationManager locationManager;
    Activity mActivity;
    MiscUtil mUtil;
    GoogleMap mGoogleMap;
    FloatingActionButton buttonMyLoc;
    SharedPreferences preferences;
    public static final int LOCATION_SETTING_REQUEST_CODE = 201;
    private static final int GOOGLE_PLAY_SERVICES_REQUEST = 202;
    GoogleApiClient mGoogleApiClient;

    public MapFragment()
    {

    }

    public MapFragment(Activity activity)
    {
        mActivity = activity;
        mUtil = new MiscUtil(mActivity);
        preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
    }

    public boolean isGoogleServicesAvailable()
    {
        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mActivity);
        if (isAvailable == ConnectionResult.SUCCESS)
        {
            return true;
        }
        else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable))
        {
            GooglePlayServicesUtil.getErrorDialog(isAvailable, mActivity, GOOGLE_PLAY_SERVICES_REQUEST).show();
        }
        else
        {
            mUtil.toast("Can't Access Google Play Services");
        }
        return false;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        buttonMyLoc = (FloatingActionButton) mActivity.findViewById(R.id.my_loc);
        addIssueMenu = (RelativeLayout) mActivity.findViewById(R.id.fab_wrapper);
        setUpMyLocationButton();

        if (isGoogleServicesAvailable())
        {
            mGoogleMap = this.getMap();
            if (mGoogleMap != null)
            {
                mGoogleMap.setMyLocationEnabled(true);
                gotoLastKnownPos(false);
            }
            else
            {
                mUtil.toast("Google Map Is Not Available");
            }
        }
    }

    public void showMyLocButton()
    {
        if (buttonMyLoc != null && addIssueMenu != null)
        {
            buttonMyLoc.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) addIssueMenu.getLayoutParams();
            layoutParams.setMargins(0, 0, mUtil.dp(10), mUtil.dp(80));
            addIssueMenu.setLayoutParams(layoutParams);
        }
    }

    public void hideMyLocButton()
    {
        if (buttonMyLoc != null && addIssueMenu != null)
        {
            buttonMyLoc.setVisibility(View.GONE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) addIssueMenu.getLayoutParams();
            layoutParams.setMargins(0, 0, mUtil.dp(10), mUtil.dp(10));
            addIssueMenu.setLayoutParams(layoutParams);
        }
    }

    public void setUpMyLocationButton()
    {
        buttonMyLoc.setIconDrawable(mUtil.getIcon(FontAwesomeIcon.SCREENSHOT, R.color.white));
        buttonMyLoc.setStrokeVisible(false);
        buttonMyLoc.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setMyLocationOnMap();
            }
        });
        showMyLocButton();
    }


    public void setMyLocationOnMap()
    {
        if (isGoogleServicesAvailable())
        {
            if (mGoogleApiClient == null)
            {
                mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
                mGoogleApiClient.connect();
            }
            else if (!mGoogleApiClient.isConnected())
            {
                mGoogleApiClient.connect();
            }
            else
            {
                updateCurrentLoc();
            }
        }
    }

    private void requestLocationPermission()
    {
        MainActivity.requestPermission(
                Arrays.asList(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                MainActivity.LOCATION_REQUEST, mActivity);
    }

    public boolean isLocationPermissionAvailable()
    {
        return ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }

    @Override
    public void onPause()
    {
        super.onPause();
        CameraPosition pos = mGoogleMap.getCameraPosition();
        preferences.edit()
                .putFloat(LATITUDE, (float) pos.target.latitude)
                .putFloat(LONGITUDE, (float) pos.target.longitude)
                .putFloat(TILT, pos.tilt)
                .putFloat(BEARING, pos.bearing)
                .putFloat(ZOOM, pos.zoom).apply();
    }

    public LatLng getLatLng()
    {
        return new LatLng(preferences.getFloat(LATITUDE, 12.9667f), preferences.getFloat(LONGITUDE, 77.5667f));
    }


    private float getBrng()
    {
        return preferences.getFloat(BEARING, 0);
    }

    public float getZoom()
    {
        return preferences.getFloat(ZOOM, 15);
    }

    public float getTilt()
    {
        return preferences.getFloat(TILT, 0);
    }

    public CameraPosition getLastKnownPos()
    {
        return new CameraPosition(getLatLng(), getZoom(), getTilt(), getBrng());
    }

    public void gotoLastKnownPos(boolean animate)
    {
        // Check everything all requirements again
        CameraPosition newCameraPosition = getLastKnownPos();

        if (!mGoogleMap.getCameraPosition().equals(newCameraPosition))
        {
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(newCameraPosition);
            int mapType = Integer.parseInt(preferences.getString(MAP_TYPE, "4"));

            if (mGoogleMap.getMapType() != mapType)
            {
                mGoogleMap.setMapType(mapType);
            }
            if (animate)
            {
                mGoogleMap.animateCamera(update);
            }
            else
            {
                mGoogleMap.moveCamera(update);
            }
        }
    }

    RelativeLayout addIssueMenu;

    @Override
    public void onLocationChanged(Location location)
    {

    }

    public void updateCurrentLoc()
    {
        if (isLocationPermissionAvailable())
        {
            if (isLocationServicesEnabled())
            {
                Location location = LocationServices.FusedLocationApi
                        .getLastLocation(mGoogleApiClient);

                if (location != null)
                {
                    CameraPosition pos = mGoogleMap.getCameraPosition();
                    preferences.edit()
                            .putFloat(LATITUDE, (float) location.getLatitude())
                            .putFloat(LONGITUDE, (float) location.getLongitude())
                            .putFloat(TILT, pos.tilt)
                            .putFloat(BEARING, pos.bearing)
                            .putFloat(ZOOM, pos.zoom).apply();
                    gotoLastKnownPos(true);

                }
                else
                {
                    //gotoLastKnownPos();
                    mUtil.toast("Please Turn on GPS & Try Again!");
                }
            }
        }
        else
        {
            requestLocationPermission();
        }
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        mUtil.toast("Connected To Google Service");
        updateCurrentLoc();
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {

    }

    boolean isLocationServicesEnabled()
    {
        try
        {
            locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                    !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            {
                showLocationSettingsDialog();
                return false;
            }
            return true;
        }
        catch (Exception ex)
        {
            Toast.makeText(mActivity, "Can't Access Location Services!\n" + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void showLocationSettingsDialog()
    {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(60 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        LocationServices
                .SettingsApi
                .checkLocationSettings(mGoogleApiClient, builder.build())
                .setResultCallback(this);
    }

    @Override
    public void onResult(LocationSettingsResult result)
    {
        final Status status = result.getStatus();
        switch (status.getStatusCode())
        {
            case LocationSettingsStatusCodes.SUCCESS:
                mUtil.toast("enabled");
                updateCurrentLoc();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try
                {
                    status.startResolutionForResult(mActivity, LOCATION_SETTING_REQUEST_CODE);
                }
                catch (IntentSender.SendIntentException e)
                {
                    Toast.makeText(mActivity, "Can't Access Location Services!", Toast.LENGTH_SHORT).show();
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Toast.makeText(mActivity, "Can't Access Location Services!", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}