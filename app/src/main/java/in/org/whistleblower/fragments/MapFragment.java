package in.org.whistleblower.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import in.org.whistleblower.R;
import in.org.whistleblower.asynctasks.GeoCoderTask;
import in.org.whistleblower.interfaces.GeoCodeListener;
import in.org.whistleblower.interfaces.LocationChangeListener;
import in.org.whistleblower.interfaces.PlacesResultListener;
import in.org.whistleblower.services.LocationTrackingService;
import in.org.whistleblower.utilities.FABUtil;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.NavigationUtil;
import in.org.whistleblower.utilities.PermissionUtil;
import in.org.whistleblower.utilities.TouchableWrapper;

public class MapFragment extends SupportMapFragment implements
        View.OnClickListener,
        LocationChangeListener,
        PlacesResultListener,
        TouchableWrapper.OnMapTouchListener,
        GeoCodeListener,
        GoogleMap.OnCameraChangeListener
{

    private View mOriginalContentView;
    private int searchBarMargin;
    private boolean isSubmitButtonShown;
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String ZOOM = "ZOOM";
    public static final String TILT = "TILT";
    public static final String BEARING = "BEARING";

    public static final String MAP_TYPE = "mapType";
    public static final String ADDRESS = "ADDRESS";
    private static final String KEY_TRAVELLING_MODE_DISP_COUNTER = "KEY_TRAVELLING_MODE_DISP_COUNTER";
    public static final String MARKER = "MARKER";
    public static final String RADIUS = "RADIUS";
    public static final String SHOW_MARKER = "SHOW_MARKER";
    public static final String ANIMATE = "ANIMATE";
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 91;
    private static final String MY_LOC = "MY_LOC";
    private static final String ACCURACY = "ACCURACY";
    private Map<String, Marker> markerMap = new HashMap<>();
    private Map<String, Circle> circleMap = new HashMap<>();
    private GoogleMap mGoogleMap;

    AppCompatActivity mActivity;
    static SharedPreferences preferences;
    private View mSubmitButton;
    private ImageView searchIcon;
    private String action;
    ViewGroup map_fab_buttons;
    RelativeLayout searchBar;
    private FloatingActionButton buttonMyLoc;
    private static boolean mTravelModeOn;
    int travelModeColor, normalModeColor;
    Bundle bundle;
    private Location mCurrentLocation;
    private AppBarLayout mToolbar;
    private TextView searchText;
    private ProgressBar searchProgress;

    LocationTrackingService mLocationTrackingService;
    boolean isLocationServiceBound = false;
    private boolean isShowingMyLocation;
    private boolean isMapMovedManually;
    private static int retryAttemptsCount;
    private GeoCoderTask mGeoCoderTask;
    private LatLng mGeoCodeLatLng;
    //Flow #1
    //Start and bind service
    //Goto last know location
    //once location is obtained goto obtained location
    //stop service

    //Flow #2
    //after above flow goto location other than my location
    //click on my location
    //Start Service
    //Goto last know location
    //once location is obtained goto obtained location
    //stop service


    //Implementation done
    public MapFragment()
    {
        Log.d("FlowLogs", "Constructor");
    }

    //Implementation done
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        Log.d("FlowLogs", "onCreateView");
        mOriginalContentView = super.onCreateView(inflater, parent, savedInstanceState);
        TouchableWrapper mTouchView = new TouchableWrapper(getActivity(), this);
        mTouchView.addView(mOriginalContentView);
        return mTouchView;
    }

    //Implementation done
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Log.d("FlowLogs", "onActivityCreated");
        setRetainInstance(true);
        mActivity = (AppCompatActivity) getActivity();
        startLocationTrackingServiceAndBind();
        Log.d("FlowLogs", "Start Service Called");

        preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mToolbar = (AppBarLayout) mActivity.findViewById(R.id.appBarLayout);

        travelModeColor = mActivity.getColor(R.color.travel_mode);
        normalModeColor = mActivity.getColor(R.color.colorAccent);

        map_fab_buttons = (ViewGroup) mActivity.findViewById(R.id.map_fab_buttons);
        searchBar = (RelativeLayout) mActivity.findViewById(R.id.searchBar);

        searchText = (TextView) mActivity.findViewById(R.id.hoverPlaceName);
        searchProgress = (ProgressBar) mActivity.findViewById(R.id.searchProgress);
        searchText.setOnClickListener(this);

        searchIcon = (ImageView) mActivity.findViewById(R.id.searchIcon);
        searchIcon.setOnClickListener(this);

        mSubmitButton = mActivity.findViewById(R.id.submitButton);
        mSubmitButton.setOnClickListener(this);

        buttonMyLoc = (FloatingActionButton) mActivity.findViewById(R.id.my_loc);
        setUpMyLocationButton();
    }

    public void showSubmitButtonAndHideSearchIcon(int drawableId)
    {
        mSubmitButton.setVisibility(View.VISIBLE);
        isSubmitButtonShown = true;
        searchIcon.setImageDrawable(mActivity.getDrawable(drawableId));
    }

    public void hideSubmitButtonAndShowSearchIcon()
    {
        isSubmitButtonShown = false;
        mSubmitButton.setVisibility(View.INVISIBLE);
        searchIcon.setImageDrawable(mActivity.getDrawable(R.drawable.search_primary_dark));
    }

    //Implementation done
    @Override
    public void onStart()
    {
        super.onStart();
        bundle = getArguments();
        reloadMapParameters(bundle);
    }

    //Implementation done
    public void reloadMapParameters(Bundle bundle)
    {
        if (bundle != null)
        {
            Set<String> keys = bundle.keySet();
            if (keys.contains(LATITUDE) && keys.contains(LONGITUDE))
            {
                mCurrentLocation.setLatitude(Double.parseDouble(bundle.getString(LATITUDE)));
                mCurrentLocation.setLongitude(Double.parseDouble(bundle.getString(LONGITUDE)));
                mCurrentLocation.setAccuracy(bundle.getFloat(ACCURACY));
                isShowingMyLocation = false;
            }
            if (keys.contains(FABUtil.ACTION))
            {
                action = bundle.getString(FABUtil.ACTION);
            }
        }
        else
        {
            isShowingMyLocation = true;
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        searchText.clearFocus();
        map_fab_buttons.setVisibility(View.VISIBLE);
        searchBar.setVisibility(View.VISIBLE);//
        mGoogleMap = getMap();
        if (bundle != null)
        {
            gotoCurrentPos(bundle.getBoolean(ANIMATE, false));
        }
        else
        {
            Log.d("FlowLogs", "onResume : gotoMyLocation");
            Toast.makeText(mActivity, "onResume isShowingMyLocation : " + isShowingMyLocation, Toast.LENGTH_SHORT).show();
            gotoMyLocation(false);
        }
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);

        if (PermissionUtil.isLocationPermissionAvailable())
        {
            mGoogleMap.setMyLocationEnabled(false);
        }
        NavigationUtil.highlightNavigationDrawerMenu(mActivity, R.id.nav_map);
        mGoogleMap.setOnCameraChangeListener(this);

        if (mLocationTrackingService != null)
        {
            mLocationTrackingService.registerLocationChangedListener(this);
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        mLocationTrackingService.unRegisterLocationChangedListener();
        map_fab_buttons.setVisibility(View.GONE);
        searchBar.setVisibility(View.GONE);
    }

    public void setUpMyLocationButton()
    {
        /*buttonMyLoc.setIconDrawable(mActivity.getDrawable(R.mipmap.my_loc_icon));
        buttonMyLoc.setStrokeVisible(false);
        */
        buttonMyLoc.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showTravellingModeHint();
                isShowingMyLocation = true;
                startLocationTrackingServiceAndBind();
                gotoMyLocation(true);
            }
        });

        buttonMyLoc.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                if (!mTravelModeOn)
                {
                    Toast.makeText(mActivity, "Travelling Mode : On", Toast.LENGTH_SHORT).show();
                    mTravelModeOn = true;
                    updateTravellingModeUI();
                    showTravellingModeHint();
                    preferences.edit()
                            .putBoolean(LocationTrackingService.KEY_TRAVELLING_MODE, true)
                            .putInt(KEY_TRAVELLING_MODE_DISP_COUNTER, 6)
                            .commit();
                    isShowingMyLocation = true;
                    startLocationTrackingServiceAndBind();
                    gotoMyLocation(true);
                }
                else
                {
                    Toast.makeText(mActivity, "Travelling Mode : Off", Toast.LENGTH_SHORT).show();
                    mTravelModeOn = false;
                    updateTravellingModeUI();
                    preferences.edit()
                            .putInt(KEY_TRAVELLING_MODE_DISP_COUNTER, 9)
                            .putBoolean(LocationTrackingService.KEY_TRAVELLING_MODE, false)
                            .commit();
                    mLocationTrackingService.stopService();
                }
                return true;
            }
        });
        updateTravellingModeUI();
    }

    private void updateTravellingModeUI()
    {
        if (mTravelModeOn)
        {
            buttonMyLoc.setColorNormal(travelModeColor);
            buttonMyLoc.setColorPressedResId(R.color.travel_mode_pressed);
        }
        else
        {
            buttonMyLoc.setColorNormal(normalModeColor);
            buttonMyLoc.setColorPressedResId(R.color.colorAccentPressed);
        }
    }

    private void showTravellingModeHint()
    {
        int travellingModeInfoCounter = preferences.getInt(KEY_TRAVELLING_MODE_DISP_COUNTER, 0);
        if (travellingModeInfoCounter < 5)
        {
            Toast.makeText(mActivity, "Click and hold to turn \"On\" Travelling Mode", Toast.LENGTH_SHORT).show();
            preferences.edit().putInt(KEY_TRAVELLING_MODE_DISP_COUNTER
                    , ++travellingModeInfoCounter).apply();
        }
        else if (travellingModeInfoCounter > 5 && travellingModeInfoCounter < 8)
        {
            Toast.makeText(mActivity, "Click and hold to turn \"Off\" Travelling Mode", Toast.LENGTH_SHORT).show();
            preferences.edit().putInt(KEY_TRAVELLING_MODE_DISP_COUNTER
                    , ++travellingModeInfoCounter).apply();
        }
    }

    private void startLocationTrackingServiceAndBind()
    {
        Log.d("FlowLogs", "startLocationTrackingServiceAndBind");
        Intent intent = new Intent(mActivity, LocationTrackingService.class);
        mActivity.startService(intent);
        Log.d("FlowLogs", "isLocationServiceBound : " + isLocationServiceBound);
        if (!isLocationServiceBound)
        {
            Log.d("FlowLogs", "bindService");
            mActivity.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

    }

    public void gotoCurrentPos(boolean animate)
    {
        setMapType();
        showNearByPlaces();
        moveMap(animate);
    }

    public static CameraPosition getLastKnownPos()
    {
        return new CameraPosition(getLatLng(), getZoom(), getTilt(), getBearing());
    }

    public static LatLng getLatLng()
    {
        double latitude = Double.parseDouble(preferences.getString(LATITUDE, "12.9667"));
        double longitude = Double.parseDouble(preferences.getString(LONGITUDE, "77.5667"));
        return new LatLng(latitude, longitude);
    }

    public static void setLatLng(LatLng latLng)
    {
        preferences.edit()
                .putString(LATITUDE, "" + latLng.latitude)
                .putString(LONGITUDE, "" + latLng.longitude)
                .apply();
    }

    public static float getBearing()
    {
        return preferences.getFloat(BEARING, 0);
    }

    public static float getZoom()
    {
        float zoom = preferences.getFloat(ZOOM, 17);
        if (zoom < 3)
        {
            zoom = 17;
        }
        return zoom;
    }

    public static float getTilt()
    {
        return preferences.getFloat(TILT, 0);
    }

    private void gotoMyLocation(boolean animate)
    {
        Log.d("FlowLogs", "gotoMyLocation : " + animate + ", isShowingMyLocation : " + isShowingMyLocation);
        if (mGoogleMap != null)
        {
            showMyLocDot(getLatLng(), MY_LOC);
            showAccuracyCircle(getLatLng(), getAccuracy(), MY_LOC);
            if (isShowingMyLocation)
            {
                setCamera(CameraUpdateFactory.newCameraPosition(getLastKnownPos()), animate);
            }
        }
    }

    public static double distFrom(double lat1, double lng1, double lat2, double lng2)
    {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (earthRadius * c);
    }

    private float getAccuracy()
    {
        return preferences.getFloat(ACCURACY, 500);
    }

    private void setCamera(CameraUpdate update, boolean animate)
    {
        if (animate)
        {
            mGoogleMap.animateCamera(update);
        }
        else
        {
            mGoogleMap.moveCamera(update);
        }
        isMapMovedManually = false;
    }

    private void setCameraPosition(Location location)
    {
        preferences.edit().putString(LATITUDE, "" + location.getLatitude())
                .putString(LONGITUDE, "" + location.getLongitude())
                .putFloat(ACCURACY, location.getAccuracy())
                .commit();
    }

    private void moveMap(boolean animate)
    {
        CameraUpdate update;
        if (mCurrentLocation != null)
        {
            update = CameraUpdateFactory.newCameraPosition(
                    new CameraPosition(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), getZoom(), getTilt(), getBearing()));
        }
        else
        {
            update = CameraUpdateFactory.newCameraPosition(getLastKnownPos());
        }
        setCamera(update, animate);
    }

    private void setMapType()
    {
        int mapType = Integer.parseInt(preferences.getString(MAP_TYPE, "1"));
        if (mGoogleMap.getMapType() != mapType)
        {
            mGoogleMap.setMapType(mapType);
        }
    }

    private void showNearByPlaces()
    {
    }

    @Override
    public void onClick(View v)
    {
        if (MiscUtil.isConnected(mActivity))
        {
            switch (v.getId())
            {
//              case R.id.hoverPlaceName:
                case R.id.searchIcon:
                    try
                    {
                        PlaceAutocomplete.IntentBuilder builder = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN);
                        startActivityForResult(builder.build(mActivity), PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    }
                    catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e)
                    {
                        //TODO
                    }
                    break;
                case R.id.submitButton:
                    /*if (mGoogleMap != null)
                    {
                        LatLng latLng = mGoogleMap.getCameraPosition().target;
                        switch (action)
                        {
                            case FABUtil.SET_ALARM:
                                //mActivity.startLocationTrackingServiceAndBind(new Intent(mActivity, LocationTrackingService.class));
                                //Toast.makeText(mActivity, "Location : " + mLocationTrackingService.getCurrentLocation(), Toast.LENGTH_SHORT).show();
                                break;

                            case FABUtil.ADD_ISSUE:
                                Image.captureImage(mActivity);
                                break;

                            case FABUtil.ADD_FAV_PLACE:
                                Intent intent = new Intent(mActivity, FavoritePlaceEditActivity.class);
                                intent.putExtra("LatLang", latLng);
                                startActivity(intent);
                                break;
                        }
                        hideSubmitButtonAndShowSearchIcon();
                    }*/
                    Toast.makeText(mActivity, "Submit : " + searchText.getText(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        else
        {
            Toast.makeText(mActivity, "No Internet!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View getView()
    {
        return mOriginalContentView;
    }

    @Override
    public void onActionUp()
    {
        Slide slide = new Slide();
        TransitionManager.beginDelayedTransition(map_fab_buttons, slide);
        map_fab_buttons.setVisibility(View.VISIBLE);

        TransitionManager.beginDelayedTransition(searchBar);

        RelativeLayout.LayoutParams searchBarLayoutParams = (RelativeLayout.LayoutParams) searchBar.getLayoutParams();
        searchBarLayoutParams.topMargin = searchBarMargin;
        searchBar.setLayoutParams(searchBarLayoutParams);

        mToolbar.animate().translationY(0).start();
    }

    @Override
    public void onActionDown()
    {
        Slide slide = new Slide();
        TransitionManager.beginDelayedTransition(map_fab_buttons, slide);
        isMapMovedManually = true;
        map_fab_buttons.setVisibility(View.GONE);
        mToolbar.animate().translationY(-mToolbar.getBottom()).start();

        TransitionManager.beginDelayedTransition(searchBar);

        RelativeLayout.LayoutParams searchBarLayoutParams = (RelativeLayout.LayoutParams) searchBar.getLayoutParams();
        searchBarMargin = searchBarLayoutParams.topMargin;
        searchBarLayoutParams.topMargin = 0;
        searchBar.setLayoutParams(searchBarLayoutParams);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Toast.makeText(mActivity, "requestCode : " + requestCode, Toast.LENGTH_SHORT).show();
        switch (requestCode)
        {
            case MapFragment.PLACE_AUTOCOMPLETE_REQUEST_CODE:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        Place place = PlaceAutocomplete.getPlace(mActivity, data);
                        mCurrentLocation.setAccuracy(0);
                        mCurrentLocation.setLongitude(place.getLatLng().longitude);
                        mCurrentLocation.setLatitude(place.getLatLng().latitude);
                        gotoCurrentPos(true);
                        isShowingMyLocation = false;
                        searchText.setText(place.getAddress());
                        break;
                    case PlaceAutocomplete.RESULT_ERROR:
                        //TODO Status status = PlaceAutocomplete.getStatus(mActivity, data);
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;
        }
    }

    ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.d("FlowLogs", "onServiceConnected");
            LocationTrackingService.LocalBinder binder = (LocationTrackingService.LocalBinder) service;
            mLocationTrackingService = binder.getService();
            mLocationTrackingService.registerLocationChangedListener(MapFragment.this);
            isLocationServiceBound = true;
            Log.d("FlowLogs", "Service Bounded : " + isLocationServiceBound);
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            isLocationServiceBound = false;
            Log.d("FlowLogs", "Service Bounded : " + isLocationServiceBound);
        }
    };

    @Override
    public void onLocationChanged(Location location)
    {
        Toast.makeText(mActivity, "Location : " + location + "\nisShowingMyLocation : " + isShowingMyLocation, Toast.LENGTH_SHORT).show();
        Log.d("FlowLogs", "onLocationChanged : isShowingMyLocation : " + isShowingMyLocation);
        setCameraPosition(location);
        gotoMyLocation(true);
    }

    void showMyLocDot(LatLng latLng, String key)
    {
        Log.d("FlowLogs", "showMyLocDot");
        Marker marker = null;
        if (markerMap.containsKey(key))
        {
            Log.d("FlowLogs", "getting marker from map");
            marker = markerMap.get(key);
        }
        if (marker != null)
        {
            Log.d("FlowLogs", "marker setPosition");
            marker.setPosition(latLng);
        }
        else
        {
            Log.d("FlowLogs", "creating marker first time");
            markerMap.put(key, mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .anchor(0.5f, 0.5f)
                    .flat(true)
                    .icon(MiscUtil.getMapMarker(mActivity, R.mipmap.my_loc_dot_48, 17))));
        }
    }

    void showAccuracyCircle(LatLng latLng, float accuracy, String key)
    {
        Circle circle = null;
        if (circleMap.containsKey(key))
        {
            circle = circleMap.get(key);
        }
        if (circle != null)
        {
            circle.setCenter(latLng);
            circle.setRadius(accuracy);
        }
        else
        {
            circleMap.put(key, mGoogleMap.addCircle(new CircleOptions()
                    .radius(accuracy)
                    .strokeWidth(2)
                    .strokeColor(mActivity.getColor(R.color.colorAccent))
                    .fillColor(mActivity.getColor(R.color.my_location_radius))
                    .center(latLng)));
        }
    }

    @Override
    public void onListObtained(List<HashMap<String, String>> list)
    {
        int index = 0;
        for (HashMap<String, String> map : list)
        {
            Log.d("PlacesList", "List Index" + index++);
            for (String key : map.keySet())
            {
                Log.d("PlacesList", "Key :" + key + ", Value : " + map.get(key));
            }
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition)
    {
        double dist = distFrom(cameraPosition.target.latitude, cameraPosition.target.longitude, getLatLng().latitude, getLatLng().longitude);
        int threshold = 1000;
        if (isMapMovedManually)
        {
            threshold = 100;
        }
        if (dist > threshold)
        {
            isShowingMyLocation = false;
        }
        Toast.makeText(mActivity, "dist : " + dist + ", isShowingMyLocation : " + isShowingMyLocation, Toast.LENGTH_SHORT).show();
        mGeoCodeLatLng = cameraPosition.target;

        updateLocationInfo();
        retryAttemptsCount = 0;
    }

    void updateLocationInfo()
    {
        showSearchProgress();
        if(mGeoCoderTask != null)
        {
            AsyncTask.Status status = mGeoCoderTask.getStatus();
            if( status == AsyncTask.Status.RUNNING || status == AsyncTask.Status.PENDING)
            {
                mGeoCoderTask.cancel(true);
            }
        }
        mGeoCoderTask = new GeoCoderTask(mActivity, mGeoCodeLatLng, this);
        mGeoCoderTask.execute(retryAttemptsCount++);
    }

    @Override
    public void onAddressObtained(String result)
    {
        hideSearchProgress();
        if(result == null || "".equals(result))
        {
            searchText.setText("Unknown Place...");
        }
        else
        {
            searchText.setText(result);

        }
    }

    void showSearchProgress()
    {
        searchProgress.setVisibility(View.VISIBLE);
        if(isSubmitButtonShown)
        {
            mSubmitButton.setVisibility(View.INVISIBLE);
        }
    }

    private void hideSearchProgress()
    {
        searchProgress.setVisibility(View.GONE);
        if(isSubmitButtonShown)
        {
            mSubmitButton.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onGeoCodingFailed()
    {
        hideSearchProgress();
        if(retryAttemptsCount < 10)
        {
            updateLocationInfo();
        }else
        {
            onAddressObtained(null);
        }
    }

    @Override
    public void onCancelled()
    {
        hideSearchProgress();
        retryAttemptsCount = 0;
    }
}