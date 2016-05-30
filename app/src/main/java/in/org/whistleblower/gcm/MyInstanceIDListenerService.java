package in.org.whistleblower.gcm;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

import java.io.File;
import java.io.FileOutputStream;

public class MyInstanceIDListenerService extends InstanceIDListenerService
{
    @Override
    public void onTokenRefresh()
    {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
        new TokenLogsSavingTask().execute();
    }

    private class TokenLogsSavingTask extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... params)
        {
            Log.d("TokenLogsSavingTask", Environment.getExternalStorageDirectory().getAbsolutePath());
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/TokenLogsSavingTask.txt");
            try
            {
                if(file.exists() || file.createNewFile())
                {
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] data = "TokenRefreshed".getBytes();
                    fos.write(data);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.d("TokenLogsSavingTask", e.getMessage());
            }
            return null;
        }
    }
}
