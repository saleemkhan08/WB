package in.org.whistleblower.utilities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.net.URL;

import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.models.NotificationData;
import in.org.whistleblower.models.Notifications;
import in.org.whistleblower.receivers.NotificationActionReceiver;

public class NotificationsUtil
{
    private static final String TAG = "NotificationsUtil";
    private static Context mAppContext;
    private static NotificationManager mNotificationManager;

    static
    {
        mAppContext = WhistleBlower.getAppContext();
        mNotificationManager = (NotificationManager) mAppContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void showReceivingNotifyLocation(Notifications notification)
    {
        NotificationData data = new NotificationData();
        data.largeIconUrl = notification.senderPhotoUrl;
        data.contentTitle = notification.senderName;
        data.contentText = notification.message;
        data.contentIntentTag = notification.type;
        data.notificationId = (int) notification.id;
        data.vibrate = true;
        showNotification(data);
    }

    public static void showAlarmNotification(String name, int noOfAlarms)
    {
        NotificationData data = new NotificationData();

        data.action1IntentText = (noOfAlarms > 1) ? "Turn Off All Alarms" : "Turn Off Alarm";
        data.action1IntentIcon = R.mipmap.bell_cross_accent;
        data.action1IntentTag = NotificationActionReceiver.CANCEL_ALL_ALARMS;

        data.contentIntentTag = NavigationUtil.FRAGMENT_TAG_LOCATION_ALARM;
        data.contentText = name;
        data.contentTitle = "Location Alarm";
        data.onGoing = true;
        data.notificationId = NotificationActionReceiver.NOTIFICATION_ID_LOCATION_ALARMS;
        showNotification(data);
    }


    public static void showNotification(NotificationData data)
    {
        if (data.largeIconUrl == null)
        {
            showNotification(data, null);
        }
        else
        {
            new ShowNormalNotification().execute(data);
        }
    }

    public static void showNotification(NotificationData data, Bitmap mLargeIcon)
    {
        Intent contentIntent = new Intent(mAppContext, NotificationActionReceiver.class);

        contentIntent.putExtra(NotificationActionReceiver.NOTIFICATION_ACTION, data.contentIntentTag);
        contentIntent.putExtra(NotificationActionReceiver.KEY_NOTIFICATION, data.notification);

        PendingIntent contentPendingIntent = PendingIntent.getBroadcast(mAppContext, (int) System.currentTimeMillis(), contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mAppContext);
        mBuilder.setContentTitle(data.contentTitle)
                .setSmallIcon(R.drawable.bullhorn_white)
                .setOngoing(data.onGoing)
                .setContentText(data.contentText)
                .setContentIntent(contentPendingIntent);

        if (data.action1IntentTag != null)
        {
            Intent actionIntent = new Intent(mAppContext, NotificationActionReceiver.class);
            actionIntent.putExtra(NotificationActionReceiver.NOTIFICATION_ACTION, data.action1IntentTag);
            PendingIntent actionPendingIntent = PendingIntent.getBroadcast(mAppContext, (int) System.currentTimeMillis(), actionIntent, 0);

            mBuilder.addAction(data.action1IntentIcon, data.action1IntentText, actionPendingIntent);
        }

        if (data.action2IntentTag != null)
        {
            Intent actionIntent = new Intent(mAppContext, NotificationActionReceiver.class);
            actionIntent.putExtra(NotificationActionReceiver.NOTIFICATION_ACTION, data.action2IntentTag);
            PendingIntent actionPendingIntent = PendingIntent.getBroadcast(mAppContext, (int) System.currentTimeMillis(), actionIntent, 0);

            mBuilder.addAction(data.action2IntentIcon, data.action2IntentText, actionPendingIntent);
        }

        if (mLargeIcon != null)
        {
            mBuilder.setLargeIcon(mLargeIcon);
        }

        if (data.vibrate)
        {
            Notification notificationDefault = new Notification();
            notificationDefault.defaults |= Notification.DEFAULT_LIGHTS; // LED
            notificationDefault.defaults |= Notification.DEFAULT_VIBRATE; //Vibration
            notificationDefault.defaults |= Notification.DEFAULT_SOUND; // Sound
            mBuilder.setDefaults(notificationDefault.defaults);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            {
                mBuilder.setPriority(data.priority);
            }
        }
        int notificationId = getNotificationId(data.contentIntentTag, data.notificationId);
        mNotificationManager.notify(notificationId, mBuilder.build());
    }

    public static int getLastIntDigitsFromLong(long notificationId)
    {
        return (int) (notificationId % 1000000000);
    }

    private static int getNotificationId(String contentIntentTag, long notificationId)
    {
        switch (contentIntentTag)
        {
            //Cancellable Notifications
            case NavigationUtil.FRAGMENT_TAG_RECEIVING_NOTIFIED_LOCATION:
                return NotificationActionReceiver.NOTIFICATION_ID_RECEIVING_NOTIFIED_LOCATION;

            case NavigationUtil.FRAGMENT_TAG_RECEIVING_SHARED_LOCATION_ONCE:
                return NotificationActionReceiver.NOTIFICATION_ID_RECEIVING_SHARED_LOCATION_ONCE;

            //On Going Notifications
            case NavigationUtil.FRAGMENT_TAG_LOCATION_ALARM:
                return NotificationActionReceiver.NOTIFICATION_ID_LOCATION_ALARMS;

            case NavigationUtil.FRAGMENT_TAG_NOTIFY_LOCATION:
                return NotificationActionReceiver.NOTIFICATION_ID_NOTIFY_LOCATION;

            case NavigationUtil.FRAGMENT_TAG_SHARING_REAL_TIME_LOCATION:
                return NotificationActionReceiver.NOTIFICATION_ID_SHARING_REAL_TIME_LOCATION;

            case NavigationUtil.FRAGMENT_TAG_RECEIVING_SHARED_LOCATION:
                return NotificationActionReceiver.NOTIFICATION_ID_RECEIVING_SHARED_LOCATION;

            case Notifications.KEY_INITIATE_SHARE_LOCATION :
                return getLastIntDigitsFromLong(notificationId);
            default:
                return -1;
        }

    }

    public static void showRemoteViewsNotification(RemoteViews contentView, String notificationType, int id)
    {
        Intent contentIntent = new Intent(mAppContext, NotificationActionReceiver.class);
        contentIntent.putExtra(NotificationActionReceiver.NOTIFICATION_ACTION, notificationType);

        PendingIntent contentPendingIntent = PendingIntent.getBroadcast(mAppContext, (int) System.currentTimeMillis(), contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mAppContext);

        Notification notificationDefault = new Notification();
        notificationDefault.defaults |= Notification.DEFAULT_LIGHTS; // LED
        notificationDefault.defaults |= Notification.DEFAULT_VIBRATE; //Vibration
        notificationDefault.defaults |= Notification.DEFAULT_SOUND; // Sound

        mBuilder.setContent(contentView)
                .setSmallIcon(R.drawable.bullhorn_white)
                .setContentIntent(contentPendingIntent)
                .setDefaults(notificationDefault.defaults);
        mNotificationManager.notify(id, mBuilder.build());
    }

    private static Bitmap getCircleBitmapFromUrl(String photoUrl)
    {
        try
        {
            URL url = new URL(photoUrl);
            return getCircleBitmap(BitmapFactory.decodeStream(url.openConnection().getInputStream()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d(TAG, " Error : " + e.getMessage());
        }
        return BitmapFactory.decodeResource(mAppContext.getResources(), R.drawable.bullhorn_invert);
    }

    public static void showReceivingSharedLocation(Notifications notification)
    {
        NotificationData data = new NotificationData();
        data.largeIconUrl = notification.senderPhotoUrl;
        data.contentTitle = notification.senderName;
        data.contentText = "Click to view " + notification.senderName + "'s location...";
        data.contentIntentTag = notification.type;
        data.notificationId = (int) notification.id;
        data.vibrate = true;
        showNotification(data);
    }

    private static class ShowNormalNotification extends AsyncTask<NotificationData, Void, Void>
    {
        NotificationData mNotificationData;
        Bitmap mLargeIcon;

        @Override
        protected Void doInBackground(NotificationData... params)
        {
            mNotificationData = params[0];
            mLargeIcon = getCircleBitmapFromUrl(mNotificationData.largeIconUrl);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            showNotification(mNotificationData, mLargeIcon);
        }
    }

    private static Bitmap getCircleBitmap(Bitmap bitmap)
    {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();
        return output;
    }

    public static void removeNotification(int id)
    {
        mNotificationManager.cancel(id);
    }
}