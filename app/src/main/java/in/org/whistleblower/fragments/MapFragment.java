package in.org.whistleblower.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RelativeLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import in.org.whistleblower.R;
import in.org.whistleblower.icon.FontAwesomeIcon;
import in.org.whistleblower.utilities.ConnectivityListener;
import in.org.whistleblower.utilities.MiscUtil;

public class MapFragment extends SupportMapFragment implements ConnectivityListener,
        ResultCallback<LocationSettingsResult>, LocationListener, GoogleMap.OnCameraChangeListener, GoogleApiClient.ConnectionCallbacks
{
    //Constants
    // Request Codes
    public static final int REQUEST_CODE_LOCATION_PERMISSION = 0x91;
    public static final int REQUEST_CODE_LOCATION_SETTINGS = 0x92;

    //Keys
    public static final String KEY_LOCATION_UPDATE_FREQ = "updateFreq";
    public static final String KEY_LOCATION_SETTINGS_DIALOG_SHOWN = "locationSettingsDialogShown";
    private static final String KEY_TRAVEL_MODE = "KEY_TRAVEL_MODE";
    private static final String KEY_PERMISSION_ASKED = "permissionAsked";
    private static final String KEY_UPDATE_COUNTER = "KEY_UPDATE_COUNTER";
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String ZOOM = "ZOOM";
    public static final String TILT = "TILT";
    public static final String BEARING = "BEARING";
    public static final String MAP_TYPE = "mapType";
    public static final String ADDRESS = "ADDRESS";
    private static final String KEY_TRAVELLING_MODE_INFO = "KEY_TRAVELLING_MODE_INFO";

    //Map & Location Related
    private GoogleMap mGoogleMap;
    protected LocationRequest mLocationRequest;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationSettingsRequest mLocationSettingsRequest;

    MiscUtil util;
    static SharedPreferences preferences;
    Activity mActivity;
    private boolean mTravelModeOn;
    private View mapFragmentContainer;
    FloatingActionButton buttonMyLoc;
    RelativeLayout addIssueMenu;
    static CameraPosition lastScrollPos;
    public static boolean permissionAsked = false;
    public static Location mCurrentLocation;
    static boolean isFirstTime;
    private static int updateCounter;

    public MapFragment()
    {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        // setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        MiscUtil.log("OnCreate");
        mActivity = getActivity();
        util = new MiscUtil(mActivity);
        preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState)
    {
        super.onViewStateRestored(savedInstanceState);
        preferences.edit().putBoolean(MapFragment.KEY_LOCATION_SETTINGS_DIALOG_SHOWN, false).apply();

        if (savedInstanceState != null)
        {
            MiscUtil.log("savedInstanceState not null");
            mTravelModeOn = savedInstanceState.getBoolean(KEY_TRAVEL_MODE);
            permissionAsked = savedInstanceState.getBoolean(KEY_PERMISSION_ASKED);
            lastScrollPos = new CameraPosition(
                    new LatLng(savedInstanceState.getFloat(LATITUDE),
                            savedInstanceState.getFloat(LONGITUDE)),
                    savedInstanceState.getFloat(ZOOM),
                    savedInstanceState.getFloat(TILT),
                    savedInstanceState.getFloat(BEARING));
            isFirstTime = false;
            updateCounter = savedInstanceState.getInt(KEY_UPDATE_COUNTER);
        }
        else
        {
            MiscUtil.log("savedInstanceState null");
            isFirstTime = true;
            permissionAsked = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        MiscUtil.log("Map Fragment onSaveInstanceState");
        outState.putBoolean(KEY_PERMISSION_ASKED, permissionAsked);
        CameraPosition pos = mGoogleMap.getCameraPosition();
        outState.putFloat(LATITUDE, (float) pos.target.latitude);
        outState.putFloat(LONGITUDE, (float) pos.target.longitude);
        outState.putFloat(TILT, pos.tilt);
        outState.putFloat(BEARING, pos.bearing);
        outState.putFloat(ZOOM, pos.zoom);
        outState.putInt(KEY_UPDATE_COUNTER, updateCounter);
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

    protected void buildLocationSettingsRequest()
    {
        MiscUtil.log("buildLocationSettingsRequest");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mGoogleMap = getMap();
        mGoogleMap.setOnCameraChangeListener(this);
        buttonMyLoc = (FloatingActionButton) mActivity.findViewById(R.id.my_loc);
        addIssueMenu = (RelativeLayout) mActivity.findViewById(R.id.fab_wrapper);
        setUpMyLocationButton();
        gotoLastScrollPosition();
        mapFragmentContainer = mActivity.findViewById(android.R.id.content);
        MiscUtil.log("Map Fragment OnResume");
        util.isConnected(this);
        mLocationRequest.setFastestInterval(Integer.parseInt(preferences.getString(KEY_LOCATION_UPDATE_FREQ, "30000")));
    }

    @Override
    public void onInternetConnected()
    {
        MiscUtil.log("Connected To Internet, isFirstTime : " + isFirstTime);
        if (isFirstTime)
        {
            updateCurrentLocationOnMap();
        }
        else
        {
            gotoLastScrollPosition();
        }
    }

    public void gotoLastScrollPosition()
    {
        MiscUtil.log("gotoLastScrollPosition, lastScrollPos : " + lastScrollPos);
        if (lastScrollPos == null)
        {
            gotoLastKnownPos(false, true);
        }
        else
        {
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(lastScrollPos);
            int mapType = Integer.parseInt(preferences.getString(MAP_TYPE, "1"));
            if (mGoogleMap.getMapType() != mapType)
            {
                mGoogleMap.setMapType(mapType);
            }
            mGoogleMap.moveCamera(update);
        }
    }

    public static CameraPosition getLastKnownPos()
    {
        return new CameraPosition(getLatLng(), getZoom(), getTilt(), getBearing());
    }

    public static LatLng getLatLng()
    {
        return new LatLng(preferences.getFloat(LATITUDE, 12.9667f), preferences.getFloat(LONGITUDE, 77.5667f));
    }

    public static float getBearing()
    {
        return preferences.getFloat(BEARING, 0);
    }

    public static float getZoom()
    {
        float zoom = preferences.getFloat(ZOOM, 15);
        if (zoom < 3)
        {
            zoom = 15;
        }
        return zoom;
    }

    public static float getTilt()
    {
        return preferences.getFloat(TILT, 0);
    }

    public void gotoLastKnownPos(boolean animate, boolean addMarker)
    {
        MiscUtil.log("gotoLastKnownPos, animate : " + animate + ", addMarker : " + addMarker);
        // Check everything all requirements again
        lastScrollPos = getLastKnownPos();

        if (mGoogleMap.getCameraPosition().target.latitude != lastScrollPos.target.latitude ||
                mGoogleMap.getCameraPosition().target.longitude != lastScrollPos.target.longitude)
        {
            MiscUtil.log("position not same");
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(lastScrollPos);
            int mapType = Integer.parseInt(preferences.getString(MAP_TYPE, "1"));

            if (mGoogleMap.getMapType() != mapType)
            {
                mGoogleMap.setMapType(mapType);
            }

            mGoogleMap.clear();
            if (addMarker)
            {
                mGoogleMap.addMarker(new MarkerOptions().position(lastScrollPos.target));
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
        else
        {
            MiscUtil.log("Position Same");
        }
    }

    public void setUpMyLocationButton()
    {
        MiscUtil.log("setUpMyLocationButton");
        buttonMyLoc.setIconDrawable(util.getIcon(FontAwesomeIcon.SCREENSHOT, R.color.white));
        MiscUtil.log("Icon Set for My Location Button");
        buttonMyLoc.setStrokeVisible(false);
        buttonMyLoc.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (isLocationSettingsOn())
                {
                    util.toast("Updating Location...");
                }
                preferences.edit().putBoolean(KEY_LOCATION_SETTINGS_DIALOG_SHOWN, false).apply();
                MiscUtil.log("My Location Button Clicked: updateCurrentLoc");
                int travellingModeInfoCounter = preferences.getInt(KEY_TRAVELLING_MODE_INFO, 0);
                if (travellingModeInfoCounter < 5)
                {
                    util.toast("Click and hold to turn \"On\" Travelling Mode");
                    preferences.edit().putInt(KEY_TRAVELLING_MODE_INFO, ++travellingModeInfoCounter).apply();
                }
                startLocationUpdates();
                updateCurrentLocationOnMap();
                updateCounter = 0;
            }
        });

        buttonMyLoc.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                MiscUtil.log("My Location Button Long clicked");

                int travelModeColor = getActivity().getResources().getColor(R.color.travel_mode, null);
                int normalModeColor = getActivity().getResources().getColor(R.color.colorAccent, null);

                if (buttonMyLoc.getColorNormal() == normalModeColor)
                {
                    util.toast("Travelling Mode : On");
                    buttonMyLoc.setColorNormal(travelModeColor);
                    buttonMyLoc.setColorPressedResId(R.color.travel_mode_pressed);
                    mTravelModeOn = true;
                    int travellingModeInfoCounter = preferences.getInt(KEY_TRAVELLING_MODE_INFO, 5);
                    if (travellingModeInfoCounter < 5)
                    {
                        travellingModeInfoCounter = 5;
                    }
                    preferences.edit().putInt(KEY_TRAVELLING_MODE_INFO, ++travellingModeInfoCounter).apply();

                    if (travellingModeInfoCounter < 8)
                    {
                        util.toast("Click and hold to turn \"Off\" Travelling Mode");
                    }
                    startLocationUpdates();
                }
                else
                {
                    buttonMyLoc.setColorNormal(normalModeColor);
                    buttonMyLoc.setColorPressedResId(R.color.colorAccentPressed);
                    mTravelModeOn = false;
                    util.toast("Travelling Mode : Off");
                    preferences.edit().putInt(KEY_TRAVELLING_MODE_INFO, 9).apply();
                    stopLocationUpdates();
                }
                return true;
            }
        });
        showMyLocButton();
    }

    public void showMyLocButton()
    {
        MiscUtil.log("showMyLocButton : buttonMyLoc = " + buttonMyLoc + ", addIssueMenu = " + addIssueMenu);
        if (buttonMyLoc != null && addIssueMenu != null)
        {
            buttonMyLoc.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams addBtnLayoutParams = (RelativeLayout.LayoutParams) addIssueMenu.getLayoutParams();
            if (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                addBtnLayoutParams.setMargins(0, 0, util.dp(10), util.dp(80));
            }
            addIssueMenu.setLayoutParams(addBtnLayoutParams);
        }
    }

    private boolean isPermissionAvailable()
    {
        MiscUtil.log("isPermissionAvailable");
        return ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        MiscUtil.log("onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                MiscUtil.log("REQUEST_CODE_LOCATION_PERMISSION");
                updateCurrentLocationOnMap();
            }
            else
            {
                Snackbar snackbar = Snackbar.make(mapFragmentContainer, "App Can't be used without this permission!", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Retry", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        MiscUtil.log("onActivityResult : checkLocationSettings Again - Snack Bar");
                        updateCurrentLocationOnMap();
                    }
                });
                snackbar.show();
            }
        }
    }

    private void requestLocationPermission()
    {
        MiscUtil.log("requestLocationPermission");
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
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
                updateCurrentLocationOnMap();
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

    public void startLocationUpdates()
    {
        MiscUtil.log("startLocationUpdates");
        mLocationRequest.setFastestInterval(Integer.parseInt(preferences.getString(KEY_LOCATION_UPDATE_FREQ, "30000")));
        MiscUtil.log(mLocationRequest.getFastestInterval() + "");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates()
    {
        MiscUtil.log("stopLocationUpdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        isFirstTime = false;
        CameraPosition pos = mGoogleMap.getCameraPosition();
        preferences.edit()
                .putFloat(TILT, pos.tilt)
                .putFloat(ZOOM, pos.zoom).apply();
    }

    @Override
    public void onStop()
    {
        MiscUtil.log("onStop");
        super.onStop();
        if (mGoogleApiClient.isConnected() && isPermissionAvailable() && isLocationSettingsOn())
        {
            stopLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        MiscUtil.log("Location Changed : " + location);
        mCurrentLocation = location;
        if (!mTravelModeOn)
        {
            MiscUtil.log("mTravelModeOn : false");
            stopLocationUpdates();
            if (updateCounter < 2)
            {
                updateCurrentLocationOnMap();
                updateCounter++;
            }
        }
        else
        {
            updateCurrentLocationOnMap();
        }
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

    @SuppressLint("CommitPrefEdits")
    public void updateCurrentLocationOnMap()
    {
        MiscUtil.log("updateCurrentLocationOnMap");
        if (isPermissionAvailable())
        {
            if (isLocationSettingsOn())
            {
                if (mGoogleApiClient.isConnected())
                {
                    if (mCurrentLocation == null)
                    {
                        mCurrentLocation = LocationServices.FusedLocationApi
                                .getLastLocation(mGoogleApiClient);
                        MiscUtil.log("mCurrentLocation obtained from getLastLocation");
                    }
                    try
                    {
                        Thread.sleep(250);
                    }
                    catch (InterruptedException ignored)
                    {

                    }
                    if (mCurrentLocation != null)
                    {
                        saveLocation(mCurrentLocation);
                    }
                    else
                    {
                        MiscUtil.log("mCurrentLocation is still null");
                    }
                }
                gotoLastKnownPos(true, true);
            }
            else
            {
                checkLocationSettings();
            }
        }
        else
        {
            requestLocationPermission();
        }
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onCameraChange(CameraPosition pos)
    {
        preferences.edit()
                .putFloat(TILT, pos.tilt)
                .putFloat(ZOOM, pos.zoom).commit();
    }

    private void saveLocation(Location location)
    {
        new SaveLocationTask(mActivity,location,mGoogleMap).execute();
//        String address = "@Unknown Place";
//        Geocoder gcd = new Geocoder(mActivity);
//        try
//        {
//            List<Address> addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
//            if (addresses.size() > 0)
//            {
//                address = "@" + addresses.get(0).getFeatureName();
//            }
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//
//        CameraPosition pos = mGoogleMap.getCameraPosition();
//        preferences.edit()
//                .putFloat(TILT, pos.tilt)
//                .putFloat(ZOOM, pos.zoom)
//                .putFloat(LATITUDE, (float) location.getLatitude())
//                .putFloat(LONGITUDE, (float) location.getLongitude())
//                .putString(ADDRESS, address.trim())
//                .apply();
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        if (isPermissionAvailable() && isLocationSettingsOn() && mGoogleMap != null)
        {
            startLocationUpdates();
            updateCurrentLocationOnMap();
        }
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    public static class SaveLocationTask extends AsyncTask<Void, Void, Void>{
        Context mContext;
        Location location;
        String address = "@Unknown Place";
        GoogleMap mGoogleMap;

        public SaveLocationTask(Context mContext, Location location, GoogleMap googleMap)
        {
            this.mContext = mContext;
            this.location = location;
            this.mGoogleMap = googleMap;
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            Geocoder gcd = new Geocoder(mContext);
            try
            {
                List<Address> addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses.size() > 0)
                {
                    address = "@" + addresses.get(0).getFeatureName();
                }
            }
            catch (IOException e)
            {

                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            CameraPosition pos = mGoogleMap.getCameraPosition();
            preferences.edit()
                    .putFloat(TILT, pos.tilt)
                    .putFloat(ZOOM, pos.zoom)
                    .putFloat(LATITUDE, (float) location.getLatitude())
                    .putFloat(LONGITUDE, (float) location.getLongitude())
                    .putString(ADDRESS, address.trim())
                    .apply();
            super.onPostExecute(aVoid);
        }
    }
}