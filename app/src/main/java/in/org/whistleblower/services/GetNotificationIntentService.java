package in.org.whistleblower.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.interfaces.ResultListener;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.Notifications;
import in.org.whistleblower.dao.NotificationsDao;
import in.org.whistleblower.utilities.VolleyUtil;

public class GetNotificationIntentService extends IntentService
{
    private static final String TAG = "GetNotificationsService";

    public GetNotificationIntentService()
    {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        SharedPreferences preferences = WhistleBlower.getPreferences();
        Map<String, String> data = new HashMap<>();
        data.put(VolleyUtil.KEY_ACTION, "getNewNotifications");
        data.put(Notifications.RECEIVER_EMAIL, preferences.getString(Accounts.EMAIL, ""));
        VolleyUtil.sendGetData(data, new ResultListener<String>()
        {
            @Override
            public void onSuccess(final String result)
            {
                try
                {
                    JSONArray array = new JSONArray(result);
                    int totalNoOfNotifications = array.length();
                    Log.d(TAG, "Result : " + result + ", totalNoOfNotifications : " + totalNoOfNotifications);
                    for (int notificationIndex = 0; notificationIndex < totalNoOfNotifications; notificationIndex++)
                    {
                        Notifications notification = new Notifications();
                        JSONObject json = (JSONObject) array.get(notificationIndex);
                        notification.id = json.getLong(Notifications.ID);
                        notification.senderEmail = json.getString(Notifications.SENDER_EMAIL);
                        notification.senderName = json.getString(Notifications.SENDER_NAME);
                        notification.senderPhotoUrl = json.getString(Notifications.SENDER_PHOTO_URL);
                        notification.message = json.getString(Notifications.MESSAGE);
                        notification.type = json.getString(Notifications.TYPE);
                        notification.senderLatitude = json.getString(Notifications.SENDER_LATITUDE);
                        notification.senderLongitude = json.getString(Notifications.SENDER_LONGITUDE);
                        notification.timeStamp = json.getLong(Notifications.TIME_STAMP);
                        notification.status = json.getInt(Notifications.RECEIVER_STATUS);
                        NotificationsDao.insert(notification);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.d(TAG, "Error : " + e.getMessage());
                }
            }

            @Override
            public void onError(VolleyError error)
            {
                error.printStackTrace();
                Log.d(TAG, "Error : " + error.getMessage());
            }
        });
    }
}