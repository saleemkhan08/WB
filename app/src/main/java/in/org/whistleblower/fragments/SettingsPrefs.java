package in.org.whistleblower.fragments;


import android.os.Bundle;
import android.preference.PreferenceFragment;

import in.org.whistleblower.R;

public class SettingsPrefs extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_preferences);
    }
}
