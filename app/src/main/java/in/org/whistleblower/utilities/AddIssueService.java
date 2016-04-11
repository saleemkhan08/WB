package in.org.whistleblower.utilities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

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
import in.org.whistleblower.models.IssuesDao;
import in.org.whistleblower.storage.ResultListener;

public class AddIssueService extends Service
{
    private static final String ACTION_ADD_ISSUE = "addIssue";
    private static final String FILE_NAME = "image";
    NotificationManager mNotifyManager;
    public static final int NOTIFICATION_ID = 805;
    public static final String ERROR = "ERROR";
    public static final String RETRY = "RETRY";
    public static final String CANCEL = "CANCEL";
    PendingIntent cancelPendingIntent, retryPendingIntent;
    String mImageUri, mAreaType, mDescription;
    NotificationCompat.Builder mBuilder;
    //RemoteViews mRemoteViews;
    long totalSize;
    Context mContext;
    SharedPreferences preferences;
    UploadAsyncTask uploadAsyncTask;

    public AddIssueService()
    {
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mContext = this;
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        //Obtaining data from intent
        mImageUri = intent.getStringExtra(IssuesDao.IMAGE_LOCAL_URI);
        mAreaType = intent.getStringExtra(IssuesDao.AREA_TYPE);
        mDescription = intent.getStringExtra(IssuesDao.DESCRIPTION);

        //Creating Cancel Upload Intent
        Intent cancelIntent = new Intent(mContext, StopRetryReceiver.class);
        cancelIntent.putExtra(CANCEL, true);
        cancelPendingIntent = PendingIntent.getBroadcast(mContext, (int) System.currentTimeMillis(), cancelIntent, 0);

        //Creating Retry Upload Intent
        Intent retryIntent = new Intent(mContext, StopRetryReceiver.class);
        retryIntent.putExtra(IssuesDao.IMAGE_LOCAL_URI, mImageUri);
        retryIntent.putExtra(IssuesDao.AREA_TYPE, mAreaType);
        retryIntent.putExtra(IssuesDao.DESCRIPTION, mDescription);
        retryIntent.putExtra(RETRY, true);
        retryPendingIntent = PendingIntent.getBroadcast(mContext, (int) System.currentTimeMillis(), retryIntent, 0);
        mBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.bullhorn)
                .setOngoing(true)
                .setContentTitle("Posting the Issue.")
                .setContentText("0% Completed")
                .setProgress(100, 0, false)
                .setAutoCancel(false)
                .addAction(R.mipmap.retry_primary_dark, RETRY, retryPendingIntent)
                .addAction(R.mipmap.cross_accent, CANCEL, cancelPendingIntent);

        mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
        Log.d("AddIssueService", "NOTIFICATION Shown");
        uploadAsyncTask = new UploadAsyncTask();
        uploadAsyncTask.execute();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class UploadAsyncTask extends AsyncTask<Void, Void, String>
    {
        @Override
        protected String doInBackground(Void... params)
        {
            return upload();
        }

        @Override
        protected void onPostExecute(String response)
        {
            Log.d("ServerResponse", response);
            if (response.trim().equalsIgnoreCase(ERROR))
            {
                mBuilder.setOngoing(false)
                        .setSmallIcon(R.drawable.bullhorn)
                        .setContentTitle("Posting Issue Failed")
                        .setContentText("Click on retry to post again")
                        .setProgress(0, 0, false)
                        .setAutoCancel(true);
            }
            else
            {
                Intent openIntent = new Intent(mContext, IssueActivity.class);
                //TODO add extra to identify the issue.
                PendingIntent pIntent = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), openIntent, 0);
                mBuilder = new NotificationCompat.Builder(mContext);
                mBuilder.setOngoing(false)
                        .setSmallIcon(R.drawable.bullhorn)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true)
                        .setContentTitle("Issue Posted.")
                        .setContentText("Click on notification to open it.");
            }
            mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
            AddIssueService.this.stopSelf();
        }

        public String upload()
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
                                if (!uploadAsyncTask.isCancelled())
                                {
                                    int progress = ((int) ((num / (float) totalSize) * 100));
                                    if (progress % 10 == 0)
                                    {
                                        mBuilder.setProgress(100, progress, false);
                                        mBuilder.setContentText(progress + "% Completed");
                                        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
                                    }
                                }
                            }
                        });

                boolean anonymous = preferences.getBoolean(IssuesDao.ANONYMOUS, false);
                String username = preferences.getString(Accounts.NAME, "Anonymous");
                String userDpURL = preferences.getString(Accounts.PHOTO_URL, "");
                if (anonymous)
                {
                    userDpURL = "";
                    username = "Anonymous";

                }
                File sourceFile = new File(mImageUri);

                //TODO allow multiple file uploading
                entity.addPart(FILE_NAME, new FileBody(sourceFile));
                entity.addPart(ResultListener.ACTION, new StringBody(ACTION_ADD_ISSUE));
                //TODO allow multiple file uploading
                entity.addPart(IssuesDao.NO_OF_IMAGES, new StringBody("1"));
                entity.addPart(IssuesDao.USER_ID, new StringBody(preferences.getString(Accounts.GOOGLE_ID, "")));
                entity.addPart(IssuesDao.USER_DP_URL, new StringBody(userDpURL));
                entity.addPart(IssuesDao.USERNAME, new StringBody(username));
                entity.addPart(IssuesDao.DESCRIPTION, new StringBody(mDescription));
                entity.addPart(IssuesDao.AREA_TYPE, new StringBody(mAreaType));
                entity.addPart(IssuesDao.RADIUS, new StringBody(preferences.getInt(IssuesDao.RADIUS, 1) + ""));
                entity.addPart(IssuesDao.LATITUDE, new StringBody("" + preferences.getFloat(MapFragment.LATITUDE, (float) 0)));
                entity.addPart(IssuesDao.LONGITUDE, new StringBody("" + preferences.getFloat(MapFragment.LONGITUDE, (float) 0)));

                totalSize = entity.getContentLength();
                Log.d("AddIssueService", "totalSize : " + totalSize);
                httppost.setEntity(entity);

                HttpResponse response = httpclient.execute(httppost);
                Log.d("ServerResponse", "HttpResponse : " + response);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200)
                {
                    responseString = EntityUtils.toString(r_entity);
                    Log.d("AddIssueService", "statusCode : " + statusCode + ", Response : " + responseString);
                }
                else
                {
                    responseString = ERROR;
                }
                Log.d("AddIssueService", "statusCode : " + statusCode + ", Response : " + responseString);
            }
            catch (Exception e)
            {
                responseString = ERROR;
                e.printStackTrace();
                Log.d("AddIssueService", "Exception : " + e.getMessage());
            }
            return responseString;
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (uploadAsyncTask != null && uploadAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
        {
            uploadAsyncTask.cancel(true);
            Log.d("StopRetryReceiver", "Task Running");
        }
        else
        {
            Log.d("StopRetryReceiver", "Task Not Running");
        }
    }
}
