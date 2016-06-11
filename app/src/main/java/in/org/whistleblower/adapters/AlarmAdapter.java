package in.org.whistleblower.adapters;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.org.whistleblower.R;
import in.org.whistleblower.fragments.LocationAlarmListFragment;
import in.org.whistleblower.fragments.NotifyLocationFragment;
import in.org.whistleblower.models.LocationAlarm;
import in.org.whistleblower.dao.LocationAlarmDao;
import in.org.whistleblower.services.LocationTrackingService;
import in.org.whistleblower.singletons.Otto;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.PlaceViewHolder>
{
    AppCompatActivity mActivity;
    LayoutInflater inflater;
    List<LocationAlarm> mAlarmList;
    public AlarmAdapter(AppCompatActivity activity, List<LocationAlarm> mAlarmList)
    {
        mActivity = activity;
        this.mAlarmList = mAlarmList;
        inflater = LayoutInflater.from(mActivity);
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.location_alarm_row, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PlaceViewHolder holder, final int position)
    {
        final LocationAlarm alarm = mAlarmList.get(position);
        holder.alarmAddress.setText(NotifyLocationFragment.getAddressLines(alarm.address, 3));

        holder.range.setText(getRadiusText(alarm.radius));
        if(alarm.status == LocationAlarm.ALARM_ON)
        {
            holder.cancelAlarm.setImageResource(R.drawable.bell_cross_accent);
        }
        else
        {
            holder.cancelAlarm.setImageResource(R.mipmap.bell_icon_accent);
        }

        holder.cancelAlarm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int status = LocationAlarmDao.getAlarm(alarm.address).status;
                if (status == LocationAlarm.ALARM_ON)
                {
                    setAlarm(alarm, false);
                    holder.cancelAlarm.setImageResource(R.mipmap.bell_icon_accent);
                }
                else
                {
                    setAlarm(alarm, true);
                    holder.cancelAlarm.setImageResource(R.drawable.bell_cross_accent);
                }
            }
        });

        holder.deleteAlarm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LocationAlarmDao.delete(alarm.address);
                removeAt(position);
            }
        });
    }

    void setAlarm(LocationAlarm alarm, boolean isSet)
    {
        if(isSet)
        {
            Log.d("FlowLogs", "setAlarm");
            LocationAlarmDao.update(alarm.address, LocationAlarm.ALARM_ON);
            toast("Alarm Set : \n" + alarm.address);
        }
        else
        {
            Log.d("FlowLogs", "resetAlarm");
            LocationAlarmDao.update(alarm.address, LocationAlarm.ALARM_OFF);
            toast("Alarm Turned off : \n" + alarm.address);
        }

        Intent intent = new Intent(mActivity, LocationTrackingService.class);
        intent.putExtra(LocationTrackingService.KEY_ALARM_SET, true);
        mActivity.startService(intent);

    }

    private void toast(String str)
    {
        Toast toast = Toast.makeText(mActivity, str, Toast.LENGTH_LONG);
        ViewGroup view = (ViewGroup) toast.getView();
        ((TextView) view.getChildAt(0)).setGravity(Gravity.CENTER);
        toast.setView(view);
        toast.show();
    }

    private String getRadiusText(int radius)
    {
        return "Range : " + ((radius >= 1000) ? (radius / 1000) + "km" : radius + "m");
    }

    public void removeAt(int position)
    {
        mAlarmList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mAlarmList.size());
        if (mAlarmList.size() < 1)
        {
            Otto.post(LocationAlarmListFragment.ALARM_LIST_EMPTY_TEXT);
        }else
        {
            Otto.post(LocationTrackingService.DELETE_ALARM_NOTIFICATION);
        }
    }

    @Override
    public int getItemCount()
    {
        return mAlarmList.size();
    }

    class PlaceViewHolder extends RecyclerView.ViewHolder
    {
        View item;

        @Bind(R.id.alarmAddress)
        TextView alarmAddress;

        @Bind(R.id.range)
        TextView range;

        @Bind(R.id.cancelAlarm)
        ImageView cancelAlarm;

        @Bind(R.id.deleteAlarm)
        ImageView deleteAlarm;

        public PlaceViewHolder(View itemView)
        {
            super(itemView);
            item = itemView;
            ButterKnife.bind(this, itemView);
        }
    }
}