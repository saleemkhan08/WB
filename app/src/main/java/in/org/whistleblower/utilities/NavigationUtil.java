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
import in.org.whistleblower.fragments.ShareLocationListFragment;
import in.org.whistleblower.fragments.TabbedDialogFragment;
import in.org.whistleblower.models.FavPlaces;
import in.org.whistleblower.models.Issue;
import in.org.whistleblower.singletons.Otto;

public class NavigationUtil implements NavigationView.OnNavigationItemSelectedListener
{
    public static final String MAP_FRAGMENT_TAG = "mapFragmentTag";
    public static final String MAIN_FRAGMENT_TAG = "mainFragmentTag";
    public static final String KEY_CATEGORY = "KEY_CATEGORY";
    public static final String ADD_FRIEND = "ADD_FRIEND";
    public static final String FRIEND_LIST = "FRIEND_LIST";
    public static final String ADD_FAV_PLACE = "ADD_FAV_PLACE";
    public static final String FAV_PLACE = "FAV_PLACE";
    public static final String FAV_PLACE_FRAGMENT_TAG = "FAV_PLACE_FRAGMENT_TAG";
    public static final String FRIEND_LIST_FRAGMENT_TAG = "FRIEND_LIST_FRAGMENT_TAG";
    public static final String NOTIFY_LOCATION_FRAGMENT_TAG = "NOTIFY_LOCATION_FRAGMENT_TAG";
    public static final String SHARE_LOCATION_LIST_FRAGMENT_TAG = "SHARE_LOCATION_LIST_FRAGMENT_TAG";
    public static final String LOCATION_ALARM_FRAGMENT_TAG = "LOCATION_ALARM_FRAGMENT_TAG";
    public static final String DIALOG_FRAGMENT_TAG = "dialogFragmentTag";

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
    private FavoritePlacesFragment favoritePlacesFragment;

    public NavigationUtil(Context context)
    {
        this.mActivity = (AppCompatActivity) context;
        mFragmentManager = mActivity.getSupportFragmentManager();
        mPreferences = WhistleBlower.getPreferences();
        drawer = ((DrawerLayout) mActivity.findViewById(R.id.drawer_layout));
        Otto.register(this);
        ButterKnife.bind(this, mActivity);
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
        Bundle bundle = new Bundle();
        bundle.putParcelable(MapFragment.SHOW_FAV_PLACE, address);
        showMapFragment(bundle);
        favoritePlacesFragment.dismiss();
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
        Log.d("Action", "Bundle : " + bundle);
        if (MiscUtil.isGoogleServicesOk(mActivity))
        {
            if (mapFragment == null)
            {
                mapFragment = getMapFragment();
            }

            if (!mapFragment.isVisible())
            {
                mapFragment.setArguments(bundle);
                mFragmentManager
                        .beginTransaction()
                        .replace(R.id.content_layout, mapFragment, MAP_FRAGMENT_TAG)
                        .addToBackStack(null)
                        .commit();
            }
            else
            {
                mapFragment.reloadMapParameters(bundle);
            }
        }
    }

    public void unregisterOtto()
    {
        Otto.unregister(this);
    }

    public void setUp(MiscUtil util)
    {
        navigationView = ((NavigationView) mActivity.findViewById(R.id.nav_view));
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();
        /*menu.getItem(0).setIcon(mActivity.getDrawable(R.drawable.map_marker_icon_accent));
        menu.getItem(1).setIcon(mActivity.getDrawable(R.drawable.map_marker_icon_accent));//newsfeeds_icon_accent
        menu.getItem(2).setIcon(mActivity.getDrawable(R.drawable.map_marker_icon_accent));
        menu.getItem(3).setIcon(mActivity.getDrawable(R.drawable.map_marker_icon_accent));
        menu.findItem(R.id.nav_share_loc).setIcon(mActivity.getDrawable(R.drawable.map_marker_icon_accent));
        menu.findItem(R.id.nav_notify_loc).setIcon(mActivity.getDrawable(R.drawable.map_marker_icon_accent));
        menu.findItem(R.id.nav_logout).setIcon(mActivity.getDrawable(R.drawable.map_marker_icon_accent));*/
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
            }

            @Override
            public void onDrawerOpened(View drawerView)
            {
                Otto.post(FABUtil.HIDE_DESCRIPTION_TOAST);
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

    public void showShareLocationList()
    {
        Log.d(NavigationUtil.DIALOG_FRAGMENT_TAG,"showShareLocationList");
        ShareLocationListFragment shareLocationListFragment = (ShareLocationListFragment)
                mFragmentManager.findFragmentByTag(SHARE_LOCATION_LIST_FRAGMENT_TAG);
        if (shareLocationListFragment == null)
        {
            shareLocationListFragment = new ShareLocationListFragment();
        }
        mFragmentManager.executePendingTransactions();
        if(!shareLocationListFragment.isAdded())
        {
            shareLocationListFragment.show(mFragmentManager, SHARE_LOCATION_LIST_FRAGMENT_TAG);
        }
    }

    public void showNotifyLocationList()
    {
        TabbedDialogFragment tabbedDialogFragment = (TabbedDialogFragment)
                mFragmentManager.findFragmentByTag(NOTIFY_LOCATION_FRAGMENT_TAG);
        if (tabbedDialogFragment == null)
        {
            tabbedDialogFragment = new TabbedDialogFragment();
        }
        mFragmentManager.executePendingTransactions();
        if(!tabbedDialogFragment.isAdded())
        {
            tabbedDialogFragment.show(mFragmentManager, NOTIFY_LOCATION_FRAGMENT_TAG);
        }

//        NotifyLocationListFragment notifyLocationListFragment = (NotifyLocationListFragment)
//                mFragmentManager.findFragmentByTag(NOTIFY_LOCATION_FRAGMENT_TAG);
//        if (notifyLocationListFragment == null)
//        {
//            notifyLocationListFragment = new NotifyLocationListFragment();
//        }
//        mFragmentManager.executePendingTransactions();
//        if(!notifyLocationListFragment.isAdded())
//        {
//            notifyLocationListFragment.show(mFragmentManager, NOTIFY_LOCATION_FRAGMENT_TAG);
//        }
    }

    public void showAlarmFragment()
    {
        Log.d(NavigationUtil.DIALOG_FRAGMENT_TAG,"showAlarmFragment");

        LocationAlarmListFragment locationAlarmListFragment = (LocationAlarmListFragment)
                mFragmentManager.findFragmentByTag(LOCATION_ALARM_FRAGMENT_TAG);
        if (locationAlarmListFragment == null)
        {
            locationAlarmListFragment = new LocationAlarmListFragment();
        }
        mFragmentManager.executePendingTransactions();
        if(!locationAlarmListFragment.isAdded())
        {
            locationAlarmListFragment.show(mFragmentManager, LOCATION_ALARM_FRAGMENT_TAG);
        }
    }

    public void showFavPlacesList()
    {
        favoritePlacesFragment = (FavoritePlacesFragment) mFragmentManager.findFragmentByTag(FAV_PLACE_FRAGMENT_TAG);
        if (favoritePlacesFragment == null)
        {
            favoritePlacesFragment = new FavoritePlacesFragment();
        }
        mFragmentManager.executePendingTransactions();
        if(!favoritePlacesFragment.isAdded())
        {
            favoritePlacesFragment.show(mFragmentManager, FAV_PLACE_FRAGMENT_TAG);
        }
    }

    public void showFriendsList()
    {
        FriendListFragment friendListFragment = (FriendListFragment) mFragmentManager.findFragmentByTag(FRIEND_LIST_FRAGMENT_TAG);
        if (friendListFragment == null)
        {
            friendListFragment = new FriendListFragment();
        }
        mFragmentManager.executePendingTransactions();
        if(!friendListFragment.isAdded())
        {
            mFragmentManager
                    .beginTransaction()
                    .replace(R.id.content_layout, friendListFragment, FRIEND_LIST_FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
        }

    }

    public void showMapFragment()
    {
        if (MiscUtil.isGoogleServicesOk(mActivity))
        {
            mapFragment = (MapFragment) mFragmentManager.findFragmentByTag(MAP_FRAGMENT_TAG);
            if (mapFragment == null)
            {
                mapFragment = new MapFragment();
            }
            mFragmentManager.executePendingTransactions();
            if(!mapFragment.isAdded())
            {
                mFragmentManager
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.content_layout, mapFragment, MAP_FRAGMENT_TAG)
                        .commit();
            }
        }
    }

    public MapFragment getMapFragment()
    {
        MapFragment mapFragment = (MapFragment) mFragmentManager.findFragmentByTag(MAP_FRAGMENT_TAG);
        if (mapFragment == null)
        {
            mapFragment = new MapFragment();
        }
        return mapFragment;
    }

    public void showNewsFeedsFragment()
    {
        mainFragment = (MainFragment) mFragmentManager.findFragmentByTag(MAIN_FRAGMENT_TAG);
        if (mainFragment == null)
        {
            mainFragment = new MainFragment();
        }
        mFragmentManager.executePendingTransactions();
        if(!mainFragment.isAdded())
        {
            mFragmentManager
                    .beginTransaction()
                    .replace(R.id.content_layout, mainFragment, MAIN_FRAGMENT_TAG)
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
}