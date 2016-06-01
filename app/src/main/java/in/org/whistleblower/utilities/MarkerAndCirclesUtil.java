package in.org.whistleblower.utilities;

import com.google.android.gms.maps.GoogleMap;

import java.util.List;
import java.util.Map;

import in.org.whistleblower.models.MarkerAndCircle;

public class MarkerAndCirclesUtil
{
    List<Map<String, MarkerAndCircle>> mMarkerAndCircleList;
    GoogleMap googleMap;

    public MarkerAndCirclesUtil(GoogleMap googleMap)
    {
        this.googleMap = googleMap;
    }
}
