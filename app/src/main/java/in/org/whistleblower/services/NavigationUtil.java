package in.org.whistleblower.services;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import in.org.whistleblower.LoginActivity;
import in.org.whistleblower.MainActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.fragments.MainFragment;
import in.org.whistleblower.icon.FontAwesomeIcon;

public class NavigationUtil implements NavigationView.OnNavigationItemSelectedListener
{
    public static final String MAP_FRAGMENT_TAG = "mapFragmentTag";
    public static final String MAIN_FRAGMENT_TAG = "mainFragmentTag";

    MiscUtil util;
    LocationUtil locationUtil;

    DrawerLayout drawer;
    private AppCompatActivity activity;
    FragmentManager fragmentManager;
    public NavigationUtil(DrawerLayout drawer, AppCompatActivity activity)
    {
        this.drawer = drawer;
        this.activity = activity;
        fragmentManager = activity.getSupportFragmentManager();
    }
    public void setUp(MiscUtil util, LocationUtil locationUtil)
    {
        this.util = util;
        this.locationUtil = locationUtil;
        NavigationView navigationView = ((NavigationView) activity.findViewById(R.id.nav_view));
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
            util.toast("Not Implemented");
        }
        else if (id == R.id.nav_share_loc)
        {
            util.toast("Not Implemented");
        }
        else if (id == R.id.nav_add_friend)
        {
            util.toast("Not Implemented");
        }
        else if (id == R.id.nav_logout)
        {
            activity.startActivity(new Intent(activity, LoginActivity.class));
            activity.getSharedPreferences(MainActivity.WHISTLE_BLOWER_PREFERENCE, Context.MODE_PRIVATE).edit()
                    .putBoolean(LoginActivity.LOGIN_STATUS, false)
                    .commit();
            activity.finish();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void showMapFragment()
    {
        locationUtil.showMyLocButton();
        fragmentManager
                .beginTransaction()
                .replace(R.id.content_layout, locationUtil.mapFragment, MAP_FRAGMENT_TAG)
                .commit();
    }

    public void showNewsFeedsFragment()
    {
        locationUtil.hideMyLocButton();
        MainFragment mainFragment = new MainFragment();
        fragmentManager
                .beginTransaction()
                .replace(R.id.content_layout, mainFragment, MAIN_FRAGMENT_TAG)
                .commit();
    }
}
