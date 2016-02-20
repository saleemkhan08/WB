package in.org.whistleblower.fragments;


import android.app.Activity;
import android.content.ContentValues;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import in.org.whistleblower.R;
import in.org.whistleblower.adapters.IssueAdapter;
import in.org.whistleblower.models.Issues;
import in.org.whistleblower.models.WBDataBase;
import in.org.whistleblower.utilities.MiscUtil;

public class MainFragment extends Fragment implements View.OnTouchListener, SwipeRefreshLayout.OnRefreshListener
{
    RecyclerView issuesRecyclerView;
    List<ParseObject> mParseObjectList;
    MiscUtil mUtil;
    Activity mActivity;
    IssueAdapter adapter;
    FloatingActionButton addButton;
    private ArrayList<Issues> issuesList = null;
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
        mActivity = getActivity();
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
        hideMyLocButton();
    }

    public void hideMyLocButton()
    {
        MiscUtil.log("hideMyLocButton");
        FloatingActionButton buttonMyLoc = (FloatingActionButton) mActivity.findViewById(R.id.my_loc);
        RelativeLayout addIssueMenu = (RelativeLayout) mActivity.findViewById(R.id.fab_wrapper);
        if (buttonMyLoc != null && addIssueMenu != null)
        {
            buttonMyLoc.setVisibility(View.GONE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) addIssueMenu.getLayoutParams();

            if (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                layoutParams.setMargins(0, 0, mUtil.dp(10), mUtil.dp(10));
            }
            addIssueMenu.setLayoutParams(layoutParams);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_UP)
        {

        }
        return true;
    }

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
                ParseQuery<ParseObject> query = new ParseQuery<>(Issues.TABLE);
                query.orderByDescending("createdAt");
                MiscUtil.log("Try : Getting the list");
                mParseObjectList = query.find();
                MiscUtil.log("List Obtained, mParseObjectList : " + mParseObjectList);
                WBDataBase wbDataBase = new WBDataBase(getActivity());
                if (mParseObjectList != null)
                {
                    wbDataBase.delete(Issues.TABLE, null, null);
                    for (ParseObject issueParseObj : mParseObjectList)
                    {
                        // Locate images in flag column
                        if(null == issueParseObj)
                            continue;
                        Issues issue = new Issues();
                        issue.imgUrl = (((ParseFile) issueParseObj.get(Issues.IMAGE_URL)).getUrl());
                        issue.issueId = issueParseObj.getObjectId();
                        issue.latitude = (((Double) issueParseObj.get(Issues.LATITUDE)).floatValue());
                        issue.longitude = (((Double) issueParseObj.get(Issues.LONGITUDE)).floatValue());

                        issue.description = ((String) issueParseObj.get(Issues.DESCRIPTION));
                        issue.placeName = ((String) issueParseObj.get(Issues.PLACE_NAME));
                        issue.userDpUrl = ((String) issueParseObj.get(Issues.USER_DP_URL));
                        issue.userId = ((String) issueParseObj.get(Issues.USER_ID));
                        issue.username = ((String) issueParseObj.get(Issues.USERNAME));

                        MiscUtil.log("Place Name : " + issueParseObj.get(Issues.PLACE_NAME));
                        issue.radius = ((int) issueParseObj.get(Issues.RADIUS));
                        issue.areaType = ((String) issueParseObj.get(Issues.AREA_TYPE));
                        issuesList.add(issue);
                        ContentValues values = new ContentValues();
                        values.put(Issues.IMAGE_URL, issue.imgUrl);
                        values.put(Issues.ISSUE_ID, issue.issueId);
                        values.put(Issues.LATITUDE, issue.latitude);
                        values.put(Issues.LONGITUDE, issue.longitude);
                        values.put(Issues.DESCRIPTION, issue.description);
                        values.put(Issues.PLACE_NAME, issue.placeName);
                        values.put(Issues.USER_DP_URL, issue.userDpUrl);
                        values.put(Issues.USER_ID, issue.userId);
                        values.put(Issues.USERNAME, issue.username);
                        values.put(Issues.RADIUS, issue.radius);
                        values.put(Issues.AREA_TYPE, issue.areaType);
                        wbDataBase.insert(Issues.TABLE, values);
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
            WBDataBase wbDataBase = new WBDataBase(getActivity());
            Cursor cursor = wbDataBase.query(Issues.TABLE, null, null, null, null, null);
            if (null != cursor)
            {
                while (cursor.moveToNext())
                {
                    Issues issue = new Issues();
                    issue.issueId = cursor.getString(cursor.getColumnIndex(Issues.ISSUE_ID));
                    issue.imgUrl = cursor.getString(cursor.getColumnIndex(Issues.IMAGE_URL));
                    issue.latitude = cursor.getFloat(cursor.getColumnIndex(Issues.LATITUDE));
                    issue.longitude = cursor.getFloat(cursor.getColumnIndex(Issues.LONGITUDE));

                    issue.description = cursor.getString(cursor.getColumnIndex(Issues.DESCRIPTION));
                    issue.placeName = cursor.getString(cursor.getColumnIndex(Issues.PLACE_NAME));
                    issue.userDpUrl = cursor.getString(cursor.getColumnIndex(Issues.USER_DP_URL));
                    issue.userId = cursor.getString(cursor.getColumnIndex(Issues.USER_ID));
                    issue.username = cursor.getString(cursor.getColumnIndex(Issues.USERNAME));
                    issue.radius = cursor.getInt(cursor.getColumnIndex(Issues.RADIUS));
                    issue.areaType = cursor.getString(cursor.getColumnIndex(Issues.AREA_TYPE));
                    issuesList.add(issue);
                }
                cursor.close();
            }
            wbDataBase.closeDb();
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
