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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
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

import java.util.Set;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;
import in.org.whistleblower.AddIssueActivity;
import in.org.whistleblower.MainActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.actions.Image;
import in.org.whistleblower.asynctasks.GeoCoderTask;
import in.org.whistleblower.dao.FavPlacesDao;
import in.org.whistleblower.dao.LocationAlarmDao;
import in.org.whistleblower.interfaces.GeoCodeListener;
import in.org.whistleblower.models.FavPlaces;
import in.org.whistleblower.models.Issue;
import in.org.whistleblower.models.LocationAlarm;
import in.org.whistleblower.models.NotifyLocation;
import in.org.whistleblower.receivers.InternetConnectivityListener;
import in.org.whistleblower.services.LocationTrackingService;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.FABUtil;
import in.org.whistleblower.utilities.LocationUtil;
import in.org.whistleblower.utilities.MarkerAndCirclesUtil;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.NavigationUtil;
import in.org.whistleblower.utilities.PermissionUtil;
import in.org.whistleblower.utilities.TouchableWrapper;
import in.org.whistleblower.utilities.TransitionUtil;

public class MapFragment extends SupportMapFragment implements
        View.OnClickListener,
        TouchableWrapper.OnMapTouchListener,
        GeoCodeListener,
        GoogleMap.OnCameraChangeListener
{
    public static final String DIALOG_DISMISS = "dialogDismiss";

    @BindString(R.string.noInternet)
    String NO_INTERNET;

    double dist;
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
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String ZOOM = "ZOOM";
    public static final String TILT = "TILT";
    public static final String BEARING = "BEARING";

    public static final String MAP_TYPE = "mapType";
    public static final String ADDRESS = "ADDRESS";
    private static final String KEY_TRAVELLING_MODE_DISP_COUNTER = "KEY_TRAVELLING_MODE_DISP_COUNTER";
    public static final String RADIUS = "RADIUS";
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 91;
    public static final String ACCURACY = "ACCURACY";

    private Marker myLocMarker;
    private Circle myLocCircle;

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

    private static int retryAttemptsCount;
    private GeoCoderTask mGeoCoderTask;
    private LatLng mGeoCodeLatLng, mOnActionDownLatLng;
    private boolean isKm;
    private float currentZoom;
    private boolean moveCameraToMyLocOnLocUpdate;
    private boolean showShareLocationOptions = true;
    private boolean isPlacesApiResult;
    private MarkerAndCirclesUtil mMarkerAndCircle;
    private Circle mActionCircle;

    @BindString(R.string.unknownPlace)
    String UNKNOWN_PLACE;

    //Implementation done
    public MapFragment()
    {
        Log.d("MapFragmentFlowLogs", "Constructor");
    }

    //Implementation done
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        Log.d("MapFragmentFlowLogs", "onCreateView");
        mOriginalContentView = super.onCreateView(inflater, parent, savedInstanceState);
        TouchableWrapper mTouchView = new TouchableWrapper(getActivity(), this);
        mTouchView.addView(mOriginalContentView);
        moveCameraToMyLocOnLocUpdate = true;
        return mTouchView;
    }

    //Implementation done
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Log.d("MapFragmentFlowLogs", "onActivityCreated");
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
        Log.d("MapFragmentFlowLogs", "Start Service Called");

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
        Otto.register(this);
        Log.d("MapFragmentFlowLogs", "onResume");
        map_fab_buttons.setVisibility(View.VISIBLE);
        select_location.setVisibility(View.VISIBLE);
        searchBar.setVisibility(View.VISIBLE);//
        mGoogleMap = getMap();
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        mMarkerAndCircle = new MarkerAndCirclesUtil(mGoogleMap, accentColor, radiusColor);
        if (PermissionUtil.isLocationPermissionAvailable())
        {
            mGoogleMap.setMyLocationEnabled(false);
        }
        mGoogleMap.setOnCameraChangeListener(this);
        NavigationUtil.highlightNavigationDrawerMenu(mActivity, R.id.nav_map);
        bundle = getArguments();
        reloadMapParameters(bundle);
    }

    public void reloadMapParameters(Bundle bundle)
    {
        Log.d("MapFragmentFlowLogs", "reloadMapParameters : " + bundle);
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
            showMyLocOnMap(false);
        }
    }

    private void showMyLocOnMap(boolean animate)
    {
        Log.d("MapFragmentFlowLogs", "showMyLocOnMap");
        LatLng latLng = getLatLng();
        showMyLocationMarkerAndCircle(latLng, getAccuracy());
        gotoLatLng(latLng, animate);
    }

    private void showIssueOnMap(Issue issue)
    {
        Log.d("MapFragmentFlowLogs", "showIssueOnMap");
        LatLng latLng = LocationUtil.getLatLng(issue.latitude, issue.longitude);
        mMarkerAndCircle.addMarkerAndCircle(issue);
        gotoLatLng(latLng, false);
    }

    private void showFavPlaceOnMap(FavPlaces favPlace)
    {
        Log.d("MapFragmentFlowLogs", "showFavPlaceOnMap");
        mFavPlace = favPlace;
        LatLng latLng = LocationUtil.getLatLng(favPlace.latitude, favPlace.longitude);
        mMarkerAndCircle.addMarkerAndCircle(favPlace);
        gotoLatLng(latLng, false);
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
        TransitionUtil.defaultTransition(shareLocationOptions);
        shareLocationOptions.setVisibility(View.GONE);

        setupRadiusSeekBar();
        switch (action)
        {
            case FABUtil.ADD_FAV_PLACE:
                drawableId = R.mipmap.fav_holo;
                placeType = HOME;
                favPlaceTypeSelector.setVisibility(View.GONE);
                TransitionUtil.defaultTransition(favPlaceTypeSelector);
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
        searchIcon.setImageDrawable(ContextCompat.getDrawable(mActivity,drawableId));

        TransitionUtil.slideTransition(map_fab_buttons);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) map_fab_buttons.getLayoutParams();
        layoutParams.bottomMargin = MiscUtil.dp(mActivity, 60);
        map_fab_buttons.setLayoutParams(layoutParams);

        TransitionUtil.defaultTransition(radiusSeekBarInnerWrapper);
        radiusSeekBarInnerWrapper.setVisibility(View.VISIBLE);

        Log.d("hjki", "radius : VISIBLE");
        drawCircleOnMap();
    }

    public void hideSubmitButtonAndShowSearchIcon()
    {
        showShareLocationOptions = true;
        TransitionUtil.defaultTransition(favPlaceTypeSelector);
        favPlaceTypeSelector.setVisibility(View.GONE);

        isSubmitButtonShown = false;
        searchIcon.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.search_primary_dark));
        mSubmitButton.setVisibility(View.INVISIBLE);
        TransitionUtil.slideTransition(map_fab_buttons);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) map_fab_buttons.getLayoutParams();
        layoutParams.bottomMargin = MiscUtil.dp(mActivity, 0);
        map_fab_buttons.setLayoutParams(layoutParams);

        TransitionUtil.defaultTransition(radiusSeekBarInnerWrapper);
        radiusSeekBarInnerWrapper.setVisibility(View.GONE);
        removeActionCircle();
        showShareLocationOptions();
        Otto.post(FABUtil.HIDE_DESCRIPTION_TOAST);
    }

    private void removeActionCircle()
    {
        if (mActionCircle != null)
        {
            mActionCircle.remove();
            mActionCircle = null;
        }
    }


    @Override
    public void onStop()
    {
        super.onStop();
        Log.d("MapFragmentFlowLogs", "onStop");
        mMarkerAndCircle.unregister();
        Otto.unregister(this);
        //hideSubmitButtonAndShowSearchIcon();
        map_fab_buttons.setVisibility(View.GONE);
        searchBar.setVisibility(View.GONE);
        select_location.setVisibility(View.GONE);
        shareLocationOptions.setVisibility(View.GONE);
        cancelAsyncTask(mGeoCoderTask);
        Otto.post(FABUtil.HIDE_DESCRIPTION_TOAST);
    }

    public void setUpMyLocationButton()
    {
        buttonMyLoc.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                moveCameraToMyLocOnLocUpdate = true;
                showTravellingModeHint();
                startLocationTrackingService();
                Log.d("MapFragmentFlowLogs", "My Loc : onClick");
                showMyLocOnMap(true);
            }
        });

        buttonMyLoc.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                moveCameraToMyLocOnLocUpdate = true;
                Log.d("MapFragmentFlowLogs", "My Loc onLongClick");
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
        Log.d("MapFragmentFlowLogs", "startLocationTrackingService");
        Intent intent = new Intent(mActivity, LocationTrackingService.class);
        mActivity.startService(intent);
    }

    private void setAlarm()
    {
        Log.d("MapFragmentFlowLogs", "setAlarm");
        Intent intent = new Intent(mActivity, LocationTrackingService.class);
        LocationAlarm alarm = new LocationAlarm();
        alarm.status = 1;
        alarm.radius = getRadius();
        alarm.address = searchText.getText().toString();
        alarm.latitude = mGeoCodeLatLng.latitude + "";
        alarm.longitude = mGeoCodeLatLng.longitude + "";

        LocationAlarmDao.insert(alarm);
        intent.putExtra(LocationTrackingService.KEY_ALARM_SET, true);
        mActivity.startService(intent);

        mMarkerAndCircle.addMarkerAndCircle(alarm);

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
            Toast.makeText(mActivity, NO_INTERNET, Toast.LENGTH_SHORT).show();
        }
    }

    private void notifyLocation()
    {
        Log.d("MapFragmentFlowLogs", "notifyLocation : Before");

        NotifyLocation mNotifyLocation = new NotifyLocation();
        mNotifyLocation.radius = getRadius();
        mNotifyLocation.message = searchText.getText().toString();
        mNotifyLocation.latitude = mGeoCodeLatLng.latitude + "";
        mNotifyLocation.longitude = mGeoCodeLatLng.longitude + "";

        FragmentManager manager = getFragmentManager();
        NotifyLocationFragment dialog = new NotifyLocationFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(NotifyLocation.FRAGMENT_TAG, mNotifyLocation);
        dialog.setArguments(bundle);
        dialog.show(manager, NavigationUtil.FRAGMENT_TAG_NOTIFY_LOCATION);
    }

    int getRadius()
    {
        return (radiusSeekBar.getProgress() + 1) * (isKm ? 1000 : 100);
    }

    private void addFavPlace()
    {
        Log.d("MapFragmentFlowLogs", "addFavPlace");

        mFavPlace.radius = getRadius();
        mFavPlace.addressLine = searchText.getText().toString();
        mFavPlace.latitude = mGeoCodeLatLng.latitude + "";
        mFavPlace.longitude = mGeoCodeLatLng.longitude + "";
        mFavPlace.placeTypeIndex = placeTypeIndex;
        mFavPlace.placeType = placeType;
        String result = FavPlacesDao.insert(mFavPlace);
        mMarkerAndCircle.addMarkerAndCircle(mFavPlace);
        toast(result);
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
        TransitionUtil.slideTransition(map_fab_buttons);
        map_fab_buttons.setVisibility(View.VISIBLE);

        TransitionUtil.defaultTransition(searchBar);

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
        mOnActionDownLatLng = mGeoCodeLatLng;
        TransitionUtil.slideTransition(map_fab_buttons);
        map_fab_buttons.setVisibility(View.GONE);
        mToolbar.animate().translationY(-mToolbar.getBottom()).start();

        TransitionUtil.defaultTransition(searchBar);

        RelativeLayout.LayoutParams searchBarLayoutParams = (RelativeLayout.LayoutParams) searchBar.getLayoutParams();
        searchBarMargin = searchBarLayoutParams.topMargin;
        searchBarLayoutParams.topMargin = 0;
        searchBar.setLayoutParams(searchBarLayoutParams);

        hideShareLocationOptions();

        if (isSubmitButtonShown)
        {
            removeActionCircle();
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
                        Toast.makeText(mActivity, UNKNOWN_PLACE, Toast.LENGTH_SHORT).show();
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
        Log.d("MapFragmentFlowLogs", "onLocationChanged : moveCameraToMyLocOnLocUpdate : " + moveCameraToMyLocOnLocUpdate);
        setCameraPosition(location);
        if (moveCameraToMyLocOnLocUpdate)
        {
            showMyLocOnMap(true);
        }
    }

    void showMyLocationMarkerAndCircle(LatLng latLng, float accuracy)
    {
        if (myLocMarker != null)
        {
            myLocMarker.remove();
        }

        myLocMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .anchor(0.5f, 0.5f)
                .flat(true)
                .icon(MiscUtil.getMapMarker(mActivity, R.mipmap.my_loc_dot_48, 17)));

        if (myLocCircle != null)
        {
            myLocCircle.remove();
        }
        myLocCircle = mGoogleMap.addCircle(new CircleOptions()
                .radius(accuracy)
                .strokeWidth(2)
                .strokeColor(accentColor)
                .fillColor(radiusColor)
                .center(latLng));
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition)
    {
        dist = distFrom(cameraPosition.target.latitude, cameraPosition.target.longitude, getLatLng().latitude, getLatLng().longitude);
        currentZoom = cameraPosition.zoom;
        mGeoCodeLatLng = cameraPosition.target;
        Log.d("MapFragmentFlowLogs", "onCameraChange dist : "+dist);

        if (dist < 100)
        {
            moveCameraToMyLocOnLocUpdate = true;
        }
        else
        {
            moveCameraToMyLocOnLocUpdate = false;
        }

        if (dist > 50)
        {
            updateLocationInfo();
        }
        else
        {
            String searchTextValue = searchText.getText().toString();
            Log.d("MapFragmentFlowLogs", "searchTextValue : " + searchTextValue);
            if (searchTextValue.contains(NO_INTERNET.substring(0, NO_INTERNET.length() - 4))
                    || searchTextValue.contains(UNKNOWN_PLACE.substring(0, UNKNOWN_PLACE.length() - 4)))
            {
                Log.d("MapFragmentFlowLogs", "NO_INTERNET || : UNKNOWN_PLACE");
                updateLocationInfo();
            }
        }
        retryAttemptsCount = 0;

        Log.d("MapFragmentFlowLogs", "Circle : isSubmitButtonShown : " + isSubmitButtonShown + ", Dist : " + dist);
        if (isSubmitButtonShown)
        {
            drawCircleOnMap();
        }
        showShareLocationOptions();
    }

    private void drawCircleOnMap()
    {
        mRadius = radiusSeekBar.getProgress() + 1;
        Log.d("MapFragmentFlowLogs", "Circle : radius : " + mRadius);

        if (isKm)
        {
            mRadius *= 1000;
        }
        else
        {
            mRadius *= 100;
        }
        Log.d("MapFragmentFlowLogs", "zoom : radius : " + mRadius);
        showActionCircle(mGeoCodeLatLng, mRadius);
        setZoomLevel(mRadius);
    }

    private void showActionCircle(LatLng latLng, int radius)
    {
        removeActionCircle();
        mActionCircle = mGoogleMap.addCircle(new CircleOptions()
                .radius(radius)
                .strokeWidth(2)
                .strokeColor(accentColor)
                .fillColor(radiusColor)
                .center(latLng));
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

        if (!isCurrentZoomSetByUser(currentZoom))
        {
            if (zoom != currentZoom)
            {
                if (mGoogleMap != null && mGeoCodeLatLng != null)
                {
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mGeoCodeLatLng, (float) zoom));
                }
            }
        }
    }

    private boolean isCurrentZoomSetByUser(float currentZoom)
    {
        double[] zooms = {16, 15.5, 15, 14.5, 14, 13, 12.5, 12, 11.5, 11};
        for (double zoom : zooms)
        {
            if (zoom == currentZoom)
            {
                return false;
            }
        }
        return true;
    }

    void updateLocationInfo()
    {
        Log.d("MapFragmentFlowLogs", "updateLocationInfo");
        if (MiscUtil.isConnected(mActivity))
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
            searchText.setText(NO_INTERNET);
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
            searchText.setText(UNKNOWN_PLACE);
        }
        else if (result != null)
        {
            searchText.setText(result);
        }
        else
        {
            searchText.setText(NO_INTERNET);
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
            updateLocationInfo();
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
        Log.d("MapFragmentFlowLogs", "setFavPlaceType");
        String[] favPlaceTypeName = mActivity.getResources().getStringArray(R.array.fav_place_type);
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
        if (!isOther)
        {
            Toast.makeText(mActivity, placeType, Toast.LENGTH_SHORT).show();
        }
        cancelToast(2);
    }

    private void cancelToast(int i)
    {
        Log.d("MapFragmentFlowLogs", "cancelToast");
        Bundle bundle = new Bundle();
        bundle.putBoolean(FABUtil.HIDE_DESCRIPTION_TOAST, true);
        bundle.putInt(FABUtil.TOAST_INDEX, i);
        Otto.post(bundle);
    }

    private void showFavPlaceNameEditDialog()
    {
        Log.d("MapFragmentFlowLogs", "showFavPlaceNameEditDialog");
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
        Log.d("MapFragmentFlowLogs", "showShareLocationOptions : " + dist);
        if (dist < 10 && showShareLocationOptions)
        {
            TransitionUtil.defaultTransition(shareLocationOptions);
            shareLocationOptions.setVisibility(View.VISIBLE);
        }
        else
        {
            hideShareLocationOptions();
        }
    }

    void hideShareLocationOptions()
    {
        Log.d("MapFragmentFlowLogs", "hideShareLocationOptions");
        TransitionUtil.defaultTransition(shareLocationOptions);
        shareLocationOptions.setVisibility(View.GONE);
    }

    @Subscribe
    public void onDismiss(String action)
    {
        switch (action)
        {
            case DIALOG_DISMISS:
                mMarkerAndCircle = new MarkerAndCirclesUtil(mGoogleMap, accentColor, radiusColor);
                showMyLocationMarkerAndCircle(getLatLng(), getAccuracy());
                break;
            case InternetConnectivityListener.INTERNET_CONNECTED :
                updateLocationInfo();
                break;
        }
    }
}