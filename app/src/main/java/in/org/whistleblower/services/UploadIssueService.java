package in.org.whistleblower.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;

import in.org.whistleblower.IssueActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.fragments.MapFragment;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.dao.IssuesDao;
import in.org.whistleblower.interfaces.ResultListener;
import in.org.whistleblower.utilities.AndroidMultiPartEntity;
import in.org.whistleblower.receivers.StopRetryReceiver;

public class UploadIssueService extends IntentService
{
    public static final String ERROR = "ERROR";
    public static final String RETRY = "RETRY";
    public static final String CANCEL = "CANCEL";
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private long totalSize;
    public static final int NOTIFICATION_ID = 805;
    private SharedPreferences preferences;
    private static String mImageUri, mAreaType, mDescription;
    private Handler handler;
    static PendingIntent cancelPendingIntent, retryPendingIntent;

    public UploadIssueService()
    {
        super("UploadIssueService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        handler = new Handler();
        return super.onStartCommand(intent, flags, startId);
    }

    private void toast(final String msg)
    {
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Intent cancelIntent = new Intent(UploadIssueService.this, StopRetryReceiver.class);
        cancelIntent.putExtra(CANCEL, true);
        cancelPendingIntent = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), cancelIntent, 0);

        Intent retryIntent = new Intent(UploadIssueService.this, StopRetryReceiver.class);
        retryIntent.putExtra(IssuesDao.IMAGE_LOCAL_URI, mImageUri);
        retryIntent.putExtra(IssuesDao.AREA_TYPE, mAreaType);
        retryIntent.putExtra(IssuesDao.DESCRIPTION, mDescription);
        retryIntent.putExtra(RETRY, true);

        retryPendingIntent = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), retryIntent, 0);

        mImageUri = intent.getStringExtra(IssuesDao.IMAGE_LOCAL_URI);
        mAreaType = intent.getStringExtra(IssuesDao.AREA_TYPE);
        mDescription = intent.getStringExtra(IssuesDao.DESCRIPTION);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Posting Issue")
                .setSmallIcon(R.drawable.bullhorn_white)
                .setProgress(100, 0, false)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelPendingIntent)
                .addAction(android.R.drawable.ic_menu_revert, "Retry", retryPendingIntent)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentText(0 + "% Completed");

        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
        String response = upload();
        if (response.trim().equalsIgnoreCase(ERROR))
        {
            retryIntent = new Intent(UploadIssueService.this, UploadIssueService.class);
            retryIntent.putExtra(IssuesDao.IMAGE_LOCAL_URI, mImageUri);
            retryIntent.putExtra(IssuesDao.AREA_TYPE, mAreaType);
            retryIntent.putExtra(IssuesDao.DESCRIPTION, mDescription);
            retryIntent.putExtra(RETRY, true);

            PendingIntent pIntent = PendingIntent.getService(this, (int) System.currentTimeMillis(), retryIntent, 0);

            mBuilder.setOngoing(false)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .setProgress(0, 0, false)
                    .setContentTitle("Could Not Post The Issue")
                    .setContentText("Click on notification to retry.");
        }
        else
        {
            Intent openIntent = new Intent(UploadIssueService.this, IssueActivity.class);
            //TODO add extra to identify the issue.
            PendingIntent pIntent = PendingIntent.getActivity(UploadIssueService.this, (int) System.currentTimeMillis(), openIntent, 0);
            mBuilder.setOngoing(false)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .setProgress(0, 0, false)
                    .setContentTitle("Issue Posted.")
                    .setContentText("Click on notification to open it.");
        }
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

    }

    private String upload()
    {
        String responseString;
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(ResultListener.URL);
        try
        {
            AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                    new AndroidMultiPartEntity.ProgressListener()
                    {
                        @Override
                        public void transferred(long num)
                        {
                            int progress = ((int) ((num / (float) totalSize) * 100));
                            mBuilder.setProgress(100, progress, false)
                                    .setContentTitle("Posting Issue : " + progress + "%");
                            mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
                        }
                    });

            File sourceFile = new File(mImageUri);
            entity.addPart("image", new FileBody(sourceFile));
            entity.addPart("action", new StringBody("addIssue"));
            entity.addPart(IssuesDao.AREA_TYPE, new StringBody(mAreaType));

            boolean anonymous = preferences.getBoolean(IssuesDao.ANONYMOUS, false);
            String username = preferences.getString(Accounts.NAME, "Anonymous");
            if (anonymous)
            {
                username = "Anonymous";
            }
            entity.addPart(IssuesDao.ANONYMOUS, new StringBody(anonymous + ""));
            entity.addPart(IssuesDao.USERNAME, new StringBody(username));
            entity.addPart(IssuesDao.USER_DP_URL, new StringBody(preferences.getString(Accounts.PHOTO_URL, "")));
            entity.addPart(IssuesDao.USER_ID, new StringBody(preferences.getString(Accounts.GOOGLE_ID, "")));
            entity.addPart(IssuesDao.DESCRIPTION, new StringBody(mDescription));
            entity.addPart(IssuesDao.LATITUDE, new StringBody("" + preferences.getFloat(MapFragment.LATITUDE, (float) 0)));
            entity.addPart(IssuesDao.LONGITUDE, new StringBody("" + preferences.getFloat(MapFragment.LONGITUDE, (float) 0)));

            totalSize = entity.getContentLength();
            httppost.setEntity(entity);

            HttpResponse response = httpclient.execute(httppost);
            HttpEntity r_entity = response.getEntity();

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
                responseString = EntityUtils.toString(r_entity);
                Log.d("responseString", "Response : " + responseString);
            }
            else
            {
                responseString = ERROR;
            }
        }
        catch (Exception e)
        {
            responseString = ERROR;
            Log.d("responseString", "Response : " + e.getMessage());
        }
        return responseString;
    }
}