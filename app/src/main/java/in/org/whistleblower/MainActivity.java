package in.org.whistleblower;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

import javax.inject.Inject;

import in.org.whistleblower.fragments.FriendListFragment;
import in.org.whistleblower.interfaces.ConnectivityListener;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.services.GetNotificationIntentService;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.FABUtil;
import in.org.whistleblower.utilities.ImageUtil;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.NavigationUtil;

public class MainActivity extends AppCompatActivity
{
    public static final String FAVORITE_PLACES = "Favorite Places";
    public static final String WHISTLE_BLOWER = "Whistle Blower";
    public static final String NEWS_FEEDS = "News Feeds";
    public static final String FRIEND_LIST = "Friends List";
    public static final String SHARE_LOCATION_LIST = "SHARE_LOCATION_LIST";
    MiscUtil mUtil;
    FloatingActionsMenu fabMenu;
    NavigationUtil mNavigationUtil;
    FABUtil mFabUtil;

    @Inject
    SharedPreferences preferences;
    //static Typeface mFont;
    DrawerLayout drawer;
    RelativeLayout mainActivityContainer;
    ImageView profilePic;
    TextView username, emailId;
    Toolbar toolbar;
    private boolean isShareLocationShared;

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
            WhistleBlower.getComponent().inject(this);

            Intent getNotificationsService = new Intent(this, GetNotificationIntentService.class);
            startService(getNotificationsService);

            mainActivityContainer = (RelativeLayout) findViewById(R.id.mainActivityContainer);
            //Toolbar
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle("");
            setSupportActionBar(toolbar);
            //Navigation Drawer
            NavigationView navigationHeader = (NavigationView) findViewById(R.id.nav_view);
            View header = navigationHeader.getHeaderView(0);
            profilePic = (ImageView) header.findViewById(R.id.navigationProfilePic);
            ImageUtil imageUtil = new ImageUtil(this);
            String dpUrl = preferences.getString(Accounts.PHOTO_URL, "");
            if (dpUrl.isEmpty())
            {
                profilePic.setImageResource(R.mipmap.user_accent_o);
            }
            else
            {
                imageUtil.displayImage(dpUrl, profilePic, true);
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
            mFabUtil = new FABUtil(this);
            mFabUtil.setUp();
            if (savedInstanceState == null)
            {
                mNavigationUtil.showMapFragment();
            }

            fabMenu = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
            fabMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener()
            {
                @Override
                public void onMenuExpanded()
                {
                    MiscUtil.isConnected(new ConnectivityListener()
                    {
                        @Override
                        public void onInternetConnected()
                        {

                        }

                        @Override
                        public void onCancelled()
                        {
                            fabMenu.collapse();
                        }
                    }, MainActivity.this);
                }

                @Override
                public void onMenuCollapsed()
                {
                }
            });
        }
        FriendListFragment.getFriendListFromServer(null);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        fabMenu.collapse();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mNavigationUtil != null)
        {
            mNavigationUtil.unregisterOtto();
        }
        if (mFabUtil != null)
        {
            mFabUtil.unregisterOtto();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Intent intent = getIntent();
        Log.d(NavigationUtil.DIALOG_FRAGMENT_TAG, "Extras : " + intent.getExtras());
        if (intent.hasExtra(NavigationUtil.DIALOG_FRAGMENT_TAG))
        {
            showDialogFragment(intent.getStringExtra(NavigationUtil.DIALOG_FRAGMENT_TAG));
        }
    }

    private void showDialogFragment(String tag)
    {
        Log.d(NavigationUtil.DIALOG_FRAGMENT_TAG, "has extra : " + tag);
        switch (tag)
        {
            case NavigationUtil.LOCATION_ALARM_FRAGMENT_TAG:
                mNavigationUtil.showAlarmFragment();
                break;
            case NavigationUtil.SHARE_LOCATION_LIST_FRAGMENT_TAG:
                mNavigationUtil.showShareLocationList();
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        Log.d(NavigationUtil.DIALOG_FRAGMENT_TAG, "onNewIntent");
        if (preferences.contains(NavigationUtil.DIALOG_FRAGMENT_TAG))
        {
            String tag = preferences.getString(NavigationUtil.DIALOG_FRAGMENT_TAG, null);
            Log.d(NavigationUtil.DIALOG_FRAGMENT_TAG, "tag : " + tag);
            if (tag != null)
            {
                showDialogFragment(tag);
                preferences.edit().putString(NavigationUtil.DIALOG_FRAGMENT_TAG, null).apply();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState)
    {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (fabMenu.isExpanded())
        {
            fabMenu.collapse();
        }
        else if (mNavigationUtil.mapFragment != null && mNavigationUtil.mapFragment.isVisible())
        {
            if (mNavigationUtil.mapFragment.isSubmitButtonShown)
            {
                mNavigationUtil.mapFragment.hideSubmitButtonAndShowSearchIcon();
            }
            else
            {
                if (checkLastFragment())
                {
                    super.onBackPressed();
                }
            }
        }
        else
        {
            if (checkLastFragment())
            {
                super.onBackPressed();
            }
        }
        Otto.post(FABUtil.HIDE_DESCRIPTION_TOAST);
    }

    boolean checkLastFragment()
    {
        if (getSupportFragmentManager().getBackStackEntryCount() < 2)
        {
            finishAffinity();
            return false;
        }
        return true;
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
        switch (id)
        {
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.notifications :
                mNavigationUtil.showNotificationsFragment();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title)
    {
        toolbar.setTitle(title);
    }

    public void favPlaceSelector(View view)
    {
        mNavigationUtil.mapFragment.setFavPlaceType(view);
    }

}
