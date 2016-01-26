package in.org.whistleblower.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import in.org.whistleblower.utilities.MiscUtil;

public class MapFragment extends SupportMapFragment
{
    LatLng mLatLng;
    SharedPreferences preferences;
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String ZOOM = "ZOOM";
    public static final String TILT = "TILT";
    public static final String BEARING = "BEARING";
    public static final String MAP_TYPE = "mapType";
    private GoogleMap mGoogleMap;
    Activity mActivity;
    MiscUtil mUtil;

    public MapFragment()
    {
        initializeMap();
    }

    private void initializeMap()
    {
        if (preferences == null)
        {
            preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        }
        mLatLng = new LatLng(preferences.getFloat(LATITUDE, 0), preferences.getFloat(LONGITUDE, 0));
        mUtil = new MiscUtil(mActivity);
    }

    public MapFragment(double lat, double lng)
    {
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.edit()
                .putFloat(LATITUDE, (float) lat)
                .putFloat(LONGITUDE, (float) lng)
                .commit();
        initializeMap();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mGoogleMap = getMap();
        gotoPos(false,true);
    }

    public void gotoPos(boolean animate, boolean addMarker)
    {
        MiscUtil.log("gotoLastKnownPos, animate : " + animate + ", addMarker : " + addMarker);
        CameraUpdate update = CameraUpdateFactory.newLatLng(mLatLng);
        int mapType = Integer.parseInt(preferences.getString(MAP_TYPE, "1"));

        if (mGoogleMap.getMapType() != mapType)
        {
            mGoogleMap.setMapType(mapType);
        }

        if (addMarker)
        {
            mGoogleMap.clear();
            mGoogleMap.addMarker(new MarkerOptions().position(mLatLng));
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