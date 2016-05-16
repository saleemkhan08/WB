package in.org.whistleblower.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

public class LocationProviderChangedReceiver extends BroadcastReceiver
{
    public LocationProviderChangedReceiver()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        boolean anyLocationProv = false;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        anyLocationProv |= locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        anyLocationProv |=  locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.i("Saleem", "Location service status" + anyLocationProv);
    }
}
