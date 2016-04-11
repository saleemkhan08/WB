package in.org.whistleblower;

import android.content.Context;

public class WhistleBlower extends android.app.Application
{
    private static WhistleBlower sInstance;

    @Override
    public void onCreate()
    {
        super.onCreate();
        sInstance = this;
    }

    public static Context getAppContext()
    {
        return sInstance.getApplicationContext();
    }
}

