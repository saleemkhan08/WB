package in.org.whistleblower.fragments;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.org.whistleblower.R;
import in.org.whistleblower.adapters.AlarmAdapter;
import in.org.whistleblower.models.LocationAlarm;
import in.org.whistleblower.models.LocationAlarmDao;
import in.org.whistleblower.singletons.Otto;

public class LocationAlarmListFragment extends DialogFragment
{
    public static final String ALARM_LIST_EMPTY_TEXT = "ALARM_LIST_EMPTY_TEXT";

    @BindString(R.string.noLocationAlarmsAreSet)
    String youHaventSetAnyLocationAlarm;

    @Bind(R.id.emptyList)
    ViewGroup emptyList;

    @Bind(R.id.emptyListTextView)
    TextView emptyListTextView;

    public LocationAlarmListFragment()
    {
    }

    @Bind(R.id.locationAlarmList)
    RecyclerView locationAlarmList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_location_alarm_list, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);
        ArrayList<LocationAlarm> mAlarmList = new LocationAlarmDao().getList();
        AppCompatActivity mActivity = (AppCompatActivity) getActivity();
        if(mAlarmList.size() < 1)
        {
            showEmptyListString();
        }

        locationAlarmList.setAdapter(new AlarmAdapter(mActivity, mAlarmList));
        locationAlarmList.setLayoutManager(new LinearLayoutManager(mActivity));

        return parentView;
    }
    @Subscribe
    public void showEmptyListString(String msg)
    {
        if(msg.equals(ALARM_LIST_EMPTY_TEXT))
        {
            showEmptyListString();
            emptyListTextView.setText(youHaventSetAnyLocationAlarm);
        }
    }

    private void showEmptyListString()
    {
        TransitionManager.beginDelayedTransition(emptyList);
        emptyList.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        Otto.unregister(this);
    }

    @OnClick(R.id.closeDialog)
    public void close()
    {
        dismiss();
    }
}
