package in.org.whistleblower.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.org.whistleblower.MainActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.adapters.IssueAdapter;
import in.org.whistleblower.interfaces.ResultListener;
import in.org.whistleblower.models.Issue;
import in.org.whistleblower.models.IssuesDao;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.NavigationUtil;
import in.org.whistleblower.utilities.VolleyUtil;

public class MainFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{
    private static final String GET_ISSUES = "getIssues";
    private static final String LIMIT = "limit";
    private static final String OFFSET = "offset";
    RecyclerView issuesRecyclerView;
    MiscUtil mUtil;
    AppCompatActivity mActivity;
    IssueAdapter adapter;
    private ArrayList<Issue> issuesList = null;
    LocalDataTask mLocalDataTask;
    private SwipeRefreshLayout swipeRefreshLayout;
    public MainFragment()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        // Create the array
        issuesList = new ArrayList<>();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_main, container, false);
        mActivity = (AppCompatActivity) getActivity();
        WhistleBlower.getComponent().inject(this);

        mUtil = new MiscUtil(mActivity);
        swipeRefreshLayout = (SwipeRefreshLayout) parentView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        MiscUtil.log("onCreateView");
        return parentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart()
    {
        mLocalDataTask = new LocalDataTask();
        mLocalDataTask.execute();
        super.onStart();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        MiscUtil.log("onResume");
        NavigationUtil.highlightNavigationDrawerMenu(mActivity, R.id.nav_news);
        mActivity.setTitle(MainActivity.NEWS_FEEDS);
    }

    //TODO Make issueList static
    //TODO On Scroll down at the bottom of the list, load old data to same list
    //TODO On swipe down at the top of the list, refresh the same list but don't delete the old records
    // data base should only have latest 30 records
    // where as list can have as many records as user scrolls

    private class LocalDataTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            swipeRefreshLayout.post(new Runnable()
            {
                @Override
                public void run()
                {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            MiscUtil.log("LocalDataTask : doInBackground");
            issuesList = new IssuesDao().getIssuesList();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            MiscUtil.log("LocalDataTask : onPostExecute - > " + issuesList);
            if (null != issuesList && issuesList.size() <= 0)
            {
                onRefresh();
            }
            else
            {
                issuesRecyclerView = (RecyclerView) mActivity.findViewById(R.id.issuesList);
                adapter = new IssueAdapter(mActivity, issuesList);
                issuesRecyclerView.setAdapter(adapter);
                issuesRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    @Override
    public void onRefresh()
    {
        MiscUtil.log("RemoteDataTask : doInBackground");
        Map<String, String> getParams = new HashMap<>();
        getParams.put(ResultListener.ACTION, GET_ISSUES);
        getParams.put(LIMIT, "10");
        getParams.put(OFFSET, "0");

        VolleyUtil.sendGetData(getParams, new ResultListener<String>()
        {
            @Override
            public void onSuccess(String result)
            {
                try
                {
                    JSONArray array = new JSONArray(result);

                    if (array != null)
                    {
                        IssuesDao issueDao = new IssuesDao();
                        issueDao.delete();//TODO Find a better Logic to do this
                        issuesList = new ArrayList<>();
                        int totalNoOfIssues = array.length();
                        for (int issueIndex = 0; issueIndex < totalNoOfIssues; issueIndex++)
                        {
                            Issue issue = new Issue();
                            JSONObject json = (JSONObject) array.get(issueIndex);
                            issue.issueId = json.getString(IssuesDao.ISSUE_ID);
                            issue.imgUrl = VolleyUtil.IMAGE_URL + issue.issueId + ".png";
                            issue.userDpUrl = json.getString(IssuesDao.USER_DP_URL);
                            issue.userId = json.getString(IssuesDao.USER_ID);
                            issue.username = json.getString(IssuesDao.USERNAME);
                            issue.description = json.getString(IssuesDao.DESCRIPTION);
                            issue.areaType = json.getString(IssuesDao.AREA_TYPE);
                            issue.radius = json.getInt(IssuesDao.RADIUS);
                            issue.latitude = json.getDouble(IssuesDao.LATITUDE) + "";
                            issue.longitude = json.getDouble(IssuesDao.LONGITUDE) + "";
                            issuesList.add(issue);
                            issueDao.insert(issue);
                        }
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

                //TODO find how to check if UI is available and the update it
                issuesRecyclerView = (RecyclerView) mActivity.findViewById(R.id.issuesList);
                adapter = new IssueAdapter(mActivity, issuesList);
                issuesRecyclerView.setAdapter(adapter);
                issuesRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(VolleyError error)
            {
                Toast.makeText(mActivity, "Error : " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (mLocalDataTask != null && mLocalDataTask.getStatus().equals(AsyncTask.Status.RUNNING))
        {
            mLocalDataTask.cancel(true);
        }
    }
}
