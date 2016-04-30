package in.org.whistleblower.utilities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
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

import in.org.whistleblower.AddIssueActivity;
import in.org.whistleblower.IssueActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.interfaces.ResultListener;
import in.org.whistleblower.models.Issue;
import in.org.whistleblower.models.IssuesDao;

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
    NotificationCompat.Builder mBuilder;
    //RemoteViews mRemoteViews;
    long totalSize;
    Context mContext;
    UploadAsyncTask uploadAsyncTask;
    Issue mIssue;

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
        mIssue = new Issue();

        mIssue = intent.getParcelableExtra(AddIssueActivity.ISSUE_DATA);

        //Creating Cancel Upload Intent
        Intent cancelIntent = new Intent(mContext, StopRetryReceiver.class);
        cancelIntent.putExtra(CANCEL, true);
        cancelPendingIntent = PendingIntent.getBroadcast(mContext, (int) System.currentTimeMillis(), cancelIntent, 0);

        //Creating Retry Upload Intent
        Intent retryIntent = new Intent(mContext, StopRetryReceiver.class);
        retryIntent.putExtra(AddIssueActivity.ISSUE_DATA, mIssue);
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

                File sourceFile = new File(mIssue.imgUrl);

                //TODO allow multiple file uploading
                entity.addPart(FILE_NAME, new FileBody(sourceFile));
                entity.addPart(ResultListener.ACTION, new StringBody(ACTION_ADD_ISSUE));
                //TODO allow multiple file uploading
                entity.addPart(IssuesDao.NO_OF_IMAGES, new StringBody("1"));
                entity.addPart(IssuesDao.USER_ID, new StringBody(mIssue.userId));
                entity.addPart(IssuesDao.USER_DP_URL, new StringBody(mIssue.userDpUrl));
                entity.addPart(IssuesDao.USERNAME, new StringBody(mIssue.username));
                entity.addPart(IssuesDao.DESCRIPTION, new StringBody(mIssue.description));
                entity.addPart(IssuesDao.AREA_TYPE, new StringBody(mIssue.areaType));
                entity.addPart(IssuesDao.RADIUS, new StringBody(mIssue.radius+""));
                entity.addPart(IssuesDao.LATITUDE, new StringBody(mIssue.latitude));
                entity.addPart(IssuesDao.LONGITUDE, new StringBody(mIssue.longitude));

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
