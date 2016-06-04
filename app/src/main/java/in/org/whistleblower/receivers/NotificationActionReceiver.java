package in.org.whistleblower.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import in.org.whistleblower.MainActivity;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.gcm.RegistrationIntentService;
import in.org.whistleblower.models.LocationAlarmDao;
import in.org.whistleblower.utilities.NavigationUtil;

public class NotificationActionReceiver extends BroadcastReceiver
{
    public static final String NOTIFICATION_ACTION = "notificationAction";
    public static final String CANCEL_ALL_ALARMS = "CancelAllAlarms";
    public static final int NOTIFICATION_ID_ALARMS = 181;
    public static final String NOTIFICATION_ACTION_2 = "notificationAction2";
    public static final int NOTIFICATION_ID_RECEIVING_LOCATION_NOTIFICATION = 182;

    static int retryCnt = 0;
    public NotificationActionReceiver()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getStringExtra(NOTIFICATION_ACTION);
        Log.d(NOTIFICATION_ACTION, "NOTIFICATION_ACTION : "+action);
        switch (action)
        {
            case RegistrationIntentService.TAG :
                Intent registrationService = new Intent( context, RegistrationIntentService.class);
                registrationService.putExtra(RegistrationIntentService.TAG, ++retryCnt);
                context.startService(registrationService);
                break;

            case CANCEL_ALL_ALARMS :
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                new LocationAlarmDao().cancelAllAlarms();
                notificationManager.cancel(NOTIFICATION_ID_ALARMS);
                break;

            case NavigationUtil.LOCATION_ALARM_FRAGMENT_TAG :
            case NavigationUtil.NOTIFY_LOCATION_FRAGMENT_TAG :
            case NavigationUtil.SHARE_LOCATION_LIST_FRAGMENT_TAG :
            case NavigationUtil.NOTIFICATION_FRAGMENT_TAG :
                context.startActivity(getMainActivityDialogIntent(context, action));
                break;
        }
    }

    private Intent getMainActivityDialogIntent(Context context, String dialogTag)
    {
        SharedPreferences preferences = WhistleBlower.getPreferences();

        Intent intentMainActivity = new Intent(context, MainActivity.class);
        intentMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intentMainActivity.putExtra(NavigationUtil.DIALOG_FRAGMENT_TAG, dialogTag);

        preferences.edit().putString(NavigationUtil.DIALOG_FRAGMENT_TAG, dialogTag).commit();

        return intentMainActivity;
    }
}
