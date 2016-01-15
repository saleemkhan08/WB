package in.org.whistleblower.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import in.org.whistleblower.R;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.utilities.ImageUtil;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserSearchViewHolder>
{
    Context mContext;
    LayoutInflater inflater;
    List<Accounts> mAccountsList = Collections.emptyList();
    public UserSearchAdapter(Context context, List<Accounts> accountsList)
    {
        mContext = context;
        mAccountsList = accountsList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public UserSearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.user_search_result_row, parent, false);
        return new UserSearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserSearchViewHolder holder, final int position)
    {
        Accounts account = mAccountsList.get(position);
        holder.usernameView.setText(account.name);
        holder.addFriendView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });

        ImageUtil.loadImage(mContext, holder.dpView, account.photo_url);
    }

    @Override
    public int getItemCount()
    {
        return mAccountsList.size();
    }

    class UserSearchViewHolder extends RecyclerView.ViewHolder
    {
        Button addFriendView;
        TextView usernameView;
        ImageView dpView;
        public UserSearchViewHolder(View itemView)
        {
            super(itemView);
            addFriendView = (Button) itemView.findViewById(R.id.addFriendButton);
            dpView = (ImageView) itemView.findViewById(R.id.displayPic);
            usernameView = (TextView) itemView.findViewById(R.id.username);
        }
    }
}