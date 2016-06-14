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
import in.org.whistleblower.receivers.NotificationActionReceiver;
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
    private boolean isShareLocationShared;
    private TextView toolbarTitle;

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
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
            toolbarTitle.setTypeface(WhistleBlower.getTypeface());

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
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
        Log.d(NavigationUtil.FRAGMENT_TAG_DIALOG, "Extras : " + intent.getExtras());
        if (intent.hasExtra(NavigationUtil.FRAGMENT_TAG_DIALOG))
        {
            int notificationId = -1;
            if (intent.hasExtra(NavigationUtil.FRAGMENT_TAG_DIALOG))
            {
                notificationId = intent.getIntExtra(NotificationActionReceiver.NOTIFICATION_ID, -1);
            }
            showDialogFragment(intent.getStringExtra(NavigationUtil.FRAGMENT_TAG_DIALOG),notificationId);
        }
    }

    private void showDialogFragment(String tag, int notificationId)
    {
        Log.d(NavigationUtil.FRAGMENT_TAG_DIALOG, "has extra : " + tag);
        switch (tag)
        {
            case NavigationUtil.FRAGMENT_TAG_LOCATION_ALARM:
                mNavigationUtil.showAlarmFragment();
                break;
            case NavigationUtil.FRAGMENT_TAG_SHARING_REAL_TIME_LOCATION:
                mNavigationUtil.showShareLocationList();
                break;
            case NavigationUtil.FRAGMENT_TAG_NOTIFICATIONS:
                mNavigationUtil.showNotificationsFragment();
                break;
            case NavigationUtil.FRAGMENT_TAG_RECEIVING_SHARED_LOCATION:
                mNavigationUtil.showShareLocationList(NavigationUtil.FRAGMENT_TAG_RECEIVING_SHARED_LOCATION);
                break;
            case NavigationUtil.FRAGMENT_TAG_NOTIFY_LOCATION_LIST:
                mNavigationUtil.showNotifyLocationList();
                break;
            case NavigationUtil.FRAGMENT_TAG_RECEIVING_NOTIFIED_LOCATION:
                mNavigationUtil.showNotifiedLocationOnMap();
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        Log.d(NavigationUtil.FRAGMENT_TAG_DIALOG, "onNewIntent");
        if (preferences.contains(NavigationUtil.FRAGMENT_TAG_DIALOG))
        {
            String tag = preferences.getString(NavigationUtil.FRAGMENT_TAG_DIALOG, null);
            Log.d(NavigationUtil.FRAGMENT_TAG_DIALOG, "tag : " + tag);
            int notificationId = preferences.getInt(NotificationActionReceiver.NOTIFICATION_ID, -1);
            if (tag != null)
            {
                showDialogFragment(tag, notificationId);
                preferences.edit()
                        .putString(NavigationUtil.FRAGMENT_TAG_DIALOG, null)
                        .putInt(NotificationActionReceiver.NOTIFICATION_ID, -1)
                        .apply();
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
            case R.id.notifications:
                mNavigationUtil.showNotificationsFragment();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title)
    {
        toolbarTitle.setText(title);
    }

    public void favPlaceSelector(View view)
    {
        mNavigationUtil.mapFragment.setFavPlaceType(view);
    }

}
