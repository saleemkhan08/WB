package in.org.whistleblower.services;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;

import in.org.whistleblower.MainActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.icon.FontAwesomeIcon;

public class LocationUtil implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult>,
        LocationListener, OnMapReadyCallback
{
    public static final int LOCATION_SETTING_REQUEST_CODE = 201;
    private static final int GOOGLE_PLAY_SERVICES_REQUEST = 202;
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String ZOOM = "ZOOM";
    public static final String TILT = "TILT";
    public static final String BEARING = "BEARING";
    public static final String MAP_TYPE = "MAP_TYPE";
    LocationRequest mLocationRequest;
    public GoogleApiClient mGoogleApiClient;
    AppCompatActivity activity;
    Util util;
    LocationManager locationManager;
    FloatingActionButton buttonMyLoc;
    RelativeLayout addIssueMenu;
    GoogleMap mGoogleMap;
    SupportMapFragment mapFragment;
    SharedPreferences preferences;

    public LocationUtil(AppCompatActivity activity)
    {
        this.activity = activity;
        buttonMyLoc = (FloatingActionButton) activity.findViewById(R.id.my_loc);
        addIssueMenu = (RelativeLayout) activity.findViewById(R.id.fab_wrapper);
        preferences = activity.getSharedPreferences(MainActivity.WHISTLE_BLOWER_PREFERENCE, Context.MODE_PRIVATE);
        mapFragment = SupportMapFragment.newInstance();
    }

    public void setUp(Util util)
    {
        this.util = util;
        setUpMyLocationButton();
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();

        showCurrentLocation();
    }

    public void showCurrentLocation()
    {
        if (isGoogleServicesAvailable())
        {
            if (isLocationServicesEnabled())
            {
                if (!isLocationPermissionAvailable())
                {
                    setUpMap();
                    requestLocationPermission();
                }

            }
        }
    }

    private CameraPosition getCurrentLoc()
    {
        Location currentLocation = null;
        if (mGoogleApiClient.isConnected())
        {
            if (mLocationRequest == null)
            {
                mLocationRequest = LocationRequest.create();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationRequest.setInterval(60 * 1000);
                mLocationRequest.setFastestInterval(5 * 1000);
            }
            if(isLocationPermissionAvailable())
            {
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, this);
                currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
            else
            {
                requestLocationPermission();
            }
        }
        if (currentLocation == null)
        {
            util.toast("Location is not available");
            return new CameraPosition(getLatLong(), 0, 0, 0);
        }
        else
        {
            return new CameraPosition(setLatLng(currentLocation), getZoom(), getTilt(), getBrng());
        }

    }

    private LatLng setLatLng(Location location)
    {
        preferences.edit().putFloat(LATITUDE, (float) location.getLatitude())
                .putFloat(LONGITUDE, (float) location.getLongitude()).commit();
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    private float getBrng()
    {
        return preferences.getFloat(BEARING, 0);
    }

    private LatLng getLatLong()
    {
        return new LatLng(getLat(), getLon());
    }

    public double getLat()
    {
        return preferences.getFloat(LATITUDE, 0);
    }

    public double getLon()
    {
        return preferences.getFloat(LONGITUDE, 0);
    }

    public float getZoom()
    {
        return preferences.getFloat(ZOOM, 15);
    }

    public float getTilt()
    {
        return preferences.getFloat(TILT, 0);
    }

    public int getType()
    {
        return preferences.getInt(MAP_TYPE, GoogleMap.MAP_TYPE_HYBRID);
    }

    public void moveToLocation(double latitude, double longitude)
    {
        LatLng latLng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
        mGoogleMap.moveCamera(cameraUpdate);
    }

    public void moveToLocation(CameraPosition cameraPosition)
    {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        if (mGoogleMap == null)
        {
            mGoogleMap = mapFragment.getMap();
        }
        mGoogleMap.moveCamera(cameraUpdate);
    }

    public boolean isGoogleServicesAvailable()
    {
        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (isAvailable == ConnectionResult.SUCCESS)
        {
            return true;
        }
        else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable))
        {
            GooglePlayServicesUtil.getErrorDialog(isAvailable, activity, GOOGLE_PLAY_SERVICES_REQUEST).show();
        }
        else
        {
            Toast.makeText(activity, "Can't Access Google Play Services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void setUpMap()
    {
        mapFragment.getMapAsync(this);
    }

    private void requestLocationPermission()
    {
        MainActivity.requestPermission(
                Arrays.asList(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                MainActivity.LOCATION_REQUEST, activity);
    }

    boolean isLocationServicesEnabled()
    {
        try
        {
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
            Toast.makeText(activity, "Can't Access Location Services!\n" + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void showMyLocButton()
    {
        buttonMyLoc.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) addIssueMenu.getLayoutParams();
        layoutParams.setMargins(0, 0, util.dp(10), util.dp(80));
        addIssueMenu.setLayoutParams(layoutParams);
    }

    public void hideMyLocButton()
    {
        buttonMyLoc.setVisibility(View.GONE);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) addIssueMenu.getLayoutParams();
        layoutParams.setMargins(0, 0, util.dp(10), util.dp(10));
        addIssueMenu.setLayoutParams(layoutParams);
    }

    public void setUpMyLocationButton()
    {
        showMyLocButton();
        buttonMyLoc.setIconDrawable(util.getIcon(FontAwesomeIcon.SCREENSHOT, R.color.white));
        buttonMyLoc.setStrokeVisible(false);
        buttonMyLoc.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setMyLocationOnMap();
            }
        });
    }

    public void setMyLocationOnMap()
    {
        moveToLocation(getCurrentLoc());
    }

    public void showLocationSettingsDialog()
    {
        if (mGoogleApiClient == null)
        {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            mGoogleApiClient.connect();
        }
        mLocationRequest = LocationRequest.create();
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
    public void onConnected(Bundle bundle)
    {
        util.toast("Connected");
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {

    }

    @Override
    public void onResult(LocationSettingsResult result)
    {
        final Status status = result.getStatus();
        switch (status.getStatusCode())
        {
            case LocationSettingsStatusCodes.SUCCESS:
                setMyLocationOnMap();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try
                {
                    status.startResolutionForResult(activity, LOCATION_SETTING_REQUEST_CODE);
                }
                catch (IntentSender.SendIntentException e)
                {
                    Toast.makeText(activity, "Can't Access Location Services!", Toast.LENGTH_SHORT).show();
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Toast.makeText(activity, "Can't Access Location Services!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public boolean isLocationPermissionAvailable()
    {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }

    @Override
    public void onLocationChanged(Location location)
    {

    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleMap = googleMap;
        setMyLocationOnMap();
    }
}