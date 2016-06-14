package in.org.whistleblower.gcm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONObject;

import java.util.ArrayList;

import in.org.whistleblower.dao.NotificationsDao;
import in.org.whistleblower.models.NotificationData;
import in.org.whistleblower.models.Notifications;
import in.org.whistleblower.receivers.NotificationActionReceiver;
import in.org.whistleblower.services.FriendsLocationTrackingService;
import in.org.whistleblower.utilities.NavigationUtil;
import in.org.whistleblower.utilities.NotificationsUtil;

public class GcmPushReceiver extends GcmListenerService
{
    public static final String SENDER_NOTIFICATION = "senderNotification";

    @Override
    public void onMessageReceived(String from, Bundle bundle)
    {
        Log.d("GcmPushReceiver", "from : " + from + ", bundle : " + bundle);
        Notifications receivedNotification = saveNotificationToDatabase(bundle.getString("message"));
        ArrayList<Notifications> unreadNotifications = NotificationsDao.getUnreadList();
        Log.d("GcmPushReceiver", "unreadNotifications : " + unreadNotifications);

        int cancellable = 0;
        Notifications cancellableNotification = null;
        for (Notifications notification : unreadNotifications)
        {
            switch (notification.type)
            {
                //Cancellable
                case NavigationUtil.FRAGMENT_TAG_RECEIVING_NOTIFIED_LOCATION:
                case NavigationUtil.FRAGMENT_TAG_RECEIVING_SHARED_LOCATION_ONCE:
                    cancellableNotification = notification;
                    cancellable++;
                    break;
                case NavigationUtil.FRAGMENT_TAG_RECEIVING_SHARED_LOCATION:
                    Intent intent = new Intent(this, FriendsLocationTrackingService.class);
                    intent.putExtra(SENDER_NOTIFICATION, receivedNotification);
                    startService(intent);
                    break;
            }
        }

        if (cancellable > 1)
        {
            showGenericNotification(cancellable);
        }
        else if (cancellable == 1)
        {
            if (cancellableNotification.type.equals(NavigationUtil.FRAGMENT_TAG_RECEIVING_NOTIFIED_LOCATION))
            {
                NotificationsUtil.showReceivingNotifyLocation(cancellableNotification);
            }
            else
            {
                NotificationsUtil.showReceivingSharedLocation(cancellableNotification);
            }
        }
    }

    private void showGenericNotification(int count)
    {
        NotificationsUtil.removeNotification(NotificationActionReceiver.NOTIFICATION_ID_RECEIVING_NOTIFIED_LOCATION);
        NotificationsUtil.removeNotification(NotificationActionReceiver.NOTIFICATION_ID_RECEIVING_SHARED_LOCATION);
        NotificationsUtil.removeNotification(NotificationActionReceiver.NOTIFICATION_ID_RECEIVING_SHARED_LOCATION_ONCE);

        NotificationData notification = new NotificationData();
        notification.contentTitle = "You have " + (count) + " new notifications!";
        notification.contentText = "Click to view all.";
        notification.contentIntentTag = NavigationUtil.FRAGMENT_TAG_NOTIFICATIONS;
        NotificationsUtil.showNotification(notification, null);
    }

    private Notifications saveNotificationToDatabase(String result)
    {
        Notifications notification = null;
        try
        {
            notification = new Notifications();
            JSONObject json = new JSONObject(result);
            notification.userEmail = json.getString(Notifications.SENDER_EMAIL);
            notification.name = json.getString(Notifications.SENDER_NAME);
            notification.photoUrl = json.getString(Notifications.SENDER_PHOTO_URL);
            notification.message = json.getString(Notifications.MESSAGE);
            notification.type = json.getString(Notifications.TYPE);
            notification.latitude = json.getString(Notifications.LATITUDE);
            notification.longitude = json.getString(Notifications.LONGITUDE);
            notification.timeStamp = json.getLong(Notifications.TIME_STAMP);
            notification.status = Notifications.UNREAD;
            NotificationsDao.insert(notification);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d("GcmPushReceiver", "Error : " + e.getMessage());
        }
        return notification;
    }
}
