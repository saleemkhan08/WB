package in.org.whistleblower;

import android.app.IntentService;
import android.content.Intent;

public class LocationUpdateService extends IntentService
{
    public LocationUpdateService()
    {
        super("LocationUpdateService");
    }
    public static final int REQUEST_CODE_LOCATION_PERMISSION = 0x91;
    public static final int REQUEST_CODE_LOCATION_SETTINGS = 0x92;
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
