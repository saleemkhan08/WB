package in.org.whistleblower.fragments;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.adapters.AlarmAdapter;
import in.org.whistleblower.dao.LocationAlarmDao;
import in.org.whistleblower.models.LocationAlarm;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.TransitionUtil;

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

        ArrayList<LocationAlarm> mAlarmList = LocationAlarmDao.getList();
        AppCompatActivity mActivity = (AppCompatActivity) getActivity();
        if(mAlarmList.size() < 1)
        {
            showEmptyListString();
        }

        TextView dialogTitle = (TextView) parentView.findViewById(R.id.dialogTitle);
        dialogTitle.setTypeface(WhistleBlower.getTypeface());

        locationAlarmList.setAdapter(new AlarmAdapter(mActivity, mAlarmList));
        locationAlarmList.setLayoutManager(new LinearLayoutManager(mActivity));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
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
        TransitionUtil.defaultTransition(emptyList);
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

    @Override
    public void onPause()
    {
        super.onPause();
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        Otto.post(MapFragment.DIALOG_DISMISS);
    }
}
