package in.org.whistleblower.adapters;

import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.org.whistleblower.FriendListActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.interfaces.ResultListener;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.AccountsDao;
import in.org.whistleblower.utilities.ImageUtil;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.VolleyUtil;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendViewHolder>
{
    AppCompatActivity mActivity;
    LayoutInflater inflater;
    List<Accounts> mFriendList;
    private ImageUtil mImageUtil;
    MiscUtil mUtil;
    private boolean busy;
    private SharedPreferences preferences;

    public FriendListAdapter(AppCompatActivity activity, List<Accounts> friendList)
    {
        mActivity = activity;
        mFriendList = friendList;
        inflater = LayoutInflater.from(mActivity);
        mUtil = new MiscUtil(mActivity);
        mImageUtil = new ImageUtil(mActivity);
        preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.friend_card, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, final int position)
    {
        final Accounts account = mFriendList.get(position);
        if (account.photo_url == null || account.photo_url.isEmpty())
        {
            holder.friendCardIcon.setImageResource(R.mipmap.user_accent_primary_o);
        }
        else
        {
            mImageUtil.displayImage(account.photo_url, holder.friendCardIcon, true);
        }
        holder.friendsEmail.setText(account.email);
        holder.friendsName.setText(account.name);
        holder.friendCardOptions.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PopupMenu popup = new PopupMenu(mActivity, v);
                popup.getMenuInflater()
                        .inflate(R.menu.friend_options, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        switch (item.getItemId())
                        {
                            case R.id.req_loc_share:
                                reqLocShare();
                                break;
                            case R.id.loc_notification:
                                reqLocNotification();
                                break;
                            case R.id.removeFriend:
                                removeFriend(account, position);
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    private void reqLocNotification()
    {

    }

    private void reqLocShare()
    {

    }

    public void removeFriend(final Accounts account, final int position)
    {
        if (!busy)
        {
            FriendListActivity.showProgressFab();
            Map<String, String> data = new HashMap<>();
            data.put(VolleyUtil.KEY_ACTION, "removeFriend");
            data.put(Accounts.USER_EMAIL, preferences.getString(Accounts.EMAIL, "saleemkhan08@gmail.com"));
            data.put(Accounts.FRIENDS_EMAIL, account.email);
            busy = true;
            VolleyUtil.sendPostData(data, new ResultListener<String>()
            {
                @Override
                public void onSuccess(String result)
                {
                    Log.d("removeFriend", "removeFriend result : " + result);
                    if (result.equals("1"))
                    {
                        new AccountsDao().update(account.email, Accounts.RELATION, Accounts.NOT_A_FRIEND);
                        account.relation = Accounts.NOT_A_FRIEND;
                        removeAt(position);
                    }
                    else
                    {
                        Toast.makeText(mActivity, "Please Try Again!", Toast.LENGTH_SHORT).show();
                        Log.d("ToastMsg", "error : " + result);
                    }
                    resetBusy();
                }

                @Override
                public void onError(VolleyError error)
                {
                    FriendListActivity.hideProgressFab();
                    Toast.makeText(mActivity, "Please Try again!", Toast.LENGTH_SHORT).show();
                    Log.d("ToastMsg", "error : " + error.getMessage());
                    resetBusy();
                }
            });
        }
        else
        {
            Toast.makeText(mActivity, "Please Wait!", Toast.LENGTH_SHORT).show();
        }
    }

    public void removeAt(int position)
    {
        mFriendList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mFriendList.size());
    }

    @Override
    public int getItemCount()
    {
        return mFriendList.size();
    }

    class FriendViewHolder extends RecyclerView.ViewHolder
    {
        View item;

        @Bind(R.id.friendsEmail)
        TextView friendsEmail;

        @Bind(R.id.friendsName)
        TextView friendsName;

        @Bind(R.id.friendCardIcon)
        ImageView friendCardIcon;

        @Bind(R.id.friendCardOptions)
        ImageView friendCardOptions;

        public FriendViewHolder(View itemView)
        {
            super(itemView);
            item = itemView;
            ButterKnife.bind(this, itemView);
        }
    }

    private void resetBusy()
    {
        Handler myHandler = new Handler();
        myHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                busy = false;
            }
        }, 600);
    }
}