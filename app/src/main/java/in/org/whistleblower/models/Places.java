package in.org.whistleblower.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Places
{

    public List<HashMap<String, String>> parse(JSONObject jsonObject)
    {
        JSONArray jsonArray = null;
        try
        {
            jsonArray = jsonObject.getJSONArray("results");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return getPlaces(jsonArray);
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray)
    {
        int placesCount = jsonArray.length();
        List<HashMap<String, String>> placesList = new ArrayList<>();
        HashMap<String, String> placeMap = null;

        for (int i = 0; i < placesCount; i++)
        {
            try
            {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placesList.add(placeMap);

            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        return placesList;
    }

    private HashMap<String, String> getPlace(JSONObject googlePlaceJson)
    {
        HashMap<String, String> googlePlaceMap = new HashMap<>();
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String latitude = "";
        String longitude = "";
        String reference = "";
        String icon = "";
        try
        {
            if (!googlePlaceJson.isNull("name"))
            {
                placeName = googlePlaceJson.getString("name");
            }
            if (!googlePlaceJson.isNull("vicinity"))
            {
                vicinity = googlePlaceJson.getString("vicinity");
            }
            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference = googlePlaceJson.getString("reference");
            icon = googlePlaceJson.getString("icon");
            googlePlaceMap.put("place_name", placeName);
            googlePlaceMap.put("vicinity", vicinity);
            googlePlaceMap.put("lat", latitude);
            googlePlaceMap.put("lng", longitude);
            googlePlaceMap.put("reference", reference);
            googlePlaceMap.put("icon", icon);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return googlePlaceMap;
    }
}