package in.org.whistleblower.asynctasks;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import in.org.whistleblower.interfaces.PlacesResultListener;
import in.org.whistleblower.models.Places;

public class PlacesDisplayTask extends AsyncTask<Object, Integer, List<HashMap<String, String>>>
{
    JSONObject googlePlacesJson;
    PlacesResultListener listener;
    @Override
    protected List<HashMap<String, String>> doInBackground(Object... inputObj)
    {
        List<HashMap<String, String>> googlePlacesList = null;
        Places placeJsonParser = new Places();
        try
        {
            googlePlacesJson = new JSONObject((String)inputObj[0]);
            listener = (PlacesResultListener) inputObj[1];
            googlePlacesList = placeJsonParser.parse(googlePlacesJson);
        }
        catch (Exception e)
        {
            Log.d("Exception", e.toString());
        }
        return googlePlacesList;
    }

    @Override
    protected void onPostExecute(List<HashMap<String, String>> list)
    {
        listener.onListObtained(list);
    }
}