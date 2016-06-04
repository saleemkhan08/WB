package in.org.whistleblower.gcm;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONArray;
import org.json.JSONObject;

import in.org.whistleblower.models.Notifications;
import in.org.whistleblower.models.NotificationsDao;

public class MyGcmPushReceiver extends GcmListenerService
{
    private static final String TAG = MyGcmPushReceiver.class.getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle bundle)
    {
        Log.d("Saleem", "from : "+from+", bundle : "+bundle);

        switch(bundle.getString("title"))
        {
            case "NotifyLocation" :
        }

    }

    private void saveNotificationToDatabase(String result)
    {
        NotificationsDao dao = new NotificationsDao();
        try
        {
            JSONArray array = new JSONArray(result);
            int totalNoOfNotifications = array.length();
            for (int notificationIndex = 0; notificationIndex < totalNoOfNotifications; notificationIndex++)
            {
                Notifications notification = new Notifications();
                JSONObject json = (JSONObject) array.get(notificationIndex);
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
                dao.insert(notification);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d("ToastMsg", "Error occurred deleting from Data base : " + e.getMessage());
        }
    }
}
