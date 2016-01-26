package in.org.whistleblower.fragments;


import android.app.Activity;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import in.org.whistleblower.utilities.MiscUtil;

public class MainFragment extends Fragment implements View.OnTouchListener
{
    RecyclerView issuesRecyclerView;
    List<ParseObject> mParseObjectList;
    MiscUtil mUtil;
    Activity mActivity;
    IssueAdapter adapter;
    FloatingActionButton addButton;
    private ArrayList<Issues> issuesList = null;
    RemoteDataTask mDataTask;
    public MainFragment()
    {

    }

    @Override
    public void onStop()
    {
        super.onStop();
        if(mDataTask != null && mDataTask.getStatus().equals(AsyncTask.Status.RUNNING))
        {
            mDataTask.cancel(true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_main, container, false);
        mActivity = getActivity();
        mUtil = new MiscUtil(mActivity);
        MiscUtil.log("onCreateView");
        return parentView;
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

        mDataTask = new RemoteDataTask();
        mDataTask.execute();
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
            mUtil.showIndeterminateProgressDialog("Loading...");
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            // Create the array
            issuesList = new ArrayList<>();
            try
            {
                ParseQuery<ParseObject> query = new ParseQuery<>(Issues.TABLE);
                query.orderByDescending("createdAt");
                MiscUtil.log("Try : Getting the list");
                mParseObjectList = query.find();
                MiscUtil.log("List Obtained, mParseObjectList : " + mParseObjectList);
                if (mParseObjectList != null)
                {
                    for (ParseObject issueParseObj : mParseObjectList)
                    {
                        // Locate images in flag column
                        Issues issue = new Issues();
                        issue.imgUrl = (((ParseFile) issueParseObj.get(Issues.FILE_URL)).getUrl());
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
                        issue.zone = ((String) issueParseObj.get(Issues.AREA_TYPE));
                        issuesList.add(issue);
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
            issuesRecyclerView = (RecyclerView) mActivity.findViewById(R.id.issuesList);
            // Pass the results into ListViewAdapter.java
            adapter = new IssueAdapter(mActivity, issuesList);
            // Binds the Adapter to the ListView
            issuesRecyclerView.setAdapter(adapter);
            issuesRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
            // Close the progressdialog
            mUtil.hideProgressDialog();
        }
    }
}
