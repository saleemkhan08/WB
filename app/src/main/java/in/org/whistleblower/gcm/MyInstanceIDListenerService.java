package in.org.whistleblower.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

public class MyInstanceIDListenerService extends InstanceIDListenerService
{
    private static final String TAG = MyInstanceIDListenerService.class.getSimpleName();
    @Override
    public void onTokenRefresh()
    {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
