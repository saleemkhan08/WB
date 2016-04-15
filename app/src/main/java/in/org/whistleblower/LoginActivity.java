package in.org.whistleblower;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import in.org.whistleblower.gcm.RegistrationIntentService;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.interfaces.ConnectivityListener;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.NavigationUtil;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener, ViewPager.OnPageChangeListener, ConnectivityListener
{
    public static final String LOGIN_STATUS = "login_status";
    private static final String SIGNING_IN = "Signing in...";
    private MiscUtil util;
    static Typeface mFont;
    private ViewPager mViewPager;
    private TextView mTxtPageIndicator;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;

    @Override
    public void onInternetConnected()
    {
        signIn();
    }

    @Override
    public void onCancelled()
    {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {
        Log.d(TAG, "onPageScrolled");
    }

    @Override
    public void onPageSelected(int position)
    {
        Log.d(TAG, "onPageSelected :: " + position);
        String pageIndicator = "";
        switch (position)
        {
            case 0:
                pageIndicator = pageIndicator + getString(R.string.current_page) + " " + getString(R.string.other_page) + " " + getString(R.string.other_page);
                break;
            case 1:
                pageIndicator = pageIndicator + getString(R.string.other_page) + " " + getString(R.string.current_page) + " " + getString(R.string.other_page);
                break;
            case 2:
                pageIndicator = pageIndicator + getString(R.string.other_page) + " " + getString(R.string.other_page) + " " + getString(R.string.current_page);
                break;

        }
        mTxtPageIndicator.setText(pageIndicator);
    }

    @Override
    public void onPageScrollStateChanged(int state)
    {
        Log.d(TAG, "onPageScrollStateChanged :: " + state);
    }

    public static class PlaceholderFragment extends Fragment
    {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public static PlaceholderFragment newInstance(int sectionNumber)
        {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment()
        {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_image, container, false);
            TextView iconTextView = (TextView) rootView.findViewById(R.id.fragmentIcon);
            TextView descTextView = (TextView) rootView.findViewById(R.id.fragmentDesc);

            iconTextView.setTypeface(mFont);

            switch (getArguments().getInt(ARG_SECTION_NUMBER))
            {
                case 1:
                    iconTextView.setText(R.string.user_icon);
                    descTextView.setText(R.string.user_text);
                    break;
                case 2:
                    iconTextView.setText(R.string.whistle_blower_icon);
                    descTextView.setText(R.string.whistle_blower_text);
                    break;
                case 3:
                    iconTextView.setText(R.string.volunteer_icon);
                    descTextView.setText(R.string.volunteer_text);
            }
            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {

        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount()
        {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            switch (position)
            {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }

    // ---------------------------------------------------------------------------

    private static final String TAG = "IdTokenActivity";
    private static final int RC_GET_TOKEN = 9002;

    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        util = new MiscUtil(this);

        mFont = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.hide();
        }
        // Button click listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        mTxtPageIndicator = (TextView) findViewById(R.id.pageIndicator);
        mTxtPageIndicator.setTypeface(mFont);
        mTxtPageIndicator.setText("" + getString(R.string.current_page) + " " + getString(R.string.other_page) + " " + getString(R.string.other_page));

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build GoogleAPIClient with the Google Sign-In API and the above options.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(gso.getScopeArray());

        mRegistrationBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(RegistrationIntentService.SENT_TOKEN_TO_SERVER, false);
                if (sentToken)
                {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    util.hideProgressDialog();
                    finish();
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Please Try Again Later!", Toast.LENGTH_SHORT).show();
                    util.hideProgressDialog();
                    signOut();
                }
            }
        };
        registerReceiver();
    }

    private void registerReceiver()
    {
        if (!isReceiverRegistered)
        {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(RegistrationIntentService.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause()
    {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }

    private void signOut()
    {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>()
                {
                    @Override
                    public void onResult(Status status)
                    {
                        Log.d(TAG, "signOut:onResult:" + status);
                    }
                });
    }

    private void revokeAccess()
    {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>()
                {
                    @Override
                    public void onResult(Status status)
                    {
                        Log.d(TAG, "revokeAccess:onResult:" + status);
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GET_TOKEN)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, "onActivityResult:GET_TOKEN:success:" + result.getStatus().isSuccess());

            if (result.isSuccess())
            {
                GoogleSignInAccount acct = result.getSignInAccount();
                if (NavigationUtil.isGoogleServicesOk(LoginActivity.this))
                {
                    Uri photo_url = acct.getPhotoUrl();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    preferences.edit()
                            .putString(Accounts.NAME, acct.getDisplayName())
                            .putBoolean(LoginActivity.LOGIN_STATUS, true)
                            .putString(Accounts.EMAIL, acct.getEmail())
                            .putString(Accounts.PHOTO_URL, photo_url != null ? photo_url.toString() : "")
                            .putString(Accounts.GOOGLE_ID, acct.getId())
                            .commit();

                    Intent intent = new Intent(this, RegistrationIntentService.class);
                    startService(intent);
                }
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        util.hideProgressDialog();
        util.toast("Something went wrong!");
        util.toast("Please Try again!");
        signOut();
        revokeAccess();
    }

    private void signIn()
    {
        signOut();
        revokeAccess();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GET_TOKEN);
        util.showIndeterminateProgressDialog(SIGNING_IN);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.sign_in_button:
                util.isConnected((ConnectivityListener) this);
                break;
        }
    }
}