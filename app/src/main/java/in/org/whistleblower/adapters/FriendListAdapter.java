package in.org.whistleblower.adapters;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import in.org.whistleblower.FriendListActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.utilities.ImageUtil;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendListViewHolder>
{
    LayoutInflater inflater;
    private ImageUtil mImageUtil;
    SharedPreferences preferences;
    int dividerPos;
    List<Accounts> mAccountsList;
    FriendListActivity mActivity;

    public FriendListAdapter(FriendListActivity activity, List<Accounts> accountsList)
    {
        mActivity = activity;
        mAccountsList = accountsList;
        inflater = LayoutInflater.from(mActivity);
        preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mImageUtil = new ImageUtil(mActivity);
    }

    @Override
    public FriendListViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.user_row, parent, false);
        return new FriendListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendListViewHolder holder, final int position)
    {
        final Accounts account = mAccountsList.get(position);
        if (account == null)
        {
            dividerPos = position;
            holder.suggestionsLabelView.setVisibility(View.VISIBLE);
            holder.userRowView.setVisibility(View.GONE);
        }
        else
        {
            holder.suggestionsLabelView.setVisibility(View.GONE);
            holder.userRowView.setVisibility(View.VISIBLE);

            holder.usernameView.setText(account.name);
            if (account.photo_url == null || account.photo_url.isEmpty())
            {
                holder.profilePicView.setImageResource(R.drawable.anonymous_white_primary_dark);
            }
            else
            {
                mImageUtil.displayImage(account.photo_url, holder.profilePicView, true);
            }

            if (account.relation.equals(Accounts.FRIEND))
            {
                holder.removeFriendButton.setVisibility(View.VISIBLE);
                holder.addFriendButton.setVisibility(View.GONE);

                holder.removeFriendButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        mActivity.removeFriend(account, position);
                    }
                });
            }
            else
            {
                holder.removeFriendButton.setVisibility(View.GONE);
                holder.addFriendButton.setVisibility(View.VISIBLE);

                holder.addFriendButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        mActivity.addFriend(account, position);
                    }
                });
            }
        }
    }

    public void removeUser(int position, Accounts account)
    {
        removeAt(position, account);
        int size = mAccountsList.size();
        if (dividerPos >= size)
        {
            mAccountsList.add(size, null);
        }
        addAt(dividerPos, account);
        dividerPos--;
    }

    public void addUser(int position, Accounts account)
    {
        removeAt(position, account);
        addAt(dividerPos, account);
        dividerPos++;
        if (dividerPos >= (mAccountsList.size() - 1))
        {
            removeAt(dividerPos, null);
        }
    }

    public void removeAt(int position, Accounts account)
    {
        mAccountsList.remove(position);
        if (account != null)
        {
            if (account.relation.equals(Accounts.FRIEND))
            {
                FriendListActivity.mFriendList.remove(account);
            }
            else
            {
                FriendListActivity.mUserList.remove(account);
            }
        }
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mAccountsList.size());
    }

    public void addAt(int position, Accounts account)
    {
        mAccountsList.add(position, account);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, mAccountsList.size());
        if (account.relation.equals(Accounts.FRIEND))
        {
            FriendListActivity.mFriendList.add(account);
        }
        else
        {
            FriendListActivity.mUserList.add(account);
        }
    }

    @Override
    public int getItemCount()
    {
        return mAccountsList.size();
    }

    class FriendListViewHolder extends RecyclerView.ViewHolder
    {
        Button addFriendButton, removeFriendButton;

        View suggestionsLabelView;
        View userRowView;

        TextView usernameView;
        ImageView profilePicView;

        public FriendListViewHolder(View itemView)
        {
            super(itemView);
            addFriendButton = (Button) itemView.findViewById(R.id.addFriendButton);
            removeFriendButton = (Button) itemView.findViewById(R.id.removeFriendButton);
            suggestionsLabelView = itemView.findViewById(R.id.suggestionsLabel);
            userRowView = itemView.findViewById(R.id.placeContent);
            profilePicView = (ImageView) itemView.findViewById(R.id.profilePic);
            usernameView = (TextView) itemView.findViewById(R.id.username);
        }
    }

    public void animateTo(List<Accounts> accounts)
    {
        applyAndAnimateRemovals(accounts);
        applyAndAnimateAdditions(accounts);
        applyAndAnimateMovedItems(accounts);
    }

    private void applyAndAnimateRemovals(List<Accounts> newAccountsList)
    {
        for (int i = mAccountsList.size() - 1; i >= 0; i--)
        {
            final Accounts account = mAccountsList.get(i);
            if (!newAccountsList.contains(account))
            {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<Accounts> newAccountsList)
    {
        for (int i = 0, count = newAccountsList.size(); i < count; i++)
        {
            final Accounts account = newAccountsList.get(i);
            if (!mAccountsList.contains(account))
            {
                addItem(i, account);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<Accounts> newAccountsList)
    {
        for (int toPosition = newAccountsList.size() - 1; toPosition >= 0; toPosition--)
        {
            final Accounts model = newAccountsList.get(toPosition);
            final int fromPosition = mAccountsList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition)
            {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public Accounts removeItem(int position)
    {
        final Accounts model = mAccountsList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, Accounts model)
    {
        mAccountsList.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition)
    {
        final Accounts model = mAccountsList.remove(fromPosition);
        mAccountsList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
}