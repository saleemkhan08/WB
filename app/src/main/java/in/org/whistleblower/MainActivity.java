package in.org.whistleblower;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;

import in.org.whistleblower.fragments.MapFragment;
import in.org.whistleblower.utilities.FABUtil;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.NavigationUtil;

public class MainActivity extends AppCompatActivity
{
    public static final int IMAGE_AND_STORAGE_REQUEST = 101;
    public static final int VIDEO_AND_STORAGE_REQUEST = 102;
    public static final int LOCATION_REQUEST = 103;
    private static final String FRAGMENT_TAG = "FRAGMENT_TAG";

    // Activity request codes
    MiscUtil util;
    NavigationUtil mNavigationUtil;
    FABUtil mFabUtil;
    SharedPreferences preferences;
    //static Typeface mFont;
    DrawerLayout drawer;
    RelativeLayout mainActivityContainer;
    private MapFragment mMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        util = new MiscUtil(this);
        if (!util.hasUserSignedIn())
        {
            finish();
        }
        else
        {
            setContentView(R.layout.activity_main);
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
            mainActivityContainer = (RelativeLayout) findViewById(R.id.mainActivityContainer);
            //Toolbar
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            //Navigation Drawer
            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
            MiscUtil.log("OnCreate");
            mNavigationUtil = new NavigationUtil(drawer, this);
            mMapFragment = mNavigationUtil.setUp(util);
            mFabUtil = new FABUtil(this);
            mFabUtil.setUp(util);
            if (savedInstanceState == null)
            {
                mNavigationUtil.showMapFragment();
                mNavigationUtil.navigationView.getMenu().getItem(0).setChecked(true);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState)
    {
        super.onSaveInstanceState(outState, outPersistentState);
        //for()
        //outPersistentState.putString(FRAGMENT_TAG, );
    }

    //If Location Settings is "OFF" then this Call Back will be used
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // if the result is capturing Image
        switch (requestCode)
        {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case MapFragment.REQUEST_CODE_LOCATION_SETTINGS:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        MiscUtil.log("User agreed to make required location settings changes.");
                        mMapFragment.updateCurrentLocationOnMap();
                        mMapFragment.startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        preferences.edit().putBoolean(MapFragment.KEY_LOCATION_SETTINGS_DIALOG_SHOWN, false).apply();
                        MiscUtil.log("User chose not to make required location settings changes.");
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "App requires Location settings to be on!", Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("Retry", new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                MiscUtil.log("onActivityResult : checkLocationSettings Again - Snack Bar");
                                mMapFragment.updateCurrentLocationOnMap();
                            }
                        });
                        snackbar.show();
                        break;
                }
                break;
            case FABUtil.RECORD_VIDEO_REQUEST:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        mFabUtil.launchIssueEditor(false);
                        break;
                    case Activity.RESULT_CANCELED:
                        util.toast("User cancelled video recording");
                        break;
                    default:
                        util.toast("Sorry! Failed to record video");
                }
                break;
            case FABUtil.CAPTURE_IMAGE_REQUEST:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        mFabUtil.launchIssueEditor(true);
                        break;
                    case Activity.RESULT_CANCELED:
                        util.toast("User cancelled image capture");
                        break;
                    default:
                        util.toast("Sorry! Failed to capture image");
                }
                break;
        }
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        /*// Get the root inflator.
        LayoutInflater baseInflater = (LayoutInflater) getBaseContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Inflate your custom view.
        View locView = baseInflater.inflate(R.layout.my_custom_view, null);

        TextView myLoc = ((TextView) locView.findViewById(R.id.icon_view));

        myLoc.setText(R.string.my_loc_icon);
        myLoc.setTypeface(mFont);

        // Inflate your custom view.
        View searchView = baseInflater.inflate(R.layout.my_custom_view, null);

        TextView search = ((TextView) searchView.findViewById(R.id.icon_view));

        search.setText(R.string.search_icon);
        search.setTypeface(mFont);
        menu.findItem(R.id.se).setActionView(searchView);*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.settings)
        {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        else if (id == R.id.search_issue)
        {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case IMAGE_AND_STORAGE_REQUEST:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    mFabUtil.captureImage();
                }
                else
                {
                    Toast.makeText(this, "To save images Locally this permissions are required!", Toast.LENGTH_LONG).show();
                }
            }
            break;

            case VIDEO_AND_STORAGE_REQUEST:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    mFabUtil.addFavoritePlace();
                }
                else
                {
                    Toast.makeText(this, "To save videos Locally this permissions are required!", Toast.LENGTH_LONG).show();
                }
            }
            break;
        }
    }

    public static void requestPermission(List<String> permissionsList, int requestCode, Activity activity)
    {
        String[] permissions = new String[permissionsList.size()];
        ActivityCompat.requestPermissions(activity, permissionsList.toArray(permissions), requestCode);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        //util.toast("on New Intent");
    }
}
