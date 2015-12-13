package in.org.whistleblower;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import in.org.whistleblower.services.CameraUtil;
import in.org.whistleblower.services.LocationUtil;
import in.org.whistleblower.services.NavigationUtil;
import in.org.whistleblower.services.MiscUtil;

public class MainActivity extends AppCompatActivity //implements PermissionRequestListener
{
    public static final int IMAGE_AND_STORAGE_REQUEST = 101;
    public static final int VIDEO_AND_STORAGE_REQUEST = 102;
    public static final int LOCATION_REQUEST = 103;

    public static String WHISTLE_BLOWER_PREFERENCE = "WHISTLE_BLOWER_PREFERENCE";
    // Activity request codes
    MiscUtil util;
    LocationUtil locationUtil;
    NavigationUtil navigationUtil;
    CameraUtil cameraUtil;

    static Typeface mFont;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //Initialize Util Classes
        util = new MiscUtil(this);
        //Check For Sign In
        if (!util.hasUserSignedIn())
        {
            finish();
        }
        else
        {
            setContentView(R.layout.activity_main);

            //Set Up Location Util
            locationUtil = new LocationUtil(this);
            locationUtil.setUp(util);
            //Toolbar
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            //Navigation Drawer
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            //Set up Navigation Util
            navigationUtil = new NavigationUtil(drawer, this);
            navigationUtil.setUp(util, locationUtil);
            navigationUtil.showMapFragment();
            cameraUtil = new CameraUtil(this);
            cameraUtil.setUp(util);
            mFont = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
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
                    cameraUtil.captureImage();
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
                    cameraUtil.recordVideo();
                }
                else
                {
                    Toast.makeText(this, "To save videos Locally this permissions are required!", Toast.LENGTH_LONG).show();
                }
            }
            break;
            case LOCATION_REQUEST:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    locationUtil.setMyLocationOnMap();
                }
                else
                {
                    Toast.makeText(this, "To access your location this permissions are required!", Toast.LENGTH_LONG).show();
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // if the result is capturing Image
        if (requestCode == CameraUtil.CAPTURE_IMAGE_REQUEST)
        {
            if (resultCode == RESULT_OK)
            {
                cameraUtil.launchIssueEditor(true);
            }
            else if (resultCode == RESULT_CANCELED)
            {
                // user cancelled Image capture
                util.toast("User cancelled image capture");
            }
            else
            {
                util.toast("Sorry! Failed to capture image");
            }
        }
        else if (requestCode == CameraUtil.RECORD_VIDEO_REQUEST)
        {
            if (resultCode == RESULT_OK)
            {
                cameraUtil.launchIssueEditor(false);
            }
            else if (resultCode == RESULT_CANCELED)
            {
                util.toast("User cancelled video recording");
            }
            else
            {
                util.toast("Sorry! Failed to record video");
            }
        }
    }

    //@Override
    public static void requestPermission(List<String> permissionsList, int requestCode, Activity activity)
    {
        String[] permissions = new String[permissionsList.size()];
        ActivityCompat.requestPermissions(activity, permissionsList.toArray(permissions), requestCode);
    }
}
