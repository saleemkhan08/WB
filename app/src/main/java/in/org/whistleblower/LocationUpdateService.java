package in.org.whistleblower;

import android.app.IntentService;
import android.content.Intent;

public class LocationUpdateService extends IntentService
{
    public LocationUpdateService()
    {
        super("LocationUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (intent != null)
        {

        }
    }

    public void updateCurrentLocationOnMap()
    {

    }

    public void startLocationUpdates()
    {

    }
}
