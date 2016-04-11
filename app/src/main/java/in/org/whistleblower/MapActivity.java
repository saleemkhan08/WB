package in.org.whistleblower;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

import in.org.whistleblower.icon.FontAwesomeIcon;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.utilities.ConnectivityListener;
import in.org.whistleblower.utilities.FABUtil;
import in.org.whistleblower.utilities.ImageUtil;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.NavigationUtil;

public class MapActivity extends AppCompatActivity
{
    NavigationUtil mNavigationUtil;
    ImageView profilePic;
    SharedPreferences preferences;
    MiscUtil mUtil;
    private TextView emailId;
    private TextView username;
    private FABUtil mFabUtil;
    private DrawerLayout drawer;
    private FloatingActionsMenu fabMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_acivity);

        mUtil = new MiscUtil(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
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
        mNavigationUtil = new NavigationUtil(this);
        mNavigationUtil.setUp(mUtil);
        mFabUtil = new FABUtil(this);
        mFabUtil.setUp();
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
                }, MapActivity.this);
            }

            @Override
            public void onMenuCollapsed()
            {
            }
        });
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
}