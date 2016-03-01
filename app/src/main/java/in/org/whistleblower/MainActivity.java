package in.org.whistleblower;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import in.org.whistleblower.actions.Image;
import in.org.whistleblower.actions.Place;
import in.org.whistleblower.icon.FontAwesomeIcon;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.utilities.FABUtil;
import in.org.whistleblower.utilities.ImageUtil;
import in.org.whistleblower.utilities.LocationUtil;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.NavigationUtil;

public class MainActivity extends AppCompatActivity
{
    public static final int IMAGE_AND_STORAGE_REQUEST = 101;
    public static final int VIDEO_AND_STORAGE_REQUEST = 102;
    public static final int LOCATION_REQUEST = 103;
    private static final String FRAGMENT_TAG = "FRAGMENT_TAG";
    private LocationUpdateService locationUpdateService;
    // Activity request codes
    MiscUtil mUtil;
    NavigationUtil mNavigationUtil;
    FABUtil mFabUtil;
    SharedPreferences preferences;
    //static Typeface mFont;
    DrawerLayout drawer;
    RelativeLayout mainActivityContainer;
    ImageView profilePic;
    TextView username, emailId;
    private Place mPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mUtil = new MiscUtil(this);
        if (!mUtil.hasUserSignedIn())
        {
            finish();
        }
        else
        {
            setContentView(R.layout.activity_main);
            locationUpdateService = new LocationUpdateService();
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
            mainActivityContainer = (RelativeLayout) findViewById(R.id.mainActivityContainer);
            //Toolbar
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            //Navigation Drawer
            NavigationView navigationHeader = (NavigationView) findViewById(R.id.nav_view);
            View header = navigationHeader.getHeaderView(0);
            profilePic = (ImageView) header.findViewById(R.id.navigationProfilePic);
            ImageUtil imageUtil = new ImageUtil(this);
            String dpUrl = preferences.getString(Accounts.PHOTO_URL, "");
            if (dpUrl.isEmpty())
            {
                profilePic.setBackground(mUtil.getIcon(FontAwesomeIcon.ANONYMOUS, R.color.colorPrimary));
                profilePic.setImageResource(android.R.color.transparent);
            }
            else
            {
                imageUtil.displayImage(dpUrl, profilePic, true);
                profilePic.setBackgroundColor(getResources().getColor(android.R.color.transparent, null));
            }


            emailId = (TextView) header.findViewById(R.id.emailId);
            emailId.setText(preferences.getString(Accounts.EMAIL, "No Email Id Found!"));

            username = (TextView) header.findViewById(R.id.username);
            username.setText(preferences.getString(Accounts.NAME, "Anonymous"));


            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
            MiscUtil.log("OnCreate");
            mNavigationUtil = new NavigationUtil(this);
            mNavigationUtil.setUp(mUtil);
            mFabUtil = new FABUtil(this);
            mFabUtil.setUp();
            if (savedInstanceState == null)
            {
                mNavigationUtil.showMapFragment();
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
            case LocationUpdateService.REQUEST_CODE_LOCATION_SETTINGS:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        MiscUtil.log("User agreed to make required location settings changes.");
                        locationUpdateService.updateCurrentLocationOnMap();
                        locationUpdateService.startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        preferences.edit().putBoolean(LocationUtil.KEY_LOCATION_SETTINGS_DIALOG_SHOWN, false).apply();
                        MiscUtil.log("User chose not to make required location settings changes.");
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "App requires Location settings to be on!", Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("Retry", new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                MiscUtil.log("onActivityResult : checkLocationSettings Again - Snack Bar");
                                locationUpdateService.updateCurrentLocationOnMap();
                            }
                        });
                        snackbar.show();
                        break;
                }
                break;
            case Image.RECORD_VIDEO_REQUEST:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        Image.launchIssueEditor(this, false);
                        break;
                    case Activity.RESULT_CANCELED:
                        mUtil.toast("Cancelled video recording");
                        break;
                    default:
                        mUtil.toast("Sorry! Failed to record video");
                }
                break;
            case Image.CAPTURE_IMAGE_REQUEST:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        Image.launchIssueEditor(this, true);
                        break;
                    case Activity.RESULT_CANCELED:
                        mUtil.toast("Cancelled image capture");
                        break;
                    default:
                        mUtil.toast("Sorry! Failed to capture image");
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
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra(NavigationUtil.KEY_CATEGORY, NavigationUtil.ADD_FAV_PLACE);
            startActivity(intent);
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
                    Image.captureImage(this);
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
                    mPlace.addFavPlace();
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
        //mUtil.toast("on New Intent");
    }
}
