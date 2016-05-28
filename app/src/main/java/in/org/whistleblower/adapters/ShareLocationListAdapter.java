package in.org.whistleblower.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.org.whistleblower.LocationListActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.models.ShareLocation;
import in.org.whistleblower.models.ShareLocationDao;
import in.org.whistleblower.services.LocationTrackingService;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.ImageUtil;

public class ShareLocationListAdapter extends RecyclerView.Adapter<ShareLocationListAdapter.UserSearchViewHolder>
{
    Context mContext;
    LayoutInflater inflater;
    List<ShareLocation> mAccountsList = Collections.emptyList();
    private ImageUtil mImageUtil;
    private SharedPreferences preference;

    public ShareLocationListAdapter(Context context, List<ShareLocation> accountsList)
    {
        mContext = context;
        preference = PreferenceManager.getDefaultSharedPreferences(context);
        mAccountsList = accountsList;
        inflater = LayoutInflater.from(context);
        mImageUtil = new ImageUtil(mContext);
    }

    @Override
    public UserSearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.common_user_row, parent, false);
        return new UserSearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserSearchViewHolder holder, final int position)
    {
        final ShareLocation shareLocation = mAccountsList.get(position);
        holder.username.setText(shareLocation.name);
        if (shareLocation.photoUrl == null || shareLocation.photoUrl.isEmpty())
        {
            holder.profilePic.setImageResource(R.drawable.anonymous_white_primary_dark);
        }
        else
        {
            mImageUtil.displayImage(shareLocation.photoUrl, holder.profilePic, true);
        }


        holder.userOptionsIcon.setVisibility(View.VISIBLE);
        holder.userOptionsIcon.setImageResource(R.mipmap.delete_primary_dark);
        holder.userOptionsIcon.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ShareLocationDao dao = new ShareLocationDao(mContext);
                dao.delete(shareLocation.userEmail);
                removeAt(position);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return mAccountsList.size();
    }

    class UserSearchViewHolder extends RecyclerView.ViewHolder
    {
        View itemView;

        @Bind(R.id.username)
        TextView username;

        @Bind(R.id.profilePic)
        ImageView profilePic;

        @Bind(R.id.userOptionsIcon)
        ImageView userOptionsIcon;

        public UserSearchViewHolder(View itemView)
        {
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
        }
    }

    public void removeAt(int position)
    {
        int accSize = mAccountsList.size();
        if (accSize > position)
        {
            mAccountsList.remove(position);
            int size = mAccountsList.size();
            if(size < 1)
            {
                Otto.post(LocationListActivity.FINISH_ACTIVITY);
            }
            else
            {
                Otto.post(LocationTrackingService.UPDATE_NOTIFICATION);
            }
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, size);
        }
    }
}