package in.org.whistleblower;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import in.org.whistleblower.adapters.FriendListAdapter;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.AccountsDao;
import in.org.whistleblower.interfaces.ResultListener;
import in.org.whistleblower.utilities.VolleyUtil;

public class FriendListActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener
{
    private static final String KEY_USERS_FETCHED = "KEY_USERS_FETCHED";
    private static final String IS_FRIEND_LIST = "IS_FRIEND_LIST";
    private static final String IS_SEARCH_ENABLE = "IS_SEARCH_ENABLE";
    private static GetUserListTask mTask;
    SharedPreferences preferences;
    static FriendListActivity mStaticContext;
    public static ArrayList<Accounts> mUserList;
    public static ArrayList<Accounts> mFriendList;
    private static ArrayList<Accounts> mShowList;
    Toolbar toolbar;
    View searchToolBar;
    EditText searchUser;
    static View progress;
    static RecyclerView mFriendAndUserRecyclerView;
    private boolean isSearchEnabled;
    private static FriendListAdapter mAdapter;
    private boolean isFriendList;
    FloatingActionButton fab;
    private View fabWrapper;
    String currentUserMail;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        progress = findViewById(R.id.progressFab);
        mFriendAndUserRecyclerView = (RecyclerView) findViewById(R.id.friendAndUserList);
        mStaticContext = this;
        currentUserMail = preferences.getString(Accounts.EMAIL, "saleemkhan08@gmail.com");
        fabWrapper = findViewById(R.id.fab_wrapper);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        searchToolBar = findViewById(R.id.searchToolBar);
        searchUser = (EditText) findViewById(R.id.searchUser);
        if (searchUser != null)
        {
            searchUser.addTextChangedListener(this);
        }
        setSupportActionBar(toolbar);
        fab = (FloatingActionButton) findViewById(R.id.addFriendFab);
        if (fab != null)
        {
            fab.setOnClickListener(this);
        }
        if (savedInstanceState != null)
        {
            isFriendList = savedInstanceState.getBoolean(IS_FRIEND_LIST, true);
            isSearchEnabled = savedInstanceState.getBoolean(IS_SEARCH_ENABLE, false);
        }
        else
        {
            isFriendList = true;
            isSearchEnabled = false;
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (mFriendList != null)
        {
            for (Iterator<Accounts> iterator = mFriendList.iterator(); iterator.hasNext(); )
            {
                Accounts account = iterator.next();
                if (account != null)
                {
                    if (!account.relation.equals(Accounts.FRIEND))
                    {
                        iterator.remove();
                    }
                }
            }

        }
        if (mUserList != null)
        {
            for (Iterator<Accounts> iterator = mUserList.iterator(); iterator.hasNext(); )
            {
                Accounts account = iterator.next();
                if (account != null)
                {
                    if (account.relation.equals(Accounts.FRIEND))
                    {
                        iterator.remove();
                    }
                }
            }
        }

        if (!preferences.getBoolean(KEY_USERS_FETCHED, false))
        {
            getUserListFromServer();
        }
        else
        {
            showUserListFromDatabase();
        }
    }

    private void changeList(List<Accounts> list, String query)
    {
        mAdapter.animateTo(filter(list, query));
        mFriendAndUserRecyclerView.scrollToPosition(0);
    }

    public static void showUserListFromDatabase()
    {
        Log.d("Doodle", "UI update call");
        mTask = new GetUserListTask();
        mTask.execute();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.addFriendFab:
                isFriendList = false;
                fabWrapper.setVisibility(View.GONE);
                changeList(mUserList, "");
                break;
        }
    }

    private static class GetUserListTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            Log.d("Doodle", "Pre Exe");
            showProgressFab();
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            Log.d("Doodle", "Do in backgrnd");
            AccountsDao dao = new AccountsDao(mStaticContext);
            if (mFriendList == null || mFriendList.size() <= 0)
            {
                mFriendList = dao.getFriendsList();
            }
            mShowList = new ArrayList<>();
            mShowList.addAll(mFriendList);

            if (mShowList.size() < 20)
            {
                mShowList.add(null);
                if (mUserList == null || mUserList.size() <= 0)
                {
                    mUserList = dao.getUsersList();
                }
                mShowList.addAll(mUserList);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            Log.d("Doodle", "Post exe");
            mAdapter = new FriendListAdapter(mStaticContext, mShowList);
            mFriendAndUserRecyclerView.setLayoutManager(new LinearLayoutManager(mStaticContext));
            mFriendAndUserRecyclerView.setAdapter(mAdapter);
            hideProgressFab();
        }
    }

    public static void showProgressFab()
    {
        progress.setVisibility(View.VISIBLE);
    }

    public static void hideProgressFab()
    {
        progress.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.friend, menu);
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.friendSearch)
        {
            isSearchEnabled = true;
            showSearchBar();
            searchUser.setText("");
            findViewById(R.id.cancelSearch).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    isSearchEnabled = false;
                    showToolBar();
                    showUserListFromDatabase();
                }
            });

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void showSearchBar()
    {
        Slide slide = new Slide(Gravity.LEFT);
        TransitionManager.beginDelayedTransition(toolbar, slide);
        searchToolBar.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.GONE);
        searchUser.requestFocus();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(searchUser, InputMethodManager.SHOW_IMPLICIT);
    }

    void showToolBar()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchUser.getWindowToken(), 0);
        Slide slide = new Slide(Gravity.LEFT);
        TransitionManager.beginDelayedTransition(toolbar, slide);
        searchToolBar.setVisibility(View.GONE);
        toolbar.setVisibility(View.VISIBLE);
    }

    public void getUserListFromServer()
    {
        Log.d("Doodle", "Server Call");
        showProgressFab();
        Map<String, String> parameters = new HashMap<>();
        parameters.put(VolleyUtil.KEY_ACTION, "getUsers");
        parameters.put(VolleyUtil.KEY_PARTIAL_STR, "");
        parameters.put(VolleyUtil.KEY_OFFSET, "0");
        parameters.put(VolleyUtil.KEY_LIMIT, "50");
        parameters.put(Accounts.CATEGORY, Accounts.BOTH);

        parameters.put(Accounts.EMAIL, preferences.getString(Accounts.EMAIL, "saleemkhan08@gmail.com"));

        VolleyUtil.sendPostData(parameters, new ResultListener<String>()
        {
            @Override
            public void onSuccess(String result)
            {
                Log.d("Doodle", "result : " + result);
                saveUserListInDataBase(result);
                showUserListFromDatabase();
                hideProgressFab();
            }

            @Override
            public void onError(VolleyError error)
            {
                Toast.makeText(FriendListActivity.this, "error : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("ToastMsg", "error : " + error.getMessage());
                hideProgressFab();
            }
        });
    }

    private void saveUserListInDataBase(String result)
    {
        Log.d("Doodle", "Data base saving");
        AccountsDao accountsDao = new AccountsDao(this);
        try
        {
            JSONArray array = new JSONArray(result);
            mUserList = new ArrayList<>();
            mFriendList = new ArrayList<>();
            int totalNoOfAccounts = array.length();
            for (int accIndex = 0; accIndex < totalNoOfAccounts; accIndex++)
            {
                Accounts account = new Accounts();
                JSONObject json = (JSONObject) array.get(accIndex);
                account.email = json.getString(Accounts.EMAIL);
                account.photo_url = json.getString(Accounts.PHOTO_URL);
                account.name = json.getString(Accounts.NAME);
                account.relation = json.getString(Accounts.RELATION);
                if (account.relation.equals(Accounts.FRIEND))
                {
                    mFriendList.add(account);
                }
                else
                {
                    mUserList.add(account);
                }
                accountsDao.insert(account);
            }
            preferences.edit().putBoolean(KEY_USERS_FETCHED, true).commit();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            accountsDao.delete();
            preferences.edit().putBoolean(KEY_USERS_FETCHED, false).commit();
            Toast.makeText(this, "Error occurred deleting from Data base", Toast.LENGTH_SHORT).show();
            Log.d("ToastMsg", "Error occurred deleting from Data base : " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed()
    {
        Log.d("onBackPressed", "isSearchEnabled : " + isSearchEnabled + ", isFriendList : " + isFriendList);
        if (isSearchEnabled)
        {
            showToolBar();
            isSearchEnabled = false;
            showUserListFromDatabase();
        }
        else if (isFriendList)
        {
            super.onBackPressed();
        }
        if (!isFriendList)
        {
            isFriendList = true;
            fabWrapper.setVisibility(View.VISIBLE);
            showUserListFromDatabase();
        }
    }

    private List<Accounts> filter(List<Accounts> accountsList, String query)
    {
        query = query.toLowerCase();
        final List<Accounts> filteredModelList = new ArrayList<>();
        for (Accounts account : accountsList)
        {
            if (account != null)
            {
                final String text = account.name.toLowerCase();
                if (text.contains(query) || query.trim().isEmpty())
                {
                    filteredModelList.add(account);
                }
            }
        }
        return filteredModelList;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState)
    {
        outState.putBoolean(IS_FRIEND_LIST, isFriendList);
        outState.putBoolean(IS_SEARCH_ENABLE, isSearchEnabled);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (mTask != null && mTask.getStatus().equals(AsyncTask.Status.RUNNING))
        {
            mTask.cancel(true);
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {

    }

    @Override
    public void onTextChanged(CharSequence query, int start, int before, int count)
    {
        if (isFriendList)
        {
            changeList(mFriendList, query.toString());
        }
        else
        {
            changeList(mUserList, query.toString());
        }
    }

    @Override
    public void afterTextChanged(Editable s)
    {

    }

    public void removeFriend(final Accounts account, final int position)
    {
        FriendListActivity.showProgressFab();
        Map<String, String> data = new HashMap<>();
        data.put(VolleyUtil.KEY_ACTION, "removeFriend");
        data.put(Accounts.USER_EMAIL, currentUserMail);
        data.put(Accounts.FRIENDS_EMAIL, account.email);
        VolleyUtil.sendPostData(data, new ResultListener<String>()
        {
            @Override
            public void onSuccess(String result)
            {
                Log.d("removeFriend", "removeFriend result : " + result);
                if (result.equals("1"))
                {
                    new AccountsDao(FriendListActivity.this).update(account.email, Accounts.RELATION, Accounts.NOT_A_FRIEND);
                    account.relation = Accounts.NOT_A_FRIEND;
                    mAdapter.removeUser(position, account);
                    FriendListActivity.hideProgressFab();
                }
                else
                {
                    Toast.makeText(FriendListActivity.this, "Please Try Again!", Toast.LENGTH_SHORT).show();
                    Log.d("ToastMsg", result + " : Please Try again!");
                }
            }

            @Override
            public void onError(VolleyError error)
            {
                FriendListActivity.hideProgressFab();
                Toast.makeText(FriendListActivity.this, error.getMessage() + "\nPlease Try again!", Toast.LENGTH_SHORT).show();
                Log.d("ToastMsg", error.getMessage() + "\nPlease Try again!");
            }
        });
    }

    public void addFriend(final Accounts account, final int position)
    {
        FriendListActivity.showProgressFab();
        Map<String, String> data = new HashMap<>();
        data.put(Accounts.FRIENDS_PHOTO, account.photo_url);
        data.put(Accounts.FRIENDS_NAME, account.name);
        data.put(Accounts.FRIENDS_EMAIL, account.email);
        data.put(Accounts.USER_EMAIL, currentUserMail);
        data.put(VolleyUtil.KEY_ACTION, "addFriend");
        VolleyUtil.sendPostData(data, new ResultListener<String>()
        {
            @Override
            public void onSuccess(String result)
            {
                Log.d("addFriend", "Result : " + result);
                new AccountsDao(FriendListActivity.this).update(account.email, Accounts.RELATION, Accounts.FRIEND);
                account.relation = Accounts.FRIEND;
                mAdapter.addUser(position, account);
                FriendListActivity.hideProgressFab();
            }

            @Override
            public void onError(VolleyError error)
            {
                FriendListActivity.hideProgressFab();
                Toast.makeText(FriendListActivity.this, error.getMessage() + "\nPlease Try again!", Toast.LENGTH_SHORT).show();
                Log.d("ToastMsg", error.getMessage() + "\nPlease Try again!");
            }
        });

    }


}
