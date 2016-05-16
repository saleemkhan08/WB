package in.org.whistleblower;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.org.whistleblower.fragments.MapFragment;
import in.org.whistleblower.interfaces.ResultListener;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.Issue;
import in.org.whistleblower.models.IssuesDao;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.ImageUtil;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.VolleyUtil;

public class IssueActivity extends AppCompatActivity
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

    ImageUtil mImageUtil;

    @Inject
    SharedPreferences preferences;
    private MiscUtil mUtil;
    Issue issue;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        mImageUtil = new ImageUtil(this);
        mUtil = new MiscUtil(this);
        ButterKnife.bind(this);
        WhistleBlower.getComponent().inject(this);
        if (!intent.hasExtra(IssuesDao.ISSUE_ID))
        {
            startActivity(new Intent(this, MainActivity.class));
        }
        issue = intent.getParcelableExtra(IssuesDao.ISSUE_ID);
        // Set the results into TextViews
        areaTypeName.setText(issue.areaType);
        username.setText(issue.username);
        issueDescription.setText(issue.description);

        locationContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Bundle bundle = new Bundle();
                bundle.putParcelable(MapFragment.SHOW_ISSUE, issue);
                Otto.post(issue);
            }
        });


        shareContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(IssueActivity.this, "Share Post", Toast.LENGTH_SHORT).show();
            }
        });

        volunteerContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(IssueActivity.this, "Volunteer", Toast.LENGTH_SHORT).show();
            }
        });

        optionsIconContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PopupMenu popup = new PopupMenu(IssueActivity.this, v);
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
                                deleteIssue(issue.issueId);
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

        mImageUtil.displayImage(issue.imgUrl, issueImage, false);
        String dpUrl = issue.userDpUrl;
        if (dpUrl == null || dpUrl.isEmpty())
        {
            profilePic.setBackground(getDrawable(R.drawable.anonymous_white_primary_dark));
            profilePic.setImageResource(android.R.color.transparent);
        }
        else
        {
            mImageUtil.displayImage(dpUrl, profilePic, true);
            profilePic.setBackgroundColor(getResources().getColor(android.R.color.transparent, null));
        }

        issueImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
            }
        });
    }

    private void editIssue(Issue issue)
    {
        mUtil.toast("Not Implemented");
    }

    private void deleteIssue(String issueId)
    {
        mUtil.showIndeterminateProgressDialog("Deleting...");
        Map<String, String> map = new HashMap<>();
        map.put(VolleyUtil.KEY_ACTION, "deleteIssue");
        map.put(IssuesDao.ISSUE_ID, issueId);
        VolleyUtil.sendPostData(map, new ResultListener<String>()
        {
            @Override
            public void onSuccess(String result)
            {
                mUtil.hideProgressDialog();
                Toast.makeText(IssueActivity.this, result, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(VolleyError error)
            {
                mUtil.hideProgressDialog();
                Toast.makeText(IssueActivity.this, "Please Try Again" + "\n" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reportIssue(String issueId)
    {
        mUtil.showIndeterminateProgressDialog("Reporting...");
        Map<String, String> map = new HashMap<>();
        map.put(VolleyUtil.KEY_ACTION, "reportSpam");
        map.put(IssuesDao.ISSUE_ID, issueId);
        VolleyUtil.sendPostData(map, new ResultListener<String>()
        {
            @Override
            public void onSuccess(String result)
            {
                mUtil.hideProgressDialog();
                Toast.makeText(IssueActivity.this, result, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(VolleyError error)
            {
                mUtil.hideProgressDialog();
                Toast.makeText(IssueActivity.this, "Please Try Again" + "\n" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
