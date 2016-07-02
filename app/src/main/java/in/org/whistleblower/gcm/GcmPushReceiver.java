package in.org.whistleblower.gcm;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.org.whistleblower.R;
import in.org.whistleblower.dao.NotificationsDao;
import in.org.whistleblower.models.NotificationData;
import in.org.whistleblower.models.Notifications;
import in.org.whistleblower.receivers.NotificationActionReceiver;
import in.org.whistleblower.utilities.NavigationUtil;
import in.org.whistleblower.utilities.NotificationsUtil;
import in.org.whistleblower.utilities.VolleyUtil;

public class GcmPushReceiver extends GcmListenerService
{
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
                    break;

                case Notifications.KEY_INITIATE_SHARE_LOCATION:

                    NotificationData data = new NotificationData();
                    data.notificationId = notification.id;
                    data.notification = notification;
                    data.contentTitle = notification.senderName;
                    data.largeIconUrl = notification.senderPhotoUrl;
                    data.contentIntentTag = Notifications.KEY_INITIATE_SHARE_LOCATION;
                    data.contentText = "Sharing Location";

                    data.action1IntentIcon = R.mipmap.check_primary_dark;
                    data.action1IntentTag = NotificationActionReceiver.START_RECEIVING_LOCATION;
                    data.action1IntentText = "Accept";

                    data.action2IntentIcon = R.mipmap.reject_primary_dark;
                    data.action2IntentTag = NotificationActionReceiver.NOTIFY_REJECTION_TO_SENDER;
                    data.action2IntentText = "Reject";
                    NotificationsUtil.showNotification(data);
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
            notification.senderEmail = json.getString(Notifications.SENDER_EMAIL);
            notification.senderName = json.getString(Notifications.SENDER_NAME);
            notification.senderPhotoUrl = json.getString(Notifications.SENDER_PHOTO_URL);
            notification.message = json.getString(Notifications.MESSAGE);
            notification.type = json.getString(Notifications.TYPE);
            notification.senderLatitude = json.getString(Notifications.SENDER_LATITUDE);
            notification.senderLongitude = json.getString(Notifications.SENDER_LONGITUDE);
            notification.timeStamp = json.getLong(Notifications.TIME_STAMP);
            notification.serverNotificationId = json.getLong(Notifications.SERVER_NOTIFICATION_ID);
            notification.status = Notifications.UNREAD;
            NotificationsDao.insert(notification);

            Map<String, String> data = new HashMap<>();
            data.put(Notifications.SERVER_NOTIFICATION_ID, notification.serverNotificationId + "");
            data.put(Notifications.RECEIVER_STATUS, notification.serverNotificationId + "");
            data.put(Notifications.SENDER_LATITUDE, notification.senderLatitude);
            data.put(Notifications.SENDER_LONGITUDE, notification.senderLongitude);
            data.put(VolleyUtil.KEY_ACTION, Notifications.UPDATE_NOTIFICATION_STATUS);

            VolleyUtil.sendPostData(data, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d("GcmPushReceiver", "Error : " + e.getMessage());
        }
        return notification;
    }
}
