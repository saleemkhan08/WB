package in.org.whistleblower.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.org.whistleblower.FriendListActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.AccountsDao;
import in.org.whistleblower.storage.ResultListener;
import in.org.whistleblower.storage.VolleyUtil;
import in.org.whistleblower.utilities.ImageUtil;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendListViewHolder>
{
    static Context mContext;
    LayoutInflater inflater;
    private ImageUtil mImageUtil;
    SharedPreferences preferences;
    String userMail;
    int dividerPos;
    private static List<Accounts> mAccountsList;

    public FriendListAdapter(Context context, List<Accounts> accountsList)
    {
        mContext = context;
        mAccountsList = accountsList;
        inflater = LayoutInflater.from(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mImageUtil = new ImageUtil(mContext);
        userMail = preferences.getString(Accounts.EMAIL, "saleemkhan08@gmail.com");
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
                        removeFriend(account, position);
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
                        addFriend(account, position);
                    }
                });
            }
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

    private void removeFriend(final Accounts account, final int position)
    {
        FriendListActivity.showProgressFab();
        Map<String, String> data = new HashMap<>();
        data.put(VolleyUtil.KEY_ACTION, "removeFriend");
        data.put(Accounts.USER_EMAIL, userMail);
        data.put(Accounts.FRIENDS_EMAIL, account.email);
        VolleyUtil.sendPostData(data, new ResultListener<String>()
        {
            @Override
            public void onSuccess(String result)
            {
                Log.d("removeFriend", "removeFriend result : " + result);
                if (result.equals("1"))
                {
                    new AccountsDao(mContext).update(account.email, Accounts.RELATION, Accounts.NOT_A_FRIEND);
                    account.relation = Accounts.NOT_A_FRIEND;

                    removeAt(position, account);
                    int size = mAccountsList.size();
                    if (dividerPos >= size)
                    {
                        mAccountsList.add(size, null);
                    }
                    addAt(dividerPos, account);
                    dividerPos--;
                    FriendListActivity.hideProgressFab();
                }
                else
                {
                    Toast.makeText(mContext, "Please Try Again!", Toast.LENGTH_SHORT).show();
                    Log.d("ToastMsg", result + " : Please Try again!");
                }
            }

            @Override
            public void onError(VolleyError error)
            {
                FriendListActivity.hideProgressFab();
                Toast.makeText(mContext, error.getMessage() + "\nPlease Try again!", Toast.LENGTH_SHORT).show();
                Log.d("ToastMsg", error.getMessage() + "\nPlease Try again!");
            }
        });
    }

    private void addFriend(final Accounts account, final int position)
    {
        FriendListActivity.showProgressFab();
        Map<String, String> data = new HashMap<>();
        data.put(Accounts.FRIENDS_PHOTO, account.photo_url);
        data.put(Accounts.FRIENDS_NAME, account.name);
        data.put(Accounts.FRIENDS_EMAIL, account.email);
        data.put(Accounts.USER_EMAIL, userMail);
        data.put(VolleyUtil.KEY_ACTION, "addFriend");
        VolleyUtil.sendPostData(data, new ResultListener<String>()
        {
            @Override
            public void onSuccess(String result)
            {
                Log.d("addFriend", "Result : " + result);
                new AccountsDao(mContext).update(account.email, Accounts.RELATION, Accounts.FRIEND);
                account.relation = Accounts.FRIEND;
                removeAt(position, account);
                addAt(dividerPos, account);
                dividerPos++;
                if (dividerPos >= (mAccountsList.size() - 1))
                {
                    removeAt(dividerPos, null);
                }
                FriendListActivity.hideProgressFab();
                //FriendListActivity.showUserListFromDatabase();
            }

            @Override
            public void onError(VolleyError error)
            {
                FriendListActivity.hideProgressFab();
                Toast.makeText(mContext, error.getMessage() + "\nPlease Try again!", Toast.LENGTH_SHORT).show();
                Log.d("ToastMsg", error.getMessage() + "\nPlease Try again!");
            }
        });

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
            userRowView = itemView.findViewById(R.id.userRow);
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