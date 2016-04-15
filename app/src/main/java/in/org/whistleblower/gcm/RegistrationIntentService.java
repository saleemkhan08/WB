package in.org.whistleblower.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.VolleyError;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import in.org.whistleblower.R;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.interfaces.ResultListener;
import in.org.whistleblower.utilities.VolleyUtil;

public class RegistrationIntentService extends IntentService
{
    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String REGISTRATION_ERROR = "REGISTRATION_ERROR";
    private SharedPreferences sharedPreferences;

    public RegistrationIntentService()
    {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try
        {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Log.d("Token", token);
            Map<String, String> userAccount = new HashMap<>();
            userAccount.put(Accounts.EMAIL, sharedPreferences.getString(Accounts.EMAIL, "saleemkhan08@gmail.com"));
            userAccount.put(Accounts.NAME, sharedPreferences.getString(Accounts.NAME, "Saleem Khan"));
            userAccount.put(Accounts.GOOGLE_ID, sharedPreferences.getString(Accounts.GOOGLE_ID, "111955688396506807880"));
            userAccount.put(Accounts.PHOTO_URL, sharedPreferences.getString(Accounts.PHOTO_URL, ""));
            userAccount.put(Accounts.GCM_ID, token);
            userAccount.put(VolleyUtil.KEY_ACTION, "addAccount");
            sendRegistrationToServer(userAccount);
            subscribeTopics(token);
        }
        catch (Exception e)
        {
            Log.d(TAG, "Failed to complete token refresh", e);
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    private void sendRegistrationToServer(final Map<String, String> userAccount)
    {
        final Intent registrationComplete = new Intent(REGISTRATION_COMPLETE);
        VolleyUtil.sendPostData(userAccount, new ResultListener<String>()
        {
            @Override
            public void onSuccess(String result)
            {
                PreferenceManager.getDefaultSharedPreferences(RegistrationIntentService.this)
                        .edit()
                        .putString(Accounts.GCM_ID, userAccount.get(Accounts.GCM_ID))
                        .putBoolean(SENT_TOKEN_TO_SERVER, true)
                        .apply();
                registrationComplete.putExtra(REGISTRATION_ERROR, false);
                LocalBroadcastManager.getInstance(RegistrationIntentService.this).sendBroadcast(registrationComplete);
            }

            @Override
            public void onError(VolleyError error)
            {
                sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();

                registrationComplete.putExtra(REGISTRATION_ERROR, true);
                LocalBroadcastManager.getInstance(RegistrationIntentService.this).sendBroadcast(registrationComplete);
            }
        });

    }

    private void subscribeTopics(String token) throws IOException
    {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS)
        {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
}