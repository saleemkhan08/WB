package in.org.whistleblower.fragments;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class CompositeListener implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private List<SharedPreferences.OnSharedPreferenceChangeListener> registeredListeners = new ArrayList<SharedPreferences.OnSharedPreferenceChangeListener>();

    public void registerListener(SharedPreferences.OnSharedPreferenceChangeListener listener)
    {
        registeredListeners.add(listener);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        for(SharedPreferences.OnSharedPreferenceChangeListener listener : registeredListeners) {
            listener.onSharedPreferenceChanged(sharedPreferences,key);
        }
    }
}
