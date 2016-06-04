package in.org.whistleblower.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.VolleyError;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import in.org.whistleblower.R;
import in.org.whistleblower.interfaces.ResultListener;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.receivers.NotificationActionReceiver;
import in.org.whistleblower.utilities.VolleyUtil;

public class RegistrationIntentService extends IntentService
{
    public static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    private SharedPreferences sharedPreferences;
    static Map<String, String> userAccount;
    public RegistrationIntentService()
    {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.d(TAG, "onHandleIntent");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try
        {
            if(intent.hasExtra(TAG))
            {
                int waitTime = intent.getIntExtra(TAG, 0);
                Log.d(TAG, "onHandleIntent waitTime : "+waitTime);
                Thread.sleep( waitTime* 500);
            }
            register();
        }
        catch (Exception e)
        {
            Log.d(TAG, "onHandleIntent Exception : " + e.getMessage());
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
            restartRegistration();
        }
    }

    private void register() throws Exception
    {
        Log.d(TAG, "register");
        InstanceID instanceID = InstanceID.getInstance(this);
        String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

        Log.d(TAG, token);
        userAccount = new HashMap<>();
        userAccount.put(Accounts.EMAIL, sharedPreferences.getString(Accounts.EMAIL, "saleemkhan08@gmail.com"));
        userAccount.put(Accounts.NAME, sharedPreferences.getString(Accounts.NAME, "Saleem Khan"));
        userAccount.put(Accounts.GOOGLE_ID, sharedPreferences.getString(Accounts.GOOGLE_ID, "111955688396506807880"));
        userAccount.put(Accounts.PHOTO_URL, sharedPreferences.getString(Accounts.PHOTO_URL, ""));
        userAccount.put(Accounts.GCM_ID, token);
        userAccount.put(VolleyUtil.KEY_ACTION, "addAccount");
        sendRegistrationToServer();
        subscribeTopics(token);
    }

    private void restartRegistration()
    {
        Log.d(TAG, "restartRegistration");
        Intent intent = new Intent(this, NotificationActionReceiver.class);
        intent.putExtra(NotificationActionReceiver.NOTIFICATION_ACTION, TAG);
        sendBroadcast(intent);
    }

    private void sendRegistrationToServer()
    {
        Log.d(TAG, "sendRegistrationToServer");
        VolleyUtil.sendPostData(userAccount, new ResultListener<String>()
        {
            @Override
            public void onSuccess(String result)
            {
                Log.d(TAG, "sendRegistrationToServer : success");
                PreferenceManager.getDefaultSharedPreferences(RegistrationIntentService.this)
                        .edit()
                        .putString(Accounts.GCM_ID, userAccount.get(Accounts.GCM_ID))
                        .putBoolean(SENT_TOKEN_TO_SERVER, true)
                        .apply();
            }

            @Override
            public void onError(VolleyError error)
            {
                Log.d(TAG, "sendRegistrationToServer : "+error.getMessage());
                sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
                restartRegistration();
            }
        });

    }

    private void subscribeTopics(String token) throws IOException
    {
        Log.d(TAG, "subscribeTopics");
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS)
        {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
}