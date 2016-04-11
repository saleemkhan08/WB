package in.org.whistleblower;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
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
import java.util.List;
import java.util.Map;

import in.org.whistleblower.adapters.FriendListAdapter;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.AccountsDao;
import in.org.whistleblower.storage.ResultListener;
import in.org.whistleblower.storage.VolleyUtil;

public class FriendListActivity extends AppCompatActivity
{
    private static final String KEY_USERS_FETCHED = "KEY_USERS_FETCHED";
    private static final String IS_FRIEND_LIST = "IS_FRIEND_LIST";
    private static final String IS_SEARCH_ENABLE = "IS_SEARCH_ENABLE";
    private static GetUserListTask mTask;
    SharedPreferences preferences;
    static Context context;
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        if(mFriendList!=null)
        {
            for (Accounts account : mFriendList)
            {
                if(account!=null)
                {
                    if (!account.relation.equals(Accounts.FRIEND))
                    {
                        mFriendList.remove(account);
                    }
                }
            }
        }
        if(mUserList!=null)
        {
            for (Accounts account : mUserList)
            {
                if(account!=null)
                {
                    if (account.relation.equals(Accounts.FRIEND))
                    {
                        mUserList.remove(account);
                    }
                }
            }
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        progress = findViewById(R.id.progressFab);
        isFriendList = true;
        Log.d("Doodle", "On create");
        mFriendAndUserRecyclerView = (RecyclerView) findViewById(R.id.friendAndUserList);
        context = this;
        fabWrapper = findViewById(R.id.fab_wrapper);
        if (!preferences.getBoolean(KEY_USERS_FETCHED, false))
        {
            getUserListFromServer();
        }
        else
        {
            showUserListFromDatabase();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        searchToolBar = findViewById(R.id.searchToolBar);
        searchUser = (EditText) findViewById(R.id.searchUser);
        searchUser.addTextChangedListener(new TextWatcher()
        {
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
        });
        setSupportActionBar(toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null)
        {
            fab.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    isFriendList = false;
                    fabWrapper.setVisibility(View.GONE);
                    changeList(mUserList, "");
                }
            });
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
            AccountsDao dao = new AccountsDao(context);
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
            mAdapter = new FriendListAdapter(context, mShowList);
            mFriendAndUserRecyclerView.setLayoutManager(new LinearLayoutManager(context));
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
}
