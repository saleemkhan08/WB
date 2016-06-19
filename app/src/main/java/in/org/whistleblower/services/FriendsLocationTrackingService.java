package in.org.whistleblower.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.VolleyError;

import java.util.HashMap;
import java.util.Map;

import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.interfaces.ResultListener;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.NotificationData;
import in.org.whistleblower.receivers.NotificationActionReceiver;
import in.org.whistleblower.utilities.NavigationUtil;
import in.org.whistleblower.utilities.NotificationsUtil;
import in.org.whistleblower.utilities.VolleyUtil;

public class FriendsLocationTrackingService extends Service implements ResultListener<String>
{
    public static final String NO_OF_FRIENDS_SHARING_LOCATION = "noOfFriendsSharingLocation";
    public static final String LOCATION_RECEIVING_FREQUENCY = "locationUpdateFrequency";
    private static final String TAG = "FrndsLocTrackinService";
    SharedPreferences mPreferences = WhistleBlower.getPreferences();

    public FriendsLocationTrackingService()
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Map<String, String> receivingAcknowledgement = new HashMap<>();
        receivingAcknowledgement.put("receiverEmail", mPreferences.getString(Accounts.EMAIL, ""));
        receivingAcknowledgement.put("action", "receivingLocation");
        VolleyUtil.sendPostData(receivingAcknowledgement, this);
        return START_REDELIVER_INTENT;
    }


    private Handler mHandler;


    Runnable mStatusChecker = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                Log.d(TAG, "startingTask");
                new LocationPollingTask().execute();
            }
            finally
            {
                mHandler.postDelayed(mStatusChecker, mPreferences.getInt(LOCATION_RECEIVING_FREQUENCY, 5000));
            }
        }
    };

    void startRepeatingTask()
    {
        mStatusChecker.run();
    }

    void stopRepeatingTask()
    {
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    public void onSuccess(String result)
    {
        // your code here
        mHandler = new Handler();
        startRepeatingTask();
    }

    @Override
    public void onError(VolleyError error)
    {

    }

    private class LocationPollingTask extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... params)
        {
            //while (mPreferences.getInt(NO_OF_FRIENDS_SHARING_LOCATION, 0))
            return null;
        }
    }

    private void showLocationSharingNotification(int count)
    {
        NotificationsUtil.removeNotification(NotificationActionReceiver.NOTIFICATION_ID_RECEIVING_SHARED_LOCATION);
        NotificationData notification = new NotificationData();
        notification.contentTitle = (count) + " of your friends are sharing location.";

        notification.contentText = "Click to view all.";
        notification.contentIntentTag = NavigationUtil.FRAGMENT_TAG_RECEIVING_SHARED_LOCATION;
        NotificationsUtil.showNotification(notification, null);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        //TODO Disconnect from Database2
    }
}
