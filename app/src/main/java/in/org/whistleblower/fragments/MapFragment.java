package in.org.whistleblower.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Set;

import in.org.whistleblower.R;
import in.org.whistleblower.actions.Alarm;
import in.org.whistleblower.actions.Image;
import in.org.whistleblower.actions.Place;
import in.org.whistleblower.asynctasks.SaveLocationTask;
import in.org.whistleblower.utilities.FABUtil;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.NavigationUtil;

public class MapFragment extends SupportMapFragment implements View.OnClickListener
{
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String ZOOM = "ZOOM";
    public static final String TILT = "TILT";
    public static final String BEARING = "BEARING";
    public static final String MAP_TYPE = "mapType";
    public static final String ADDRESS = "ADDRESS";
    public static final String KEY_LOCATION_SETTINGS_DIALOG_SHOWN = "locationSettingsDialogShown";
    private GoogleMap mGoogleMap;
    AppCompatActivity mActivity;
    static SharedPreferences preferences;
    View mLocationSelector;
    private String action;

    public MapFragment()
    {
    }

    private void initializeMap()
    {
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        reloadMapParameters(getArguments());
    }

    public static void updateLocation(Context context, Bundle bundle)
    {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit()
                .putFloat(LATITUDE, bundle.getFloat(LATITUDE, 0))
                .putFloat(LONGITUDE, bundle.getFloat(LONGITUDE, 0))
                .apply();
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
        initializeMap();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mGoogleMap = getMap();
        gotoPos(false, false);
        mActivity = (AppCompatActivity) getActivity();
        NavigationUtil.highlightMenu(mActivity, R.id.nav_map);
        mGoogleMap.setMyLocationEnabled(true);
        mActivity.findViewById(R.id.ok_map).setOnClickListener(this);
        mLocationSelector = mActivity.findViewById(R.id.select_location);

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
            mGoogleMap.addMarker(new MarkerOptions().position(getLatLng()));
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
        if (mGoogleMap != null)
        {
            LatLng latLng = mGoogleMap.getCameraPosition().target;
            setLatLng(latLng);
            switch (action)
            {
                case FABUtil.SET_ALARM:
                    new Alarm(mActivity).setAlarm();
                    break;

                case FABUtil.ADD_ISSUE:
                    Image.captureImage(mActivity);
                    break;

                case FABUtil.ADD_FAV_PLACE:
                    new Place(mActivity).addFavPlace();
                    break;

            }
            mLocationSelector.setVisibility(View.GONE);
            new SaveLocationTask(mActivity,latLng).execute();
        }
    }
    public void reloadMapParameters(Bundle bundle)
    {
        if(bundle != null)
        {
            if(bundle != null)
            {
                Set<String> keys = bundle.keySet();
                if (keys.contains(LATITUDE) && keys.contains(LONGITUDE))
                {
                    preferences.edit()
                            .putFloat(LATITUDE, bundle.getFloat(LATITUDE, 0))
                            .putFloat(LONGITUDE, bundle.getFloat(LONGITUDE, 0))
                            .apply();
                }
                if(keys.contains(FABUtil.ACTION))
                {
                    action = bundle.getString(FABUtil.ACTION);
                }
            }
        }
    }
}