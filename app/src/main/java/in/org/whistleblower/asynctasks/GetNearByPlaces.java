package in.org.whistleblower.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import in.org.whistleblower.R;

public class GetNearByPlaces extends AsyncTask<Object, Integer, String>
{
    private static final int PROXIMITY_RADIUS = 10000;
    String googlePlacesData = null;
    Context context;
    private LatLng latlng;

    @Override
    protected String doInBackground(Object... inputObj)
    {
        try
        {
            context = (Context)inputObj[0];
            latlng = (LatLng)inputObj[1];
            String server_key = context.getResources().getString(R.string.server_key);
            StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            googlePlacesUrl.append("location=" + latlng.latitude + "," + latlng.longitude);
            googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
            googlePlacesUrl.append("&key=" + server_key);
            Http http = new Http();
            googlePlacesData = http.read(googlePlacesUrl.toString());
        }
        catch (Exception e)
        {
            Log.d("Google Place Read Task", e.toString());
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String result)
    {
        new PlacesDisplayTask().execute(result, context);
    }
}