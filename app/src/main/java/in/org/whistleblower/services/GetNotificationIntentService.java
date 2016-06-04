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
import in.org.whistleblower.models.NotificationsDao;
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
                final NotificationsDao dao = new NotificationsDao();
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