package in.org.whistleblower.adapters;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.org.whistleblower.IssueActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.fragments.MapFragment;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.Issue;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.ImageUtil;
import in.org.whistleblower.utilities.MiscUtil;

public class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.IssueViewHolder>
{
    LayoutInflater mInflater;
    AppCompatActivity mActivity;
    ArrayList<Issue> mIssuesArrayList;
    MiscUtil mUtil;
    ImageUtil mImageUtil;

    @Inject
    SharedPreferences preferences;

    public IssueAdapter(AppCompatActivity activity, ArrayList<Issue> mIssuesList)
    {
        mActivity = activity;
        mInflater = LayoutInflater.from(mActivity);
        WhistleBlower.getComponent().inject(this);

        this.mIssuesArrayList = mIssuesList;
        mUtil = new MiscUtil(mActivity);
        mImageUtil = new ImageUtil(mActivity);
        //preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
    }

    @Override
    public IssueViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = mInflater.inflate(R.layout.issue_layout, parent, false);
        return new IssueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(IssueViewHolder holder, final int position)
    {
        final Issue issue = mIssuesArrayList.get(position);
        // Set the results into TextViews
        holder.areaTypeName.setText(issue.areaType);
        holder.username.setText(issue.username);
        holder.issueDescription.setText(issue.description);

        holder.locationContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Bundle bundle = new Bundle();
                bundle.putParcelable(MapFragment.SHOW_ISSUE, issue);
                Otto.getBus().post(issue);
            }
        });

        holder.shareContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mUtil.toast("Share This Post : " + position);
            }
        });

        holder.volunteerContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mUtil.toast("Volunteer : " + position);
            }
        });

        holder.optionsIconContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PopupMenu popup = new PopupMenu(mActivity, v);
                popup.getMenuInflater()
                        .inflate(R.menu.issue_options, popup.getMenu());

                Menu menu = popup.getMenu();
                if (issue.userId.equals(preferences.getString(Accounts.GOOGLE_ID, "")))
                {
                    menu.getItem(0).setVisible(false);
                    menu.getItem(1).setVisible(true);
                    menu.getItem(2).setVisible(true);
                }
                else
                {
                    menu.getItem(0).setVisible(true);
                    menu.getItem(1).setVisible(false);
                    menu.getItem(2).setVisible(false);
                }
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        switch (item.getItemId())
                        {
                            case R.id.editIssue:
                                editIssue(issue);
                                break;
                            case R.id.deleteIssue:
                                deleteIssue(issue.issueId, position);
                                break;
                            case R.id.reportIssue:
                                reportIssue(issue.issueId);
                                break;

                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        mImageUtil.displayImage(issue.imgUrl, holder.issueImage, false);
        String dpUrl = issue.userDpUrl;
        if (dpUrl == null || dpUrl.isEmpty())
        {
            holder.profilePic.setBackground(mActivity.getDrawable(R.drawable.anonymous_white_primary_dark));
            holder.profilePic.setImageResource(android.R.color.transparent);
        }
        else
        {
            mImageUtil.displayImage(dpUrl, holder.profilePic, true);
            holder.profilePic.setBackgroundColor(mActivity.getResources().getColor(android.R.color.transparent, null));
        }

        holder.issueImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                Intent issueActivityIntent = new Intent(mActivity, IssueActivity.class);
                //issueActivityIntent.putExtra()
                mActivity.startActivity(issueActivityIntent);
                mUtil.toast("Image : " + position + " clicked");
            }
        });
    }

    private void reportIssue(String issueId)
    {
        mUtil.showIndeterminateProgressDialog("Reporting...");
        /*ParseQuery<ParseObject> query = ParseQuery.getQuery(IssuesDao.TABLE);
        query.getInBackground(issueId, new GetCallback<ParseObject>()
        {
            public void done(ParseObject issue, ParseException e)
            {
                if (e == null)
                {
                    issue.put(IssuesDao.STATUS, IssuesDao.SPAM);
                    issue.saveInBackground(new SaveCallback()
                    {
                        @Override
                        public void done(ParseException e)
                        {
                            if (e == null)
                            {
                                mUtil.toast("Reported!");
                                mUtil.hideProgressDialog();
                            }
                        }
                    });
                }
            }
        });*/
    }

    private void deleteIssue(String issueId, final int position)
    {
        mUtil.showIndeterminateProgressDialog("Deleting...");
        /*ParseQuery<ParseObject> query = new ParseQuery<>(IssuesDao.TABLE);
        query.getInBackground(issueId, new GetCallback<ParseObject>()
        {
            @Override
            public void done(final ParseObject object, ParseException e)
            {
                object.deleteInBackground(new DeleteCallback()
                {
                    @Override
                    public void done(ParseException e)
                    {
                        mUtil.toast("Deleted");
                        mUtil.hideProgressDialog();
                        removeAt(position);
                    }
                });
            }
        });*/
    }

    public void removeAt(int position)
    {
        mIssuesArrayList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mIssuesArrayList.size());
    }

    private void editIssue(Issue issue)
    {

        mUtil.toast("Not Implemented");
    }

    @Override
    public int getItemCount()
    {
        return mIssuesArrayList.size();
    }

    class IssueViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.areaTypeName)
        TextView areaTypeName;
        @Bind(R.id.username)
        TextView username;
        @Bind(R.id.issueDescription)
        TextView issueDescription;

        @Bind(R.id.issueImg)
        ImageView issueImage;
        @Bind(R.id.profilePic)
        ImageView profilePic;
        @Bind(R.id.optionsIcon)
        ImageView optionsIcon;
        @Bind(R.id.shareIcon)
        ImageView shareIcon;
        @Bind(R.id.locationIcon)
        ImageView locationIcon;
        @Bind(R.id.volunteerIcon)
        ImageView volunteerIcon;

        @Bind(R.id.optionsIconContainer)
        View optionsIconContainer;
        @Bind(R.id.locationContainer)
        View locationContainer;
        @Bind(R.id.volunteerContainer)
        View volunteerContainer;
        @Bind(R.id.shareContainer)
        View shareContainer;

        public IssueViewHolder(View view)
        {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}