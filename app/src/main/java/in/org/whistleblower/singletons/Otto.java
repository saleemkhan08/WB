package in.org.whistleblower.singletons;

import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class Otto
{
    private static final Bus BUS = new Bus(ThreadEnforcer.ANY);
    public static void register(Object object)
    {
        try
        {
            BUS.register(object);
        }catch (Exception e)
        {
            Log.d("Otto", e.getMessage());
        }
    }

    public static void unregister(Object object)
    {
        try
        {
            BUS.unregister(object);
        }catch (Exception e)
        {
            Log.d("Otto", e.getMessage());
        }
    }

    public static void post(Object object)
    {
        try
        {
            BUS.post(object);
        }catch (Exception e)
        {
            Log.d("Otto", e.getMessage());
        }
    }

    private Otto(){}
}
