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
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import in.org.whistleblower.FriendListActivity;
import in.org.whistleblower.LocationListActivity;
import in.org.whistleblower.LoginActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.fragments.FavoritePlacesFragment;
import in.org.whistleblower.fragments.MainFragment;
import in.org.whistleblower.fragments.MapFragment;
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
    //To get fragment manager
    AppCompatActivity mActivity;
    public MapFragment mapFragment;

    FragmentManager fragmentManager;
    public NavigationView navigationView;
    public MainFragment mainFragment;
    private DrawerLayout drawer;

    @Inject
    SharedPreferences mPreferences;

    public NavigationUtil(Context context)
    {
        this.mActivity = (AppCompatActivity) context;
        fragmentManager = mActivity.getSupportFragmentManager();
        WhistleBlower.getComponent().inject(this);
        drawer = ((DrawerLayout) mActivity.findViewById(R.id.drawer_layout));
        Otto.register(this);
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
                fragmentManager
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
                        Toast.makeText(mActivity, "Notify Location", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.nav_share_loc:
                        Intent shareLocationList = new Intent(mActivity, LocationListActivity.class);
                        shareLocationList.putExtra(LocationListActivity.SHARE_LOCATION_LIST, true);
                        mActivity.startActivity(shareLocationList);
                        break;

                    case R.id.nav_friends:
                        Intent intent = new Intent(mActivity, FriendListActivity.class);
                        intent.putExtra(KEY_CATEGORY, FRIEND_LIST);
                        mActivity.startActivity(intent);
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

    public void showFavPlacesList()
    {
        FavoritePlacesFragment favoritePlacesFragment = (FavoritePlacesFragment) mActivity.getSupportFragmentManager().findFragmentByTag(FAV_PLACE_FRAGMENT_TAG);
        if (favoritePlacesFragment == null)
        {
            favoritePlacesFragment = new FavoritePlacesFragment();
        }
        fragmentManager
                .beginTransaction()
                .replace(R.id.content_layout, favoritePlacesFragment, FAV_PLACE_FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
    }

    public void showMapFragment()
    {
        if (MiscUtil.isGoogleServicesOk(mActivity))
        {
            mapFragment = (MapFragment) mActivity.getSupportFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);
            if (mapFragment == null)
            {
                mapFragment = new MapFragment();
            }
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.content_layout, mapFragment, MAP_FRAGMENT_TAG)
                    .commit();
        }
    }

    public MapFragment getMapFragment()
    {
        if (fragmentManager == null)
        {
            fragmentManager = mActivity.getSupportFragmentManager();
        }

        MapFragment mapFragment = (MapFragment) mActivity.getSupportFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);
        if (mapFragment == null)
        {
            mapFragment = new MapFragment();
        }
        return mapFragment;
    }

    public void showNewsFeedsFragment()
    {
        mainFragment = (MainFragment) mActivity.getSupportFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);
        if (mainFragment == null)
        {
            mainFragment = new MainFragment();
        }
        fragmentManager
                .beginTransaction()
                .replace(R.id.content_layout, mainFragment, MAIN_FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
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