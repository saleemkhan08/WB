package in.org.whistleblower.fragments;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.adapters.CommonUserListAdapter;
import in.org.whistleblower.dao.AccountsDao;
import in.org.whistleblower.interfaces.ResultListener;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.TransitionUtil;
import in.org.whistleblower.utilities.VolleyUtil;

public class UserListFragment extends DialogFragment
{
    public static final String LIST_EMPTY_TEXT = "LIST_EMPTY_TEXT";
    private static final String USER_LIST_FETCHED_FROM_SERVER = "USER_LIST_FETCHED_FROM_SERVER";
    public static final String ADD_FRIEND_DIALOG_CLOSED = "ADD_FRIEND_DIALOG_CLOSED";

    SearchTask mSearchTask;
    @BindString(R.string.noUsersFound)
    String noUsersFound;

    @BindString(R.string.loading)
    String loading;

    @Bind(R.id.emptyList)
    ViewGroup emptyList;

    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.userSearch)
    EditText userSearch;

    @Bind(R.id.emptyListTextView)
    TextView emptyListTextView;
    private AppCompatActivity mActivity;

    String currentUserMail;
    SharedPreferences preferences;
    CommonUserListAdapter mAdapter;
    public UserListFragment()
    {
    }

    @Bind(R.id.userList)
    RecyclerView userListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_add_friend, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);

        TextView dialogTitle = (TextView) parentView.findViewById(R.id.dialogTitle);
        dialogTitle.setTypeface(WhistleBlower.getTypeface());

        mActivity = (AppCompatActivity) getActivity();
        mSearchTask = new SearchTask();
        mSearchTask.execute("");
        preferences = WhistleBlower.getPreferences();
        currentUserMail = preferences.getString(Accounts.EMAIL, "saleemkhan08@gmail.com");
        swipeRefreshLayout.setEnabled(false);
        userSearch.addTextChangedListener(new TextWatcher()
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
                mSearchTask = new SearchTask();
                mSearchTask.execute(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return parentView;
    }

    @Subscribe
    public void showEmptyListString(String msg)
    {
        switch (msg)
        {
            case LIST_EMPTY_TEXT:
                showEmptyListString();
                emptyListTextView.setText(noUsersFound);
                break;
        }
    }

    private void showEmptyListString()
    {
        TransitionUtil.defaultTransition(emptyList);
        emptyList.setVisibility(View.VISIBLE);
    }

    private void hideEmptyListString()
    {
        TransitionUtil.defaultTransition(emptyList);
        emptyList.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        Otto.unregister(this);
    }

    @OnClick(R.id.closeDialog)
    public void close()
    {
        dismiss();
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

    private class SearchTask extends AsyncTask<String, Void, Void> implements ResultListener<String>
    {
        ArrayList<Accounts> userList;
        volatile boolean isRunning;

        @Override
        protected void onPreExecute()
        {
            hideEmptyListString();
            showProgressFab();
        }

        @Override
        protected Void doInBackground(String... params)
        {
            Map<String, String> parameters = new HashMap<>();
            parameters.put(VolleyUtil.KEY_ACTION, "getUsers");
            parameters.put(VolleyUtil.KEY_PARTIAL_STR, params[0]);
            parameters.put(VolleyUtil.KEY_OFFSET, "0");
            parameters.put(VolleyUtil.KEY_LIMIT, "50");
            parameters.put(Accounts.CATEGORY, Accounts.USER);
            parameters.put(Accounts.EMAIL, WhistleBlower.getPreferences().getString(Accounts.EMAIL, "saleemkhan08@gmail.com"));
            VolleyUtil.sendPostData(parameters, this);
            isRunning = true;
            while (isRunning)
            {
                ;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            hideProgressFab();
            if (userList != null && userList.size() > 0)
            {
                mAdapter = new CommonUserListAdapter(mActivity, userList);
                userListView.setAdapter(mAdapter);
                userListView.setLayoutManager(new LinearLayoutManager(mActivity));
            }
            else
            {
                showEmptyListString(LIST_EMPTY_TEXT);
            }
        }

        @Override
        public void onSuccess(String result)
        {
            Log.d("Doodle", "result : " + result);
            try
            {
                JSONArray array = new JSONArray(result);
                int totalNoOfAccounts = array.length();
                userList = new ArrayList<>();
                for (int accIndex = 0; accIndex < totalNoOfAccounts; accIndex++)
                {
                    Accounts account = new Accounts();
                    JSONObject json = (JSONObject) array.get(accIndex);
                    account.email = json.getString(Accounts.EMAIL);
                    account.photo_url = json.getString(Accounts.PHOTO_URL);
                    account.name = json.getString(Accounts.NAME);
                    account.relation = json.getString(Accounts.RELATION);
                    userList.add(account);
                }
                isRunning = false;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.d("ToastMsg", "Error : " + e.getMessage());
                isRunning = false;
            }
        }

        @Override
        public void onError(VolleyError error)
        {
            Log.d("ToastMsg", "error : " + error.getMessage());
            isRunning = false;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        Otto.post(ADD_FRIEND_DIALOG_CLOSED);
    }

    @Subscribe
    public void addFriend(final Accounts account)
    {
        showProgressFab();
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
                account.relation = Accounts.FRIEND;
                AccountsDao.insert(account);
                WhistleBlower.toast("Added : " + account.name);
                mAdapter.remove(account);
                hideProgressFab();
            }

            @Override
            public void onError(VolleyError error)
            {
                hideProgressFab();
                WhistleBlower.toast("Please Try again!");
                Log.d("ToastMsg", "Error : " + error.getMessage());
            }
        });
    }

    @Override
    public void onPause()
    {
        super.onPause();
        dismiss();
    }
}