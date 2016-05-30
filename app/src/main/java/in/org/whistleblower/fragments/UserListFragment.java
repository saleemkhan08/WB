package in.org.whistleblower.fragments;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import in.org.whistleblower.interfaces.ResultListener;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.VolleyUtil;

public class UserListFragment extends DialogFragment
{
    public static final String NOTIFY_LOC_LIST_EMPTY_TEXT = "NOTIFY_LOC_LIST_EMPTY_TEXT";
    private static final String USER_LIST_FETCHED_FROM_SERVER = "USER_LIST_FETCHED_FROM_SERVER";
    public static final String ADD_FRIEND_DIALOG_CLOSED = "ADD_FRIEND_DIALOG_CLOSED";

    ArrayList<Accounts> userList;

    @BindString(R.string.noUsersFound)
    String noUsersFound;

    @BindString(R.string.loading)
    String loading;

    @Bind(R.id.emptyList)
    ViewGroup emptyList;

    @Bind(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.emptyListTextView)
    TextView emptyListTextView;
    private AppCompatActivity mActivity;

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
        showUserList();
        mActivity = (AppCompatActivity) getActivity();
        return parentView;
    }

    private void showUserList()
    {

    }

    @Subscribe
    public void showEmptyListString(String msg)
    {
        switch (msg)
        {
            case NOTIFY_LOC_LIST_EMPTY_TEXT:
                showEmptyListString();
                emptyListTextView.setText(noUsersFound);
                break;
        }
    }

    private void showEmptyListString()
    {
        TransitionManager.beginDelayedTransition(emptyList);
        emptyList.setVisibility(View.VISIBLE);
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

    public void getUserListFromServer(String str)
    {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(VolleyUtil.KEY_ACTION, "getUsers");
        parameters.put(VolleyUtil.KEY_PARTIAL_STR, str);
        parameters.put(VolleyUtil.KEY_OFFSET, "0");
        parameters.put(VolleyUtil.KEY_LIMIT, "50");
        parameters.put(Accounts.CATEGORY, Accounts.USER);
        parameters.put(Accounts.EMAIL, WhistleBlower.getPreferences().getString(Accounts.EMAIL, "saleemkhan08@gmail.com"));
        VolleyUtil.sendPostData(parameters, new ResultListener<String>()
        {
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
                    userListView.setAdapter(new CommonUserListAdapter(mActivity, userList));
                    userListView.setLayoutManager(new LinearLayoutManager(mActivity));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.d("ToastMsg", "Error : " + e.getMessage());
                    showEmptyListString(NOTIFY_LOC_LIST_EMPTY_TEXT);
                }
            }

            @Override
            public void onError(VolleyError error)
            {
                Log.d("ToastMsg", "error : " + error.getMessage());
                showEmptyListString(NOTIFY_LOC_LIST_EMPTY_TEXT);
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        Otto.post(ADD_FRIEND_DIALOG_CLOSED);
    }
}
