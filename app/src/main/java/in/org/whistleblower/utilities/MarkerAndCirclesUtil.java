package in.org.whistleblower.utilities;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.adapters.PlaceAdapter;
import in.org.whistleblower.dao.FavPlacesDao;
import in.org.whistleblower.dao.IssuesDao;
import in.org.whistleblower.dao.LocationAlarmDao;
import in.org.whistleblower.dao.NotifyLocationDao;
import in.org.whistleblower.models.FavPlaces;
import in.org.whistleblower.models.Issue;
import in.org.whistleblower.models.LocationAlarm;
import in.org.whistleblower.models.MarkerAndCircle;
import in.org.whistleblower.models.NotifyLocation;
import in.org.whistleblower.singletons.Otto;

public class MarkerAndCirclesUtil
{

    public GoogleMap mGoogleMap;

    Map<String, MarkerAndCircle> mMarkerAndCircleMap;
    Map<String, Circle> circleMap;
    Map<String, Marker> imgMarkerMap;
    Map<String, Marker> bgMarkerMap;

    int mAccentColor, mRadiusColor;

    public MarkerAndCirclesUtil(GoogleMap googleMap, int accentColor, int radiusColor)
    {
        this.mGoogleMap = googleMap;
        Otto.register(this);
        mAccentColor = accentColor;
        mRadiusColor = radiusColor;

        mMarkerAndCircleMap = new HashMap<>();
        circleMap = new HashMap<>();
        imgMarkerMap = new HashMap<>();
        bgMarkerMap = new HashMap<>();

        mGoogleMap.clear();
        new MarkerAndCircleAddingTask().execute();
    }

    public class MarkerAndCircleAddingTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            getIssueMarkers();
            getAlarms();
            getNotifyLocations();
            getFavoritePlaces();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            for (String key: mMarkerAndCircleMap.keySet())
            {
                MarkerAndCircle markerAndCircle = mMarkerAndCircleMap.get(key);
                addMarkerAndCircle(markerAndCircle);
            }
        }
    }

    private void getIssueMarkers()
    {
        List<Issue> issues = IssuesDao.getList();
        for(Issue issue: issues)
        {
           mMarkerAndCircleMap.put(issue.issueId, getMarkerAndCircle(issue));
        }
    }

    private void getAlarms()
    {
        List<LocationAlarm> locationAlarms = LocationAlarmDao.getList();
        for(LocationAlarm alarm: locationAlarms)
        {
            mMarkerAndCircleMap.put(alarm.address, getMarkerAndCircle(alarm));
        }
    }

    private void getNotifyLocations()
    {
        List<NotifyLocation> notifyLocations = NotifyLocationDao.getList();
        for(NotifyLocation notifyLocation: notifyLocations)
        {
            mMarkerAndCircleMap.put(notifyLocation.receiverEmail, getMarkerAndCircle(notifyLocation));
        }
    }

    private void getFavoritePlaces()
    {
        List<FavPlaces> favPlaces = FavPlacesDao.getList();
        for(FavPlaces favPlace: favPlaces)
        {
            mMarkerAndCircleMap.put(favPlace.addressLine, getMarkerAndCircle(favPlace));
        }
    }

    public void addMarkerAndCircle(MarkerAndCircle markerAndCircle)
    {
        if(circleMap.containsKey(markerAndCircle.id))
        {
            removeMarkerAndCircle(markerAndCircle.id);
        }

        circleMap.put(markerAndCircle.id, mGoogleMap.addCircle(new CircleOptions()
                .radius(markerAndCircle.radius)
                .strokeWidth(2)
                .strokeColor(mAccentColor)
                .fillColor(mRadiusColor)
                .center(markerAndCircle.latLng)));

        bgMarkerMap.put(markerAndCircle.id, mGoogleMap.addMarker(new MarkerOptions()
                .position(markerAndCircle.latLng)
                .anchor(0.5f, 0.5f)
                .flat(true)
                .icon(MiscUtil.getMapMarker(WhistleBlower.getAppContext(), R.mipmap.my_loc_dot_big, 40))));

        imgMarkerMap.put(markerAndCircle.id, mGoogleMap.addMarker(new MarkerOptions()
                .position(markerAndCircle.latLng)
                .anchor(0.5f, 0.5f)
                .flat(true)
                .icon(MiscUtil.getMapMarker(WhistleBlower.getAppContext(), PlaceAdapter.getDrawableResId(markerAndCircle.type), 20))));

        mMarkerAndCircleMap.put(markerAndCircle.id, markerAndCircle);
    }

    public void removeMarkerAndCircle(String id)
    {
        if (circleMap.containsKey(id))
        {
            circleMap.get(id).remove();
            circleMap.remove(id);
        }

        if (bgMarkerMap.containsKey(id))
        {
            bgMarkerMap.get(id).remove();
            bgMarkerMap.remove(id);
        }

        if (imgMarkerMap.containsKey(id))
        {
            imgMarkerMap.get(id).remove();
            imgMarkerMap.remove(id);
        }
        mMarkerAndCircleMap.remove(id);
    }

    private MarkerAndCircle getMarkerAndCircle(Issue issue)
    {
        MarkerAndCircle markerAndCircle = new MarkerAndCircle();
        markerAndCircle.id = issue.issueId;
        markerAndCircle.latLng = LocationUtil.getLatLng(issue.latitude, issue.longitude);
        markerAndCircle.radius = issue.radius;
        markerAndCircle.type = PlaceAdapter.TYPE_ISSUE;
        return markerAndCircle;
    }

    private MarkerAndCircle getMarkerAndCircle(FavPlaces favPlace)
    {
        MarkerAndCircle markerAndCircle = new MarkerAndCircle();
        markerAndCircle.id = favPlace.addressLine;
        markerAndCircle.latLng = LocationUtil.getLatLng(favPlace.latitude, favPlace.longitude);
        markerAndCircle.radius = favPlace.radius;
        markerAndCircle.type = favPlace.placeTypeIndex;
        return markerAndCircle;
    }

    private MarkerAndCircle getMarkerAndCircle(NotifyLocation notifyLocation)
    {
        MarkerAndCircle markerAndCircle = new MarkerAndCircle();
        markerAndCircle.id = notifyLocation.receiverEmail;
        markerAndCircle.latLng = LocationUtil.getLatLng(notifyLocation.latitude, notifyLocation.longitude);
        markerAndCircle.radius = notifyLocation.radius;
        markerAndCircle.type = PlaceAdapter.TYPE_NOTIFY;
        return markerAndCircle;
    }

    private MarkerAndCircle getMarkerAndCircle(LocationAlarm alarm)
    {
        MarkerAndCircle markerAndCircle = new MarkerAndCircle();
        markerAndCircle.id = alarm.address;
        markerAndCircle.latLng = LocationUtil.getLatLng(alarm.latitude, alarm.longitude);
        markerAndCircle.radius = alarm.radius;
        markerAndCircle.type = PlaceAdapter.TYPE_ALARM;
        return markerAndCircle;
    }

    public void addMarkerAndCircle(Issue issue)
    {
        addMarkerAndCircle(getMarkerAndCircle(issue));
    }

    public void addMarkerAndCircle(FavPlaces favPlace)
    {
        addMarkerAndCircle(getMarkerAndCircle(favPlace));
    }

    public void addMarkerAndCircle(LocationAlarm locationAlarm)
    {
        addMarkerAndCircle(getMarkerAndCircle(locationAlarm));
    }

    public void addMarkerAndCircle(NotifyLocation notifyLocation)
    {

        addMarkerAndCircle(getMarkerAndCircle(notifyLocation));
    }


    public void unregister()
    {
        Otto.unregister(this);
    }
}
