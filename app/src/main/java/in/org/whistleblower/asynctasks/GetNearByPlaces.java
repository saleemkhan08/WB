package in.org.whistleblower.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import in.org.whistleblower.R;
import in.org.whistleblower.interfaces.PlacesResultListener;
import in.org.whistleblower.models.Places;
import in.org.whistleblower.utilities.MiscUtil;

public class GetNearByPlaces extends AsyncTask<Void, Void, List<HashMap<String, String>>>
{
    private static final int PROXIMITY_RADIUS = 10000;
    Context mContext;
    private LatLng mLatLng;
    PlacesResultListener mListener;
    public GetNearByPlaces(Context context, LatLng latLng, PlacesResultListener listener)
    {
        mContext = context;
        mLatLng = latLng;
        mListener = listener;
    }

    @Override
    protected List<HashMap<String, String>> doInBackground(Void... params)
    {
        if(!isCancelled())
        {
            if (MiscUtil.isConnected(mContext))
            {
                try
                {
                    String server_key = mContext.getResources().getString(R.string.server_key);
                    StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
                    googlePlacesUrl.append("location=" + mLatLng.latitude + "," + mLatLng.longitude);
                    googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
                    googlePlacesUrl.append("&key=" + server_key);
                    Http http = new Http();
                    return parsePlaces(http.read(googlePlacesUrl.toString()));
                }
                catch (Exception e)
                {
                    Log.d("Google Place Read Task", e.toString());
                }
            }
        }
        return null;
    }

    private List<HashMap<String, String>> parsePlaces(String googlePlacesData)
    {
        List<HashMap<String, String>> googlePlacesList = null;
        Places placeJsonParser = new Places();
        try
        {
            googlePlacesList = placeJsonParser.parse(new JSONObject(googlePlacesData));
        }
        catch (Exception e)
        {
            Log.d("Exception", e.toString());
        }
        return googlePlacesList;
    }

    @Override
    protected void onPostExecute(List<HashMap<String, String>> result)
    {
        mListener.onListObtained(result);
    }
}