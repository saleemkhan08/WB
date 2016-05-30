package in.org.whistleblower.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.org.whistleblower.MainActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.adapters.FriendListAdapter;
import in.org.whistleblower.interfaces.ResultListener;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.AccountsDao;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.VolleyUtil;

public class FriendListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, ResultListener<String>
{
    public static final String FRIEND_LIST_FETCH_ERROR = "FRIEND_LIST_FETCH_ERROR";
    public static final String FRIEND_LIST_FETCHED_FROM_SERVER = "FRIEND_LIST_FETCHED_FROM_SERVER";
    public static final String EMPTY_FRIEND_LIST = "EMPTY_FRIEND_LIST";
    private static final String ADD_USER_FRAGMENT_TAG = "ADD_USER_FRAGMENT_TAG";
    private SharedPreferences preferences;
    AccountsDao dao;

    public FriendListFragment()
    {
    }

    @Bind(R.id.friendsList)
    RecyclerView friendsListRecyclerView;

    @Bind(R.id.addFriendFab)
    View addFriendFab;

    @Bind(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindString(R.string.allTheFriendsAreRemoved)
    String youHaveRemovedAllFriends;

    List<Accounts> mFriendList;
    AppCompatActivity mActivity;

    FriendListAdapter adapter;
    @Bind(R.id.fab_wrapper)
    ViewGroup fabWrapper;

    @Bind(R.id.emptyList)
    ViewGroup emptyList;

    @Bind(R.id.emptyListTextView)
    TextView emptyListTextView;
    public static final String KEY_USERS_FETCHED = "KEY_USERS_FETCHED";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_friend_list, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);
        preferences = WhistleBlower.getPreferences();

        mActivity = (AppCompatActivity) getActivity();
        swipeRefreshLayout.setOnRefreshListener(this);
        mActivity.setTitle(MainActivity.FRIEND_LIST);
        Log.d("Doodle", "KEY_USERS_FETCHED : " + preferences.getBoolean(KEY_USERS_FETCHED, false));
        dao = new AccountsDao();
        if (!preferences.getBoolean(KEY_USERS_FETCHED, false))
        {
            getFriendListFromServer(this);
        }
        else
        {
            showFriendList();
        }

        friendsListRecyclerView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    TransitionManager.beginDelayedTransition(fabWrapper, new Slide());
                    fabWrapper.setVisibility(View.GONE);
                }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    Handler myHandler = new Handler();
                    myHandler.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            TransitionManager.beginDelayedTransition(fabWrapper, new Slide());
                            fabWrapper.setVisibility(View.VISIBLE);
                        }
                    }, 1000);
                }
                else if (event.getAction() == MotionEvent.ACTION_CANCEL)
                {
                    Handler myHandler = new Handler();
                    myHandler.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            TransitionManager.beginDelayedTransition(fabWrapper, new Slide());
                            fabWrapper.setVisibility(View.VISIBLE);
                        }
                    }, 500);
                }
                return false;
            }
        });

        return parentView;
    }

    public void hideProgressFab()
    {
        swipeRefreshLayout.setRefreshing(false);
        Log.d("Doodle", "hideProgressFab");
    }

    public void showProgressFab()
    {
        swipeRefreshLayout.post(new Runnable()
        {
            @Override
            public void run()
            {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        Log.d("Doodle", "showProgressFab");
    }

    public static void getFriendListFromServer(final ResultListener<String> listener)
    {
        Log.d("Doodle", "Server Call");
        Map<String, String> parameters = new HashMap<>();
        //TODO get only friends
        parameters.put(VolleyUtil.KEY_ACTION, "getUsers");
        parameters.put(VolleyUtil.KEY_PARTIAL_STR, "");
        parameters.put(VolleyUtil.KEY_OFFSET, "0");
        parameters.put(VolleyUtil.KEY_LIMIT, "50");
        parameters.put(Accounts.CATEGORY, Accounts.FRIEND);

        parameters.put(Accounts.EMAIL, WhistleBlower.getPreferences().getString(Accounts.EMAIL, "saleemkhan08@gmail.com"));

        VolleyUtil.sendPostData(parameters, new ResultListener<String>()
        {
            @Override
            public void onSuccess(String result)
            {
                Log.d("Doodle", "result : " + result);
                saveFriendListInDataBase(result, listener);
            }

            @Override
            public void onError(VolleyError error)
            {
                Log.d("ToastMsg", "error : " + error.getMessage());
                if(listener != null)
                {
                    listener.onError(error);
                }
            }
        });
    }

    private static void saveFriendListInDataBase(String result, final ResultListener<String> listener)
    {
        Log.d("Doodle", "Data base saving");
        AccountsDao accountsDao = new AccountsDao();
        try
        {
            JSONArray array = new JSONArray(result);
            int totalNoOfAccounts = array.length();
            for (int accIndex = 0; accIndex < totalNoOfAccounts; accIndex++)
            {
                Accounts account = new Accounts();
                JSONObject json = (JSONObject) array.get(accIndex);
                account.email = json.getString(Accounts.EMAIL);
                account.photo_url = json.getString(Accounts.PHOTO_URL);
                account.name = json.getString(Accounts.NAME);
                account.relation = json.getString(Accounts.RELATION);
                accountsDao.insert(account);
            }
            if(listener!=null)
            {
                listener.onSuccess(FRIEND_LIST_FETCHED_FROM_SERVER);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            accountsDao.delete();
            Log.d("ToastMsg", "Error occurred deleting from Data base : " + e.getMessage());
            if(listener!=null)
            {
                listener.onError(new VolleyError(e.getMessage()));
            }
        }
    }

    void showFriendList()
    {
        hideEmptyListString();
        mFriendList = dao.getFriendsList();
        Log.d("mFriendList", "mFriendList : " + mFriendList);

        if (null != mFriendList && mFriendList.size() > 0)
        {
            adapter = new FriendListAdapter(mActivity, mFriendList);
            friendsListRecyclerView.setAdapter(adapter);
            friendsListRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        }
        else
        {
            showEmptyListString();
        }
        hideProgressFab();
    }

    @Override
    public void onRefresh()
    {
        dao.delete();
        getFriendListFromServer(this);
    }

    @Override
    public void onSuccess(String result)
    {
        Log.d("Doodle", "getFriendListFromServer : " + result);
        showFriendList();
        preferences.edit().putBoolean(KEY_USERS_FETCHED, true).apply();
    }

    @Override
    public void onError(VolleyError error)
    {
        WhistleBlower.toast("Please Try Again!");
        Log.d("FriendListFragment", "Error : " + error.getMessage());
        preferences.edit().putBoolean(KEY_USERS_FETCHED, false).apply();
        hideProgressFab();
    }

    @Subscribe
    public void showEmptyListString(String msg)
    {
        if (msg.equals(EMPTY_FRIEND_LIST))
        {
            showEmptyListString();
            emptyListTextView.setText(youHaveRemovedAllFriends);
        }
    }

    private void showEmptyListString()
    {
        TransitionManager.beginDelayedTransition(emptyList);
        emptyList.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setEnabled(false);
    }

    private void hideEmptyListString()
    {
        TransitionManager.beginDelayedTransition(emptyList);
        emptyList.setVisibility(View.GONE);
        swipeRefreshLayout.setEnabled(true);
    }

    @OnClick(R.id.addFriendFab)
    public void addFriend(View addFriendFab)
    {
        addFriendFab.setVisibility(View.GONE);
        showAddUserFragment();
    }

    private void showAddUserFragment()
    {
        FragmentManager manager = mActivity.getSupportFragmentManager();
        UserListFragment userListFragment = (UserListFragment)
                manager.findFragmentByTag(ADD_USER_FRAGMENT_TAG);
        if (userListFragment == null)
        {
            userListFragment = new UserListFragment();
        }
        manager.executePendingTransactions();
        if (!userListFragment.isAdded())
        {
            userListFragment.show(manager, ADD_USER_FRAGMENT_TAG);
        }

    }

    @Subscribe
    public void showAddFriendFab(String action)
    {
        if (action.equals(UserListFragment.ADD_FRIEND_DIALOG_CLOSED))
        {
            addFriendFab.setVisibility(View.VISIBLE);
            onRefresh();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Otto.unregister(this);
    }
}
