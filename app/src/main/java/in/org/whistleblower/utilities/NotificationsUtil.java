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
//        RemoteViews contentView = new RemoteViews(mAppContext.getPackageName(), R.layout.notify_location_notification);
//        contentView.setImageViewBitmap(R.id.notificationIcon, getCircleBitmapFromUrl(notification.photoUrl));
//        contentView.setTextViewText(R.id.senderName, notification.name);
//        contentView.setTextViewText(R.id.message, notification.message);
//        showRemoteViewsNotification(contentView, notification.type, (int) notification.id);

        NotificationData data = new NotificationData();

        data.largeIconUrl = notification.photoUrl;
        data.contentTitle = notification.name;
        data.contentText = notification.message;
        data.contentIntentTag = notification.type;
        data.notificationId = (int) notification.id;
        data.vibrate = true;
        showNotification(data);
    }

    public static void showAlarmNotification(String name, int noOfAlarms)
    {
        NotificationData data = new NotificationData();

        data.actionIntentText = (noOfAlarms > 1) ? "Turn Off All Alarms" : "Turn Off Alarm";
        data.actionIntentIcon = R.mipmap.bell_cross_accent;
        data.actionIntentTag = NotificationActionReceiver.CANCEL_ALL_ALARMS;

        data.contentIntentTag = NavigationUtil.LOCATION_ALARM_FRAGMENT_TAG;
        data.contentText = name;
        data.contentTitle = "Location Alarm";
        data.onGoing = true;
        data.notificationId = NotificationActionReceiver.NOTIFICATION_ID_ALARMS;
        showNotification(data);
    }


    public static void showNotification(NotificationData data)
    {
        if(data.largeIconUrl == null)
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
        PendingIntent contentPendingIntent = PendingIntent.getBroadcast(mAppContext, (int) System.currentTimeMillis(), contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mAppContext);
        mBuilder.setContentTitle(data.contentTitle)
                .setSmallIcon(R.drawable.bullhorn_white)
                .setOngoing(data.onGoing)
                .setContentText(data.contentText)
                .setContentIntent(contentPendingIntent);

        if (data.actionIntentTag != null)
        {
            Intent actionIntent = new Intent(mAppContext, NotificationActionReceiver.class);
            actionIntent.putExtra(NotificationActionReceiver.NOTIFICATION_ACTION, data.actionIntentTag);
            PendingIntent actionPendingIntent = PendingIntent.getBroadcast(mAppContext, (int) System.currentTimeMillis(), actionIntent, 0);

            mBuilder.addAction(data.actionIntentIcon, data.actionIntentText, actionPendingIntent);
        }

        if(mLargeIcon !=null)
        {
            mBuilder.setLargeIcon(mLargeIcon);
        }

        if(data.vibrate)
        {
            Notification notificationDefault = new Notification();
            notificationDefault.defaults |= Notification.DEFAULT_LIGHTS; // LED
            notificationDefault.defaults |= Notification.DEFAULT_VIBRATE; //Vibration
            notificationDefault.defaults |= Notification.DEFAULT_SOUND; // Sound
            mBuilder.setDefaults(notificationDefault.defaults);
        }

        mNotificationManager.notify(data.notificationId, mBuilder.build());
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
            showNotification(mNotificationData,mLargeIcon);
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