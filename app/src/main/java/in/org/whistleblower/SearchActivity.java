package in.org.whistleblower;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.FavPlaces;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.NavigationUtil;

public class SearchActivity extends AppCompatActivity
{
    String category;
    SearchTask mSearchTask;
    EditText searchInput;
    MiscUtil mUtil;
    View mProgressBar;
    static SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mUtil = new MiscUtil(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mProgressBar = findViewById(R.id.uploadProgressBar);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        searchInput = (EditText) findViewById(R.id.searchInput);
        searchInput.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (mSearchTask != null && mSearchTask.getStatus().equals(AsyncTask.Status.RUNNING))
                {
                    mSearchTask.cancel(true);
                }
                mSearchTask = new SearchTask(SearchActivity.this);
                mSearchTask.execute(category, s.toString());
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });

        Intent intent = getIntent();
        if (intent != null)
        {
            category = getIntent().getStringExtra(NavigationUtil.KEY_CATEGORY);
        }
        mSearchTask = new SearchTask(this);
        mSearchTask.execute(category, null);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed()
    {
        if (mSearchTask != null && mSearchTask.getStatus().equals(AsyncTask.Status.RUNNING))
        {
            mSearchTask.cancel(true);
            mProgressBar.setVisibility(View.GONE);
        }
        else
        {
            super.onBackPressed();
        }
    }

    private static class SearchTask extends AsyncTask<String, Void, String>
    {
        private Activity mContext;
        private View mProgressBar, mNoRecordsText;
        private List<Accounts> mUserList;
        private List<String> mFriendList;
        private List<FavPlaces> mAddressList;
        boolean isFriendList;
        RecyclerView searchResultView;
        private List<String> mFavPlacesList;

        public SearchTask(Activity mContext)
        {
            this.mContext = mContext;
            mFriendList = new ArrayList<>();
            mUserList = new ArrayList<>();
            mAddressList = new ArrayList<>();
            mFavPlacesList = new ArrayList<>();
        }

        @Override
        protected String doInBackground(String... params)
        {
            switch (params[0])
            {
                case NavigationUtil.ADD_FRIEND:
                    loadUserList(params[1]);
                    isFriendList = false;
                    break;
                case NavigationUtil.FRIEND_LIST:
                    loadFriendList(params[1]);
                    isFriendList = true;
                    break;
                case NavigationUtil.ADD_FAV_PLACE:
                    loadPlacesList(params[1]);
                    break;
                case NavigationUtil.FAV_PLACE:
                    loadFavPlacesList(params[1]);
                    break;
                default:
                    loadPlacesList(params[1]);
                    break;
            }
            return params[0];
        }

        private void loadFavPlacesList(String text)
        {/*
            try
            {
                ParseQuery<ParseObject> query = new ParseQuery<>(FavPlaces.TABLE);
                query.whereEqualTo(FavPlaces.USER_GOOGLE_ID, preferences.getString(Accounts.GOOGLE_ID, ""));
                if (text != null)
                {
                    query.whereMatches(FavPlaces.LOCALE, "(" + text + ")", "i");
                }

                MiscUtil.log("Try : Getting the list");
                List<ParseObject> mParseObjectList = query.find();
                MiscUtil.log("List Obtained, mIssues : " + mParseObjectList);


                if (mParseObjectList != null)
                {
                    for (ParseObject favPlacesParseObj : mParseObjectList)
                    {
                        FavPlaces places = new FavPlaces();
                        places.locale = ((String) favPlacesParseObj.get(FavPlaces.LOCALE));
                        places.country = ((String) favPlacesParseObj.get(FavPlaces.COUNTRY));
                        places.userGoogleId = ((String) favPlacesParseObj.get(FavPlaces.USER_GOOGLE_ID));
                        mAddressList.add(places);
                    }
                }
            }
            catch (ParseException e)
            {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }*/
        }

        @Override
        protected void onPostExecute(String category)
        {
            super.onPostExecute(category);
            searchResultView = (RecyclerView) mContext.findViewById(R.id.searchResultView);
            mProgressBar = mContext.findViewById(R.id.uploadProgressBar);
            if (category.equals(NavigationUtil.ADD_FAV_PLACE))
            {
                //searchResultView.setAdapter(new PlaceSearchAdapter(mContext, mAddressList, mFavPlacesList, false));
            }
            else if (category.equals(NavigationUtil.FAV_PLACE))
            {
                //searchResultView.setAdapter(new PlaceSearchAdapter(mContext, mAddressList, mFavPlacesList, true));
            }
            else
            {
                //searchResultView.setAdapter(new CommonUserListAdapter(mContext, mUserList, mFriendList, isFriendList));
            }
            searchResultView.setLayoutManager(new LinearLayoutManager(mContext));
            mProgressBar.setVisibility(View.GONE);
            mNoRecordsText = mContext.findViewById(R.id.noRecordsText);
            if (mUserList.size() > 0)
            {
                mNoRecordsText.setVisibility(View.GONE);
            }
            else
            {
                mNoRecordsText.setVisibility(View.VISIBLE);
            }
        }

        private void loadUserList(String text)
        {
            /*try
            {
                ParseQuery<ParseObject> friendsQuery = new ParseQuery<>(UserConnections.TABLE);
                friendsQuery.whereEqualTo(UserConnections.USER_GOOGLE_ID, preferences.getString(Accounts.GOOGLE_ID, ""));
                friendsQuery.selectKeys(Arrays.asList(UserConnections.FRIEND_GOOGLE_ID));
                List<ParseObject> friendList = friendsQuery.find();
                for (ParseObject singleObj : friendList)
                {
                    mFriendList.add(singleObj.getString(UserConnections.FRIEND_GOOGLE_ID));
                }

                ParseQuery<ParseObject> query = new ParseQuery<>(Accounts.TABLE);
                query.orderByDescending("createdAt");
                query.setLimit(20);
                if (text != null)
                {
                    query.whereMatches(Accounts.NAME, "(" + text + ")", "i");
                }
                MiscUtil.log("Try : Getting the list");
                List<ParseObject> mParseObjectList = query.find();
                MiscUtil.log("List Obtained, mIssues : " + mParseObjectList);
                if (mParseObjectList != null)
                {
                    for (ParseObject accountParseObj : mParseObjectList)
                    {
                        Accounts account = new Accounts();
                        account.name = ((String) accountParseObj.get(Accounts.NAME));
                        account.photo_url = ((String) accountParseObj.get(Accounts.PHOTO_URL));
                        account.googleId = ((String) accountParseObj.get(Accounts.GOOGLE_ID));
                        if (!preferences.getString(Accounts.GOOGLE_ID, "").equals(account.googleId))
                        {
                            mUserList.add(account);
                        }
                    }
                }
            }
            catch (ParseException e)
            {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }*/
        }

        private void loadPlacesList(String text)
        {/*
            try
            {
                ParseQuery<ParseObject> favPlacesQuery = new ParseQuery<>(FavPlaces.TABLE);
                favPlacesQuery.whereEqualTo(FavPlaces.USER_GOOGLE_ID, preferences.getString(Accounts.GOOGLE_ID, ""));
                favPlacesQuery.selectKeys(Arrays.asList(FavPlaces.LOCALE));

                List<ParseObject> favPlacesList = favPlacesQuery.find();
                for (ParseObject singleObj : favPlacesList)
                {
                    mFavPlacesList.add(singleObj.getString(FavPlaces.LOCALE));
                }

                ParseQuery<ParseObject> query = new ParseQuery<>(Accounts.TABLE);
                query.orderByDescending("createdAt");
                query.setLimit(20);
                if (text != null)
                {
                    query.whereMatches(Accounts.NAME, "(" + text + ")", "i");
                }
                MiscUtil.log("Try : Getting the list");
                List<ParseObject> mParseObjectList = query.find();
                MiscUtil.log("List Obtained, mIssues : " + mParseObjectList);
                if (mParseObjectList != null)
                {
                    for (ParseObject accountParseObj : mParseObjectList)
                    {
                        Accounts account = new Accounts();
                        account.name = ((String) accountParseObj.get(Accounts.NAME));
                        account.photo_url = ((String) accountParseObj.get(Accounts.PHOTO_URL));
                        account.googleId = ((String) accountParseObj.get(Accounts.GOOGLE_ID));
                        if (!preferences.getString(Accounts.GOOGLE_ID, "").equals(account.googleId))
                        {
                            mUserList.add(account);
                        }
                    }
                }
            }
            catch (ParseException e)
            {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            Geocoder geocoder = new Geocoder(mContext);
            try
            {
                List<Address> addressList = geocoder.getFromLocationName(text, 10);
                for (Address address : addressList)
                {
                    FavPlaces favPlaces = new FavPlaces();
                    favPlaces.country = address.getCountryName();
                    favPlaces.locale = address.getLocality();
                    favPlaces.featureName = address.getFeatureName();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }*/
        }

        private void loadFriendList(String text)
        {
           /* try
            {
                ParseQuery<ParseObject> query = new ParseQuery<>(UserConnections.TABLE);
                query.whereEqualTo(UserConnections.USER_GOOGLE_ID, preferences.getString(Accounts.GOOGLE_ID, ""));
                if (text != null)
                {
                    query.whereMatches(UserConnections.FRIEND_NAME, "(" + text + ")", "i");
                }

                MiscUtil.log("Try : Getting the list");
                List<ParseObject> mParseObjectList = query.find();
                MiscUtil.log("List Obtained, mIssues : " + mParseObjectList);


                if (mParseObjectList != null)
                {
                    for (ParseObject connectionsParseObj : mParseObjectList)
                    {
                        Accounts account = new Accounts();
                        account.name = ((String) connectionsParseObj.get(UserConnections.FRIEND_NAME));
                        account.photo_url = ((String) connectionsParseObj.get(UserConnections.FRIENDS_PHOTO));
                        account.googleId = ((String) connectionsParseObj.get(UserConnections.FRIEND_GOOGLE_ID));
                        mUserList.add(account);
                    }
                }
            }
            catch (ParseException e)
            {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }*/
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (mSearchTask != null && mSearchTask.getStatus().equals(AsyncTask.Status.RUNNING))
        {
            mSearchTask.cancel(true);
        }
    }

    @Override
    protected void onDestroy()
    {
        overridePendingTransition(R.anim.move_to_right, R.anim.move_to_right);
        super.onDestroy();
    }
}
