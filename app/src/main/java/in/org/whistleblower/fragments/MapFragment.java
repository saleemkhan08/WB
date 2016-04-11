package in.org.whistleblower.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Set;

import in.org.whistleblower.FavoritePlaceEditActivity;
import in.org.whistleblower.LocationTrackingService;
import in.org.whistleblower.R;
import in.org.whistleblower.actions.Image;
import in.org.whistleblower.icon.FontAwesomeIcon;
import in.org.whistleblower.utilities.FABUtil;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.NavigationUtil;
import in.org.whistleblower.utilities.PermissionUtil;
import in.org.whistleblower.utilities.TouchableWrapper;

public class MapFragment extends SupportMapFragment implements
        View.OnClickListener,
        TouchableWrapper.OnMapTouchListener
{
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String LOCATION = "LOCATION";
    public static final String ZOOM = "ZOOM";
    public static final String TILT = "TILT";
    public static final String BEARING = "BEARING";
    public static final String MAP_TYPE = "mapType";
    public static final String ADDRESS = "ADDRESS";
    private static final String KEY_TRAVEL_MODE = "KEY_TRAVEL_MODE";
    private static final String KEY_TRAVELLING_MODE_DISP_COUNTER = "KEY_TRAVELLING_MODE_DISP_COUNTER";
    public static final String MARKER = "MARKER";
    public static final String RADIUS = "RADIUS";
    public static final String SHOW_MARKER = "SHOW_MARKER";
    public static final String ANIMATE = "ANIMATE";
    public static final int OVERLAY_DRAW_PERMISSION_CODE = 9900;
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 91;
    private GoogleMap mGoogleMap;
    static float accuracy;
    public static Location mCurrentLocation;
    AppCompatActivity mActivity;
    static SharedPreferences preferences;
    View mLocationSelector;
    static String action;
    ViewGroup map_fab_buttons;
    RelativeLayout searchBar;
    private FloatingActionButton buttonMyLoc;
    private MiscUtil mUtil;
    private static boolean mTravelModeOn;
    private BroadcastReceiver mLocationReceiver;
    int travelModeColor, normalModeColor;
    Bundle bundle;
    private AppBarLayout mToolbar;
    private TextView searchText;

    public MapFragment()
    {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        travelModeColor = getResources().getColor(R.color.travel_mode, null);
        normalModeColor = getResources().getColor(R.color.colorAccent, null);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState)
    {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null)
        {
            MiscUtil.log("savedInstanceState not null");
            mTravelModeOn = savedInstanceState.getBoolean(KEY_TRAVEL_MODE);
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mActivity = (AppCompatActivity) getActivity();
        mToolbar = (AppBarLayout) mActivity.findViewById(R.id.appBarLayout);
        initializeMap();
        registerLocationReceiver();
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
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        if (PermissionUtil.isLocationPermissionAvailable())
        {
            mGoogleMap.setMyLocationEnabled(false);
            // mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
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
        searchBar.setOnClickListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        MiscUtil.log("Map Fragment onSaveInstanceState");
        CameraPosition pos = mGoogleMap.getCameraPosition();
        outState.putFloat(LATITUDE, (float) pos.target.latitude);
        outState.putFloat(LONGITUDE, (float) pos.target.longitude);
        outState.putFloat(TILT, pos.tilt);
        outState.putFloat(BEARING, pos.bearing);
        outState.putFloat(ZOOM, pos.zoom);
    }


    @Override
    public void onStop()
    {
        super.onStop();
        map_fab_buttons.setVisibility(View.GONE);
        searchBar.setVisibility(View.GONE);
        if (null != mLocationReceiver)
        {
            mActivity.unregisterReceiver(mLocationReceiver);
        }
    }

    private void initializeMap()
    {
        preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        map_fab_buttons = (ViewGroup) mActivity.findViewById(R.id.map_fab_buttons);
        searchBar = (RelativeLayout) mActivity.findViewById(R.id.searchBar);
        searchBar.setVisibility(View.VISIBLE);
        searchText =(TextView) mActivity.findViewById(R.id.hoverPlaceName);
        map_fab_buttons.setVisibility(View.VISIBLE);
        buttonMyLoc = (FloatingActionButton) mActivity.findViewById(R.id.my_loc);
        bundle = getArguments();
        reloadMapParameters(bundle);
        mUtil = new MiscUtil(mActivity);
        setUpMyLocationButton();
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

    public void setUpMyLocationButton()
    {
        //TODO set the icon in xml
        buttonMyLoc.setIconDrawable(mUtil.getIcon(FontAwesomeIcon.SCREENSHOT, R.color.white));
        buttonMyLoc.setStrokeVisible(false);
        buttonMyLoc.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showTravellingModeHint();
                preferences.edit()
                        .putBoolean(LocationTrackingService.KEY_GET_LOCATION, true)
                        .commit();
                startService(LocationTrackingService.GET_LOCATION_ACTION);
            }
        });

        buttonMyLoc.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                MiscUtil.log("My Location Button Long clicked");
                if (!mTravelModeOn)
                {
                    mUtil.toast("Travelling Mode : On");
                    updateTravellingModeUI();
                    mTravelModeOn = true;
                    showTravellingModeHint();
                    preferences.edit()
                            .putBoolean(LocationTrackingService.KEY_TRAVELLING_MODE, true)
                            .putInt(KEY_TRAVELLING_MODE_DISP_COUNTER, 6)
                            .commit();
                    startService(LocationTrackingService.KEY_TRAVELLING_MODE);
                }
                else
                {
                    mUtil.toast("Travelling Mode : Off");
                    updateTravellingModeUI();
                    mTravelModeOn = false;
                    preferences.edit()
                            .putInt(KEY_TRAVELLING_MODE_DISP_COUNTER, 9)
                            .apply();
                }
                return true;
            }
        });
        updateTravellingModeUI();
    }

    private void startService(String extra)
    {
        Intent getLocationIntent = new Intent(mActivity, LocationTrackingService.class);
        getLocationIntent.putExtra(extra, true);
        mActivity.startService(getLocationIntent);
    }

    private void showTravellingModeHint()
    {
        int travellingModeInfoCounter = preferences.getInt(KEY_TRAVELLING_MODE_DISP_COUNTER, 0);
        if (travellingModeInfoCounter < 5)
        {
            mUtil.toast("Click and hold to turn \"On\" Travelling Mode");
            preferences.edit().putInt(KEY_TRAVELLING_MODE_DISP_COUNTER
                    , ++travellingModeInfoCounter).apply();
        }
        else if (travellingModeInfoCounter > 5 && travellingModeInfoCounter < 8)
        {
            mUtil.toast("Click and hold to turn \"Off\" Travelling Mode");
            preferences.edit().putInt(KEY_TRAVELLING_MODE_DISP_COUNTER
                    , ++travellingModeInfoCounter).apply();
        }
    }


    private void registerLocationReceiver()
    {
        mLocationReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                mCurrentLocation = intent.getParcelableExtra(MapFragment.LOCATION);
                gotoPos(true, true);
                if (!mTravelModeOn)
                {
                    if (null != mLocationReceiver)
                    {
                        mActivity.unregisterReceiver(mLocationReceiver);
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationTrackingService.GET_LOCATION_ACTION);
        mActivity.registerReceiver(mLocationReceiver, intentFilter);
    }


    public void gotoPos(boolean animate, boolean addMarker)
    {
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
            switch (v.getId())
            {
                case R.id.searchBar:
                    try
                    {
                        PlaceAutocomplete.IntentBuilder builder = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN);
                        startActivityForResult(builder.build(mActivity), PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    }
                    catch (GooglePlayServicesRepairableException e)
                    {
                    }
                    catch (GooglePlayServicesNotAvailableException e)
                    {
                    }
                    break;
                default:
                    if (mGoogleMap != null)
                    {
                        LatLng latLng = mGoogleMap.getCameraPosition().target;
                        setLatLng(latLng);
                        //Log.d("Lucifer", action);

                        switch (action)
                        {
                            case FABUtil.SET_ALARM:
                                mActivity.startService(new Intent(mActivity, LocationTrackingService.class));
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
                        saveLocationInPreference(latLng);
                        mLocationSelector.setVisibility(View.GONE);
                    }
                    break;
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

    public void saveLocationInPreference(LatLng mLatLng)
    {
        preferences.edit()
                .putFloat(MapFragment.LATITUDE, (float) mLatLng.latitude)
                .putFloat(MapFragment.LONGITUDE, (float) mLatLng.longitude)
                .apply();
    }

    public View mOriginalContentView;
    public TouchableWrapper mTouchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        mOriginalContentView = super.onCreateView(inflater, parent, savedInstanceState);
        mTouchView = new TouchableWrapper(getActivity(), this);
        mTouchView.addView(mOriginalContentView);
        return mTouchView;
    }

    @Override
    public View getView()
    {
        return mOriginalContentView;
    }

    long upTime, downTime;
    int searchBarMargin;

    @Override
    public void onActionUp()
    {
        upTime = System.currentTimeMillis();
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
        downTime = System.currentTimeMillis();
        Slide slide = new Slide();
        TransitionManager.beginDelayedTransition(map_fab_buttons, slide);

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
        Toast.makeText(mActivity, "requestCode : "+requestCode, Toast.LENGTH_SHORT).show();
        switch (requestCode)
        {
            case MapFragment.PLACE_AUTOCOMPLETE_REQUEST_CODE:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        Place place = PlaceAutocomplete.getPlace(mActivity, data);
                        setLatLng(place.getLatLng());
                        gotoPos(true, true);
                        searchText.setText(place.getAddress());
                        break;
                    case PlaceAutocomplete.RESULT_ERROR:
                        Status status = PlaceAutocomplete.getStatus(mActivity, data);
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;
        }
    }
}