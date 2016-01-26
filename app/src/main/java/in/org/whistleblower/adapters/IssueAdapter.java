package in.org.whistleblower.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;

import in.org.whistleblower.R;
import in.org.whistleblower.icon.FontAwesomeIcon;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.Issues;
import in.org.whistleblower.utilities.ImageUtil;
import in.org.whistleblower.utilities.MiscUtil;

public class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.IssueViewHolder>
{
    LayoutInflater mInflater;
    Context mContext;
    ArrayList<Issues> mIssuesArrayList;
    MiscUtil mUtil;
    ImageUtil mImageUtil;
    SharedPreferences preferences;

    public IssueAdapter(Context mContext, ArrayList<Issues> mIssuesList)
    {
        mInflater = LayoutInflater.from(mContext);
        this.mContext = mContext;
        this.mIssuesArrayList = mIssuesList;
        mUtil = new MiscUtil(mContext);
        mImageUtil = new ImageUtil(mContext);
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    public IssueViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = mInflater.inflate(R.layout.single_issue_layout, parent, false);
        return new IssueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(IssueViewHolder holder, final int position)
    {
        final Issues issue = mIssuesArrayList.get(position);
        // Set the results into TextViews
        holder.placeName.setText(issue.placeName);
        holder.username.setText(issue.username);

        holder.locationIcon.setBackground(mUtil.getIcon(FontAwesomeIcon.MAP_MARKER));
        holder.locationContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mUtil.toast("Show Location On Map : " + position);
            }
        });

        holder.shareIcon.setBackground(mUtil.getIcon(FontAwesomeIcon.SHARE_ALT2));
        holder.shareContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mUtil.toast("Share This Post : " + position);
            }
        });

        holder.volunteerIcon.setBackground(mUtil.getIcon(FontAwesomeIcon.BOOKMARK));
        holder.volunteerContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mUtil.toast("Volunteer : " + position);
            }
        });

        holder.optionsIcon.setBackground(mUtil.getIcon(FontAwesomeIcon.MENU, R.color.white));
        holder.optionsIconContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PopupMenu popup = new PopupMenu(mContext, v);
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
                                editIssue(issue.issueId);
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
            holder.profilePic.setBackground(mUtil.getIcon(FontAwesomeIcon.ANONYMOUS, R.color.white));
            holder.profilePic.setImageResource(android.R.color.transparent);
        }
        else
        {
            mImageUtil.displayImage(dpUrl, holder.profilePic, true);
            holder.profilePic.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent, null));
        }

        holder.issueImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                mUtil.toast("Image : " + position + " clicked");
            }
        });
    }

    private void reportIssue(String issueId)
    {
        mUtil.showIndeterminateProgressDialog("Reporting...");
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Issues.TABLE);
        query.getInBackground(issueId, new GetCallback<ParseObject>()
        {
            public void done(ParseObject issue, ParseException e)
            {
                if (e == null)
                {
                    issue.put(Issues.STATUS, Issues.SPAM);
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
        });
    }

    private void deleteIssue(String issueId, final int position)
    {
        mUtil.showIndeterminateProgressDialog("Deleting...");
        ParseQuery<ParseObject> query = new ParseQuery<>(Issues.TABLE);
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
        });
    }

    public void removeAt(int position)
    {
        mIssuesArrayList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mIssuesArrayList.size());
    }

    private void editIssue(String issueId)
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
        TextView placeName, username;
        ImageView issueImage, profilePic, optionsIcon, shareIcon, locationIcon, volunteerIcon;
        View optionsIconContainer, locationContainer, volunteerContainer, shareContainer;

        public IssueViewHolder(View view)
        {
            super(view);

            issueImage = (ImageView) view.findViewById(R.id.issueImg);
            profilePic = (ImageView) view.findViewById(R.id.displayPic);

            optionsIcon = (ImageView) view.findViewById(R.id.optionsIcon);
            optionsIconContainer = view.findViewById(R.id.optionsIconContainer);

            shareIcon = (ImageView) view.findViewById(R.id.shareIcon);
            shareContainer = view.findViewById(R.id.shareContainer);

            locationIcon = (ImageView) view.findViewById(R.id.locationIcon);
            locationContainer = view.findViewById(R.id.locationContainer);

            volunteerIcon = (ImageView) view.findViewById(R.id.volunteerIcon);
            volunteerContainer = view.findViewById(R.id.volunteerContainer);

            username = (TextView) view.findViewById(R.id.username);
            placeName = (TextView) view.findViewById(R.id.placeTypeName);
        }
    }
}