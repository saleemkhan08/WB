package in.org.whistleblower.fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

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
    private static final String KEY_PERMISSION_ASKED = "KEY_PERMISSION_ASKED";
    private static final String KEY_IS_SNACK_BAR_SHOWN = "KEY_IS_SNACK_BAR_SHOWN";
    private GoogleMap mGoogleMap;
    AppCompatActivity mActivity;
    static SharedPreferences preferences;
    View mLocationSelector;
    private String action;
    private static boolean isLocationPermissionAsked;
    private static boolean isSnackBarShown;

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
    public void onViewStateRestored(Bundle savedInstanceState)
    {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null)
        {
            isLocationPermissionAsked = savedInstanceState.getBoolean(KEY_PERMISSION_ASKED);
            isSnackBarShown = savedInstanceState.getBoolean(KEY_IS_SNACK_BAR_SHOWN);
            MiscUtil.log("savedInstanceState not null : isSnackBarShown = "+isSnackBarShown+", isLocationPermissionAsked = "+isLocationPermissionAsked);

            if (isSnackBarShown)
            {
                showSnackBar();
            }
            else if (isLocationPermissionAsked && !isLocationPermissionAvailable())
            {
                requestLocationPermission();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_PERMISSION_ASKED, isLocationPermissionAsked);
        outState.putBoolean(KEY_IS_SNACK_BAR_SHOWN, isSnackBarShown);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mGoogleMap = getMap();
        gotoPos(false, false);
        mActivity = (AppCompatActivity) getActivity();
        NavigationUtil.highlightMenu(mActivity, R.id.nav_map);
        mapFragmentContainer = mActivity.findViewById(android.R.id.content);
        if (isLocationPermissionAvailable())
        {
            mGoogleMap.setMyLocationEnabled(true);
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

    private void showSnackBar()
    {
        MiscUtil.log("mapFragmentContainer : "+mapFragmentContainer);
        Snackbar snackbar = Snackbar.make(mapFragmentContainer, "App Can't be used without this permission!", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Retry", new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MiscUtil.log("onActivityResult : checkLocationSettings Again - Snack Bar");
                requestLocationPermission();
                isSnackBarShown = false;
                //TODO go to current location
            }
        });
        snackbar.show();
        isSnackBarShown = true;
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
                    Toast.makeText(mActivity, "Adding...",Toast.LENGTH_SHORT).show();
                    break;

            }
            mLocationSelector.setVisibility(View.GONE);
            new SaveLocationTask(mActivity, latLng).execute(action);
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
                mGoogleMap.setMyLocationEnabled(true);
                //TODO go to current location
            }
            else
            {
                showSnackBar();
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
}