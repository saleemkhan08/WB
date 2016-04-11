package in.org.whistleblower.gcm;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public class MyGcmPushReceiver extends GcmListenerService
{
    private static final String TAG = MyGcmPushReceiver.class.getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle bundle)
    {
        Log.d("Saleem", "from : "+from+", bundle : "+bundle);
    }
}
