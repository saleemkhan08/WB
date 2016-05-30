package in.org.whistleblower.adapters;

import android.content.Context;
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
import in.org.whistleblower.R;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.ImageUtil;

public class CommonUserListAdapter extends RecyclerView.Adapter<CommonUserListAdapter.UserSearchViewHolder>
{
    Context mContext;
    LayoutInflater inflater;
    List<Accounts> mAccountsList = Collections.emptyList();
    private ImageUtil mImageUtil;

    public CommonUserListAdapter(Context context, List<Accounts> accountsList)
    {
        mContext = context;
        mAccountsList = accountsList;
        inflater = LayoutInflater.from(context);
        mImageUtil = new ImageUtil(mContext);
    }

    @Override
    public UserSearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.friend_card, parent, false);
        return new UserSearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserSearchViewHolder holder, final int position)
    {
        final Accounts account = mAccountsList.get(position);
        holder.username.setText(account.name);
        holder.emailId.setText(account.email);
        holder.userOptions.setVisibility(View.GONE);
        if (account.photo_url == null || account.photo_url.isEmpty())
        {
            holder.profilePic.setImageResource(R.mipmap.user_accent_primary_o);
        }
        else
        {
            mImageUtil.displayImage(account.photo_url, holder.profilePic, true);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Otto.post(account);
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
        @Bind(R.id.friendsName)
        TextView username;

        @Bind(R.id.friendCardIcon)
        ImageView profilePic;

        @Bind(R.id.friendsEmail)
        TextView emailId;

        @Bind(R.id.friendCardOptions)
        ImageView userOptions;

        public UserSearchViewHolder(View itemView)
        {
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
        }
    }
}