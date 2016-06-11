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
import in.org.whistleblower.fragments.NotifyLocationFragment;
import in.org.whistleblower.fragments.NotifyLocationListFragment;
import in.org.whistleblower.models.NotifyLocation;
import in.org.whistleblower.dao.NotifyLocationDao;
import in.org.whistleblower.services.LocationTrackingService;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.ImageUtil;

public class NotifyLocationAdapter extends RecyclerView.Adapter<NotifyLocationAdapter.PlaceViewHolder>
{
    AppCompatActivity mActivity;
    LayoutInflater inflater;
    List<NotifyLocation> mNotifyLocationList;
    public NotifyLocationAdapter(AppCompatActivity activity, List<NotifyLocation> mNotifyLocationList)
    {
        mActivity = activity;
        this.mNotifyLocationList = mNotifyLocationList;
        inflater = LayoutInflater.from(mActivity);
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.notify_location_card, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PlaceViewHolder holder, final int position)
    {
        final NotifyLocation notifyLocation = mNotifyLocationList.get(position);
        holder.notifyAddress.setText(NotifyLocationFragment.getAddressLines(notifyLocation.message, 3));
        holder.friendsName.setText(notifyLocation.receiverName);
        if(notifyLocation.receiverPhotoUrl != null && !(notifyLocation.receiverPhotoUrl.trim().isEmpty()))
        {
            ImageUtil.displayImage(mActivity,notifyLocation.receiverPhotoUrl, holder.friendCardIcon,true);
        }
        else
        {
            holder.friendCardIcon.setImageResource(R.mipmap.user_accent_primary_o);
        }
        holder.deleteAlarm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                NotifyLocationDao.delete(notifyLocation.receiverEmail);
                removeAt(position);
            }
        });
    }

    public void removeAt(int position)
    {
        mNotifyLocationList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mNotifyLocationList.size());
        if (mNotifyLocationList.size() < 1)
        {
            Otto.post(NotifyLocationListFragment.NOTIFY_LOC_LIST_EMPTY_TEXT);
        }else
        {
            Otto.post(LocationTrackingService.DELETE_NOTIFY_ARRIVAL_ALARM_NOTIFICATION);
        }
    }

    @Override
    public int getItemCount()
    {
        return mNotifyLocationList.size();
    }

    class PlaceViewHolder extends RecyclerView.ViewHolder
    {
        View item;

        @Bind(R.id.friendsName)
        TextView friendsName;

        @Bind(R.id.notifyMessage)
        TextView notifyAddress;

        @Bind(R.id.deleteAlarm)
        ImageView deleteAlarm;

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