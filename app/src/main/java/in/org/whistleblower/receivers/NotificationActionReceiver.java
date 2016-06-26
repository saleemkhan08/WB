package in.org.whistleblower.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import in.org.whistleblower.MainActivity;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.dao.LocationAlarmDao;
import in.org.whistleblower.gcm.RegistrationIntentService;
import in.org.whistleblower.models.Notifications;
import in.org.whistleblower.services.FriendsLocationTrackingService;
import in.org.whistleblower.utilities.NavigationUtil;

public class NotificationActionReceiver extends BroadcastReceiver
{
    public static final String NOTIFICATION_ACTION = "notificationAction";
    public static final String CANCEL_ALL_ALARMS = "CancelAllAlarms";
    public static final String NOTIFICATION_ID = "notificationId";
    public static final int NOTIFICATION_ID_LOCATION_ALARMS = 181;
    public static final int NOTIFICATION_ID_SHARING_REAL_TIME_LOCATION = 182;
    public static final int NOTIFICATION_ID_NOTIFY_LOCATION = 183;

    public static final int NOTIFICATION_ID_RECEIVING_SHARED_LOCATION = 184;
    public static final int NOTIFICATION_ID_RECEIVING_NOTIFIED_LOCATION = 185;
    public static final int NOTIFICATION_ID_RECEIVING_SHARED_LOCATION_ONCE = 186;
    public static final String FRIENDS_LOCATION_TRACKING_SERVICE = "friendsLocationTrackingService";
    public static final String START_RECEIVING_LOCATION = "startLocationSharing";
    public static final String NOTIFY_REJECTION_TO_SENDER = "notifyRejectionToSender";
    public static final int NOTIFICATION_ID_INITIATE_SHARE_LOCATION = 187;
    public static final String KEY_NOTIFICATION = "notification";
    static int retryCnt = 0;

    public NotificationActionReceiver()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getStringExtra(NOTIFICATION_ACTION);
        int id = intent.getIntExtra(NOTIFICATION_ID, -1);
        Notifications notification = intent.getParcelableExtra(KEY_NOTIFICATION);
        Log.d(NOTIFICATION_ACTION, "NOTIFICATION_ACTION : " + action);
        switch (action)
        {
            case RegistrationIntentService.TAG:
                Intent registrationService = new Intent(context, RegistrationIntentService.class);
                registrationService.putExtra(RegistrationIntentService.TAG, ++retryCnt);
                context.startService(registrationService);
                break;

            case CANCEL_ALL_ALARMS:
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                LocationAlarmDao.cancelAllAlarms();
                notificationManager.cancel(NOTIFICATION_ID_LOCATION_ALARMS);
                break;

            case NavigationUtil.FRAGMENT_TAG_NOTIFICATIONS:
            case NavigationUtil.FRAGMENT_TAG_LOCATION_ALARM:
            case NavigationUtil.FRAGMENT_TAG_SHARING_REAL_TIME_LOCATION:
            case NavigationUtil.FRAGMENT_TAG_RECEIVING_SHARED_LOCATION:
            case NavigationUtil.FRAGMENT_TAG_NOTIFY_LOCATION_LIST:
            case NavigationUtil.FRAGMENT_TAG_RECEIVING_NOTIFIED_LOCATION:
                context.startActivity(getMainActivityDialogIntent(context, action, id));
                break;
            case START_RECEIVING_LOCATION:
            case NOTIFY_REJECTION_TO_SENDER:
                Intent friendsLocationTrackingIntent = new Intent(context, FriendsLocationTrackingService.class);
                friendsLocationTrackingIntent.putExtra(NOTIFICATION_ACTION, action);
                friendsLocationTrackingIntent.putExtra(KEY_NOTIFICATION, notification);
                context.startService(friendsLocationTrackingIntent);
                break;

        }
    }

    private Intent getMainActivityDialogIntent(Context context, String dialogTag, int id)
    {
        SharedPreferences preferences = WhistleBlower.getPreferences();

        Intent intentMainActivity = new Intent(context, MainActivity.class);
        intentMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intentMainActivity.putExtra(NavigationUtil.FRAGMENT_TAG_DIALOG, dialogTag);
        intentMainActivity.putExtra(NOTIFICATION_ID, id);

        preferences.edit()
                .putString(NavigationUtil.FRAGMENT_TAG_DIALOG, dialogTag)
                .putInt(NOTIFICATION_ID, id)
                .commit();

        return intentMainActivity;
    }
}
