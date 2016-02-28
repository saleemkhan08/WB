package in.org.whistleblower.utilities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import in.org.whistleblower.LoginActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.SearchActivity;
import in.org.whistleblower.fragments.MainFragment;
import in.org.whistleblower.fragments.MapFragment;
import in.org.whistleblower.icon.FontAwesomeIcon;

public class NavigationUtil implements NavigationView.OnNavigationItemSelectedListener
{
    public static final String MAP_FRAGMENT_TAG = "mapFragmentTag";
    public static final String MAIN_FRAGMENT_TAG = "mainFragmentTag";
    public static final String KEY_CATEGORY = "KEY_CATEGORY";
    public static final String ADD_FRIEND = "ADD_FRIEND";
    public static final String FRIEND_LIST = "FRIEND_LIST";
    public static final String ADD_FAV_PLACE = "ADD_FAV_PLACE";
    public static final String FAV_PLACE = "FAV_PLACE";

    MiscUtil util;
    //To get fragment manager
    AppCompatActivity mActivity;

    public MapFragment mapFragment;

    FragmentManager fragmentManager;
    public NavigationView navigationView;
    public MainFragment mainFragment;
    private DrawerLayout drawer;
    public NavigationUtil(Context context)
    {
        this.mActivity = (AppCompatActivity) context;
        fragmentManager = mActivity.getSupportFragmentManager();
        drawer =  ((DrawerLayout) mActivity.findViewById(R.id.drawer_layout));
    }

    public void setUp(MiscUtil util)
    {
        navigationView = ((NavigationView) mActivity.findViewById(R.id.nav_view));
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();
        menu.getItem(0).setIcon(util.getIcon(FontAwesomeIcon.MAP_MARKER));
        menu.getItem(1).setIcon(util.getIcon(FontAwesomeIcon.NEWS));
        menu.getItem(2).setIcon(util.getIcon(FontAwesomeIcon.STAR));
        menu.getItem(3).setIcon(util.getIcon(FontAwesomeIcon.GROUP));
        menu.findItem(R.id.nav_share_loc).setIcon(util.getIcon(FontAwesomeIcon.SHARE_ALT));
        menu.findItem(R.id.nav_add_friend).setIcon(util.getIcon(FontAwesomeIcon.USER));
        menu.findItem(R.id.nav_logout).setIcon(util.getIcon(FontAwesomeIcon.SIGNOUT));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        FABUtil.closeFABMenu(mActivity);
        int id = item.getItemId();
        if (id == R.id.nav_news)
        {
            showNewsFeedsFragment();
        }
        else if (id == R.id.nav_map)
        {
            showMapFragment();
        }
        else if (id == R.id.nav_fav)
        {
            util.toast("Not Implemented");
        }
        else if (id == R.id.nav_friends)
        {
            Intent intent = new Intent(mActivity, SearchActivity.class);
            intent.putExtra(KEY_CATEGORY, FRIEND_LIST);
            mActivity.startActivity(intent);
        }
        else if (id == R.id.nav_share_loc)
        {
            util.toast("Not Implemented");
        }
        else if (id == R.id.nav_add_friend)
        {
            Intent intent = new Intent(mActivity, SearchActivity.class);
            intent.putExtra(KEY_CATEGORY, ADD_FRIEND);
            mActivity.startActivity(intent);
        }
        else if (id == R.id.nav_logout)
        {
            mActivity.startActivity(new Intent(mActivity, LoginActivity.class));
            PreferenceManager.getDefaultSharedPreferences(mActivity)
                    .edit()
                    .putBoolean(LoginActivity.LOGIN_STATUS, false)
                    .commit();
            mActivity.finish();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void showMapFragment()
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

    public static void showMapFragment(AppCompatActivity mActivity, Bundle bundle)
    {
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) mActivity.getSupportFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);
        if (mapFragment == null)
        {
            mapFragment = new MapFragment();
        }

        if(!mapFragment.isVisible())
        {
            mapFragment.setArguments(bundle);
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.content_layout, mapFragment, MAP_FRAGMENT_TAG)
                    .commit();
        }
        else
        {
            mapFragment.reloadMapParameters(bundle);
        }
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
                .commit();
    }
    public static void highlightMenu(AppCompatActivity mActivity, int id)
    {
        NavigationView navigationView = (NavigationView) mActivity.findViewById(R.id.nav_view);
        Menu navigationMenu = navigationView.getMenu();
        MenuItem menuItem = navigationMenu.findItem(id);
        menuItem.setChecked(true);
        menuItem.setEnabled(true);
    }
}