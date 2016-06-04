package in.org.whistleblower.utilities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.net.URL;

import in.org.whistleblower.R;
import in.org.whistleblower.models.Notifications;
import in.org.whistleblower.receivers.NotificationActionReceiver;

public class NotificationsUtil
{

    private static final String TAG = "NotificationsUtil";

    public static void showNotification()
    {

    }

    public static void removeNotification()
    {

    }

    public static void updateNotification()
    {

    }

    public static void showAlarmNotification(Context context, String name, int noOfAlarms)
    {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent cancelIntent = new Intent(context, NotificationActionReceiver.class);
        cancelIntent.putExtra(NotificationActionReceiver.NOTIFICATION_ACTION, NotificationActionReceiver.CANCEL_ALL_ALARMS);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), cancelIntent, 0);

        Intent contentIntent = new Intent(context, NotificationActionReceiver.class);
        contentIntent.putExtra(NotificationActionReceiver.NOTIFICATION_ACTION, NavigationUtil.LOCATION_ALARM_FRAGMENT_TAG);
        PendingIntent contentPendingIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle("Location Alarm")
                .setSmallIcon(R.drawable.bullhorn_white)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentText(name)
                .setContentIntent(contentPendingIntent)
                .addAction(R.mipmap.bell_cross_accent, (noOfAlarms > 1) ? "Turn Off All Alarms" : "Turn Off Alarm", cancelPendingIntent);
        mNotificationManager.notify(NotificationActionReceiver.NOTIFICATION_ID_ALARMS, mBuilder.build());
    }

    public static void showReceivingNotifyLocation(Context context, Notifications notification)
    {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent contentIntent = new Intent(context, NotificationActionReceiver.class);
        contentIntent.putExtra(NotificationActionReceiver.NOTIFICATION_ACTION, NavigationUtil.NOTIFY_LOCATION_RECEIVING_FRAGMENT_TAG);
        PendingIntent contentPendingIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        try
        {
            URL url = new URL(notification.photoUrl);
            RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notify_location_notification);
            contentView.setImageViewBitmap(R.id.notificationIcon, BitmapFactory.decodeStream(url.openConnection().getInputStream()));
            contentView.setTextViewText(R.id.senderName, notification.name);
            contentView.setTextViewText(R.id.message, notification.message);

            Notification notificationDefault = new Notification();
            notificationDefault.defaults |= Notification.DEFAULT_LIGHTS; // LED
            notificationDefault.defaults |= Notification.DEFAULT_VIBRATE; //Vibration
            notificationDefault.defaults |= Notification.DEFAULT_SOUND; // Sound

            mBuilder.setContent(contentView)
                    .setSmallIcon(R.drawable.bullhorn_white)
                    .setContentIntent(contentPendingIntent)
                    .setDefaults(notificationDefault.defaults);
            mNotificationManager.notify(NotificationActionReceiver.NOTIFICATION_ID_RECEIVING_LOCATION_NOTIFICATION, mBuilder.build());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d(TAG, " Error : " + e.getMessage());
        }
    }


}
