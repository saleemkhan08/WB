package in.org.whistleblower.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import in.org.whistleblower.R;
import in.org.whistleblower.adapters.IssueAdapter;
import in.org.whistleblower.models.Issue;
import in.org.whistleblower.models.IssuesDao;
import in.org.whistleblower.utilities.FABUtil;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.NavigationUtil;

public class MainFragment extends Fragment implements View.OnTouchListener, SwipeRefreshLayout.OnRefreshListener
{
    RecyclerView issuesRecyclerView;
    List<ParseObject> mParseObjectList;
    MiscUtil mUtil;
    AppCompatActivity mActivity;
    IssueAdapter adapter;
    FloatingActionButton addButton;
    private ArrayList<Issue> issuesList = null;
    RemoteDataTask mDataTask;
    LocalDataTask mLocalDataTask;
    private SwipeRefreshLayout swipeRefreshLayout;

    public MainFragment()
    {

    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (mDataTask != null && mDataTask.getStatus().equals(AsyncTask.Status.RUNNING))
        {
            mDataTask.cancel(true);
        }
        if (mLocalDataTask != null && mLocalDataTask.getStatus().equals(AsyncTask.Status.RUNNING))
        {
            mLocalDataTask.cancel(true);
        }
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
        mActivity = (AppCompatActivity)getActivity();
        mUtil = new MiscUtil(mActivity);
        swipeRefreshLayout = (SwipeRefreshLayout) parentView.findViewById(R.id.swipe_refresh_layout);
        MiscUtil.log("onCreateView");
        return parentView;
    }

    @Override
    public void onStart()
    {
        mLocalDataTask = new LocalDataTask();
        mLocalDataTask.execute();
        super.onStart();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        MiscUtil.log("onResume");
        FABUtil.locationSelector.setVisibility(View.GONE);
        NavigationUtil.highlightMenu(mActivity, R.id.nav_news);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_UP)
        {

        }
        return true;
    }

    //TODO Make issueList static
    //TODO On Scroll down at the bottom of the list, load old data to same list
    //TODO On swipe down at the top of the list, refresh the same list but don't delete the old records
    // data base should only have latest 30 records
    // where as list can have as many records as user scrolls

    // RemoteDataTask AsyncTask
    private class RemoteDataTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            // showing refresh animation before making http call
            swipeRefreshLayout.setRefreshing(true);
            // mUtil.showIndeterminateProgressDialog("Loading...");
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            try
            {
                ParseQuery<ParseObject> query = new ParseQuery<>(IssuesDao.TABLE);
                query.orderByDescending("createdAt");
                MiscUtil.log("Try : Getting the list");
                mParseObjectList = query.find();
                MiscUtil.log("List Obtained, mParseObjectList : " + mParseObjectList);
                IssuesDao issueDao = new IssuesDao(mActivity);
                if (mParseObjectList != null)
                {
                    issueDao.delete();
                    for (ParseObject issueParseObj : mParseObjectList)
                    {
                        // Locate images in flag column
                        if(null == issueParseObj)
                            continue;
                        Issue issue = new Issue();
                        issue.imgUrl = (((ParseFile) issueParseObj.get(IssuesDao.IMAGE_URL)).getUrl());
                        issue.issueId = issueParseObj.getObjectId();
                        issue.latitude = (((Double) issueParseObj.get(IssuesDao.LATITUDE)).floatValue());
                        issue.longitude = (((Double) issueParseObj.get(IssuesDao.LONGITUDE)).floatValue());

                        issue.description = ((String) issueParseObj.get(IssuesDao.DESCRIPTION));
                        issue.placeName = ((String) issueParseObj.get(IssuesDao.PLACE_NAME));
                        issue.userDpUrl = ((String) issueParseObj.get(IssuesDao.USER_DP_URL));
                        issue.userId = ((String) issueParseObj.get(IssuesDao.USER_ID));
                        issue.username = ((String) issueParseObj.get(IssuesDao.USERNAME));

                        MiscUtil.log("Place Name : " + issueParseObj.get(IssuesDao.PLACE_NAME));
                        issue.radius = ((int) issueParseObj.get(IssuesDao.RADIUS));
                        issue.areaType = ((String) issueParseObj.get(IssuesDao.AREA_TYPE));
                        issuesList.add(issue);
                        issueDao.insert(issue);
                    }
                }
            }
            catch (ParseException e)
            {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            // Locate the listview in listview_main.xml
//            issuesRecyclerView = (RecyclerView) mActivity.findViewById(R.id.issuesList);
//            // Pass the results into ListViewAdapter.java
//            adapter = new IssueAdapter(mActivity, issuesList);
//            // Binds the Adapter to the ListView
//            issuesRecyclerView.setAdapter(adapter);
//            issuesRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
//            // Close the progressdialog
////            mUtil.hideProgressDialog();
//            // hiding refresh animation after making http call
            if(null != adapter)
                adapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private class LocalDataTask extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected void onPreExecute()
        {
            swipeRefreshLayout.setRefreshing(true);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            issuesList = new IssuesDao(mActivity).getIssuesList();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            if(null != issuesList && issuesList.size() <= 0)
            {
                onRefresh();
               // return;
            }
            // Locate the listview in listview_main.xml
            issuesRecyclerView = (RecyclerView) mActivity.findViewById(R.id.issuesList);
            // Pass the results into ListViewAdapter.java

            adapter = new IssueAdapter(mActivity, issuesList);
            // Binds the Adapter to the ListView
            issuesRecyclerView.setAdapter(adapter);
            issuesRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
            // Close the progressdialog
//            mUtil.hideProgressDialog();
            // hiding refresh animation after making http call
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onRefresh()
    {
        mDataTask = new RemoteDataTask();
        mDataTask.execute();
    }
}
