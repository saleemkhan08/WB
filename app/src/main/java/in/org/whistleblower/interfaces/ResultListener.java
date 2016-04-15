package in.org.whistleblower.interfaces;

import com.android.volley.VolleyError;

public interface ResultListener<T>
{
    String URL = "http://whistleblower-thnkin.rhcloud.com/RequestHandler.php";
    String ACTION = "action";
    void onSuccess(T result);
    void onError(VolleyError error);
}
