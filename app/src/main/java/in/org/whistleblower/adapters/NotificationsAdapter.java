package in.org.whistleblower.adapters;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.org.whistleblower.R;
import in.org.whistleblower.fragments.NotificationsFragment;
import in.org.whistleblower.models.Notifications;
import in.org.whistleblower.models.NotificationsDao;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.ImageUtil;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.PlaceViewHolder>
{
    AppCompatActivity mActivity;
    LayoutInflater inflater;
    List<Notifications> mNotificationsList;
    NotificationsDao dao;

    public NotificationsAdapter(AppCompatActivity activity, List<Notifications> mNotificationsList)
    {
        mActivity = activity;
        this.mNotificationsList = mNotificationsList;
        inflater = LayoutInflater.from(mActivity);
        dao = new NotificationsDao();

    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.notification_card, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PlaceViewHolder holder, final int position)
    {
        final Notifications notification = mNotificationsList.get(position);
        holder.notifyMessage.setText(notification.message);
        holder.friendsName.setText(notification.name);
        if(notification.photoUrl != null && !(notification.photoUrl.trim().isEmpty()))
        {
            ImageUtil.displayImage(mActivity,notification.photoUrl, holder.friendCardIcon,true);
        }
        else
        {
            holder.friendCardIcon.setImageResource(R.mipmap.user_accent_primary_o);
        }
        holder.deleteNotification.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dao.delete(notification.id);
                removeAt(position);
            }
        });
    }

    public void removeAt(int position)
    {
        mNotificationsList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mNotificationsList.size());
        if (mNotificationsList.size() < 1)
        {
            Otto.post(NotificationsFragment.NOTIFICATION_LIST_EMPTY_TEXT);
        }
    }

    @Override
    public int getItemCount()
    {
        return mNotificationsList.size();
    }

    class PlaceViewHolder extends RecyclerView.ViewHolder
    {
        View item;

        @Bind(R.id.friendsName)
        TextView friendsName;

        @Bind(R.id.notifyMessage)
        TextView notifyMessage;

        @Bind(R.id.timeElapsed)
        TextView timeElapsed;

        @Bind(R.id.deleteNotification)
        ImageView deleteNotification;

        @Bind(R.id.friendCardIcon)
        ImageView friendCardIcon;

        public PlaceViewHolder(View itemView)
        {
            super(itemView);
            item = itemView;
            ButterKnife.bind(this, itemView);
        }
    }
}