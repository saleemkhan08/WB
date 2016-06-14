package in.org.whistleblower.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import butterknife.BindString;
import butterknife.ButterKnife;
import in.org.whistleblower.LoginActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.fragments.FavoritePlacesFragment;
import in.org.whistleblower.fragments.FriendListFragment;
import in.org.whistleblower.fragments.LocationAlarmListFragment;
import in.org.whistleblower.fragments.MainFragment;
import in.org.whistleblower.fragments.MapFragment;
import in.org.whistleblower.fragments.NotificationsFragment;
import in.org.whistleblower.fragments.NotifyLocationListFragment;
import in.org.whistleblower.fragments.ShareLocationListFragment;
import in.org.whistleblower.models.FavPlaces;
import in.org.whistleblower.models.Issue;
import in.org.whistleblower.dao.IssuesDao;
import in.org.whistleblower.dao.LocationAlarmDao;
import in.org.whistleblower.dao.NotifyLocationDao;
import in.org.whistleblower.dao.ShareLocationDao;
import in.org.whistleblower.singletons.Otto;

public class NavigationUtil implements NavigationView.OnNavigationItemSelectedListener
{
    public static final String FRAGMENT_TAG_MAP = "mapFragmentTag";
    public static final String FRAGMENT_TAG_MAIN = "mainFragmentTag";
    public static final String FRAGMENT_TAG_FAV_PLACE = "favPlaceFragmentTag";
    public static final String FRAGMENT_TAG_FRIEND_LIST = "friendListFragmentTag";
    public static final String FRAGMENT_TAG_NOTIFY_LOCATION = "notifyLocationFragmentTag";
    public static final String FRAGMENT_TAG_LOCATION_ALARM = "locationAlarmFragmentTag";
    public static final String FRAGMENT_TAG_DIALOG = "dialogFragmentTag";
    public static final String FRAGMENT_TAG_NOTIFICATIONS = "notificationFragmentTag";

    public static final String FRAGMENT_TAG_NOTIFY_LOCATION_LIST = "notifyLocationListFragmentTag";
    public static final String FRAGMENT_TAG_SHARING_REAL_TIME_LOCATION = "shareLocationListFragmentTag";

    public static final String FRAGMENT_TAG_RECEIVING_NOTIFIED_LOCATION = "receivingNotifiedLocationFragmentTag";
    public static final String FRAGMENT_TAG_RECEIVING_SHARED_LOCATION = "receivingSharedLocationFragmentTag";
    public static final String FRAGMENT_TAG_RECEIVING_SHARED_LOCATION_ONCE = "receivingSharedLocationOnceFragmentTag";


    @BindString(R.string.noFriendsAreAddedYet)
    String youHaventAddedFriends;

    @BindString(R.string.noFavoritePlacesAreAdded)
    String youHaventSetAnyFavoritePlaces;

    @BindString(R.string.noLocationAlarmsAreSet)
    String youHaventSetAnyLocationAlarm;
    //To get fragment manager
    AppCompatActivity mActivity;
    public MapFragment mapFragment;

    FragmentManager mFragmentManager;
    public NavigationView navigationView;
    public MainFragment mainFragment;
    public DrawerLayout drawer;

    SharedPreferences mPreferences;
    int shareLocationSize, alarmSize, issueSize, notificationSize;
    private FavoritePlacesFragment favoritePlacesFragment;

    Menu menu;

    private void getSize()
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                shareLocationSize = ShareLocationDao.getList().size();
                notificationSize = NotifyLocationDao.getList().size();
                issueSize = IssuesDao.getList().size();
                alarmSize = LocationAlarmDao.getList().size();
            }
        }).start();

    }

    public NavigationUtil(Context context)
    {
        this.mActivity = (AppCompatActivity) context;
        getSize();
        mFragmentManager = mActivity.getSupportFragmentManager();
        mPreferences = WhistleBlower.getPreferences();
        drawer = ((DrawerLayout) mActivity.findViewById(R.id.drawer_layout));
        drawer.addDrawerListener(new DrawerLayout.DrawerListener()
        {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset)
            {
                getSize();
            }

            @Override
            public void onDrawerOpened(View drawerView)
            {
                setUp();
            }

            @Override
            public void onDrawerClosed(View drawerView)
            {
                getSize();
            }

            @Override
            public void onDrawerStateChanged(int newState)
            {

            }
        });
        Otto.register(this);
        ButterKnife.bind(this, mActivity);
        navigationView = ((NavigationView) mActivity.findViewById(R.id.nav_view));
        navigationView.setNavigationItemSelectedListener(this);
        menu = navigationView.getMenu();
        hideBadge(R.id.nav_loc_alarm);
        hideBadge(R.id.nav_news);
        hideBadge(R.id.nav_share_loc);
        hideBadge(R.id.nav_notify_loc);
    }

    @Subscribe
    public void showIssue(Issue address)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable(MapFragment.SHOW_ISSUE, address);
        showMapFragment(bundle);
    }

    @Subscribe
    public void showFavPlace(FavPlaces address)
    {
        favoritePlacesFragment.dismiss();
        Bundle bundle = new Bundle();
        bundle.putParcelable(MapFragment.SHOW_FAV_PLACE, address);
        showMapFragment(bundle);

    }

    @Subscribe
    public void handleAction(String action)
    {
        Log.d("Action", "handle Action : " + action);
        switch (action)
        {
            case FABUtil.SET_ALARM:
            case FABUtil.ADD_ISSUE:
            case FABUtil.ADD_FAV_PLACE:
            case FABUtil.NOTIFY_LOC:
                Bundle bundle = new Bundle();
                bundle.putString(MapFragment.HANDLE_ACTION, action);
                showMapFragment(bundle);
                break;
        }
    }

    void showMapFragment(Bundle bundle)
    {
        if (!mapFragment.isVisible())
        {
            showMapFragment();
        }
        mapFragment.reloadMapParameters(bundle);
    }

    public void unregisterOtto()
    {
        Log.d("NavigationUtil", "unregisterOtto");
        Otto.unregister(this);
    }

    public void showBadge(int id, int size)
    {
        Log.d("Badge", "showBadge");
        View actionView = menu.findItem(id).getActionView();
        TextView text = (TextView) actionView.findViewById(R.id.badge);
        Log.d("Badge", "size : " + size);
        if (size > 0)
        {
            actionView.setVisibility(View.VISIBLE);
            text.setText("" + size);
        }
        else
        {
            actionView.setVisibility(View.GONE);
        }
    }

    public void hideBadge(int id)
    {
        View actionView = menu.findItem(id).getActionView();
        actionView.setVisibility(View.GONE);
    }

    public void setUp()
    {
        Log.d("Badge", "setUp");
        showBadge(R.id.nav_share_loc, shareLocationSize);
        showBadge(R.id.nav_notify_loc, notificationSize);
        showBadge(R.id.nav_news, issueSize);
        showBadge(R.id.nav_loc_alarm, alarmSize);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(final MenuItem item)
    {
        // Handle navigation view item clicks here.
        drawer.closeDrawer(GravityCompat.START);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener()
        {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset)
            {
                Otto.post(FABUtil.HIDE_DESCRIPTION_TOAST);
                setUp();
            }

            @Override
            public void onDrawerOpened(View drawerView)
            {
                Otto.post(FABUtil.HIDE_DESCRIPTION_TOAST);
                setUp();
            }

            @Override
            public void onDrawerClosed(View drawerView)
            {
                FABUtil.closeFABMenu(mActivity);
                int id = item.getItemId();
                switch (id)
                {
                    case R.id.nav_news:
                        showNewsFeedsFragment();
                        break;
                    case R.id.nav_logout:
                        mActivity.startActivity(new Intent(mActivity, LoginActivity.class));
                        mPreferences.edit()
                                .putBoolean(LoginActivity.LOGIN_STATUS, false)
                                .commit();
                        mActivity.finish();
                        break;
                    case R.id.nav_notify_loc:
                        showNotifyLocationList();
                        break;
                    case R.id.nav_loc_alarm:
                        showAlarmFragment();
                        break;
                    case R.id.nav_share_loc:
                        showShareLocationList();
                        break;

                    case R.id.nav_friends:
                        showFriendsList();
                        break;

                    case R.id.nav_fav:
                        showFavPlacesList();
                        break;
                    case R.id.nav_map:
                        showMapFragment();
                        break;
                }
                drawer.removeDrawerListener(this);
            }

            @Override
            public void onDrawerStateChanged(int newState)
            {

            }
        });
        return true;
    }

    public void showShareLocationList(String... arguments)
    {
        Log.d(NavigationUtil.FRAGMENT_TAG_DIALOG, "showShareLocationList");
        ShareLocationListFragment shareLocationListFragment = (ShareLocationListFragment)
                mFragmentManager.findFragmentByTag(FRAGMENT_TAG_SHARING_REAL_TIME_LOCATION);

        if (shareLocationListFragment == null)
        {
            shareLocationListFragment = new ShareLocationListFragment();
        }

        if (arguments != null && arguments.length > 0)
        {
            Bundle bundle = new Bundle();
            bundle.putBoolean(arguments[0], true);
            shareLocationListFragment.setArguments(bundle);
        }
        mFragmentManager.executePendingTransactions();
        if (!shareLocationListFragment.isAdded())
        {
            shareLocationListFragment.show(mFragmentManager, FRAGMENT_TAG_SHARING_REAL_TIME_LOCATION);
        }
    }

    public void showNotifyLocationList(String... arguments)
    {
        NotifyLocationListFragment notifyLocationListFragment = (NotifyLocationListFragment)
                mFragmentManager.findFragmentByTag(FRAGMENT_TAG_NOTIFY_LOCATION);
        if (notifyLocationListFragment == null)
        {
            notifyLocationListFragment = new NotifyLocationListFragment();
        }
        if (arguments != null && arguments.length > 0)
        {
            Bundle bundle = new Bundle();
            bundle.putBoolean(arguments[0], true);
            notifyLocationListFragment.setArguments(bundle);
        }
        mFragmentManager.executePendingTransactions();
        if (!notifyLocationListFragment.isAdded())
        {
            notifyLocationListFragment.show(mFragmentManager, FRAGMENT_TAG_NOTIFY_LOCATION);
        }
    }

    public void showAlarmFragment()
    {
        Log.d(NavigationUtil.FRAGMENT_TAG_DIALOG, "showAlarmFragment");

        LocationAlarmListFragment locationAlarmListFragment = (LocationAlarmListFragment)
                mFragmentManager.findFragmentByTag(FRAGMENT_TAG_LOCATION_ALARM);
        if (locationAlarmListFragment == null)
        {
            locationAlarmListFragment = new LocationAlarmListFragment();
        }
        mFragmentManager.executePendingTransactions();
        if (!locationAlarmListFragment.isAdded())
        {
            locationAlarmListFragment.show(mFragmentManager, FRAGMENT_TAG_LOCATION_ALARM);
        }
    }

    public void showFavPlacesList()
    {
        favoritePlacesFragment = (FavoritePlacesFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TAG_FAV_PLACE);
        if (favoritePlacesFragment == null)
        {
            favoritePlacesFragment = new FavoritePlacesFragment();
        }
        mFragmentManager.executePendingTransactions();
        if (!favoritePlacesFragment.isAdded())
        {
            favoritePlacesFragment.show(mFragmentManager, FRAGMENT_TAG_FAV_PLACE);
        }
    }

    public void showFriendsList()
    {
        FriendListFragment friendListFragment = (FriendListFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TAG_FRIEND_LIST);
        if (friendListFragment == null)
        {
            friendListFragment = new FriendListFragment();
        }
        mFragmentManager.executePendingTransactions();
        if (!friendListFragment.isAdded())
        {
            mFragmentManager
                    .beginTransaction()
                    .replace(R.id.content_layout, friendListFragment, FRAGMENT_TAG_FRIEND_LIST)
                    .addToBackStack(null)
                    .commit();
        }

    }

    public void showMapFragment()
    {
        if (MiscUtil.isGoogleServicesOk(mActivity))
        {
            mapFragment = (MapFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TAG_MAP);
            if (mapFragment == null)
            {
                mapFragment = new MapFragment();
            }
            mFragmentManager.executePendingTransactions();
            if (!mapFragment.isAdded())
            {
                mFragmentManager
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.content_layout, mapFragment, FRAGMENT_TAG_MAP)
                        .commit();
            }
        }
    }

    public MapFragment getMapFragment()
    {
        MapFragment mapFragment = (MapFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TAG_MAP);
        if (mapFragment == null)
        {
            mapFragment = new MapFragment();
        }
        return mapFragment;
    }

    public void showNewsFeedsFragment()
    {
        mainFragment = (MainFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TAG_MAIN);
        if (mainFragment == null)
        {
            mainFragment = new MainFragment();
        }
        mFragmentManager.executePendingTransactions();
        if (!mainFragment.isAdded())
        {
            mFragmentManager
                    .beginTransaction()
                    .replace(R.id.content_layout, mainFragment, FRAGMENT_TAG_MAIN)
                    .addToBackStack(null)
                    .commit();
        }
    }

    public static void highlightNavigationDrawerMenu(AppCompatActivity mActivity, int id)
    {
        NavigationView navigationView = (NavigationView) mActivity.findViewById(R.id.nav_view);
        Menu navigationMenu = navigationView.getMenu();
        MenuItem menuItem = navigationMenu.findItem(id);
        menuItem.setChecked(true);
        menuItem.setEnabled(true);
    }

    public void showNotificationsFragment()
    {
        NotificationsFragment notificationsFragment = (NotificationsFragment)
                mFragmentManager.findFragmentByTag(FRAGMENT_TAG_NOTIFICATIONS);
        if (notificationsFragment == null)
        {
            notificationsFragment = new NotificationsFragment();
        }
        mFragmentManager.executePendingTransactions();
        if (!notificationsFragment.isAdded())
        {
            notificationsFragment.show(mFragmentManager, FRAGMENT_TAG_NOTIFY_LOCATION);
        }
    }

    public void showNotifiedLocationOnMap()
    {

    }
}