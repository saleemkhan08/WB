package in.org.whistleblower.utilities;

import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class LocationUtil implements OnMapReadyCallback
{
    public static final String LOCATION_UPDATE_FREQ = "updateFreq";
    public static final String MAP_TYPE = "mapType";
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String ZOOM = "ZOOM";
    public static final String TILT = "TILT";
    public static final String BEARING = "BEARING";

    AppCompatActivity mActivity;
    MiscUtil mUtil;
    GoogleMap mGoogleMap;
    SharedPreferences preferences;
    public SupportMapFragment mapFragment;
    private static final int GOOGLE_PLAY_SERVICES_REQUEST = 202;

    public LocationUtil(AppCompatActivity activity, MiscUtil util)
    {
        setup(activity, util);
        preferences = PreferenceManager.getDefaultSharedPreferences(activity);//activity.getSharedPreferences(MainActivity.WHISTLE_BLOWER_PREFERENCE, Context.MODE_PRIVATE);
    }

    public void setup(AppCompatActivity activity, MiscUtil util)
    {
        this.mActivity = activity;
        this.mUtil = util;
        if (isGoogleServicesAvailable())
        {
            MiscUtil.log("Google Service Available");
            initMap();
        }
    }

    public LatLng getLatLng()
    {
        return new LatLng(preferences.getFloat(LATITUDE, 12.9667f), preferences.getFloat(LONGITUDE, 77.5667f));
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
        CameraPosition position = new CameraPosition(getLatLng(), getZoom(),getTilt(),getBrng());
        return position;
    }

    public void gotoLastKnownPos()
    {
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(getLastKnownPos());
        mGoogleMap.moveCamera(update);
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
            Toast.makeText(mActivity, "Can't Access Google Play Services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void initMap()
    {
        if (mGoogleMap == null)
        {
            mapFragment = SupportMapFragment.newInstance();
            mapFragment.onResume();
            mapFragment.getMapAsync(this);
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleMap = googleMap;
        MiscUtil.log("Map Available");
        gotoLastKnownPos();
        mGoogleMap.setMapType(preferences.getInt(MAP_TYPE, 4));

    }
}