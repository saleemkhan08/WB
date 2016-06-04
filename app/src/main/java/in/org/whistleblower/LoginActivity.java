package in.org.whistleblower;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import in.org.whistleblower.interfaces.ConnectivityListener;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.WBDataBase;
import in.org.whistleblower.utilities.MiscUtil;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        ViewPager.OnPageChangeListener, ConnectivityListener
{
    public static final String LOGIN_STATUS = "login_status";
    private static final String SIGNING_IN = "Signing in...";
    private MiscUtil util;
    public static Typeface typeface;
    RelativeLayout pageIndicator;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        util = new MiscUtil(this);
        pageIndicator = (RelativeLayout) findViewById(R.id.selected);
        deleteDatabase(WBDataBase.DATABASE_NAME);
        WhistleBlower.getPreferences().edit().clear().apply();
        typeface = Typeface.createFromAsset(getAssets(), "Gabriola.ttf");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.hide();
        }
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
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
        signInButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                util.isConnected((ConnectivityListener) LoginActivity.this);
            }
        });
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(gso.getScopeArray());
    }

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

        TransitionManager.beginDelayedTransition(pageIndicator, new Slide());
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        switch (position)
        {
            case 0:
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                break;
            case 1:
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;
            case 2:
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                break;
        }
        pageIndicator.setLayoutParams(params); //causes layout update
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
            ImageView iconView = (ImageView) rootView.findViewById(R.id.fragmentIcon);
            TextView descTextView = (TextView) rootView.findViewById(R.id.fragmentDesc);
            descTextView.setTypeface(typeface);
            switch (getArguments().getInt(ARG_SECTION_NUMBER))
            {
                case 1:
                    iconView.setImageResource(R.mipmap.notifier);
                    descTextView.setText(R.string.user_text);
                    break;
                case 2:
                    iconView.setImageResource(R.mipmap.whistle_blower);
                    descTextView.setText(R.string.whistle_blower_text);
                    break;
                case 3:
                    iconView.setImageResource(R.mipmap.volunteer);
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

    private void signOut()
    {
        if(mGoogleApiClient.isConnected())
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
    }

    private void revokeAccess()
    {
        if (mGoogleApiClient.isConnected())
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
                if (MiscUtil.isGoogleServicesOk(LoginActivity.this))
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

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    util.hideProgressDialog();
                    Intent intent = new Intent(this, RegistrationIntentService.class);
                    startService(intent);
                    finish();
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
}