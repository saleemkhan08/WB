package in.org.whistleblower;

import android.content.Intent;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.List;

import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.storage.QueryResultListener;
import in.org.whistleblower.storage.RStorageObject;
import in.org.whistleblower.storage.RStorageQuery;
import in.org.whistleblower.storage.StorageListener;
import in.org.whistleblower.storage.StorageObject;
import in.org.whistleblower.utilities.ConnectivityListener;
import in.org.whistleblower.utilities.MiscUtil;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener, ConnectivityListener
{
    public static final String LOGIN_STATUS = "login_status";
    private static final String SIGNING_IN = "Signing in...";
    public static final String USER_ID = "userId";
    private MiscUtil util;
    static Typeface mFont;

    @Override
    public void onInternetConnected()
    {
        signIn();
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
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

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
    }


    private void updateUI(boolean signedIn)
    {
        if (signedIn)
        {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        }
        else
        {
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
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
                        updateUI(false);
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
                        updateUI(false);
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
                // updateUI(true);
                Uri photo_url = acct.getPhotoUrl();
                saveData(acct.getEmail(), acct.getDisplayName(), acct.getId(), photo_url != null ? photo_url.toString() : "");
            }
            else
            {
                updateUI(false);
            }
        }
    }


    public void saveData(final String email, final String name, final String googleId, final String photo_url)
    {
        RStorageQuery<RStorageObject> query = new RStorageQuery<>(Accounts.TABLE);//ParseQuery.getQuery(Account.TABLE);

        query.getWhereEqualTo(Accounts.GOOGLE_ID, googleId, new QueryResultListener<StorageObject>()
        {
            @Override
            public void onResult(List<StorageObject> userList)
            {
                MiscUtil.log("List : "+userList);
                MiscUtil.log("Len : "+ userList.size());
                if (userList.size() == 0)
                {
                    final StorageObject userAccount = new RStorageObject(Accounts.TABLE);
                    userAccount.put(Accounts.EMAIL, email);
                    userAccount.put(Accounts.NAME, name);
                    userAccount.put(Accounts.GOOGLE_ID, googleId);
                    userAccount.put(Accounts.PHOTO_URL, photo_url);
                    userAccount.store(new StorageListener()
                    {
                        @Override
                        public void onSuccess()
                        {
                            MiscUtil.log("Saved");
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            util.hideProgressDialog();
                            PreferenceManager.getDefaultSharedPreferences(LoginActivity.this)
                                    .edit()
                                    .putBoolean(LOGIN_STATUS, true)
                                    .putString(Accounts.EMAIL, email)
                                    .putString(Accounts.NAME, name)
                                    .putString(Accounts.GOOGLE_ID, googleId)
                                    .putString(Accounts.PHOTO_URL, photo_url)
                                    .putString(USER_ID, userAccount.getPrimaryKey())
                                    .commit();
                            finish();
                        }

                        @Override
                        public void onError(String e)
                        {
                            util.hideProgressDialog();
                            util.toast(e);
                            util.toast("Please Try again!");
                            signOut();
                        }
                    });
                }
                else
                {
                    MiscUtil.log("List : "+userList);
                    MiscUtil.log("Values : email : "+email+", name : "+name+", googleId : "+googleId+", photo_url : "+photo_url);

                    StorageObject userAccount = userList.get(0);
                    userAccount.put(Accounts.EMAIL, email);
                    userAccount.put(Accounts.NAME, name);
                    userAccount.put(Accounts.PHOTO_URL, photo_url);

                    userAccount.update(new StorageListener()
                    {
                        @Override
                        public void onSuccess()
                        {
                            MiscUtil.log("Updated");
                        }

                        @Override
                        public void onError(String e)
                        {
                            MiscUtil.log("Couldn't Update");
                        }
                    });

                    PreferenceManager.getDefaultSharedPreferences(LoginActivity.this)
                            .edit()
                            .putBoolean(LOGIN_STATUS, true)
                            .putString(Accounts.EMAIL, email)
                            .putString(Accounts.NAME, name)
                            .putString(Accounts.GOOGLE_ID, googleId)
                            .putString(Accounts.PHOTO_URL, photo_url)
                            .commit();

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    util.hideProgressDialog();
                    finish();
                }
            }

            @Override
            public void onError(String e)
            {
                util.hideProgressDialog();
                util.toast(e);
                util.toast("Please Try again!");
                signOut();
            }
        });
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
                util.isConnected((ConnectivityListener)this);
                break;
            case R.id.sign_out_button:
                signOut();
                break;
            case R.id.disconnect_button:
                revokeAccess();
                break;
        }
    }

}
