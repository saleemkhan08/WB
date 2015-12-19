package in.org.whistleblower.utilities;

public class LocationUtilCopy /*implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult>,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnCameraChangeListener,
        ConnectivityListener*/
{/*
    public static final String LOCATION_UPDATE_FREQ = "updateFreq";
    public static final String MAP_TYPE = "mapType";
    SharedPreferences defaultPref;
    Location mLocation;
    public static final int LOCATION_SETTING_REQUEST_CODE = 201;
    private static final int GOOGLE_PLAY_SERVICES_REQUEST = 202;
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String ZOOM = "ZOOM";
    public static final String TILT = "TILT";
    public static final String BEARING = "BEARING";

    LocationRequest mLocationRequest;
    public GoogleApiClient mGoogleApiClient;
    AppCompatActivity activity;
    MiscUtil util;
    LocationManager locationManager;
    FloatingActionButton buttonMyLoc;
    RelativeLayout addIssueMenu;
    public GoogleMap mGoogleMap;
    SupportMapFragment mapFragment;
    SharedPreferences preferences;

    public LocationUtilCopy(AppCompatActivity activity)
    {
        this.activity = activity;
        buttonMyLoc = (FloatingActionButton) activity.findViewById(R.id.my_loc);
        addIssueMenu = (RelativeLayout) activity.findViewById(R.id.fab_wrapper);
        preferences = activity.getSharedPreferences(MainActivity.WHISTLE_BLOWER_PREFERENCE, Context.MODE_PRIVATE);
        if (mapFragment == null)
        {
            mapFragment = SupportMapFragment.newInstance();
        }
        defaultPref = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    public void setUp(MiscUtil util)
    {
        this.util = util;
        util.isConnected(this);
        setUpMyLocationButton();
        setMyLocationOnMap();
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
                mLocationRequest.setInterval(Integer.parseInt(defaultPref.getString(LOCATION_UPDATE_FREQ, "60000")));
                mLocationRequest.setFastestInterval(5000);
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        if (currentLocation == null)
        {
            return new CameraPosition(getLatLong(), getZoom(), getTilt(), getBrng());
        }
        else
        {
            return new CameraPosition(setLatLng(currentLocation), getZoom(), getTilt(), getBrng());
        }
    }

    private LatLng setLatLng(Location location)
    {
        preferences.edit()
                .putFloat(LATITUDE, (float) location.getLatitude())
                .putFloat(LONGITUDE, (float) location.getLongitude()).apply();
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

    public void moveToLocation(CameraPosition cameraPosition)
    {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mGoogleMap = mapFragment.getMap();
        if (mGoogleMap != null)
        {
            mGoogleMap.moveCamera(cameraUpdate);
        }

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
        showMyLocButton();
    }

    public void setMyLocationOnMap()
    {
        if (isLocationPermissionAvailable())
        {
            locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            if (isLocationServicesEnabled())
            {
                moveToLocation(getCurrentLoc());
            }
        }
        else
        {
            requestLocationPermission();
        }
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
        if (mLocationRequest == null)
        {
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(Integer.parseInt(defaultPref.getString(LOCATION_UPDATE_FREQ, "60000")));
            mLocationRequest.setFastestInterval(5000);
        }
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
        setMyLocationOnMap();
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
        if (mLocation == null)
        {
            mLocation = location;
            mGoogleMap.clear();
            LatLng latLng = setLatLng(location);

            MarkerOptions marker = new MarkerOptions();
            marker.position(latLng);
            marker.title("My Location");

            mGoogleMap.addMarker(marker);
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, getZoom()));
        }
    }

    @Override
    public void onMapReady(GoogleMap mGoogleMap)
    {
        mGoogleMap = mGoogleMap;
        mGoogleMap.setMapType(Integer.parseInt(defaultPref.getString(MAP_TYPE, GoogleMap.MAP_TYPE_HYBRID + "")));
        mGoogleMap.setOnCameraChangeListener(this);
        mGoogleMap.moveCamera(getLastKnowCameraPositionUpdate());
    }

    public CameraUpdate getLastKnowCameraPositionUpdate()
    {
        return CameraUpdateFactory.newCameraPosition(new CameraPosition(getLatLong(), getZoom(), getTilt(), getBrng()));
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition)
    {
        if (cameraPosition.target.longitude != 0 && cameraPosition.target.latitude != 0)
        {
            LatLng latLng = cameraPosition.target;

            preferences.edit().putFloat(ZOOM, cameraPosition.zoom)
                    .putFloat(BEARING, cameraPosition.bearing)
                    .putFloat(TILT, cameraPosition.tilt)
                    .putFloat(LATITUDE, (float) latLng.latitude)
                    .putFloat(LONGITUDE, (float) latLng.longitude)
                    .apply();
        }
    }

    @Override
    public void onInternetConnected()
    {
        if (isGoogleServicesAvailable())
        {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            mGoogleApiClient.connect();
            if (isLocationPermissionAvailable())
            {
                locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                if (isLocationServicesEnabled())
                {
                    setMyLocationOnMap();
                }
            }
            else
            {
                requestLocationPermission();
            }
        }
    }*/
}