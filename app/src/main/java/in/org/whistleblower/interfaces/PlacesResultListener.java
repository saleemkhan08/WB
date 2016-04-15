package in.org.whistleblower.interfaces;

import java.util.HashMap;
import java.util.List;

public interface PlacesResultListener
{
    void onListObtained(List<HashMap<String,String>> list);
}
