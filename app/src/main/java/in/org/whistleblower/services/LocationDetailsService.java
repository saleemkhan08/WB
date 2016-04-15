package in.org.whistleblower.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class LocationDetailsService extends Service
{
    private final IBinder iBinder = new LocalBinder();
    public static final String requestPlacesUrl =
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=AIzaSyDEtuWEFj1euqWLrG7VMc6iQWwaPaobbqs&location=12.9667,77.5667";
    public LocationDetailsService()
    {
    }

    public class LocalBinder extends Binder
    {
        public LocationDetailsService getService()
        {
            return LocationDetailsService.this;
        }
    }



    @Override
    public IBinder onBind(Intent intent)
    {
        return iBinder;
    }

}
