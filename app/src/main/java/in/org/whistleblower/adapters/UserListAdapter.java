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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.org.whistleblower.FriendListActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.AccountsDao;
import in.org.whistleblower.interfaces.ResultListener;
import in.org.whistleblower.utilities.VolleyUtil;
import in.org.whistleblower.utilities.ImageUtil;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserSearchViewHolder>
{
    Context mContext;
    LayoutInflater inflater;
    List<Accounts> mAccountsList = Collections.emptyList();
    private ImageUtil mImageUtil;
    SharedPreferences preferences;

    public UserListAdapter(Context context, List<Accounts> accountsList)
    {
        mContext = context;
        mAccountsList = accountsList;
        inflater = LayoutInflater.from(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mImageUtil = new ImageUtil(mContext);
    }

    @Override
    public UserSearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.user_row, parent, false);
        return new UserSearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserSearchViewHolder holder, final int position)
    {
        final Accounts account = mAccountsList.get(position);
        holder.usernameView.setText(account.name);
        if (account.photo_url == null || account.photo_url.isEmpty())
        {
            holder.profilePic.setImageResource(R.drawable.anonymous_white_primary_dark);
        }
        else
        {
            mImageUtil.displayImage(account.photo_url, holder.profilePic, true);
        }
        holder.addFriendView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                addFriend(account.email, account.photo_url, account.name, position);
            }
        });

    }

    private void addFriend(final String email, String photoUrl, final String name, final int position)
    {
        FriendListActivity.showProgressFab();
        Map<String, String> data = new HashMap<>();
        data.put(Accounts.FRIENDS_PHOTO, photoUrl);
        data.put(Accounts.FRIENDS_NAME, name);
        data.put(Accounts.FRIENDS_EMAIL, email);
        data.put(Accounts.USER_EMAIL, preferences.getString(Accounts.EMAIL, "saleemkhan08@gmail.com"));
        data.put(VolleyUtil.KEY_ACTION, "addFriend");
        VolleyUtil.sendPostData(data, new ResultListener<String>()
        {
            @Override
            public void onSuccess(String result)
            {
                Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
                Log.d("addFriend", "Result : "+result);
                new AccountsDao(mContext).update(email, Accounts.RELATION, Accounts.FRIEND);
                FriendListActivity.showUserListFromDatabase();
            }

            @Override
            public void onError(VolleyError error)
            {
                FriendListActivity.hideProgressFab();
                Toast.makeText(mContext, error.getMessage()+"\nPlease Try again!", Toast.LENGTH_SHORT).show();
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
        Button addFriendView;
        TextView usernameView;
        ImageView profilePic;

        public UserSearchViewHolder(View itemView)
        {
            super(itemView);
            addFriendView = (Button) itemView.findViewById(R.id.addFriendButton);
            profilePic = (ImageView) itemView.findViewById(R.id.profilePic);
            usernameView = (TextView) itemView.findViewById(R.id.username);
        }
    }
}