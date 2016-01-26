package in.org.whistleblower.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.Collections;
import java.util.List;

import in.org.whistleblower.R;
import in.org.whistleblower.icon.FontAwesomeIcon;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.UserConnections;
import in.org.whistleblower.utilities.ImageUtil;
import in.org.whistleblower.utilities.MiscUtil;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserSearchViewHolder>
{
    Context mContext;
    LayoutInflater inflater;
    List<Accounts> mAccountsList = Collections.emptyList();
    private DisplayImageOptions dpOptions;
    MiscUtil mUtil;
    private ImageUtil mImageUtil;
    SharedPreferences preferences;
    boolean mIsFriendList;
    List<String> mFriendsIdList;

    public UserSearchAdapter(Context context, List<Accounts> accountsList, List<String> friendsList, boolean isFriendList)
    {
        mContext = context;
        mIsFriendList = isFriendList;
        mAccountsList = accountsList;
        mFriendsIdList = friendsList;
        inflater = LayoutInflater.from(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mUtil = new MiscUtil(mContext);
        mImageUtil = new ImageUtil(mContext);
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
        final Accounts account = mAccountsList.get(position);
        holder.usernameView.setText(account.name);
        if (account.photo_url == null || account.photo_url.isEmpty())
        {
            holder.profilePic.setBackground(mUtil.getIcon(FontAwesomeIcon.ANONYMOUS, R.color.colorPrimary));
            holder.profilePic.setImageResource(android.R.color.transparent);
        }
        else
        {
            mImageUtil.displayImage(account.photo_url, holder.profilePic, true);
            holder.profilePic.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent, null));
        }

        if (!mIsFriendList)
        {
            if (mFriendsIdList.contains(account.googleId))
            {
                disableButton(holder.addFriendView);
            }
            else
            {
                holder.addFriendView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        addFriend(account.googleId, account.photo_url, account.name, v);
                    }
                });
            }
        }
        else
        {
            holder.addFriendView.setText("Remove");
            holder.addFriendView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    removeFriend(account.name, account.googleId, position);
                }
            });
        }
    }

    private void removeFriend(final String name, String googleId, final int position)
    {
        mUtil.showIndeterminateProgressDialog("Removing...");
        ParseQuery<ParseObject> query = new ParseQuery<>(UserConnections.TABLE);
        query.whereEqualTo(UserConnections.FRIEND_GOOGLE_ID, googleId);

        query.getFirstInBackground(new GetCallback<ParseObject>()
        {
            @Override
            public void done(final ParseObject object, ParseException e)
            {
                object.deleteInBackground(new DeleteCallback()
                {
                    @Override
                    public void done(ParseException e)
                    {
                        mUtil.toast("Removed "+ name);
                        mUtil.hideProgressDialog();
                        removeAt(position);
                    }
                });
            }
        });
    }

    public void removeAt(int position) {
        mAccountsList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mAccountsList.size());
    }
    private void addFriend(String googleId, String photoUrl, final String name, final View button)
    {
        mUtil.showIndeterminateProgressDialog("Adding...");
        ParseObject friend = new ParseObject(UserConnections.TABLE);
        friend.put(UserConnections.FRIEND_DP_URL, photoUrl);
        friend.put(UserConnections.FRIEND_GOOGLE_ID, googleId);
        friend.put(UserConnections.FRIEND_NAME, name);
        friend.put(UserConnections.USER_GOOGLE_ID, preferences.getString(Accounts.GOOGLE_ID, ""));
        friend.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if (e == null)
                {
                    mUtil.hideProgressDialog();
                    mUtil.toast("Added " + name + " to your friend List");
                    disableButton(button);
                }
            }
        });
    }

    public void disableButton(View button)
    {
        button.setEnabled(false);
        ((Button) button).setText("Friends");
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
        ImageView profilePic;

        public UserSearchViewHolder(View itemView)
        {
            super(itemView);
            addFriendView = (Button) itemView.findViewById(R.id.addPlaceButton);
            profilePic = (ImageView) itemView.findViewById(R.id.displayPic);
            usernameView = (TextView) itemView.findViewById(R.id.username);
        }
    }
}