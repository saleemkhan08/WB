package in.org.whistleblower.fragments;


import android.app.Activity;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
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
    ListView issuesListView;
    List<ParseObject> mIssues;
    MiscUtil mUtil;
    Activity mActivity;
    IssueAdapter adapter;
    FloatingActionButton addButton;
    private ArrayList<Issues> issuesList = null;
    public MainFragment()
    {

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
        issuesListView = (ListView) parentView.findViewById(R.id.issuesList);
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

        RemoteDataTask dataTask = new RemoteDataTask();
        dataTask.execute();
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
        if(event.getAction()== MotionEvent.ACTION_UP)
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
                mIssues = query.find();
                MiscUtil.log("List Obtained, mIssues : " + mIssues);
                if (mIssues != null)
                {
                    for (ParseObject issueParseObj : mIssues)
                    {
                        // Locate images in flag column
                        Issues issue = new Issues();
                        issue.setImgUrl(((ParseFile) issueParseObj.get(Issues.FILE_URL)).getUrl());

                        issue.setLatitude(((Double) issueParseObj.get(Issues.LATITUDE)).floatValue());
                        issue.setLongitude(((Double) issueParseObj.get(Issues.LONGITUDE)).floatValue());

                        issue.setDescription((String) issueParseObj.get(Issues.DESCRIPTION));
                        issue.setPlaceName((String) issueParseObj.get(Issues.PLACE_NAME));
                        issue.setUserDpUrl((String) issueParseObj.get(Issues.USER_DP_URL));
                        issue.setUserId((String) issueParseObj.get(Issues.USER_ID));
                        issue.setUsername((String) issueParseObj.get(Issues.USERNAME));

                        MiscUtil.log("Place Name : " + issueParseObj.get(Issues.PLACE_NAME));
                        issue.setRadius((int) issueParseObj.get(Issues.RADIUS));
                        issue.setZone((String) issueParseObj.get(Issues.AREA_TYPE));
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
            issuesListView = (ListView) mActivity.findViewById(R.id.issuesList);
            // Pass the results into ListViewAdapter.java
            adapter = new IssueAdapter(mActivity, issuesList);
            // Binds the Adapter to the ListView
            issuesListView.setAdapter(adapter);
            // Close the progressdialog
            mUtil.hideProgressDialog();
        }
    }
}
