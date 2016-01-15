package in.org.whistleblower;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LocationTrackingService extends Service
{
    public LocationTrackingService()
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
