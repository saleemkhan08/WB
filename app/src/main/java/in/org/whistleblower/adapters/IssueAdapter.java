package in.org.whistleblower.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.ArrayList;

import in.org.whistleblower.R;
import in.org.whistleblower.icon.FontAwesomeIcon;
import in.org.whistleblower.models.Issues;
import in.org.whistleblower.utilities.MiscUtil;

public class IssueAdapter extends BaseAdapter
{
    private final MiscUtil mUtil;
    Activity mActivity;
    LayoutInflater mInflater;
    ImageLoader mImageLoader;
    private ArrayList<Issues> mIssuesArrayList;
    private DisplayImageOptions dpOptions, issueOptions;

    public IssueAdapter(Activity context, ArrayList<Issues> data)
    {
        mActivity = context;
        mIssuesArrayList = data;
        MiscUtil.log("IssueAdapter, mIssuesArrayList size : " + mIssuesArrayList.size());
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(ImageLoaderConfiguration.createDefault(mActivity));
        mInflater = LayoutInflater.from(context);
        mUtil = new MiscUtil(mActivity);
        issueOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        dpOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new RoundedBitmapDisplayer(100))
                .build();


    }

    @Override
    public int getCount()
    {
        return mIssuesArrayList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mIssuesArrayList.get(position);
    }

    public class ViewHolder
    {
        TextView placeName, username;
        ImageView issueImage, profilePic, optionsIcon, shareIcon, locationIcon, volunteerIcon;
        View optionsIconContainer, locationContainer, volunteerContainer, shareContainer;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent)
    {
        MiscUtil.log("getView");
        final ViewHolder holder;
        if (view == null)
        {
            holder = new ViewHolder();
            view = mInflater.inflate(R.layout.single_issue_layout, null);

            holder.issueImage = (ImageView) view.findViewById(R.id.issueImg);
            holder.profilePic = (ImageView) view.findViewById(R.id.displayPic);

            holder.optionsIcon = (ImageView) view.findViewById(R.id.optionsIcon);
            holder.optionsIconContainer = view.findViewById(R.id.optionsIconContainer);

            holder.shareIcon = (ImageView) view.findViewById(R.id.shareIcon);
            holder.shareContainer = view.findViewById(R.id.shareContainer);

            holder.locationIcon = (ImageView) view.findViewById(R.id.locationIcon);
            holder.locationContainer = view.findViewById(R.id.locationContainer);

            holder.volunteerIcon = (ImageView) view.findViewById(R.id.volunteerIcon);
            holder.volunteerContainer = view.findViewById(R.id.volunteerContainer);

            holder.username = (TextView) view.findViewById(R.id.username);
            holder.placeName = (TextView) view.findViewById(R.id.placeTypeName);

            view.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.placeName.setText(mIssuesArrayList.get(position).getPlaceName());
        holder.username.setText(mIssuesArrayList.get(position).getUsername());

        holder.locationIcon.setBackground(mUtil.getIcon(FontAwesomeIcon.MAP_MARKER));
        holder.locationContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mUtil.toast("Show Location On Map : "+position);
            }
        });

        holder.shareIcon.setBackground(mUtil.getIcon(FontAwesomeIcon.SHARE_ALT2));
        holder.shareContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mUtil.toast("Share This Post : "+position);
            }
        });

        holder.volunteerIcon.setBackground(mUtil.getIcon(FontAwesomeIcon.BOOKMARK));
        holder.volunteerContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mUtil.toast("Volunteer : "+position);
            }
        });

        holder.optionsIcon.setBackground(mUtil.getIcon(FontAwesomeIcon.MENU, R.color.white));
        holder.optionsIconContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PopupMenu popup = new PopupMenu(mActivity, v);
                popup.getMenuInflater()
                        .inflate(R.menu.issue_options, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        mUtil.toast(item.getTitle() + " : " + position);
                        return true;
                    }
                });
                popup.show();
            }
        });

        mImageLoader.displayImage(mIssuesArrayList.get(position).getImgUrl(), holder.issueImage, issueOptions);
        String dpUrl = mIssuesArrayList.get(position).getUserDpUrl();
        if (dpUrl == null || dpUrl.isEmpty())
        {
            holder.profilePic.setBackground(mUtil.getIcon(FontAwesomeIcon.ANONYMOUS, R.color.white));
            holder.profilePic.setImageResource(android.R.color.transparent);
        }
        else
        {
            mImageLoader.displayImage(dpUrl, holder.profilePic, dpOptions);
            holder.profilePic.setBackgroundColor(mActivity.getResources().getColor(android.R.color.transparent, null));
        }

        holder.issueImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                mUtil.toast("Image : "+position+" clicked");
            }
        });
        return view;
    }
}
