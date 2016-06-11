package in.org.whistleblower.gcm;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONObject;

import in.org.whistleblower.models.Notifications;
import in.org.whistleblower.dao.NotificationsDao;
import in.org.whistleblower.utilities.NavigationUtil;
import in.org.whistleblower.utilities.NotificationsUtil;

public class GcmPushReceiver extends GcmListenerService
{
    private static final String TAG = GcmPushReceiver.class.getSimpleName();
    Notifications mNotification;

    @Override
    public void onMessageReceived(String from, Bundle bundle)
    {
        Log.d("GcmPushReceiver", "from : " + from + ", bundle : " + bundle);
        mNotification = saveNotificationToDatabase(bundle.getString("message"));
        switch (bundle.getString("title"))
        {
            case NavigationUtil.NOTIFY_LOCATION_RECEIVING_FRAGMENT_TAG:
                Log.d("GcmPushReceiver", "type : " + bundle.getString("title"));
                NotificationsUtil.showReceivingNotifyLocation(mNotification);
                break;
        }

    }

    private Notifications saveNotificationToDatabase(String result)
    {
        Notifications notification = null;
        try
        {
            notification = new Notifications();
            JSONObject json = new JSONObject(result);
            notification.id = json.getLong(Notifications.ID);
            notification.userEmail = json.getString(Notifications.SENDER_EMAIL);
            notification.name = json.getString(Notifications.SENDER_NAME);
            notification.photoUrl = json.getString(Notifications.SENDER_PHOTO_URL);
            notification.message = json.getString(Notifications.MESSAGE);
            notification.type = json.getString(Notifications.TYPE);
            notification.latitude = json.getString(Notifications.LATITUDE);
            notification.longitude = json.getString(Notifications.LONGITUDE);
            notification.timeStamp = json.getLong(Notifications.TIME_STAMP);
            notification.status = json.getInt(Notifications.STATUS);
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
