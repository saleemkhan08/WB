package in.org.whistleblower.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
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
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import in.org.whistleblower.AddIssueActivity;
import in.org.whistleblower.MainActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.actions.Image;
import in.org.whistleblower.adapters.PlaceAdapter;
import in.org.whistleblower.asynctasks.GeoCoderTask;
import in.org.whistleblower.asynctasks.GetNearByPlaces;
import in.org.whistleblower.interfaces.GeoCodeListener;
import in.org.whistleblower.interfaces.PlacesResultListener;
import in.org.whistleblower.models.FavPlaces;
import in.org.whistleblower.models.FavPlacesDao;
import in.org.whistleblower.models.Issue;
import in.org.whistleblower.models.LocationAlarm;
import in.org.whistleblower.models.LocationAlarmDao;
import in.org.whistleblower.models.NotifyLocation;
import in.org.whistleblower.services.LocationTrackingService;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.FABUtil;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.NavigationUtil;
import in.org.whistleblower.utilities.PermissionUtil;
import in.org.whistleblower.utilities.TouchableWrapper;

public class MapFragment extends SupportMapFragment implements
        View.OnClickListener,
        PlacesResultListener,
        TouchableWrapper.OnMapTouchListener,
        GeoCodeListener,
        GoogleMap.OnCameraChangeListener
{

    public static final String SHOW_FAV_PLACE = "showFavPlace";
    public static final String SHOW_ISSUE = "showIssue";
    public static final String HANDLE_ACTION = "handleAction";
    private static final String HOME = "Home";
    public static final String LATLNG = "LATLNG";
    private View mOriginalContentView;
    private int searchBarMargin;
    public boolean isSubmitButtonShown;
    String placeType;
    int placeTypeIndex;
    private FavPlaces mFavPlace = new FavPlaces();
    private NotifyLocation mNotifyLocation = new NotifyLocation();
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
    public static final String ACCURACY = "ACCURACY";
    private Map<String, Marker> markerMap = new HashMap<>();
    private Map<String, Circle> circleMap = new HashMap<>();
    private GoogleMap mGoogleMap;

    AppCompatActivity mActivity;

    @Inject
    static SharedPreferences preferences;


    private String action;
    private static boolean mTravelModeOn;
    @BindColor(R.color.travel_mode)
    int travelModeColor;

    @BindColor(R.color.colorAccent)
    int accentColor;

    @BindColor(R.color.my_location_radius)
    int radiusColor;
    Bundle bundle;


    private int mRadius;

    //Injected Using Butter Knife

    @Bind(R.id.shareLoc1s)
    View shareLoc1s;

    @Bind(R.id.map_fab_buttons)
    ViewGroup map_fab_buttons;

    @Bind(R.id.searchBar)
    RelativeLayout searchBar;

    @Bind(R.id.my_loc)
    FloatingActionButton buttonMyLoc;

    @Bind(R.id.submitButton)
    View mSubmitButton;

    @Bind(R.id.appBarLayout)
    AppBarLayout mToolbar;

    @Bind(R.id.hoverPlaceName)
    TextView searchText;

    @Bind(R.id.searchProgress)
    ProgressBar searchProgress;

    @Bind(R.id.radiusSeekBarValueWrapper)
    View radiusSeekBarValueWrapper;

    @Bind(R.id.radiusSeekBarInnerWrapper)
    ViewGroup radiusSeekBarInnerWrapper;

    @Bind(R.id.radiusSeekBar)
    SeekBar radiusSeekBar;

    @Bind(R.id.radiusSeekBarValue)
    TextView radiusSeekBarValue;

    @Bind(R.id.select_location)
    View select_location;

    @Bind(R.id.shareLocationOptions)
    ViewGroup shareLocationOptions;

//---------------------------------------------------------

    @Bind(R.id.searchIcon)
    ImageView searchIcon;

    @Nullable
    @Bind(R.id.favPlaceTypeSelector)
    ViewGroup favPlaceTypeSelector;

    private boolean isMapMovedManually;

    private static int retryAttemptsCount;
    private GeoCoderTask mGeoCoderTask;
    private LatLng mGeoCodeLatLng, mOnActionDownLatLng;
    private GetNearByPlaces mGetNearByPlaces;

    private boolean isKm;
    private float currentZoom;
    private boolean moveCameraToMyLocOnLocUpdate;
    private boolean showShareLocationOptions = true;
    private boolean isPlacesApiResult;

    //Implementation done
    public MapFragment()
    {
        Log.d("FlowLogs", "Constructor");
        Otto.register(this);
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
        mActivity.setTitle(MainActivity.WHISTLE_BLOWER);
        ButterKnife.bind(this, mActivity);
        WhistleBlower.getComponent().inject(this);
        mTravelModeOn = false;
        preferences.edit()
                .putInt(KEY_TRAVELLING_MODE_DISP_COUNTER, 9)
                .putBoolean(LocationTrackingService.KEY_TRAVELLING_MODE, false)
                .commit();
        startLocationTrackingService();
        Log.d("FlowLogs", "Start Service Called");

        setupRadiusSeekBar();

        searchText.setOnClickListener(this);
        searchIcon.setOnClickListener(this);
        mSubmitButton.setOnClickListener(this);
        shareLoc1s.setOnClickListener(this);
        setUpMyLocationButton();

    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d("FlowLogs", "onResume");
        map_fab_buttons.setVisibility(View.VISIBLE);
        select_location.setVisibility(View.VISIBLE);
        searchBar.setVisibility(View.VISIBLE);//
        mGoogleMap = getMap();
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);

        if (PermissionUtil.isLocationPermissionAvailable())
        {
            mGoogleMap.setMyLocationEnabled(false);
        }
        NavigationUtil.highlightNavigationDrawerMenu(mActivity, R.id.nav_map);
        mGoogleMap.setOnCameraChangeListener(this);
        bundle = getArguments();
        reloadMapParameters(bundle);
    }


    //Implementation done
    public void reloadMapParameters(Bundle bundle)
    {
        Log.d("FlowLogs", "reloadMapParameters : " + bundle);
        if (bundle != null)
        {
            Set<String> keys = bundle.keySet();
            if (keys.contains(SHOW_FAV_PLACE))
            {
                moveCameraToMyLocOnLocUpdate = false;
                showFavPlaceOnMap((FavPlaces) bundle.getParcelable(SHOW_FAV_PLACE));
            }
            if (keys.contains(SHOW_ISSUE))
            {
                moveCameraToMyLocOnLocUpdate = false;
                showIssueOnMap((Issue) bundle.getParcelable(SHOW_ISSUE));
            }
            if (keys.contains(HANDLE_ACTION))
            {
                moveCameraToMyLocOnLocUpdate = false;
                if (action != null)
                {
                    removeMarkerAndCircle(action);
                }
                action = bundle.getString(HANDLE_ACTION);
                showSubmitButtonAndHideSearchIcon();
            }
        }
        else if (isPlacesApiResult)
        {
            isPlacesApiResult = false;
        }
        else
        {
            moveCameraToMyLocOnLocUpdate = true;
            Log.d("showMyLocOnMap", "reloadMapParameters");
            showMyLocOnMap(false);
        }
    }

    private void showMyLocOnMap(boolean animate)
    {
        LatLng latLng = getLatLng();
        showMarker(latLng, MY_LOC, 0);
        showAccuracyCircle(latLng, (int) getAccuracy(), MY_LOC);
        gotoLatLng(latLng, animate);
    }

    private void showIssueOnMap(Issue issue)
    {
        LatLng latLng = new LatLng(Double.parseDouble(issue.latitude), Double.parseDouble(issue.longitude));
        showMarker(latLng, issue.issueId, -1);
        showAccuracyCircle(latLng, issue.radius, issue.issueId);
        gotoLatLng(latLng, false);
    }

    private void showFavPlaceOnMap(FavPlaces favPlace)
    {
        mFavPlace = favPlace;
        LatLng latLng = new LatLng(Double.parseDouble(favPlace.latitude), Double.parseDouble(favPlace.longitude));
        showMarker(latLng, favPlace.latitude + favPlace.longitude, favPlace.placeTypeIndex);
        showAccuracyCircle(latLng, favPlace.radius, favPlace.latitude + favPlace.longitude);
        gotoLatLng(latLng, false);
    }

    private void hideMyLocMarker()
    {
        boolean hide = false;
        if (markerMap.containsKey(MY_LOC))
        {
            Marker marker = markerMap.get(MY_LOC);
            LatLng myLocLatLng = marker.getPosition();
            for (String key : markerMap.keySet())
            {
                if (!key.equals(MY_LOC))
                {
                    LatLng latLng = markerMap.get(key).getPosition();
                    double dist = distFrom(latLng.latitude, latLng.longitude, myLocLatLng.latitude, myLocLatLng.longitude);
                    if (dist < 50)
                    {
                        hide = true;
                    }
                }
            }
            if (hide)
            {
                marker.remove();
                markerMap.remove(MY_LOC);
            }
        }
    }

    private void setupRadiusSeekBar()
    {
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                setRadiusSeekBarValue();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        radiusSeekBarValueWrapper.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PopupMenu popup = new PopupMenu(mActivity, v);
                popup.getMenuInflater()
                        .inflate(R.menu.radius_options, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        switch (item.getItemId())
                        {
                            case R.id.radius_km:
                                isKm = true;
                                break;
                            case R.id.radius_mts:
                                isKm = false;
                                break;
                        }
                        setRadiusSeekBarValue();
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    private void setRadiusSeekBarValue()
    {
        String radiusSeekBarText;
        if (isKm)
        {
            radiusSeekBarText = (radiusSeekBar.getProgress() + 1) + getText(R.string.kilometer).toString();

        }
        else
        {
            radiusSeekBarText = (radiusSeekBar.getProgress() + 1) * 100 + getText(R.string.meter).toString();
        }
        radiusSeekBarValue.setText(radiusSeekBarText);
        drawCircleOnMap();
    }

    public void showSubmitButtonAndHideSearchIcon()
    {
        int drawableId;
        favPlaceTypeSelector.setVisibility(View.GONE);
        showShareLocationOptions = false;
        TransitionManager.beginDelayedTransition(shareLocationOptions);
        shareLocationOptions.setVisibility(View.GONE);

        setupRadiusSeekBar();
        switch (action)
        {
            case FABUtil.ADD_FAV_PLACE:
                drawableId = R.mipmap.fav_holo;
                placeType = HOME;
                favPlaceTypeSelector.setVisibility(View.GONE);
                TransitionManager.beginDelayedTransition(favPlaceTypeSelector);
                favPlaceTypeSelector.setVisibility(View.VISIBLE);
                break;
            case FABUtil.ADD_ISSUE:
                drawableId = R.drawable.bullhorn_primary_dark;
                break;
            case FABUtil.NOTIFY_LOC:
                drawableId = R.mipmap.notify_loc_primary_dark;
                break;
            case FABUtil.SET_ALARM:
                drawableId = R.mipmap.bell_holo;
                break;
            default:
                drawableId = R.drawable.search_primary_dark;
                break;
        }
        mSubmitButton.setVisibility(View.VISIBLE);
        isSubmitButtonShown = true;
        searchIcon.setImageDrawable(mActivity.getDrawable(drawableId));

        TransitionManager.beginDelayedTransition(map_fab_buttons);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) map_fab_buttons.getLayoutParams();
        layoutParams.bottomMargin = MiscUtil.dp(mActivity, 60);
        map_fab_buttons.setLayoutParams(layoutParams);

        TransitionManager.beginDelayedTransition(radiusSeekBarInnerWrapper);
        radiusSeekBarInnerWrapper.setVisibility(View.VISIBLE);

        Log.d("hjki", "radius : VISIBLE");
        drawCircleOnMap();
    }

    public void hideSubmitButtonAndShowSearchIcon()
    {
        showShareLocationOptions = true;
        TransitionManager.beginDelayedTransition(favPlaceTypeSelector);
        favPlaceTypeSelector.setVisibility(View.GONE);

        isSubmitButtonShown = false;
        searchIcon.setImageDrawable(mActivity.getDrawable(R.drawable.search_primary_dark));
        mSubmitButton.setVisibility(View.INVISIBLE);
        TransitionManager.beginDelayedTransition(map_fab_buttons);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) map_fab_buttons.getLayoutParams();
        layoutParams.bottomMargin = MiscUtil.dp(mActivity, 0);
        map_fab_buttons.setLayoutParams(layoutParams);

        TransitionManager.beginDelayedTransition(radiusSeekBarInnerWrapper);
        radiusSeekBarInnerWrapper.setVisibility(View.GONE);
        removeMarkerAndCircle(action);
        Log.d("hjki", "radius : Gone");
        showShareLocationOptions();
        Otto.post(FABUtil.HIDE_DESCRIPTION_TOAST);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        Otto.unregister(this);
        hideSubmitButtonAndShowSearchIcon();
        map_fab_buttons.setVisibility(View.GONE);
        searchBar.setVisibility(View.GONE);
        select_location.setVisibility(View.GONE);
        shareLocationOptions.setVisibility(View.GONE);
        cancelAsyncTask(mGeoCoderTask);
        cancelAsyncTask(mGetNearByPlaces);
        Otto.post(FABUtil.HIDE_DESCRIPTION_TOAST);
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
                moveCameraToMyLocOnLocUpdate = true;
                showTravellingModeHint();
                startLocationTrackingService();
                Log.d("showMyLocOnMap", "onClick");
                showMyLocOnMap(true);
            }
        });

        buttonMyLoc.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                moveCameraToMyLocOnLocUpdate = true;
                Log.d("showMyLocOnMap", "onLongClick");
                showMyLocOnMap(true);
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
                    startLocationTrackingService();
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
                    Otto.post(LocationTrackingService.STOP_SERVICE);
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
            buttonMyLoc.setColorNormal(accentColor);
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

    private void startLocationTrackingService()
    {
        Log.d("FlowLogs", "startLocationTrackingService");
        Intent intent = new Intent(mActivity, LocationTrackingService.class);
        mActivity.startService(intent);
    }

    private void setAlarm()
    {
        Log.d("FlowLogs", "setAlarm");
        Intent intent = new Intent(mActivity, LocationTrackingService.class);
        LocationAlarm alarm = new LocationAlarm();
        alarm.status = 1;
        alarm.radius = getRadius();
        alarm.address = searchText.getText().toString();
        alarm.latitude = mGeoCodeLatLng.latitude+"";
        alarm.longitude = mGeoCodeLatLng.longitude+"";
        new LocationAlarmDao().insert(alarm);
        intent.putExtra(LocationTrackingService.KEY_ALARM_SET, true);
        mActivity.startService(intent);
        toast("Alarm Set : \n" + searchText.getText());
    }

    public static CameraPosition getCameraPos(LatLng latLng)
    {
        return new CameraPosition(latLng, getZoom(), getTilt(), getBearing());
    }

    public static LatLng getLatLng()
    {
        double latitude = Double.parseDouble(preferences.getString(LATITUDE, "12.9667"));
        double longitude = Double.parseDouble(preferences.getString(LONGITUDE, "77.5667"));
        return new LatLng(latitude, longitude);
    }

    public static float getBearing()
    {
        return preferences.getFloat(BEARING, 0);
    }

    public static float getZoom()
    {
        float zoom = preferences.getFloat(ZOOM, 16);
        if (zoom < 3)
        {
            zoom = 16;
        }
        return zoom;
    }

    public static float getTilt()
    {
        return preferences.getFloat(TILT, 0);
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

    void gotoLatLng(LatLng latLng, boolean animate)
    {
        setMapType();
        setCamera(CameraUpdateFactory.newCameraPosition(getCameraPos(latLng)), animate);
    }

    private void setMapType()
    {
        int mapType = Integer.parseInt(preferences.getString(MAP_TYPE, "1"));
        if (mGoogleMap.getMapType() != mapType)
        {
            mGoogleMap.setMapType(mapType);
        }
    }

    @Override
    public void onClick(View v)
    {
        if (MiscUtil.isConnected(mActivity))
        {
            switch (v.getId())
            {
                case R.id.hoverPlaceName:
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
                    if (mGoogleMap != null)
                    {
                        switch (action)
                        {
                            case FABUtil.SET_ALARM:
                                setAlarm();
                                hideSubmitButtonAndShowSearchIcon();
                                break;

                            case FABUtil.ADD_ISSUE:

                                Intent intent = new Intent(mActivity, AddIssueActivity.class);
                                intent.putExtra(LATLNG, mGeoCodeLatLng);
                                intent.putExtra(ADDRESS, searchText.getText().toString());
                                intent.putExtra(RADIUS, mRadius);
                                startActivity(intent);
                                hideSubmitButtonAndShowSearchIcon();
                                break;

                            case FABUtil.ADD_FAV_PLACE:
                                addFavPlace();
                                hideSubmitButtonAndShowSearchIcon();
                                break;

                            case FABUtil.NOTIFY_LOC:
                                notifyLocation();
                                hideSubmitButtonAndShowSearchIcon();
                                break;
                        }
                    }
                    break;
                case R.id.shareLoc1s:
                    FragmentManager manager = getFragmentManager();
                    ShareLocationFragment dialog = new ShareLocationFragment();
                    dialog.show(manager, "ShareLocationFragment");
                    break;
            }
        }
        else
        {
            Toast.makeText(mActivity, "No Internet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void notifyLocation()
    {
        mNotifyLocation.radius = getRadius();
        mNotifyLocation.message = searchText.getText().toString();
        mNotifyLocation.latitude = mGeoCodeLatLng.latitude + "";
        mNotifyLocation.longitude = mGeoCodeLatLng.longitude + "";

        FragmentManager manager = getFragmentManager();
        NotifyLocationFragment dialog = new NotifyLocationFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(NotifyLocation.FRAGMENT_TAG, mNotifyLocation);
        dialog.setArguments(bundle);
        dialog.show(manager, "ShareLocationFragment");

    }

    int getRadius()
    {
        return (radiusSeekBar.getProgress() + 1) * (isKm ? 1000 : 100);
    }
    private void addFavPlace()
    {
        mFavPlace.radius = getRadius();
        mFavPlace.addressLine = searchText.getText().toString();
        mFavPlace.latitude = mGeoCodeLatLng.latitude + "";
        mFavPlace.longitude = mGeoCodeLatLng.longitude + "";
        mFavPlace.placeTypeIndex = placeTypeIndex;
        mFavPlace.placeType = placeType;

        Log.d("latLng", mFavPlace.addressLine + "\n"
                + mFavPlace.placeTypeIndex + "\n"
                + mFavPlace.placeType + "\n"
                + mFavPlace.latitude + "\n"
                + mFavPlace.longitude + "\n"
                + mFavPlace.radius);

        String result = new FavPlacesDao().insert(mFavPlace);
        toast(result);
        hideSubmitButtonAndShowSearchIcon();
    }

    private void toast(String str)
    {
        Toast toast = Toast.makeText(mActivity, str, Toast.LENGTH_LONG);
        ViewGroup view = (ViewGroup) toast.getView();
        ((TextView) view.getChildAt(0)).setGravity(Gravity.CENTER);
        toast.setView(view);
        toast.show();
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

        if (isSubmitButtonShown && mOnActionDownLatLng.equals(mGeoCodeLatLng))
        {
            drawCircleOnMap();
        }
        Handler myHandler = new Handler();
        myHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                showShareLocationOptions();
            }
        }, 800);
    }

    @Override
    public void onActionDown()
    {
        Slide slide = new Slide();
        mOnActionDownLatLng = mGeoCodeLatLng;
        TransitionManager.beginDelayedTransition(map_fab_buttons, slide);
        isMapMovedManually = true;
        map_fab_buttons.setVisibility(View.GONE);
        mToolbar.animate().translationY(-mToolbar.getBottom()).start();

        TransitionManager.beginDelayedTransition(searchBar);

        RelativeLayout.LayoutParams searchBarLayoutParams = (RelativeLayout.LayoutParams) searchBar.getLayoutParams();
        searchBarMargin = searchBarLayoutParams.topMargin;
        searchBarLayoutParams.topMargin = 0;
        searchBar.setLayoutParams(searchBarLayoutParams);

        hideShareLocationOptions();

        if (isSubmitButtonShown)
        {
            removeMarkerAndCircle(action);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d("Activity Result", "requestCode : " + requestCode + ", Image : " + Image.CAPTURE_IMAGE_REQUEST);
        switch (requestCode)
        {
            case PLACE_AUTOCOMPLETE_REQUEST_CODE:
                Place place = PlaceAutocomplete.getPlace(mActivity, data);
                Log.d("PlacesApi", "" + place);
                isPlacesApiResult = true;
                moveCameraToMyLocOnLocUpdate = false;
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        mGeoCodeLatLng = place.getLatLng();
                        Log.d("PlacesApi", "" + place.getAddress());
                        Log.d("PlacesApi", "" + place.getLatLng());
                        Log.d("PlacesApi", "" + place.getName());

                        gotoLatLng(mGeoCodeLatLng, true);
                        searchText.setText(place.getAddress());
                        break;
                    case PlaceAutocomplete.RESULT_ERROR:
                        Log.d("PlacesApi", "RESULT_ERROR");
                        Toast.makeText(mActivity, "Unknown Location", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.d("PlacesApi", "RESULT_CANCELED");
                        break;
                }
                break;
            case Image.CAPTURE_IMAGE_REQUEST:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        Image.launchIssueEditor(mActivity, true);
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(mActivity, "Cancelled image capture", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(mActivity, "Failed to capture image", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    @Subscribe
    public void onLocationChanged(Location location)
    {
        Log.d("showMyLocOnMap", "onLocationChanged : moveCameraToMyLocOnLocUpdate : " + moveCameraToMyLocOnLocUpdate);
        setCameraPosition(location);
        if (moveCameraToMyLocOnLocUpdate)
        {
            showMyLocOnMap(true);
        }
    }

    void showMarker(LatLng latLng, String key, int type)
    {
        Marker marker = null;
        if (markerMap.containsKey(key))
        {
            Log.d("FlowLogs", "getting marker from map");
            marker = markerMap.get(key);
        }
        if (marker != null && marker.isVisible())
        {
            Log.d("FlowLogs", "marker setPosition");
            marker.remove();
        }
        if (markerMap.containsKey(key + "img"))
        {
            markerMap.remove(key + "img");
        }
        Log.d("FlowLogs", "creating marker first time");
        if (key.equals(MY_LOC))
        {
            markerMap.put(key, mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .anchor(0.5f, 0.5f)
                    .flat(true)
                    .icon(MiscUtil.getMapMarker(mActivity, R.mipmap.my_loc_dot_48, 17))));

        }
        else
        {
            markerMap.put(key, mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .anchor(0.5f, 0.5f)
                    .flat(true)
                    .icon(MiscUtil.getMapMarker(mActivity, R.mipmap.my_loc_dot_big, 40))));

            int imgId = PlaceAdapter.getDrawableResId(type);

            markerMap.put(key + "img", mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .anchor(0.5f, 0.5f)
                    .flat(true)
                    .icon(MiscUtil.getMapMarker(mActivity, imgId, 20))));
        }

    }

    void removeMarkerAndCircle(String key)
    {
        if (circleMap.containsKey(key))
        {
            Circle circle = circleMap.get(key);
            circleMap.remove(key);
            circle.remove();
        }
        if (markerMap.containsKey(key))
        {
            Marker marker = markerMap.get(key);
            markerMap.remove(key);
            marker.remove();
        }
    }

    void showAccuracyCircle(LatLng latLng, float accuracy, String key)
    {
        Circle circle = null;
        Log.d("hijk", "Circle : showAccuracyCircle");
        if (circleMap.containsKey(key))
        {
            circle = circleMap.get(key);
        }

        if (circle != null && circle.isVisible())
        {
            circle.remove();
        }

        Log.d("hijk", "circleMap : " + circleMap + ", mGoogleMap : " + mGoogleMap);

        circleMap.put(key, mGoogleMap.addCircle(new CircleOptions()
                .radius(accuracy)
                .strokeWidth(2)
                .strokeColor(accentColor)
                .fillColor(radiusColor)
                .center(latLng)));
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

    double dist;

    @Override
    public void onCameraChange(CameraPosition cameraPosition)
    {
        dist = distFrom(cameraPosition.target.latitude, cameraPosition.target.longitude, getLatLng().latitude, getLatLng().longitude);
        int threshold = 1000;
        currentZoom = cameraPosition.zoom;
        hideMyLocMarker();
        mGeoCodeLatLng = cameraPosition.target;

        if (isMapMovedManually)
        {
            threshold = 100;
        }

        if (dist < threshold)
        {
            moveCameraToMyLocOnLocUpdate = true;
        }

        updateLocationInfo(MiscUtil.isConnected(mActivity));
        retryAttemptsCount = 0;

        Log.d("hijk", "Circle : isSubmitButtonShown : " + isSubmitButtonShown + ", Dist : " + dist);
        if (isSubmitButtonShown)
        {
            drawCircleOnMap();
        }
        showShareLocationOptions();
    }

    private void drawCircleOnMap()
    {
        mRadius = radiusSeekBar.getProgress() + 1;
        Log.d("hijk", "Circle : radius : " + mRadius);

        if (isKm)
        {
            mRadius *= 1000;
        }
        else
        {
            mRadius *= 100;
        }
        Log.d("zoom", "zoom : radius : " + mRadius);
        showAccuracyCircle(mGeoCodeLatLng, mRadius, action);
        setZoomLevel(mRadius);
    }

    private void setZoomLevel(int radius)
    {
        double zoom = 16;
        if (radius < 400)
        {
            zoom = 16;
        }
        else if (radius < 600)
        {
            zoom = 15.5;
        }
        else if (radius < 800)
        {
            zoom = 15;
        }
        else if (radius < 1000)
        {
            zoom = 14.5;
        }
        else if (radius < 2000)
        {
            zoom = 14;
        }
        else if (radius < 3000)
        {
            zoom = 13;
        }
        else if (radius < 5000)
        {
            zoom = 12.5;
        }
        else if (radius < 6000)
        {
            zoom = 12;
        }
        else if (radius < 8000)
        {
            zoom = 11.5;
        }
        else if (radius <= 10000)
        {
            zoom = 11;
        }

        if (currentZoom != zoom)
        {
            if (mGoogleMap != null && mGeoCodeLatLng != null)
            {
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mGeoCodeLatLng, (float) zoom));
            }
        }
    }

    void updateLocationInfo(boolean internet)
    {
        if (internet)
        {
            showSearchProgress();
            if (mGeoCoderTask != null)
            {
                cancelAsyncTask(mGeoCoderTask);
            }
            mGeoCoderTask = new GeoCoderTask(mActivity, mGeoCodeLatLng, this);
            mGeoCoderTask.execute(retryAttemptsCount++);
        }
        else
        {
            searchText.setText("No Internet...");
        }
    }

    private void cancelAsyncTask(AsyncTask task)
    {
        if (task != null)
        {
            AsyncTask.Status status = task.getStatus();
            if (status == AsyncTask.Status.RUNNING || status == AsyncTask.Status.PENDING)
            {
                task.cancel(true);
            }
        }
    }


    @Override
    public void onAddressObtained(String result)
    {
        hideSearchProgress();
        if (result == null)
        {
            searchText.setText(getText(R.string.unknownPlace));
        }
        else if (result != null)
        {
            searchText.setText(result);
        }
        else
        {
            searchText.setText(getText(R.string.noInternet));
        }
    }

    void showSearchProgress()
    {
        searchProgress.setVisibility(View.VISIBLE);
        if (isSubmitButtonShown)
        {
            mSubmitButton.setVisibility(View.INVISIBLE);
        }
    }

    private void hideSearchProgress()
    {
        searchProgress.setVisibility(View.GONE);
        if (isSubmitButtonShown)
        {
            mSubmitButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onGeoCodingFailed()
    {
        hideSearchProgress();
        if (retryAttemptsCount < 10)
        {
            updateLocationInfo(MiscUtil.isConnected(mActivity));
        }
        else
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

    public void setFavPlaceType(View view)
    {
        String[] favPlaceTypeName = mActivity.getResources().getStringArray(R.array.fav_place_type);
        /*
        <item>Home</item>
        <item>Friend's Place</item>
        <item>Work Place</item>
        <item>School / College</item>
        <item>Shopping Mall</item>
        <item>Cinema Hall</item>
        <item>Library</item>
        <item>Play Ground</item>
        <item>Hospital</item>
        <item>Jogging Place</item>
        <item>Gym</item>
        <item>Restaurant</item>
        <item>Coffee Shop</item>
        <item>Pub</item>
        <item>Others</item>
        */
        ((FloatingActionButton) mActivity.findViewById(R.id.fav_home)).setIcon(R.mipmap.home_gray);
        ((FloatingActionButton) mActivity.findViewById(R.id.fav_friendsPlace)).setIcon(R.mipmap.friends_place_gray);
        ((FloatingActionButton) mActivity.findViewById(R.id.fav_work)).setIcon(R.mipmap.work_gray);
        ((FloatingActionButton) mActivity.findViewById(R.id.fav_college)).setIcon(R.mipmap.school_gray);
        ((FloatingActionButton) mActivity.findViewById(R.id.fav_shopping_place)).setIcon(R.mipmap.shopping_gray);
        ((FloatingActionButton) mActivity.findViewById(R.id.fav_movie)).setIcon(R.mipmap.movie_gray);
        ((FloatingActionButton) mActivity.findViewById(R.id.fav_worship)).setIcon(R.mipmap.worship_grey);
        ((FloatingActionButton) mActivity.findViewById(R.id.fav_library)).setIcon(R.mipmap.library_gray);
        ((FloatingActionButton) mActivity.findViewById(R.id.fav_play_ground)).setIcon(R.mipmap.play_ground_gray);
        ((FloatingActionButton) mActivity.findViewById(R.id.fav_hospital)).setIcon(R.mipmap.hospital_gray);
        ((FloatingActionButton) mActivity.findViewById(R.id.fav_jogging)).setIcon(R.mipmap.jogging_gray);
        ((FloatingActionButton) mActivity.findViewById(R.id.fav_gym)).setIcon(R.mipmap.gym_gray);
        ((FloatingActionButton) mActivity.findViewById(R.id.fav_hotel)).setIcon(R.mipmap.hotel_gray);
        ((FloatingActionButton) mActivity.findViewById(R.id.fav_coffee_shop)).setIcon(R.mipmap.coffee_shop_gray);
        ((FloatingActionButton) mActivity.findViewById(R.id.fav_bar)).setIcon(R.mipmap.bar_gray);
        ((FloatingActionButton) mActivity.findViewById(R.id.fav_others)).setIcon(R.mipmap.others_gray);

        boolean isOther = false;

        Log.d("Tracking", "placeTypeIndex : " + placeTypeIndex);
        switch (view.getId())
        {
            case R.id.fav_home:
                ((FloatingActionButton) view).setIcon(R.mipmap.home_accent);
                placeTypeIndex = 0;
                break;
            case R.id.fav_friendsPlace:
                ((FloatingActionButton) view).setIcon(R.mipmap.friends_place_accent);
                placeTypeIndex = 1;
                break;
            case R.id.fav_work:
                ((FloatingActionButton) view).setIcon(R.mipmap.work_accent);
                placeTypeIndex = 2;
                break;
            case R.id.fav_college:
                ((FloatingActionButton) view).setIcon(R.mipmap.school_accent);
                placeTypeIndex = 3;
                break;
            case R.id.fav_shopping_place:
                ((FloatingActionButton) view).setIcon(R.mipmap.shopping_accent);
                placeTypeIndex = 4;
                break;
            case R.id.fav_movie:
                ((FloatingActionButton) view).setIcon(R.mipmap.movie_accent);
                placeTypeIndex = 5;
                break;
            case R.id.fav_worship:
                ((FloatingActionButton) view).setIcon(R.mipmap.worship_accent);
                placeTypeIndex = 6;
                break;
            case R.id.fav_library:
                ((FloatingActionButton) view).setIcon(R.mipmap.library_accent);
                placeTypeIndex = 7;
                break;
            case R.id.fav_play_ground:
                ((FloatingActionButton) view).setIcon(R.mipmap.play_ground_accent);
                placeTypeIndex = 8;
                break;
            case R.id.fav_hospital:
                ((FloatingActionButton) view).setIcon(R.mipmap.hospital_accent);
                placeTypeIndex = 9;
                break;
            case R.id.fav_jogging:
                ((FloatingActionButton) view).setIcon(R.mipmap.jogging_accent);
                placeTypeIndex = 10;
                break;
            case R.id.fav_gym:
                ((FloatingActionButton) view).setIcon(R.mipmap.gym_accent);
                placeTypeIndex = 11;
                break;
            case R.id.fav_hotel:
                ((FloatingActionButton) view).setIcon(R.mipmap.hotel_accent);
                placeTypeIndex = 12;
                break;
            case R.id.fav_coffee_shop:
                ((FloatingActionButton) view).setIcon(R.mipmap.coffee_shop_accent);
                placeTypeIndex = 13;
                break;
            case R.id.fav_bar:
                ((FloatingActionButton) view).setIcon(R.mipmap.bar_accent);
                placeTypeIndex = 14;
                break;
            case R.id.fav_others:
                ((FloatingActionButton) view).setIcon(R.mipmap.others_accent);
                isOther = true;
                placeTypeIndex = 15;
                showFavPlaceNameEditDialog();
                break;
        }

        placeType = favPlaceTypeName[placeTypeIndex];
        Log.d("Tracking", "Index : " + placeTypeIndex);
        Log.d("Tracking", "Place Type : " + placeType);
        if (!isOther)
        {
            Toast.makeText(mActivity, placeType, Toast.LENGTH_SHORT).show();
        }
        cancelToast(2);
    }

    private void cancelToast(int i)
    {
        Bundle bundle = new Bundle();
        bundle.putBoolean(FABUtil.HIDE_DESCRIPTION_TOAST, true);
        bundle.putInt(FABUtil.TOAST_INDEX, i);
        Otto.post(bundle);
    }

    private void showFavPlaceNameEditDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("Title");

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_label_editor, null);
        builder.setView(dialogView);
        final EditText input = (EditText) dialogView.findViewById(R.id.favPlaceEditor);
        input.setText(placeType);
        // Set up the buttons
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                placeType = input.getText().toString();
                if (placeType == null || placeType.trim().isEmpty())
                {
                    placeType = "Others";
                }
                Toast.makeText(mActivity, placeType, Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    void showShareLocationOptions()
    {
        if (dist < 10 && showShareLocationOptions)
        {
            TransitionManager.beginDelayedTransition(shareLocationOptions);
            shareLocationOptions.setVisibility(View.VISIBLE);
        }
        else
        {
            TransitionManager.beginDelayedTransition(shareLocationOptions);
            shareLocationOptions.setVisibility(View.GONE);
        }
    }

    void hideShareLocationOptions()
    {
        TransitionManager.beginDelayedTransition(shareLocationOptions);
        shareLocationOptions.setVisibility(View.GONE);
    }
}