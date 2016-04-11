package in.org.whistleblower.utilities;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;

import in.org.whistleblower.R;
import in.org.whistleblower.storage.ResultListener;

public class UploadImage extends AsyncTask<Void, Integer, String>
{
    private final NotificationManager mNotifyManager;
    private final NotificationCompat.Builder mBuilder;
    AppCompatActivity mActivity;
    String mImageUri;
    private long totalSize;
    int id = 1;

    public UploadImage(AppCompatActivity activity, String imageUri)
    {
        mActivity = activity;
        mImageUri = imageUri;
        mNotifyManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mActivity);
        mBuilder.setContentTitle("Posting The Issue...")
                .setContentText("0%")
                .setSmallIcon(R.drawable.bullhorn_white);
        mBuilder.setAutoCancel(false);
        mBuilder.setOngoing(true);
        mBuilder.setProgress(100, 0, false);
        mNotifyManager.notify(id, mBuilder.build());
    }

    @Override
    protected String doInBackground(Void... params)
    {
        String responseString = null;

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
                            publishProgress((int) ((num / (float) totalSize) * 100));
                        }
                    });

            File sourceFile = new File(mImageUri);
            entity.addPart("image", new FileBody(sourceFile));
            entity.addPart("action", new StringBody("uploadFile"));

            totalSize = entity.getContentLength();
            httppost.setEntity(entity);

            HttpResponse response = httpclient.execute(httppost);
            HttpEntity r_entity = response.getEntity();

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
                responseString = EntityUtils.toString(r_entity);
            }
            else
            {
                responseString = "Error occurred! Http Status Code: " + statusCode;
            }
        }
        catch (ClientProtocolException e)
        {
            responseString = e.toString();
        }
        catch (IOException e)
        {
            responseString = e.toString();
        }

        return responseString;
    }

    @Override
    protected void onPostExecute(String aVoid)
    {

    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {
        mBuilder.setProgress(100, values[0], false)
                .setContentText(values[0] + "%");
        mNotifyManager.notify(id, mBuilder.build());
    }
}
