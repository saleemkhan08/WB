package in.org.whistleblower.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import in.org.whistleblower.MainActivity;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.models.LocationAlarmDao;
import in.org.whistleblower.utilities.NavigationUtil;

public class NotificationActionReceiver extends BroadcastReceiver
{
    public static final String NOTIFICATION_ACTION = "notificationAction";
    public static final String CANCEL_ALL_ALARMS = "CancelAllAlarms";
    public static final int REQUEST_CODE_CANCEL_ALL_ALARMS = 1801;
    public static final int NOTIFICATION_ID_ALARMS = 181;

    public NotificationActionReceiver()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getStringExtra(NOTIFICATION_ACTION);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intentMainActivity = new Intent(context, MainActivity.class);
        intentMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d(NOTIFICATION_ACTION, action);

        SharedPreferences preferences = WhistleBlower.getPreferences();

        switch (intent.getStringExtra(NOTIFICATION_ACTION))
        {
            case CANCEL_ALL_ALARMS :
                new LocationAlarmDao().cancelAllAlarms();
                notificationManager.cancel(NOTIFICATION_ID_ALARMS);
                break;

            case NavigationUtil.LOCATION_ALARM_FRAGMENT_TAG :
                intentMainActivity.putExtra(NavigationUtil.DIALOG_FRAGMENT_TAG, NavigationUtil.LOCATION_ALARM_FRAGMENT_TAG);
                preferences.edit().putString(NavigationUtil.DIALOG_FRAGMENT_TAG, NavigationUtil.LOCATION_ALARM_FRAGMENT_TAG).commit();
                context.startActivity(intentMainActivity);
                break;
        }
    }
}
