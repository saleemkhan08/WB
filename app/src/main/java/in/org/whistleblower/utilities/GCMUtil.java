package in.org.whistleblower.utilities;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.interfaces.ResultListener;

public class GCMUtil
{
    public static final String SERVER_KEY = "AIzaSyAF1X4WKvN-5gBdpkAHpvC8ynLlyGmdwNU";
    public static final String SENDER_ID = "372242022464";
    public static final String GCM_REG_ID = "GCM_REG_ID";
    private static GoogleCloudMessaging gcmObj;
    static String regId = "";

    public static void registerInBackground(final AppCompatActivity mActivity)
    {
        if (MiscUtil.isGoogleServicesOk(mActivity))
        {
            new AsyncTask<Void, Void, String>()
            {
                @Override
                protected String doInBackground(Void... params)
                {
                    String msg = "";
                    try
                    {
                        if (gcmObj == null)
                        {
                            gcmObj = GoogleCloudMessaging.getInstance(WhistleBlower.getAppContext());
                        }
                        regId = gcmObj.register(SENDER_ID);
                        msg = "Registration ID :" + regId;

                    }
                    catch (IOException ex)
                    {
                        msg = "Error :" + ex.getMessage();
                    }
                    return msg;
                }

                @Override
                protected void onPostExecute(String msg)
                {
                    if (!TextUtils.isEmpty(regId))
                    {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
                        prefs.edit().putString(GCM_REG_ID, regId).commit();
                        String googleId = prefs.getString(Accounts.GOOGLE_ID, "");

                        Map<String, String> postData = new HashMap<>();
                        postData.put("action", "updateGcmId");
                        postData.put("gcmId", regId);
                        postData.put(Accounts.GOOGLE_ID, googleId);
                        VolleyUtil.sendPostData(postData, new ResultListener<String>()
                        {
                            @Override
                            public void onSuccess(String result)
                            {
                                Toast.makeText(mActivity, "Registred", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(VolleyError error)
                            {
                                Toast.makeText(mActivity, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                        Toast.makeText(
                                WhistleBlower.getAppContext(),
                                "Registered with GCM Server successfully.\n\n"
                                        + msg, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(mActivity,
                                "Reg ID Creation Failed.\n\nEither you haven't enabled Internet or GCM server is busy right now. Make sure you enabled Internet and try registering again after some time."
                                        + msg, Toast.LENGTH_LONG).show();
                    }
                }
            }.execute();
        }
    }
}
