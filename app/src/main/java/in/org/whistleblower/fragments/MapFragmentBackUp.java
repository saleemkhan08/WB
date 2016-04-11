package in.org.whistleblower.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
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
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Set;

import in.org.whistleblower.AddIssueActivity;
import in.org.whistleblower.FavoritePlaceEditActivity;
import in.org.whistleblower.LocationTrackingService;
import in.org.whistleblower.LocationUpdateService;
import in.org.whistleblower.R;
import in.org.whistleblower.icon.FontAwesomeIcon;
import in.org.whistleblower.utilities.FABUtil;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.NavigationUtil;

public class MapFragmentBackUp extends SupportMapFragment implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,LocationSource.OnLocationChangedListener,
        ResultCallback<LocationSettingsResult>
{
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String LOCATION = "LOCATION";
    public static final String ZOOM = "ZOOM";
    public static final String TILT = "TILT";
    public static final String BEARING = "BEARING";
    public static final String MAP_TYPE = "mapType";
    public static final String ADDRESS = "ADDRESS";
    private static final String KEY_IS_SNACK_BAR_SHOWN = "KEY_IS_SNACK_BAR_SHOWN";
    public static final String KEY_LOCATION_SETTINGS_DIALOG_SHOWN = "locationSettingsDialogShown";
    private static final String KEY_TRAVEL_MODE = "KEY_TRAVEL_MODE";
    private static final String KEY_PERMISSION_ASKED = "permissionAsked";
    private static final String KEY_UPDATE_COUNTER = "KEY_UPDATE_COUNTER";
    private static final String KEY_TRAVELLING_MODE_INFO = "KEY_TRAVELLING_MODE_INFO";
    public static final int REQUEST_CODE_LOCATION_SETTINGS = 0x92;
    public static final String MARKER = "MARKER";
    public static final String RADIUS = "RADIUS";
    public static final String SHOW_MARKER = "SHOW_MARKER";
    public static final String ANIMATE = "ANIMATE";
    public static final int OVERLAY_DRAW_PERMISSION_CODE = 9900;
    public static final String LATLANG = "LATLANG";
    private GoogleMap mGoogleMap;
    static float accuracy;
    public static Location mCurrentLocation;
    AppCompatActivity mActivity;
    static SharedPreferences preferences;
    View mLocationSelector;
    static String action;
    private static boolean isLocationPermissionAsked;
    protected LocationRequest mLocationRequest;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationSettingsRequest mLocationSettingsRequest;
    public static final String KEY_LOCATION_UPDATE_FREQ = "updateFreq";

    View map_fab_buttons;
    private FloatingActionButton buttonMyLoc;
    private MiscUtil mUtil;
    private static int updateCounter;
    private boolean mTravelModeOn;
    public static boolean permissionAsked = false;
    static boolean isFirstTime;
    private BroadcastReceiver mLocationReceiver;

    Bundle bundle;

    public MapFragmentBackUp()
    {
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
        mLocationRequest.setInterval(Integer.parseInt(preferences.getString(KEY_LOCATION_UPDATE_FREQ,"30000")));
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

    private void initializeMap()
    {
        preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        map_fab_buttons = mActivity.findViewById(R.id.map_fab_buttons);
        map_fab_buttons.setVisibility(View.VISIBLE);
        buttonMyLoc = (FloatingActionButton) mActivity.findViewById(R.id.my_loc);
        bundle = getArguments();
        reloadMapParameters(bundle);
        mUtil = new MiscUtil(mActivity);
        setUpMyLocationButton();
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


    @SuppressLint("CommitPrefEdits")
    public void updateCurrentLocationOnMap()
    {
        MiscUtil.log("updateCurrentLocationOnMap");
        if (isLocationPermissionAvailable())
        {
            if (isLocationSettingsOn())
            {
                if (mGoogleApiClient.isConnected())
                {
                    if (mCurrentLocation == null)
                    {
                        mCurrentLocation = LocationServices.FusedLocationApi
                                .getLastLocation(mGoogleApiClient);
                    }
                    if (mCurrentLocation != null)
                    {
                        accuracy = mCurrentLocation.getAccuracy();
                        saveLocationInPreference(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                    }
                }
                gotoPos(true, true);
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

    protected void checkLocationSettings()
    {
        MiscUtil.log("checkLocationSettings");
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(mGoogleApiClient, mLocationSettingsRequest);
        result.setResultCallback(this);
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
                preferences.edit().putBoolean(KEY_LOCATION_SETTINGS_DIALOG_SHOWN, false).apply();
                MiscUtil.log("My Location Button Clicked: updateCurrentLoc");
                int travellingModeInfoCounter = preferences.getInt(KEY_TRAVELLING_MODE_INFO, 0);
                if (travellingModeInfoCounter < 5)
                {
                    mUtil.toast("Click and hold to turn \"On\" Travelling Mode");
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
                    mUtil.toast("Travelling Mode : On");
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
                        mUtil.toast("Click and hold to turn \"Off\" Travelling Mode");
                    }
                    startLocationUpdates();
                }
                else
                {
                    buttonMyLoc.setColorNormal(normalModeColor);
                    buttonMyLoc.setColorPressedResId(R.color.colorAccentPressed);
                    mTravelModeOn = false;
                    mUtil.toast("Travelling Mode : Off");
                    preferences.edit().putInt(KEY_TRAVELLING_MODE_INFO, 9).apply();
                    stopLocationUpdates();
                }
                return true;
            }
        });

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mActivity = (AppCompatActivity) getActivity();
        initializeMap();
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
        registerLocationReceiver();
    }

    private void registerLocationReceiver()
    {
        mLocationReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                mCurrentLocation = intent.getParcelableExtra(MapFragmentBackUp.LOCATION);
                updateCurrentLocationOnMap();
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("LOCATION_UPDATE");
        getActivity().registerReceiver(mLocationReceiver, intentFilter);
    }


    @Override
    public void onStop()
    {
        super.onStop();
        map_fab_buttons.setVisibility(View.GONE);
        if(null != mLocationReceiver)
        {
            getActivity().unregisterReceiver(mLocationReceiver);
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState)
    {
        super.onViewStateRestored(savedInstanceState);
        if (preferences != null)
        {
            preferences.edit().putBoolean(MapFragmentBackUp.KEY_LOCATION_SETTINGS_DIALOG_SHOWN, false).apply();
        }

        if (savedInstanceState != null)
        {
            MiscUtil.log("savedInstanceState not null");
            mTravelModeOn = savedInstanceState.getBoolean(KEY_TRAVEL_MODE);
            permissionAsked = savedInstanceState.getBoolean(KEY_PERMISSION_ASKED);
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

    @Override
    public void onResume()
    {
        super.onResume();
        mGoogleMap = getMap();
        if (bundle != null)
        {
            gotoPos(bundle.getBoolean(ANIMATE, false), bundle.getBoolean(SHOW_MARKER, false));
        }
        else
        {
            gotoPos(false, false);
        }
        NavigationUtil.highlightMenu(mActivity, R.id.nav_map);
        mapFragmentContainer = mActivity.findViewById(android.R.id.content);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        if (isLocationPermissionAvailable())
        {
            mGoogleMap.setMyLocationEnabled(false);

            // mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
        else
        {
            if (!isLocationPermissionAsked)
            {
                requestLocationPermission();
                isLocationPermissionAsked = true;
            }
        }
        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener()
        {
            @Override
            public boolean onMyLocationButtonClick()
            {
                return false;
            }
        });
        mActivity.findViewById(R.id.ok_map).setOnClickListener(this);
        mLocationSelector = mActivity.findViewById(R.id.select_location);
        mLocationRequest.setFastestInterval(Integer.parseInt(preferences.getString(KEY_LOCATION_UPDATE_FREQ, "30000")));//TODO need to check Why this line is required

        mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener()
        {
            @Override
            public void onCameraChange(CameraPosition cameraPosition)
            {
                Intent intent = new Intent(mActivity, LocationUpdateService.class);
                intent.putExtra(LATLANG, cameraPosition.target);
                mActivity.startService(intent);
            }
        });
    }

    public void gotoPos(boolean animate, boolean addMarker)
    {
        MiscUtil.log("gotoLastKnownPos, animate : " + animate + ", addMarker : " + addMarker);
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(getLastKnownPos());
        int mapType = Integer.parseInt(preferences.getString(MAP_TYPE, "1"));
        if (mGoogleMap.getMapType() != mapType)
        {
            mGoogleMap.setMapType(mapType);
        }

        if (addMarker)
        {
            mGoogleMap.clear();
            int marker = R.drawable.my_loc_marker;
            int radius = (int) accuracy;
            if (bundle != null)
            {
                marker = bundle.getInt(MARKER, R.drawable.marker);
                radius = bundle.getInt(RADIUS, 100);
            }
            mGoogleMap.addMarker(new MarkerOptions()
                    .icon(MiscUtil.getMapMarker(mActivity, marker, 80)).position(getLatLng()));
            mGoogleMap.addCircle(new CircleOptions()
                    .center(getLatLng())
                    .radius(radius)
                    .strokeWidth(2)
                    .strokeColor(getResources().getColor(R.color.colorAccent, null))
                    .fillColor(getResources().getColor(R.color.my_location_radius, null)));
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

    public static CameraPosition getLastKnownPos()
    {
        return new CameraPosition(getLatLng(), getZoom(), getTilt(), getBearing());
    }

    public static LatLng getLatLng()
    {
        return new LatLng(preferences.getFloat(LATITUDE, 12.9667f), preferences.getFloat(LONGITUDE, 77.5667f));
    }

    public static void setLatLng(LatLng latLng)
    {
        preferences.edit()
                .putFloat(LATITUDE, (float) latLng.latitude)
                .putFloat(LONGITUDE, (float) latLng.longitude)
                .apply();
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

    @Override
    public void onClick(View v)
    {
        if (MiscUtil.isConnected(mActivity))
        {
            if (mGoogleMap != null)
            {
                LatLng latLng = mGoogleMap.getCameraPosition().target;
                setLatLng(latLng);
                Log.d("Lucifer", action);
                switch (action)
                {
                    case FABUtil.SET_ALARM:
                        mActivity.startService(new Intent(mActivity, LocationTrackingService.class));
                        break;

                    case FABUtil.ADD_ISSUE:
                        Intent addIssueIntent = new Intent(mActivity, AddIssueActivity.class);
                        startActivity(addIssueIntent);
                        break;

                    case FABUtil.ADD_FAV_PLACE:
                        Intent intent = new Intent(mActivity, FavoritePlaceEditActivity.class);
                        intent.putExtra("LatLang", latLng);
                        startActivity(intent);
                        break;

                }
                saveLocationInPreference(latLng);
                mLocationSelector.setVisibility(View.GONE);
            }
        }
        else
        {
            Toast.makeText(mActivity, "No Internet!", Toast.LENGTH_SHORT).show();
        }
    }

    public void reloadMapParameters(Bundle bundle)
    {
        if (bundle != null)
        {
            if (bundle != null)
            {
                Set<String> keys = bundle.keySet();
                if (keys.contains(LATITUDE) && keys.contains(LONGITUDE))
                {
                    preferences.edit()
                            .putFloat(LATITUDE, bundle.getFloat(LATITUDE, 0))
                            .putFloat(LONGITUDE, bundle.getFloat(LONGITUDE, 0))
                            .apply();
                }
                if (keys.contains(FABUtil.ACTION))
                {
                    action = bundle.getString(FABUtil.ACTION);
                }
            }
        }
    }

    public static final int REQUEST_CODE_LOCATION_PERMISSION = 0x91;

    private static View mapFragmentContainer;

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

    public boolean isLocationPermissionAvailable()
    {
        return ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {

    }

    @Override
    public void onConnected(Bundle bundle)
    {

    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onLocationChanged(Location location)
    {
        MiscUtil.log("Location Changed : " + location);
        Toast.makeText(mActivity, "Longitude : "+ location.getLongitude(), Toast.LENGTH_SHORT).show();
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

    public void saveLocationInPreference(LatLng mLatLng)
    {
        preferences.edit()
                .putFloat(MapFragmentBackUp.LATITUDE, (float) mLatLng.latitude)
                .putFloat(MapFragmentBackUp.LONGITUDE, (float) mLatLng.longitude)
                .apply();
    }
}