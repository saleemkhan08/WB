package in.org.whistleblower.actions;

import android.support.v7.app.AppCompatActivity;

import in.org.whistleblower.utilities.MiscUtil;

public class Alarm
{
    AppCompatActivity mActivity;
    MiscUtil mUtil;
    public Alarm(AppCompatActivity activity)
    {
        mActivity = activity;
        mUtil = new MiscUtil(activity);
    }

    public void setAlarm()
    {
        mUtil.toast("Set Alarm");
    }
}
